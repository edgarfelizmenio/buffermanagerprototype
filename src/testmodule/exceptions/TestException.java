package testmodule.exceptions;

/**
 * Thrown when the prototype has failed a test.
 */
public class TestException extends Exception {

	private static final long serialVersionUID = -7732302975331518468L;

	public TestException(String message) {
		super(message);
	}

	public TestException() {
		super();
	}

}
