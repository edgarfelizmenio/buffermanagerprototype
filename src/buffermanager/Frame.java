package buffermanager;

import buffermanager.page.Page;

public class Frame {
	private String filename;
	private int pageNum;
	private int pinCount;
	private boolean dirty;
	private Page page;

	public Frame() {
		this.filename = null;
		this.pageNum = Page.NO_PAGE_NUMBER;
		this.pinCount = 0;
		this.dirty = false;
		this.page = null;
	}

	public void pin(String filename, int pageNum) {
		if ((this.filename != filename) || (this.pageNum != pageNum)) {
			this.filename = filename;
			this.pageNum = pageNum;
			this.pinCount = 0;
		}
		pinCount++;
	}

	public void unpin() {
		pinCount--;
	}

	public String getFilename() {
		return this.filename;
	}

	public void setPage(Page page) {
		this.page = page;
	}

	public void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	public Page getPage() {
		return page;
	}

	public int getPageNum() {
		return pageNum;
	}

	public int getPinCount() {
		return pinCount;
	}

	public boolean isDirty() {
		return this.dirty;
	}

	public boolean isFree() {
		return page == null;
	}
}
