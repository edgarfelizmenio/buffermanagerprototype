package dbms.buffermanager.exceptions;

/**
 * Thrown when a page that is not pinned will be unpinned.
 *
 */
public class PageNotPinnedException extends Exception {
	
	private static final long serialVersionUID = -829366587971861766L;

	public PageNotPinnedException(String message) {
		super(message);
	}

	public PageNotPinnedException() {
		super();
	}

}
