package dbms.buffermanager;

/**
 * Abstraction of a buffer replacement policy. All buffer replacement policies
 * must extend this class.
 * 
 */
public abstract class Policy {
	protected int poolSize;

	public Policy(int poolSize) {
		this.poolSize = poolSize;
	}

	/**
	 * Called by the buffer manager if it needs a frame for replacement. Returns
	 * the frame number of the frame that contains a page that can be replaced.
	 * The value of the frame number depends on the buffer replacement policy.
	 * 
	 * @return The frame number containing the page to be replaced. Return -1 if
	 *         all frames are in use.
	 */
	public abstract int chooseFrame();

	/**
	 * This must be called by the buffer manager if a page in the buffer pool is
	 * pinned. The parameters can be used by the buffer replacement policies to
	 * keep track of certain values.
	 * 
	 * @param frameNumber
	 *            The number of the frame where the page is pinned.
	 * @param pinCount
	 *            The pinCount of the page after pinning.
	 */
	public abstract void pagePinned(int frameNumber, int pinCount);

	/**
	 * This must be called by the buffer manager if a page in the buffer pool is
	 * unpinned. The parameters can be used by the buffer replacement policies
	 * to keep track of certain values.
	 * 
	 * @param frameNumber
	 *            The number of the frame where the page is unpinned.
	 * @param pinCount
	 *            The pinCount of the page after unpinning.
	 */
	public abstract void pageUnpinned(int frameNumber, int pinCount);
}
