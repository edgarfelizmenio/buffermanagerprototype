package dbms.buffermanager;

import dbms.diskspacemanager.page.Page;

/**
 * Abstraction of a frame. Holds the following information:
 * <ul>
 * <li>The reference to the page in the frame (including the filename and the page Id).</li>
 * <li>The pin count.</li>
 * <li>The dirty bit.</li>
 * </ul>
 */
public class Frame {
	private String filename;
	private int pageId;
	private int pinCount;
	private boolean dirty;
	private Page page;

	Frame() {
		this.filename = null;
		this.pageId = Page.NO_PAGE_NUMBER;
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
		this.pageId = pageNum;
		this.page = page;
	}

	Page getPage() {
		return page;
	}

	String getFilename() {
		return filename;
	}

	int getPageNum() {
		return pageId;
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
