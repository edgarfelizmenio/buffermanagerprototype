package buffermanager.filesystem;

import buffermanager.page.Page;

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
}
