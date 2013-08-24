package buffermanager.policies;

import java.util.HashMap;

import buffermanager.Frame;
import buffermanager.Policy;

public class Clock extends Policy {

	private int current;
	private HashMap<Frame, Boolean> isReferenced;

	public Clock(Frame[] bufferPool) {
		super(bufferPool);

		this.current = 0;
		this.isReferenced = new HashMap<Frame,Boolean>(); 

		for (Frame f: bufferPool) {
			isReferenced.put(f, false);
		}

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
			} else if (isReferenced.get(bufferPool[current])) {
				isReferenced.put(bufferPool[current],false);
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
		// Do nothing.
	}
		
	@Override
	public void pageUnpinned(Frame f) {
		isReferenced.put(f, true);
	}

}
