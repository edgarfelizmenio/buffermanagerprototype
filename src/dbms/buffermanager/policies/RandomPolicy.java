package dbms.buffermanager.policies;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dbms.buffermanager.Frame;
import dbms.buffermanager.Policy;


public class RandomPolicy extends Policy {

	private List<Frame> usedFrames;
	private Random random;

	public RandomPolicy(Frame[] bufferPool) {
		super(bufferPool);
		this.usedFrames = new ArrayList<Frame>();
		this.random = new Random();
	}

	@Override
	public Frame chooseFrame() {
		for (Frame f : bufferPool) {
			if (f.isFree()) {
				return f;
			}
		}

		if (!usedFrames.isEmpty()) {
			int index = random.nextInt(usedFrames.size());
			return usedFrames.remove(index);
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
