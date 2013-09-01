package main.tests;

import java.lang.reflect.InvocationTargetException;

import dbms.buffermanager.BufferManager;
import dbms.buffermanager.exceptions.PageNotPinnedException;
import dbms.diskspacemanager.DiskSpaceManager;
import dbms.diskspacemanager.exceptions.BadFileException;
import dbms.diskspacemanager.exceptions.BadPageNumberException;
import dbms.diskspacemanager.exceptions.DBFileException;
import dbms.diskspacemanager.page.Page;

import main.Test;
import main.exceptions.TestException;

/**
 * Tests the MRU replacement policy.
 * 
 */
public class Test5 implements Test {

	public void execute() throws DBFileException, BadFileException,
			BadPageNumberException, TestException, NoSuchMethodException,
			SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchFieldException, InstantiationException,
			ClassNotFoundException, PageNotPinnedException {

		int poolSize = 25;
		String filename = "test";
		DiskSpaceManager.getInstance().createFile(filename, 0);
		BufferManager bm = new BufferManager(poolSize, "MRUPolicy");

		int pageNumber;
		// allocate some pages
		pageNumber = bm.newPage(filename, 5 * bm.getPoolSize());
		bm.unpinPage(filename, pageNumber, false);

		int[] frameNumbers = new int[bm.getPoolSize()];

		// fill the buffer pool
		for (int i = 0; i < bm.getPoolSize(); i++) {
			Page p = bm.pinPage(filename, i + 5);
			if (p == null) {
				throw new TestException("Unable to pin page.");
			}

			frameNumbers[i] = bm.findFrame(filename, i + 5);
			if (frameNumbers[i] < 0 || frameNumbers[i] >= bm.getPoolSize()) {
				throw new TestException("Invalid frame returned.");
			}

			System.out.println("Page " + (i + 5) + " at frame "
					+ frameNumbers[i] + " is pinned.");
		}

		// try pinning an extra page
		Page p = bm.pinPage(filename, bm.getPoolSize() * 3);
		if (p != null) {
			throw new TestException("Pinned page in full buffer.");
		}

		// Start unpinning pages in order.
		for (int i = 0; i < bm.getPoolSize(); i++) {
			bm.unpinPage(filename, i + 5, true);
			System.out.println("Page " + (i + 5) + " at frame "
					+ frameNumbers[i] + " is unpinned.");
		}

		// Pin a new set of pages. The order of the frames
		// should match the order of the frames that are pinned earlier in
		// reverse order.
		for (int i = bm.getPoolSize(); i < bm.getPoolSize() * 2; i++) {
			p = bm.pinPage(filename, i + 5);
			if (p == null) {
				throw new TestException("Unable to pin page.");
			}

			int frameNumber = bm.findFrame(filename, i + 5);
			System.out.println("Page " + (i + 5) + " pinned in frame "
					+ frameNumber + ".");

			if (frameNumber != frameNumbers[bm.getPoolSize()
					- (i - bm.getPoolSize()) - 1]) {
				throw new TestException("Frame number incorrect!");
			}
		}

		for (int i = 0; i < 5; i++) {
			int step = (i % 2 == 0) ? 5 : -5;
			int start = (i % 2 == 0) ? bm.getPoolSize() + 5 + (i / 2) : 2
					* bm.getPoolSize() + 5 - ((i + 1) / 2);
			for (int j = start, k = 0; k < 5; j += step, k++) {
				int fn = bm.findFrame(filename, j);
				if (fn != frameNumbers[2 * bm.getPoolSize() - j + 4]) {
					throw new TestException("Page pinned in wrong frame!");
				}
				bm.unpinPage(filename, j, true);
				System.out.println("Page " + j + " at frame " + fn
						+ " is unpinned.");
			}
		}

		for (int i = bm.getPoolSize() * 2 + 5; i < bm.getPoolSize() * 2 + 10; i++) {
			p = bm.pinPage(filename, i);
			if (p == null) {
				throw new TestException("Unable to pin page.");
			}

			int frameNumber = bm.findFrame(filename, i);
			int x = i - 2 * bm.getPoolSize() - 5;
			if (frameNumber != frameNumbers[2 + x * 5]) {
				throw new TestException("Frame number incorrect!");
			}
			System.out.println("Page " + i + " pinned in frame " + frameNumber
					+ ".");

		}

		for (int i = bm.getPoolSize() * 2 + 5; i < bm.getPoolSize() * 2 + 10; i++) {
			int frameNumber = bm.findFrame(filename, i);
			int x = i - 2 * bm.getPoolSize() - 5;
			if (frameNumber != frameNumbers[2 + x * 5]) {
				throw new TestException("Page pinned in wrong frame!");
			}
			bm.unpinPage(filename, i, true);
			System.out.println("Page " + i + " at frame " + frameNumber
					+ " is unpinned.");
		}

	}
}
