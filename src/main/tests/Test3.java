package main.tests;

import java.lang.reflect.InvocationTargetException;

import dbms.buffermanager.BufferManager;
import dbms.buffermanager.exceptions.PageNotPinnedException;
import dbms.diskspacemanager.FileSystem;
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

	@Override
	public void execute() throws DBFileException, BadFileException,
			BadPageNumberException, TestException, NoSuchMethodException,
			SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchFieldException, InstantiationException,
			ClassNotFoundException, PageNotPinnedException {

		int poolSize = 20;
		String filename = "test";
		FileSystem.getInstance().createFile(filename, 0);
		BufferManager bm = new BufferManager(poolSize, "LRUPolicy");

		int pageNumber;
		// allocate some pages
		pageNumber = bm.newPage(5 * bm.getPoolSize(), filename);
		bm.unpinPage(pageNumber, filename, false);

		int[] frameNumbers = new int[bm.getPoolSize()];

		for (int i = 0; i < bm.getPoolSize(); i++) {
			Page p= bm.pinPage(i + 5, filename);
			if (p == null) {
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
		Page p = bm.pinPage(bm.getPoolSize() + 7, filename);
		if (p != null) {
			throw new TestException("Pinned page in full buffer");
		}

		// Start unpinning pages in order.
		for (int i = 0; i < bm.getPoolSize(); i++) {
			int frameNumber = bm.findFrame(i + 5, filename);
			
			if (frameNumbers[i] != frameNumber) {
				throw new TestException("Page pinned on wrong frame.");
			}
			
			bm.unpinPage(i + 5, filename, true);
			System.out.println("Page " + (i + 5) + " at frame "
					+ frameNumbers[i] + " is unpinned.");
		}

		// Start pinning a new set of pages. The order of the frames
		// should match the order of the frames that are pinned earlier.
		for (int i = bm.getPoolSize(); i < 2 * bm.getPoolSize(); i++) {
			p = bm.pinPage(i + 5, filename);
			if (p == null) {
				throw new TestException("Unable to pin page.");
			}

			int frameNumber = bm.findFrame(i + 5, filename);
			System.out.println("Page " + (i + 5) + " pinned in frame "
					+ frameNumber);

			if (frameNumber != frameNumbers[i - bm.getPoolSize()]) {
				throw new TestException("Frame number incorrect!");
			}
		}

		// Start unpinning half the pages in reverse order.
		for (int i = 2 * bm.getPoolSize() - 1; i >= bm.getPoolSize(); i -= 2) {
			int frameNumber = bm.findFrame(i + 5, filename);
			bm.unpinPage(i + 5, filename, true);
			System.out.println("Page " + (i + 5) + " at frame " + frameNumber
					+ " is unpinned.");
		}

		// Pin a new set of pages. The order of the page frames should match the
		// order of the frames that are pinned earlier in reverse order.
		for (int i = 2 * bm.getPoolSize(); i < 3 * bm.getPoolSize(); i += 2) {
			p = bm.pinPage(i + 5, filename);
			if (p == null) {
				throw new TestException("Unable to pin page.");
			}
			int frameNumber = bm.findFrame(i + 5, filename);
			System.out.println("Page " + (i + 5) + " pinned in frame "
					+ frameNumber);
			if (frameNumber != frameNumbers[(3 * bm.getPoolSize()) - i - 1]) {
				throw new TestException("Frame number incorrect!");
			}
			// unpin the page after pinning
			bm.unpinPage(i + 5, filename, false);
			// unpin other pages - check if the page is still pinned
			bm.unpinPage(i - 15, filename, false);
		}

	}
}
