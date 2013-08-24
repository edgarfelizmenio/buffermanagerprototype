package buffermanager;

import buffermanager.page.Page;

public class Frame {
	private String filename;
	private int pageNum;
	private int pinCount;
	private boolean dirty;
	private Page page;

	Frame() {
		this.filename = null;
		this.pageNum = Page.NO_PAGE_NUMBER;
		this.pinCount = 0;
		this.dirty = false;
		this.page = null;
	}

	void pin(String filename, int pageNum) {
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
		this.pageNum = pageNum;
		this.page = page;
	}

	Page getPage() {
		return page;
	}

	String getFilename() {
		return filename;
	}

	int getPageNum() {
		return pageNum;
	}

	public int getPinCount() {
		return pinCount;
	}

	boolean isDirty() {
		return this.dirty;
	}

	public boolean isFree() {
		return page == null;
	}
}
