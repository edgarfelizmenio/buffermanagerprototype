package dbms.buffermanager.policies;


import java.util.LinkedList;
import java.util.Queue;

import dbms.buffermanager.Policy;

public class LRUPolicy extends Policy {

	private Queue<Integer> recentlyUsedFrames;
	
	public LRUPolicy(int poolSize) {
		super(poolSize);
		this.recentlyUsedFrames = new LinkedList<Integer>();
	}

	@Override
	public int chooseFrame() {
		if (recentlyUsedFrames.isEmpty()) {
			return -1;
		}
		return recentlyUsedFrames.poll();
	}

	@Override
	public void pagePinned(int frameNumber, int pinCount) {
		// Do nothing
	}

	@Override
	public void pageUnpinned(int frameNumber, int pinCount) {
		if (pinCount == 0) {
			recentlyUsedFrames.add(frameNumber);
		}
	}

}
