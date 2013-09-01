package dbms.diskspacemanager.page;

import java.util.Arrays;

/**
 * An abstraction of a page.
 *
 */
public class Page {
	/**
	 * Invalid page number/Page does not exist.
	 */
	public static final int NO_PAGE_NUMBER = -1;
	
	public static final int PAGE_SIZE = 1024;

	/**
	 * Contents of a page.
	 */
	private char[] contents;

	private Page() {
		this.contents = new char[PAGE_SIZE];
		for (int i = 0; i < PAGE_SIZE; i++) {
			this.contents[i] = '\0';
		}
	}

	/**
	 * Creates a new page.
	 * @return A new page.
	 */
	public static Page makePage() {
		return new Page();
	}
	
	/**
	 * Sets the contents of the page.
	 * Assumption: The length of contents will not exceed page size. 
	 * @param contents
	 */
	public void setContents(char[] contents) {
		setContents(0, contents);
	}

	/**
	 * Sets the contents of the page starting from a specific byte in the page.
	 * Assumption: The length of contents will not exceed page size. 
	 * @param contents
	 */
	public void setContents(int start, char[] contents) {
		for (int i = 0, j = start; i < contents.length; i++, j++) {
			this.contents[j] = contents[i];
		}
	}
	
	/**
	 * Gets a copy of the page contents.
	 * @return The contents of the page.
	 */
	public char[] getContents() {
		return Arrays.copyOf(this.contents, this.contents.length);
	}
}
