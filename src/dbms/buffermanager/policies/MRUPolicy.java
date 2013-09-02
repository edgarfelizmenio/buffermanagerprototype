package dbms.buffermanager.policies;

import java.util.Deque;
import java.util.LinkedList;

import dbms.buffermanager.Policy;


public class MRUPolicy extends Policy {

	private Deque<Integer> recentlyUsedFrames;

	public MRUPolicy(int poolSize) {
		super(poolSize);
		this.recentlyUsedFrames = new LinkedList<Integer>();
	}

	@Override
	public int chooseFrame() {
		if (recentlyUsedFrames.isEmpty()) {
			return -1;
		}
		return recentlyUsedFrames.pollLast();
	}

	@Override
	public void pagePinned(int frameNumber, int pinCount, boolean dirty) {
		// do nothing
	}

	@Override
	public void pageUnpinned(int frameNumber, int pinCount, boolean dirty) {
		recentlyUsedFrames.addLast(frameNumber);
	}

}
