package testmodule.tests;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import testmodule.Test;
import testmodule.exceptions.TestException;


import dbms.buffermanager.BufferManager;
import dbms.buffermanager.exceptions.PageNotPinnedException;
import dbms.diskspacemanager.DiskSpaceManager;
import dbms.diskspacemanager.exceptions.BadFileException;
import dbms.diskspacemanager.exceptions.BadPageNumberException;
import dbms.diskspacemanager.exceptions.DBFileException;
import dbms.diskspacemanager.page.Page;


public class Test6 implements Test {

	public void execute() throws DBFileException, BadFileException,
			BadPageNumberException, TestException, NoSuchMethodException,
			SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchFieldException, InstantiationException,
			ClassNotFoundException, PageNotPinnedException {

		int poolSize = 25;
		String filename = "test";
		DiskSpaceManager.getInstance().createFile(filename, 0);
		BufferManager bm = new BufferManager(poolSize, "RandomPolicy");

		bm.newPage(filename, bm.getPoolSize() * 6);
		bm.unpinPage(filename, 0, false);

		Page p;

		// use all frames
		Set<Integer> frameNumbers = new HashSet<Integer>();

		for (int i = 0; i < bm.getPoolSize(); i++) {
			p = bm.pinPage(filename, i + 6);

			int frameNumber = bm.findFrame(filename, i + 6);

			if (frameNumber < 0 || frameNumber >= bm.getPoolSize()) {
				throw new TestException("Invalid frame returned.");
			}
			frameNumbers.add(frameNumber);

			System.err.println("Page " + (i + 6) + " at frame " + frameNumber
					+ " is pinned.");
		}

		// try pinning an extra page
		p = bm.pinPage(filename, bm.getPoolSize() * 2);
		if (p != null) {
			throw new TestException("Pinned page in full buffer.");
		}

		// unpin the pages
		for (int i = 0; i < bm.getPoolSize(); i++) {
			int frameNumber = bm.findFrame(filename, i + 6);

			if (!frameNumbers.contains(frameNumber)) {
				throw new TestException("Pinned page has invalid frame number.");
			}
			frameNumbers.remove(frameNumber);

			bm.unpinPage(filename, i + 6, false);
			System.err.println("Page " + (i + 6) + " at frame " + frameNumber
					+ " is unpinned.");
		}

		// pin a new set of pages
		for (int i = bm.getPoolSize(); i < 2 * bm.getPoolSize(); i++) {
			p = bm.pinPage(filename, i + 6);
			if (p == null) {
				throw new TestException("Unable to pin page.");
			}
			int frameNumber = bm.findFrame(filename, i + 6);

			if (frameNumbers.contains(frameNumber)) {
				throw new TestException("Duplicate frame number!");
			} else {
				frameNumbers.add(frameNumber);
			}

			System.err.println("Page " + (i + 6) + " pinned in frame "
					+ frameNumber + ".");
		}

		// unpin the new set of pages
		for (int i = bm.getPoolSize(); i < 2 * bm.getPoolSize(); i++) {
			int frameNumber = bm.findFrame(filename, i + 6);
			if (!frameNumbers.contains(frameNumber)) {
				throw new TestException("Invalid frame number!");
			} else {
				frameNumbers.remove(frameNumber);
			}
			bm.unpinPage(filename, i + 6, true);
			System.err.println("Page " + (i + 6) + " at frame " + frameNumber
					+ " is unpinned.");
		}

		// pin another set of pages
		for (int i = 2 * bm.getPoolSize(); i < 2 * bm.getPoolSize() + 5; i++) {
			p = bm.pinPage(filename, i + 7);
			if (p == null) {
				throw new TestException("Unable to pin page.");
			}
			int frameNumber = bm.findFrame(filename, i + 7);

			if (frameNumbers.contains(frameNumber)) {
				throw new TestException("Duplicate frame number!");
			} else {
				frameNumbers.add(frameNumber);
			}

			System.err.println("Page " + (i + 7) + " pinned in frame "
					+ frameNumber + ".");

		}

		// unpin the new set of pages
		for (int i = 2 * bm.getPoolSize(); i < 2 * bm.getPoolSize() + 5; i++) {
			int frameNumber = bm.findFrame(filename, i + 7);
			if (!frameNumbers.contains(frameNumber)) {
				throw new TestException("Invalid frame number!");
			} else {
				frameNumbers.remove(frameNumber);
			}
			bm.unpinPage(filename, i + 7, true);
			System.err.println("Page " + (i + 7) + " at frame " + frameNumber
					+ " is unpinned.");
		}

	}
}
