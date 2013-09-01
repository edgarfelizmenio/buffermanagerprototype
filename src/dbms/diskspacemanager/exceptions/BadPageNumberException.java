package dbms.diskspacemanager.exceptions;

/**
 * Thrown when there is a request for a page that does not exist.
 */
public class BadPageNumberException extends Exception {

	private static final long serialVersionUID = 1L;

	public BadPageNumberException(String message) {
		super(message);
	}

	public BadPageNumberException() {
		super();
	}

}
