package dbms.diskspacemanager.exceptions;

/**
 * Thrown when there is a request for a page that does not exist.
 */
public class BadPageIDException extends Exception {

	private static final long serialVersionUID = 1L;

	public BadPageIDException(String message) {
		super(message);
	}

	public BadPageIDException() {
		super();
	}

}
