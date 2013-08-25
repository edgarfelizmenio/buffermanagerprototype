package buffermanager.page;

import java.util.Arrays;

public class Page implements Cloneable {
	public static final int NO_PAGE_NUMBER = -1;
	public static final int PAGE_SIZE = 1024;

	private char[] contents;

	private Page() {
		this.contents = new char[PAGE_SIZE];
		for (int i = 0; i < PAGE_SIZE; i++) {
			this.contents[i] = '\0';
		}
	}

	public static Page makePage() {
		return new Page();
	}
	
	//Assumption: length of contents will not exceed page size
	public void setContents(char[] contents) {
		setContents(0, contents);
	}
	
	public void setContents(int start, char[] contents) {
		for (int i = 0, j = start; i < contents.length; i++, j++) {
			this.contents[j] = contents[i];
		}
	}
	
	public char[] getContents() {
		return Arrays.copyOf(this.contents, this.contents.length);
	}
}
