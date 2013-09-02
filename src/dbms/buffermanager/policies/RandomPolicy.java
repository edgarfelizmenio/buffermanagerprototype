package dbms.buffermanager.policies;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dbms.buffermanager.Policy;

public class RandomPolicy extends Policy {

	private List<Integer> usedFrames;
	private Random random;

	public RandomPolicy(int poolSize) {
		super(poolSize);
		this.usedFrames = new ArrayList<Integer>();
		this.random = new Random();
	}

	@Override
	public int chooseFrame() {
		if (!usedFrames.isEmpty()) {
			int index = random.nextInt(usedFrames.size());
			return usedFrames.remove(index);
		}
		return -1;
	}

	@Override
	public void pagePinned(int frameNumber, int pinCount) {
		// do nothing
	}

	@Override
	public void pageUnpinned(int frameNumber, int pinCount) {
		if (pinCount == 0) {
			usedFrames.add(frameNumber);
		}
	}

}
