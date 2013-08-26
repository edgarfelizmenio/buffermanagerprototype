package dbms.buffermanager.policies;


import java.util.LinkedList;
import java.util.Queue;

import dbms.buffermanager.Frame;
import dbms.buffermanager.Policy;

public class LRUPolicy extends Policy {

	private Queue<Frame> recentlyUsedFrames;
	
	public LRUPolicy(Frame[] bufferPool) {
		super(bufferPool);
		this.recentlyUsedFrames = new LinkedList<Frame>();
	}

	@Override
	public Frame chooseFrame() {
		for (Frame f: bufferPool) {
			if (f.isFree()) {
				return f;
			}
		}
		
		return recentlyUsedFrames.poll();
	}

	@Override
	public void pagePinned(Frame f) {
		// Do nothing
	}

	@Override
	public void pageUnpinned(Frame f) {
		if (f != null && f.getPinCount() == 0) {
			recentlyUsedFrames.add(f);
		}
	}

}
