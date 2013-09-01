package dbms.buffermanager.exceptions;

/**
 * Thrown when a pinned page will be deallocated.
 */
public class PagePinnedException extends Exception {
	
	private static final long serialVersionUID = 6715155096511041107L;

	public PagePinnedException(String message) {
		super(message);
	}

	public PagePinnedException() {
		super();
	}

}
