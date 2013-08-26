package dbms.diskspacemanager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import dbms.diskspacemanager.exceptions.BadFileException;
import dbms.diskspacemanager.exceptions.BadPageNumberException;
import dbms.diskspacemanager.exceptions.DBFileException;
import dbms.diskspacemanager.page.Page;


public class FileSystem {

	private static final FileSystem instance = new FileSystem();

	private Map<String, File> directory;

	private FileSystem() {
		this.directory = new HashMap<String, File>();
	}

	public static synchronized FileSystem getInstance() {
		return instance;
	}

	public void createFile(String filename, int numPages) {
		File file = new File(filename, numPages);
		directory.put(filename, file);
	}

	public boolean erase(String filename) {
		if (directory.containsKey(filename)) {
			directory.remove(filename);
			return true;
		}
		return false;
	}

	public int allocatePages(String filename, int runSize)
			throws DBFileException {

		if (runSize <= 0) {
			throw new DBFileException("Non positive run size.");
		}

		File f = directory.get(filename);
		int runStart = f.numPages;

		Page[] oldPages = Arrays.copyOf(f.pages, runStart);

		f.pages = new Page[runStart + runSize];
		f.numPages = f.pages.length;

		System.arraycopy(oldPages, 0, f.pages, 0, runStart);

		for (int i = runStart; i < f.pages.length; i++) {
			f.pages[i] = Page.makePage();
		}

		return runStart;
	}

	public void deallocatePages(String filename, int startPageNum, int runSize)
			throws DBFileException, BadPageNumberException {
		if (runSize <= 0) {
			throw new DBFileException("Non positive run size.");
		}

		File f = directory.get(filename);
		if ((startPageNum < 0) || (startPageNum + runSize - 1 >= f.numPages)) {
			throw new BadPageNumberException(startPageNum + " " + runSize);
		}

		for (int i = startPageNum; i < startPageNum + runSize; i++) {
			f.pages[i] = null;
		}
	}

	public void readPage(String filename, int pageNum, Page page)
			throws BadFileException, BadPageNumberException, DBFileException {
		if (!directory.containsKey(filename)) {
			throw new BadFileException();
		}

		File f = directory.get(filename);

		if ((pageNum < 0) || (pageNum >= f.numPages)) {
			throw new BadPageNumberException();
		}

		if (f.pages[pageNum] == null) {
			throw new DBFileException("Page not allocated");
		}

		page.setContents(f.pages[pageNum].getContents());
	}

	public void writePage(String filename, int pageNum, Page page)
			throws BadFileException, BadPageNumberException, DBFileException {
		if (!directory.containsKey(filename)) {
			throw new BadFileException();
		}

		File f = directory.get(filename);
		if ((pageNum < 0) || (pageNum >= f.numPages)) {
			throw new BadPageNumberException();
		}

		if (f.numPages == 0) {
			throw new DBFileException("Empty file.");
		}

		// Make sure that page has actually been allocated.
		if (f.pages[pageNum] == null) {
			throw new DBFileException("Page not allocated.");
		}

		f.pages[pageNum].setContents(page.getContents());
	}

	public void listFiles() {
		System.out.println("Directory: ");
		if (directory.isEmpty()) {
			System.out.println("empty");
		} else {
			for (Entry<String, File> e : this.directory.entrySet()) {
				System.out.println(e.getKey());
				System.out.println(e.getValue());
			}
		}
	}
}
