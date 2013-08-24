package buffermanager.policies;

import buffermanager.Policy;
import buffermanager.Frame;

import java.util.LinkedList;
import java.util.Queue;

public class LRU extends Policy {

	private Queue<Frame> recentlyUsedFrames;
	
	public LRU(Frame[] bufferPool) {
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
