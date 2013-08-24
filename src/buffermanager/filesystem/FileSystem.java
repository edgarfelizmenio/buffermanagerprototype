package buffermanager.filesystem;

import java.util.Arrays;
import java.util.HashMap;

import buffermanager.exceptions.BadFileException;
import buffermanager.exceptions.BadPageNumberException;
import buffermanager.exceptions.DBFileException;
import buffermanager.page.Page;


public class FileSystem {
	static final HashMap<String, File> directory = new HashMap<String, File>();

	public static void createFile(String filename, int numPages) {
		new File(filename, numPages);
	}

	public static boolean erase(String filename) {
		if (FileSystem.directory.containsKey(filename)) {
			FileSystem.directory.remove(filename);
			return true;
		}
		return false;
	}

	public static int allocatePages(String filename, int runSize)
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

	public static void deallocatePages(String filename, int startPageNum,
			int runSize) throws DBFileException, BadPageNumberException {
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

	public static Page readPage(String filename, int pageNum)
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

		return f.pages[pageNum].getCopy();
	}

	public static void writePage(String filename, int pageNum, Page page)
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

		f.pages[pageNum] = page.getCopy();
	}
}
