package dbms.buffermanager;

/**
 * Abstraction of a buffer replacement policy. All buffer replacement policies
 * must extend this class.
 * 
 */
public abstract class Policy {
	protected Frame[] bufferPool;

	public Policy(Frame[] bufferPool) {
		this.bufferPool = bufferPool;
	}

	public abstract Frame chooseFrame();

	public abstract void pagePinned(Frame f);

	public abstract void pageUnpinned(Frame f);
}
