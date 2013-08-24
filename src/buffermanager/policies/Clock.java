package buffermanager.policies;

import java.util.Arrays;

import buffermanager.Frame;
import buffermanager.Policy;

public class Clock extends Policy {

	int current;
	boolean[] referenced;

	public Clock(Frame[] bufferPool) {
		super(bufferPool);

		this.current = 0;
		this.referenced = new boolean[bufferPool.length];

		Arrays.fill(referenced, false);

	}

	@Override
	public Frame chooseFrame() {
		Frame frame = null;
		for (Frame f : bufferPool) {
			if (f.isFree()) {
				return f;
			}
		}

		int start = current;
		do {
			if (bufferPool[current].getPinCount() > 0) {
				current++;
			} else if (referenced[current] == true) {
				referenced[current] = false;
				current++;
			} else {
				frame = bufferPool[current];
				break;
			}

			if (current >= bufferPool.length) {
				current = 0;
			}
		} while (start != current);

		return frame;
	}

	@Override
	public void pagePinned(Frame f) {
		// TODO Auto-generated method stub

	}

	@Override
	public void pageUnpinned(Frame f) {
		if (f.getPinCount() == 0) {
			for (int i = 0; i < bufferPool.length; i++) {
				if ((bufferPool[i].getFilename() == f.getFilename())
						&& (bufferPool[i].getPageNum() == f.getPageNum())) {
					referenced[i] = true;
				}
			}
		}
	}

}
