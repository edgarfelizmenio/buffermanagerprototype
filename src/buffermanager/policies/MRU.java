package buffermanager.policies;

import java.util.Stack;

import buffermanager.Frame;
import buffermanager.Policy;

public class MRU extends Policy {

	private Stack<Frame> recentlyUsedFrames;

	public MRU(Frame[] bufferPool) {
		super(bufferPool);
		this.recentlyUsedFrames = new Stack<Frame>();
	}

	@Override
	public Frame chooseFrame() {
		for (Frame f : bufferPool) {
			if (f.isFree()) {
				return f;
			}
		}

		return recentlyUsedFrames.pop();
	}

	@Override
	public void pagePinned(Frame f) {
		// do nothing
	}

	@Override
	public void pageUnpinned(Frame f) {
		recentlyUsedFrames.push(f);
	}

}
