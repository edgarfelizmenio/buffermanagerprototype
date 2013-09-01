package dbms.diskspacemanager;

import java.util.Arrays;

import dbms.diskspacemanager.page.Page;

/**
 * An abstraction of a file. This allows the user to treat a file as a
 * collection of pages.
 * 
 */
class File {
	Page[] pages;
	int numPages;

	File(String filename, int numPages) {
		this.pages = new Page[numPages];
		this.numPages = numPages;
		for (int i = 0; i < numPages; i++) {
			this.pages[i] = Page.makePage();
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Page p : pages) {
			if (p == null) {
				sb.append("[]\n");
			} else {
				sb.append(Arrays.toString(p.getContents()) + "\n");
			}
		}
		return sb.toString();
	}
}
