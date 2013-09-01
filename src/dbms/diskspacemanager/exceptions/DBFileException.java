package dbms.diskspacemanager.exceptions;

/**
 * Thrown when:
 * <ul>
 * <li>A non-positive number of pages will be allocated to a file.</li>
 * <li>A non-positive number of pages will be deallocated from a file.</li>
 * <li>A read to an unallocated page is requested.</li>
 * <li>A write to an unallocated page is requested.</li>
 * </ul>
 */
public class DBFileException extends Exception {

	private static final long serialVersionUID = 1L;

	public DBFileException(String message) {
		super(message);
	}
}
