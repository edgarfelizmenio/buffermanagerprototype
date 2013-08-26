package dbms.buffermanager.policies;

import java.util.Deque;
import java.util.LinkedList;

import dbms.buffermanager.Frame;
import dbms.buffermanager.Policy;


public class MRUPolicy extends Policy {

	private Deque<Frame> recentlyUsedFrames;

	public MRUPolicy(Frame[] bufferPool) {
		super(bufferPool);
		this.recentlyUsedFrames = new LinkedList<Frame>();
	}

	@Override
	public Frame chooseFrame() {
		for (Frame f : bufferPool) {
			if (f.isFree()) {
				return f;
			}
		}

		return recentlyUsedFrames.pollLast();
	}

	@Override
	public void pagePinned(Frame f) {
		// do nothing
	}

	@Override
	public void pageUnpinned(Frame f) {
		recentlyUsedFrames.addLast(f);
	}

}
