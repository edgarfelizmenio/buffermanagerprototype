package buffermanager;

import java.lang.reflect.InvocationTargetException;

import buffermanager.database.FileSystem;
import buffermanager.database.exceptions.BadFileException;
import buffermanager.database.exceptions.BadPageNumberException;
import buffermanager.database.exceptions.DBFileException;
import buffermanager.exceptions.PageNotPinnedException;
import buffermanager.exceptions.PagePinnedException;
import buffermanager.page.Page;
import buffermanager.policies.LRUPolicy;

public class BufferManager {
	public static int DEFAULT_BUFFER_SIZE = 10;
	public static String DEFAULT_POLICY = "LRU";

	private Frame[] bufferPool;
	private Policy policy;

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

	public Page pinPage(int pageId, String filename) throws BadFileException,
			BadPageNumberException, DBFileException {
		Frame frame = null;

		// case 1: frame is in the buffer pool
		for (Frame f : bufferPool) {
			if ((f.getFilename() == filename) && (f.getPageNum() == pageId)) {
				f.pin(filename, pageId);
				frame = f;
				policy.pagePinned(frame);
				break;
			}
		}

		// case 2: frame is not in the buffer pool
		if (frame == null) {
			// choose a frame for replacement
			frame = policy.chooseFrame();

			if (frame != null) {
				// increment pin count
				frame.pin(filename, pageId);

				// write the page that the frame contains if the dirty bit for
				// replacement is on
				if (frame.isDirty()) {
					frame.setDirty(false);
					FileSystem.getInstance().writePage(frame.getFilename(),
							frame.getPageNum(), frame.getPage());
				}

				// read requested page into replacement frame
				Page p = Page.makePage();
				FileSystem.getInstance().readPage(filename, pageId, p);

				frame.setPage(filename, pageId, p);
				policy.pagePinned(frame);
			}
		}

		// return null if frame is null
		if (frame == null) {
			return null;
		}
		return frame.getPage();
	}

	public void unpinPage(int pageId, String filename, boolean dirty)
			throws PageNotPinnedException {
		for (Frame f : bufferPool) {
			if ((f.getFilename() == filename) && (f.getPageNum() == pageId)) {
				if (f.getPinCount() == 0) {
					throw new PageNotPinnedException(
							"Unpinning page that is not pinned!");
				}
				f.unpin();
				f.setDirty(dirty);
				policy.pageUnpinned(f);
				return;
			}
		}

		throw new PageNotPinnedException("Unpinning a nonexistent page!");
	}

	public int newPage(int numPages, String filename) throws DBFileException,
			BadFileException, BadPageNumberException {

		// find a frame in the buffer pool
		Frame frame = policy.chooseFrame();

		if (frame != null) {
			// if the buffer pool is not full, allocate pages and and pin the first page on the free frame
			int pageId = FileSystem.getInstance().allocatePages(filename,
					numPages);

			frame.pin(filename, pageId);

			// write the page that the frame contains if the dirty bit for
			// replacement is on
			if (frame.isDirty()) {
				frame.setDirty(false);
				FileSystem.getInstance().writePage(frame.getFilename(),
						frame.getPageNum(), frame.getPage());
			}

			// read requested page into replacement frame
			Page p = Page.makePage();
			FileSystem.getInstance().readPage(filename, pageId, p);

			frame.setPage(filename, pageId, p);
			policy.pagePinned(frame);
		}

		// return null if frame is null
		if (frame == null) {
			return Page.NO_PAGE_NUMBER;
		}
		return frame.getPageNum();
	}

	public void freePage(String filename, int pageId)
			throws PagePinnedException, DBFileException, BadPageNumberException {
		int frameNumber = this.findFrame(pageId, filename);
		if (frameNumber != -1) {
			Frame f = bufferPool[frameNumber];
			if (f.getPinCount() > 0) {
				throw new PagePinnedException();
			}

			f.setPage(null, Page.NO_PAGE_NUMBER, null);

			FileSystem.getInstance().deallocatePages(filename, pageId, 1);
		}
	}

	public void flushPage(String filename, int pageId) throws BadFileException,
			BadPageNumberException, DBFileException {
		int frameNumber = findFrame(pageId, filename);
		if (frameNumber > -1) {
			Frame f = bufferPool[frameNumber];
			if (f.isDirty()) {
				f.setDirty(false);
				FileSystem.getInstance().writePage(filename, pageId,
						f.getPage());
			}
		}
	}

	public void flushPages() throws BadFileException, BadPageNumberException,
			DBFileException {
		for (Frame f : bufferPool) {
			if (f.isDirty()) {
				f.setDirty(false);
				FileSystem.getInstance().writePage(f.getFilename(),
						f.getPageNum(), f.getPage());
			}
		}
	}
	
	public Page findPage(int pageId, String filename) {
		for (int i = 0; i < bufferPool.length; i++) {
			if ((!bufferPool[i].isFree())
					&& (bufferPool[i].getFilename() == filename && bufferPool[i]
							.getPageNum() == pageId)) {
				return bufferPool[i].getPage();
			}
		}
		return null;
	}

	public int findFrame(int pageId, String filename) {
		for (int i = 0; i < bufferPool.length; i++) {
			if ((!bufferPool[i].isFree())
					&& (bufferPool[i].getFilename() == filename && bufferPool[i]
							.getPageNum() == pageId)) {
				return i;
			}
		}
		return -1;
	}

	public int getPoolSize() {
		return bufferPool.length;
	}

	@SuppressWarnings("unchecked")
	private void loadPolicy(String policy) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {

		ClassLoader loader = BufferManager.class.getClassLoader();
		try {
			Class<Policy> policyClass = (Class<Policy>) loader
					.loadClass("buffermanager.policies." + policy);
			;
			this.policy = policyClass.getConstructor(Frame[].class)
					.newInstance((Object) bufferPool);
		} catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
			System.err.println("Policy " + policy + " not found, using LRU.");
			this.policy = new LRUPolicy(bufferPool);
		}
	}
}
