package main.tests;

import java.lang.reflect.InvocationTargetException;

import buffermanager.BufferManager;
import buffermanager.Frame;
import buffermanager.database.exceptions.BadFileException;
import buffermanager.database.exceptions.BadPageNumberException;
import buffermanager.database.exceptions.DBFileException;
import main.Test;
import main.exceptions.TestException;

/**
 * Tests the buffer replacement policy.
 * 
 */
public class Test2 implements Test {

	@Override
	public void execute(BufferManager bm, String filename)
			throws DBFileException, BadFileException, BadPageNumberException,
			TestException, NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchFieldException {

		System.out.println("TEST 2");

		Frame f;

		// allocate some pages
		f = bm.newPage(5 * bm.getPoolSize(), filename);
		bm.unpinPage(0, filename, false);
		
		int[] frameNumbers = new int[bm.getPoolSize()];

		for (int i = 0; i < bm.getPoolSize(); i++) {
			f = bm.pinPage(i + 5, filename);
			if (f == null) {
				throw new TestException("Unable to pin page");
			}

			frameNumbers[i] = bm.findFrame(i + 5, filename);
			if (frameNumbers[i] < 0 || frameNumbers[i] >= bm.getPoolSize()) {
				throw new TestException("Invalid frame returned");
			}

			System.out.println("Page " + (i + 5) + " at frame "
					+ frameNumbers[i] + " is pinned.");
		}

		// try pinning an extra page
		f = bm.pinPage(bm.getPoolSize() + 6, filename);
		if (f != null) {
			throw new TestException("Pinned page in full buffer");
		}

		// Start unpinning pages
		for (int i = bm.getPoolSize() - 1; i >= 0; i--) {
			bm.unpinPage(i + 5, filename, true);
			System.out.println("Page " + (i + 5) + " at frame "
					+ frameNumbers[i] + " is unpinned.");
		}

		// Start pinning a new set of pages again. The page frames should be
		// exactly the same order as the previous one. Clock in that case will
		// resemble MRU.
		for (int i = bm.getPoolSize(); i < 2 * bm.getPoolSize(); i++) {
			f = bm.pinPage(i + 5, filename);
			if (f == null) {
				throw new TestException("Unable to pin page");
			}

			int frameNumber = bm.findFrame(i + 5, filename);
			System.out.println("Page " + (i + 5) + " pinned in frame "
					+ frameNumber);

			if (frameNumber != frameNumbers[i - bm.getPoolSize()]) {
				throw new TestException("Frame number incorrect!");
			}
		}

		// Unpin half the pages in order.
		for (int i = bm.getPoolSize(); i < 2 * bm.getPoolSize(); i += 2) {
			bm.unpinPage(i + 5, filename, true);
			System.out.println("Page " + (i + 5) + " at frame "
					+ frameNumbers[i - bm.getPoolSize()] + " is unpinned.");
		}

		// Now, pin a new set of pages. Again, it should resemble the previous
		// sequence. In this case, Clock behaves as LRU
		for (int i = 2 * bm.getPoolSize(); i < 3 * bm.getPoolSize(); i += 2) {
			f = bm.pinPage(i + 5, filename);
			if (f == null) {
				throw new TestException("Unable to pin page");
			}
			
			int frameNumber = bm.findFrame(i + 5, filename);
			bm.unpinPage(i + 5, filename, true);
			bm.unpinPage(i - bm.getPoolSize() + 6, filename, true);
			
			System.out.println("Page " + (i + 5) + " pinned in frame " + frameNumber);
			if (frameNumber != frameNumbers[i - (2 * bm.getPoolSize())]) {
				throw new TestException("Frame number incorrect!");
			}
		}
	}

}
