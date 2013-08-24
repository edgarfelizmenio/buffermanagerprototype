package buffermanager.policies;

import java.util.ArrayList;
import java.util.List;

import buffermanager.Frame;
import buffermanager.Policy;

public class Random extends Policy {

	private List<Frame> usedFrames;
	private java.util.Random random;

	public Random(Frame[] bufferPool) {
		super(bufferPool);
		this.usedFrames = new ArrayList<Frame>();
		this.random = new java.util.Random();
	}

	@Override
	public Frame chooseFrame() {
		for (Frame f : bufferPool) {
			if (f.isFree()) {
				return f;
			}
		}

		if (usedFrames != null) {
			return usedFrames.remove(random.nextInt(bufferPool.length));
		}
		return null;
	}

	@Override
	public void pagePinned(Frame f) {
		// do nothing
	}

	@Override
	public void pageUnpinned(Frame f) {
		if (f.getPinCount() == 0) {
			usedFrames.add(f);
		}
	}

}
