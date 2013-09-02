package dbms.buffermanager.policies;

import java.util.Arrays;

import dbms.buffermanager.Policy;

public class ClockPolicy extends Policy {

	private int current;
	private boolean[] isReferenced;
	private int[] pinCount;

	public ClockPolicy(int poolSize) {
		super(poolSize);

		this.current = 0;
		this.isReferenced = new boolean[poolSize];
		this.pinCount = new int[poolSize];

		Arrays.fill(isReferenced, false);
		Arrays.fill(pinCount, 0);
	}

	@Override
	public int chooseFrame() {
		int start = current;
		int replacements = 0;
		int frameNumber = -1;

		while (true) {
			if (pinCount[current] > 0) {
				updateCurrent();
			} else if (isReferenced[current]) {
				isReferenced[current] = false;
				updateCurrent();
				replacements++;
			} else {
				frameNumber = current;
				updateCurrent();
				break;
			}

			if (start == current && replacements == 0) {
				frameNumber = -1;
				break;
			}
		}
		return frameNumber;
	}

	@Override
	public void pagePinned(int frameNumber, int pinCount, boolean dirty) {
		this.pinCount[frameNumber] = pinCount;
	}

	@Override
	public void pageUnpinned(int frameNumber, int pinCount, boolean dirty) {
		this.pinCount[frameNumber] = pinCount;
		if (pinCount == 0) {
			isReferenced[frameNumber] = true;			
		}

	}

	private void updateCurrent() {
		current++;
		if (current >= poolSize) {
			current = 0;
		}
	}

}
