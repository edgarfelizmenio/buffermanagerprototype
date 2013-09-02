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

	public abstract int chooseFrame();

	public abstract void pagePinned(int frameNumber, int pinCount, boolean dirty);

	public abstract void pageUnpinned(int frameNumber, int pinCount, boolean dirty);
}
