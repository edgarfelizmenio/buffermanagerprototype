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
 * Tests if the LRU replacement policy.
 * 
 */
public class Test3 implements Test {

	public void execute() throws DBFileException, BadFileException,
			BadPageNumberException, TestException, NoSuchMethodException,
			SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchFieldException, InstantiationException,
			ClassNotFoundException, PageNotPinnedException {

		int poolSize = 20;
		String filename = "test";
		DiskSpaceManager.getInstance().createFile(filename, 0);
		BufferManager bm = new BufferManager(poolSize, "LRUPolicy");

		int pageNumber;
		// allocate some pages
		pageNumber = bm.newPage(filename, 5 * bm.getPoolSize());
		bm.unpinPage(filename, pageNumber, false);

		int[] frameNumbers = new int[bm.getPoolSize()];

		for (int i = 0; i < bm.getPoolSize(); i++) {
			Page p= bm.pinPage(filename, i + 5);
			if (p == null) {
				throw new TestException("Unable to pin page");
			}

			frameNumbers[i] = bm.findFrame(filename, i + 5);
			if (frameNumbers[i] < 0 || frameNumbers[i] >= bm.getPoolSize()) {
				throw new TestException("Invalid frame returned");
			}

			System.out.println("Page " + (i + 5) + " at frame "
					+ frameNumbers[i] + " is pinned.");
		}

		// try pinning an extra page
		Page p = bm.pinPage(filename, bm.getPoolSize() + 7);
		if (p != null) {
			throw new TestException("Pinned page in full buffer");
		}

		// Start unpinning pages in order.
		for (int i = 0; i < bm.getPoolSize(); i++) {
			int frameNumber = bm.findFrame(filename, i + 5);
			
			if (frameNumbers[i] != frameNumber) {
				throw new TestException("Page pinned on wrong frame.");
			}
			
			bm.unpinPage(filename, i + 5, true);
			System.out.println("Page " + (i + 5) + " at frame "
					+ frameNumbers[i] + " is unpinned.");
		}

		// Start pinning a new set of pages. The order of the frames
		// should match the order of the frames that are pinned earlier.
		for (int i = bm.getPoolSize(); i < 2 * bm.getPoolSize(); i++) {
			p = bm.pinPage(filename, i + 5);
			if (p == null) {
				throw new TestException("Unable to pin page.");
			}

			int frameNumber = bm.findFrame(filename, i + 5);
			System.out.println("Page " + (i + 5) + " pinned in frame "
					+ frameNumber);

			if (frameNumber != frameNumbers[i - bm.getPoolSize()]) {
				throw new TestException("Frame number incorrect!");
			}
		}

		// Start unpinning half the pages in reverse order.
		for (int i = 2 * bm.getPoolSize() - 1; i >= bm.getPoolSize(); i -= 2) {
			int frameNumber = bm.findFrame(filename, i + 5);
			bm.unpinPage(filename, i + 5, true);
			System.out.println("Page " + (i + 5) + " at frame " + frameNumber
					+ " is unpinned.");
		}

		// Pin a new set of pages. The order of the page frames should match the
		// order of the frames that are pinned earlier in reverse order.
		for (int i = 2 * bm.getPoolSize(); i < 3 * bm.getPoolSize(); i += 2) {
			p = bm.pinPage(filename, i + 5);
			if (p == null) {
				throw new TestException("Unable to pin page.");
			}
			int frameNumber = bm.findFrame(filename, i + 5);
			System.out.println("Page " + (i + 5) + " pinned in frame "
					+ frameNumber);
			if (frameNumber != frameNumbers[(3 * bm.getPoolSize()) - i - 1]) {
				throw new TestException("Frame number incorrect!");
			}
			// unpin the page after pinning
			bm.unpinPage(filename, i + 5, false);
			// unpin other pages - check if the page is still pinned
			bm.unpinPage(filename, i - 15, false);
		}

	}
}
