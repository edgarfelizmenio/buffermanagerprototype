package dbms.diskspacemanager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import dbms.diskspacemanager.exceptions.BadFileException;
import dbms.diskspacemanager.exceptions.BadPageIDException;
import dbms.diskspacemanager.exceptions.DBFileException;
import dbms.diskspacemanager.page.Page;

/**
 * Database component that manages the disk space. This component allows the
 * other DBMS components to do the following:
 * <ul>
 * <li>Create a file.</li>
 * <li>Destroy a file.</li>
 * <li>Allocate pages to a file.</li>
 * <li>Deallocate pages from a file.</li>
 * <li>Read a page from the database.</li>
 * <li>Write a page to the database.</li>
 * </ul>
 */
public class DiskSpaceManager {
	private static final DiskSpaceManager instance = new DiskSpaceManager();

	/**
	 * The directory of files. This object keeps track of all files in the
	 * database.
	 */
	private Map<String, File> directory;

	private DiskSpaceManager() {
		this.directory = new HashMap<String, File>();
	}

	/**
	 * @return The only instance of the DiskSpaceManager during runtime.
	 */
	public static synchronized DiskSpaceManager getInstance() {
		return instance;
	}

	/**
	 * Add a new file to the database, with a specified number of pages.
	 * Overwrites the file if it already exists.
	 * 
	 * @param filename
	 *            The name of the file.
	 * @param numPages
	 *            The desired number of pages that will be allocated for the
	 *            file.
	 */
	public void createFile(String filename, int numPages) {
		File file = new File(filename, numPages);
		directory.put(filename, file);
	}

	/**
	 * Removes a file from the database.
	 * 
	 * @param filename
	 *            The name of the file.
	 * @return <code>true</code> if the file is deleted from the database.
	 *         <code>false</code> if the file does not exist.
	 */
	public boolean eraseFile(String filename) {
		if (directory.containsKey(filename)) {
			directory.remove(filename);
			return true;
		}
		return false;
	}

	/**
	 * Allocates a set of pages for a file in the database. Pages are always
	 * allocated at the end of the file for simplicity.
	 * 
	 * @param filename
	 *            The file where the pages will be allocated.
	 * @param numPages
	 *            The number of pages that will be allocated.
	 * @return The page number of the first page that is allocated.
	 * @throws DBFileException
	 *             If <code>numPages</code> is not positive.
	 */
	public int allocatePages(String filename, int numPages)
			throws DBFileException {

		if (numPages <= 0) {
			throw new DBFileException("Non positive number of pages.");
		}

		File f = directory.get(filename);
		int runStart = f.numPages;

		Page[] oldPages = Arrays.copyOf(f.pages, runStart);

		f.pages = new Page[runStart + numPages];
		f.numPages = f.pages.length;

		System.arraycopy(oldPages, 0, f.pages, 0, runStart);

		for (int i = runStart; i < f.pages.length; i++) {
			f.pages[i] = Page.makePage();
		}

		return runStart;
	}

	/**
	 * Deallocates a set of pages from a file.
	 * 
	 * @param filename
	 *            The file where the pages will be deallocated.
	 * @param startPageId
	 *            The page number of the first page that will be deallocated
	 * @param numPages
	 *            The number of pages of that will be deallocated.
	 * @throws DBFileException
	 *             If <code>numPages</code> is not positive.
	 * @throws BadPageIDException
	 *             If <code>startPageNum</code> is not illegal.
	 */
	public void deallocatePages(String filename, int startPageId, int numPages)
			throws DBFileException, BadPageIDException {
		if (numPages <= 0) {
			throw new DBFileException("Non positive run size.");
		}

		File f = directory.get(filename);
		if ((startPageId < 0) || (startPageId + numPages - 1 >= f.numPages)) {
			throw new BadPageIDException(startPageId + " " + numPages);
		}

		for (int i = startPageId; i < startPageId + numPages; i++) {
			f.pages[i] = null;
		}
	}

	/**
	 * Writes the contents of the specified page to disk.
	 * @param filename The file containing the page.
	 * @param pageId The number of the page.
	 * @param page The contents of the page.
	 * @throws BadFileException If the file is not in the database.
	 * @throws BadPageIDException If the page number if not in the file.
	 * @throws DBFileException If the file does not contain any pages.
	 */
	public void writePage(String filename, int pageId, Page page)
			throws BadFileException, BadPageIDException, DBFileException {
		if (!directory.containsKey(filename)) {
			throw new BadFileException();
		}

		File f = directory.get(filename);
		if ((pageId < 0) || (pageId >= f.numPages)) {
			throw new BadPageIDException();
		}

		if (f.numPages == 0) {
			throw new DBFileException("Empty file.");
		}

		// Make sure that page has actually been allocated.
		if (f.pages[pageId] == null) {
			throw new DBFileException("Page not allocated.");
		}

		f.pages[pageId].setContents(page.getContents());
	}

	/**
	 * Copies the contents of the specified page from disk into the <code>Page</code> provided.
	 * @param filename The name of the file containing the page that will be copied.
	 * @param pageId The page number of the page that will be copied.
	 * @param page The page object where the contents will be copied.
	 * @throws BadFileException If the file does not exist.
	 * @throws BadPageIDException If the page number is not in the file.
	 * @throws DBFileException If the file does not contain any pages.
	 */
	public void readPage(String filename, int pageId, Page page)
			throws BadFileException, BadPageIDException, DBFileException {
		if (!directory.containsKey(filename)) {
			throw new BadFileException();
		}

		File f = directory.get(filename);

		if ((pageId < 0) || (pageId >= f.numPages)) {
			throw new BadPageIDException();
		}

		if (f.pages[pageId] == null) {
			throw new DBFileException("Page not allocated");
		}

		page.setContents(f.pages[pageId].getContents());
	}

	/**
	 * Lists the files in the database. This is only used for testing purposes.
	 * @return The list of files in string format.
	 */
	public String listFiles() {
		StringBuilder sb = new StringBuilder();
		sb.append("Directory:\n");
		if (directory.isEmpty()) {
			sb.append("empty\n");
		} else {
			for (Entry<String, File> e : this.directory.entrySet()) {
sb.append(e.getKey().toString() + "\n");
sb.append(e.getValue().toString() + "\n");
			}
		}
		return sb.toString();
	}
}
