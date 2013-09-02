package dbms.buffermanager;

import java.lang.reflect.InvocationTargetException;

import dbms.buffermanager.exceptions.PageNotPinnedException;
import dbms.buffermanager.exceptions.PagePinnedException;
import dbms.buffermanager.policies.LRUPolicy;
import dbms.diskspacemanager.DiskSpaceManager;
import dbms.diskspacemanager.exceptions.BadFileException;
import dbms.diskspacemanager.exceptions.BadPageNumberException;
import dbms.diskspacemanager.exceptions.DBFileException;
import dbms.diskspacemanager.page.Page;

/**
 * DBMS component that manages the buffer pool. This component allows other DBMS
 * components to do the following:
 * <ul>
 * <li>Request a page.</li>
 * <li>Release a page.</li>
 * <li>Allocate a page to a file.</li>
 * <li>Deallocate a page from a file.</li>
 * <li>Flush a dirty page.</li>
 * </ul>
 * 
 */
public class BufferManager {
	public static int DEFAULT_BUFFER_SIZE = 10;
	public static String DEFAULT_POLICY = "LRU";

	// private Frame[] bufferPool;

	// buffer pool variables
	private class Frame {
		Page page;
		String filename;
		int pageId;
		int pinCount;
		boolean dirty;

		Frame() {
			page = null;
			filename = null;
			pageId = Page.NO_PAGE_NUMBER;
			pinCount = 0;
			dirty = false;
		}

		void pin() {
			pinCount++;
		}

		void unpin() {
			pinCount--;
		}

		void setDirty(boolean dirty) {
			this.dirty = dirty;
		}

		void setPage(String filename, int pageNum, Page page) {
			this.filename = filename;
			this.pageId = pageNum;
			this.page = page;
		}

		boolean isFree() {
			return page == null;
		}
	}

	private Frame[] bufferPool;
	private Policy policy;

	/**
	 * Creates a buffer manager with the specified size and buffer replacement
	 * policy.
	 * 
	 * @param bufferSize
	 *            Size of the buffer pool.
	 * @param policy
	 *            The policy that will be followed in replacing pages.
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws ClassNotFoundException
	 */
	public BufferManager(int bufferSize, String policy)
			throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, SecurityException,
			InvocationTargetException, NoSuchMethodException,
			ClassNotFoundException {

		this.bufferPool = new Frame[bufferSize];

		for (int i = 0; i < bufferSize; i++) {
			bufferPool[i] = new Frame();
		}

		// load the buffer replacement policy
		loadPolicy(policy);
	}

	/**
	 * Pins a page in buffer pool. If the page is not yet in the buffer pool,
	 * look for an available frame for this page, make a copy of the page, and
	 * pin it. Write out the old page, if it is dirty, before reading.
	 * 
	 * @param filename
	 *            The name of file that contains the page to be pinned.
	 * @param pageId
	 *            The page id of the page to be pinned.
	 * @return A reference to the page that is pinned in the buffer pool. If the
	 *         page is not in the buffer pool and there is no available frame,
	 *         return <code>null</code>.
	 * @throws BadFileException
	 *             If the file does not exist.
	 * @throws BadPageNumberException
	 *             If the page does not exist.
	 * @throws DBFileException
	 *             If the page is not allocated.
	 */
	public Page pinPage(String filename, int pageId) throws BadFileException,
			BadPageNumberException, DBFileException {
		Frame frame = null;

		// case 1: frame is in the buffer pool
		for (int i = 0; i < bufferPool.length; i++) {
			Frame f = bufferPool[i];
			if ((f.filename == filename) && (f.pageId == pageId)) {
				f.pin();
				frame = f;
				policy.pagePinned(i, f.pinCount, f.dirty);
				break;
			}
		}

		// case 2: frame is not in the buffer pool
		if (frame == null) {
			// choose a frame for replacement
			int frameNumber = this.selectFrame();

			if (frameNumber != -1) {

				frame = bufferPool[frameNumber];

				// increment pin count
				frame.pin();

				// write the page that the frame contains if the dirty bit for
				// replacement is on
				if (frame.dirty) {
					frame.setDirty(false);
					DiskSpaceManager.getInstance().writePage(frame.filename,
							frame.pageId, frame.page);
				}

				// read requested page into replacement frame
				Page p = Page.makePage();
				DiskSpaceManager.getInstance().readPage(filename, pageId, p);

				frame.setPage(filename, pageId, p);
				policy.pagePinned(frameNumber, frame.pinCount, frame.dirty);
			}
		}

		// return null if frame is null
		if (frame == null) {
			return null;
		}
		return frame.page;
	}

	/**
	 * Unpins a page in the buffer pool. If the pin count of the page is greater
	 * than 0, decrement its pin count. If the pin count becomes zero, it is
	 * appropriately included in a group of replacement candidates.
	 * 
	 * @param filename
	 *            The name of file that contains the page to be unpinned.
	 * @param pageId
	 *            The page id of the page to be pinned.
	 * @param dirty
	 *            Indicates whether or not the page is dirty.
	 * @throws PageNotPinnedException
	 *             If the page is not pinned.
	 */
	public void unpinPage(String filename, int pageId, boolean dirty)
			throws PageNotPinnedException {
		for (int i = 0; i < bufferPool.length; i++) {
			Frame f = bufferPool[i];
			if ((f.filename == filename) && (f.pageId == pageId)) {
				if (f.pinCount == 0) {
					throw new PageNotPinnedException(
							"Unpinning page that is not pinned!");
				}
				f.unpin();
				f.setDirty(dirty);
				policy.pageUnpinned(i, f.pinCount, dirty);
				return;
			}
		}

		throw new PageNotPinnedException("Unpinning a nonexistent page!");
	}

	/**
	 * Requests a new set of pages from the underlying database, then finds a
	 * frame in the buffer pool for the page page and pins it. Return the
	 * <code>pageId</code> of the first page that is allocated for the file. If
	 * the buffer pool is full, no new pages are allocated from the database and
	 * <code>Page.NO_PAGE_NUMBER</code> is returned.
	 * 
	 * @param filename
	 *            The name of file where the pages will be allocated.
	 * @param numPages
	 *            The number of pages to be allocated.
	 * @return The <code>pageId</code> of the first page that is allocated for
	 *         the file.
	 * @throws DBFileException
	 *             If the number of pages is not positive or a write to an
	 *             unallocated page is requested.
	 * @throws BadFileException
	 *             If the file does not exist.
	 * @throws BadPageNumberException
	 *             If the page does not exist.
	 */
	public int newPage(String filename, int numPages) throws DBFileException,
			BadFileException, BadPageNumberException {

		Frame frame = null;

		// find a frame in the buffer pool
		int frameNumber = selectFrame();

		if (frameNumber != -1) {

			frame = bufferPool[frameNumber];
			// if the buffer pool is not full, allocate pages and and pin the
			// first page on the free frame
			int pageId = DiskSpaceManager.getInstance().allocatePages(filename,
					numPages);

			frame.pin();

			// write the page that the frame contains if the dirty bit for
			// replacement is on
			if (frame.dirty) {
				frame.setDirty(false);
				DiskSpaceManager.getInstance().writePage(frame.filename,
						frame.pageId, frame.page);
			}

			// read requested page into replacement frame
			Page p = Page.makePage();
			DiskSpaceManager.getInstance().readPage(filename, pageId, p);

			frame.setPage(filename, pageId, p);
			policy.pagePinned(frameNumber, frame.pinCount, frame.dirty);
		}

		// return null if frame is null
		if (frame == null) {
			return Page.NO_PAGE_NUMBER;
		}
		return frame.pageId;
	}

	/**
	 * Deallocates a page from the underlying database. Verifies that the page
	 * is not pinned. If the page is not in the buffer pool, do nothing.
	 * 
	 * @param filename
	 *            The name of file containing the page to be deallocated.
	 * @param pageId
	 *            The pageId of the page to be deallocated.
	 * @throws PagePinnedException
	 *             If the page is pinned.
	 * @throws DBFileException
	 *             If the number of pages to be deallocated is not positive.
	 * @throws BadPageNumberException
	 *             If the page does not exist.
	 */
	public void freePage(String filename, int pageId)
			throws PagePinnedException, DBFileException, BadPageNumberException {
		int frameNumber = this.findFrame(filename, pageId);
		if (frameNumber != -1) {
			Frame f = bufferPool[frameNumber];
			if (f.pinCount > 0) {
				throw new PagePinnedException();
			}

			f.setPage(null, Page.NO_PAGE_NUMBER, null);

			DiskSpaceManager.getInstance().deallocatePages(filename, pageId, 1);
		}
	}

	/**
	 * Flushes (writes) a page from the buffer pool to the underlying database
	 * if it is dirty. If page is not dirty, it is not flushed. If the page is
	 * not in the buffer pool, do nothing, since the page is effectively flushed
	 * already.
	 * 
	 * @param filename
	 *            The name of file containing the page to be flushed.
	 * @param pageId
	 *            The pageId of the page to be flushed.
	 * @throws BadFileException
	 *             If the file does not exist.
	 * @throws BadPageNumberException
	 *             If the page does not exist.
	 * @throws DBFileException
	 *             If the page is not allocated.
	 */
	public void flushPage(String filename, int pageId) throws BadFileException,
			BadPageNumberException, DBFileException {
		int frameNumber = findFrame(filename, pageId);
		if (frameNumber > -1) {
			Frame f = bufferPool[frameNumber];
			if (f.dirty) {
				f.setDirty(false);
				DiskSpaceManager.getInstance().writePage(filename, pageId,
						f.page);
			}
		}
	}

	/**
	 * Flushes all dirty pages from the buffer pool to the underlying database.
	 * If a page is not dirty, it is not flushed.
	 * 
	 * @throws BadFileException
	 *             If a page that belongs to a deleted (or a nonexistent) file
	 *             will be flushed.
	 * @throws BadPageNumberException
	 *             If a page that is already deleted from the database will be
	 *             flushed.
	 * @throws DBFileException
	 *             If an unallocated page will be flushed.
	 */
	public void flushPages() throws BadFileException, BadPageNumberException,
			DBFileException {
		for (Frame f : bufferPool) {
			if (f.dirty) {
				f.setDirty(false);
				DiskSpaceManager.getInstance().writePage(f.filename, f.pageId,
						f.page);
			}
		}
	}

	/**
	 * Finds the page in the buffer pool with the specified filename and page
	 * Id. This is only used for testing purposes.
	 * 
	 * @param filename
	 *            The name of the file containing the page.
	 * @param pageId
	 *            The page Id of the file containing the page.
	 * @return The page in the buffer pool with the specified filename and page
	 *         Id, <code>null</code> if the page is not in the buffer pool.
	 */
	public Page findPage(String filename, int pageId) {
		for (int i = 0; i < bufferPool.length; i++) {
			if ((!bufferPool[i].isFree())
					&& (bufferPool[i].filename == filename && bufferPool[i].pageId == pageId)) {
				return bufferPool[i].page;
			}
		}
		return null;
	}

	/**
	 * Finds the frame in the buffer pool containing the page with the specified
	 * filename and page Id.
	 * 
	 * @param filename
	 *            The name of the file containing the page.
	 * @param pageId
	 *            The page Id of the file containing the page.
	 * @return The frame number of the frame containing the page ,
	 *         <code>-1</code> if the page is not in the buffer pool.
	 */
	public int findFrame(String filename, int pageId) {
		for (int i = 0; i < bufferPool.length; i++) {
			if ((!bufferPool[i].isFree())
					&& (bufferPool[i].filename == filename && bufferPool[i].pageId == pageId)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Gets the size of the buffer pool.
	 * 
	 * @return The size of the buffer pool.
	 */
	public int getPoolSize() {
		return bufferPool.length;
	}

	/**
	 * Loads the buffer replacement policy.
	 * 
	 * @param policy
	 *            The name of the policy.
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	@SuppressWarnings("unchecked")
	private void loadPolicy(String policy) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {

		ClassLoader loader = BufferManager.class.getClassLoader();
		try {
			Class<Policy> policyClass = (Class<Policy>) loader
					.loadClass("dbms.buffermanager.policies." + policy);
			;
			this.policy = (Policy) policyClass.getConstructor(Integer.TYPE)
					.newInstance((Object) bufferPool.length);
		} catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
			System.err.println("Policy " + policy + " not found, using LRU.");
			this.policy = new LRUPolicy(bufferPool.length);
		}
	}

	/**
	 * Finds a frame where a page can be pinned. Returns <code>null</code> if
	 * there are no free pages and all pages have pin count greater than 0.
	 * 
	 * @return A free frame or frame with pin count 0.
	 */
	private int selectFrame() {
		for (int i = 0; i < bufferPool.length; i++) {
			if (bufferPool[i].isFree()) {
				return i;
			}
		}

		return policy.chooseFrame();
	}
}
