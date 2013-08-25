package main.tests;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import buffermanager.BufferManager;
import buffermanager.Frame;
import buffermanager.database.FileSystem;
import buffermanager.database.exceptions.BadFileException;
import buffermanager.database.exceptions.BadPageNumberException;
import buffermanager.database.exceptions.DBFileException;
import buffermanager.exceptions.PageNotPinnedException;
import main.Test;
import main.exceptions.TestException;

public class Test6 implements Test {

	@Override
	public void execute() throws DBFileException, BadFileException,
			BadPageNumberException, TestException, NoSuchMethodException,
			SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchFieldException, InstantiationException,
			ClassNotFoundException, PageNotPinnedException {

		int poolSize = 25;
		String filename = "test";
		FileSystem.getInstance().createFile(filename, 0);
		BufferManager bm = new BufferManager(poolSize, "RandomPolicy");

		bm.newPage(bm.getPoolSize() * 6, filename);
		bm.unpinPage(0, filename, false);

		Frame f;

		// use all frames
		Set<Integer> frameNumbers = new HashSet<Integer>();

		for (int i = 0; i < bm.getPoolSize(); i++) {
			f = bm.pinPage(i + 6, filename);

			int frameNumber = bm.findFrame(i + 6, filename);

			if (frameNumber < 0 || frameNumber >= bm.getPoolSize()) {
				throw new TestException("Invalid frame returned.");
			}
			frameNumbers.add(frameNumber);

			System.out.println("Page " + (i + 6) + " at frame " + frameNumber
					+ " is pinned.");
		}

		// try pinning an extra page
		f = bm.pinPage(bm.getPoolSize() * 2, filename);
		if (f != null) {
			throw new TestException("Pinned page in full buffer.");
		}

		// unpin the pages
		for (int i = 0; i < bm.getPoolSize(); i++) {
			int frameNumber = bm.findFrame(i + 6, filename);

			if (!frameNumbers.contains(frameNumber)) {
				throw new TestException("Pinned page has invalid frame number.");
			}
			frameNumbers.remove(frameNumber);

			bm.unpinPage(i + 6, filename, false);
			System.out.println("Page " + (i + 6) + " at frame " + frameNumber
					+ " is unpinned.");
		}

		// pin a new set of pages
		for (int i = bm.getPoolSize(); i < 2 * bm.getPoolSize(); i++) {
			f = bm.pinPage(i + 6, filename);
			if (f == null) {
				throw new TestException("Unable to pin page.");
			}
			int frameNumber = bm.findFrame(i + 6, filename);

			if (frameNumbers.contains(frameNumber)) {
				throw new TestException("Duplicate frame number!");
			} else {
				frameNumbers.add(frameNumber);
			}

			System.out.println("Page " + (i + 6) + " pinned in frame "
					+ frameNumber + ".");
		}

		// unpin the new set of pages
		for (int i = bm.getPoolSize(); i < 2 * bm.getPoolSize(); i++) {
			int frameNumber = bm.findFrame(i + 6, filename);
			if (!frameNumbers.contains(frameNumber)) {
				throw new TestException("Invalid frame number!");
			} else {
				frameNumbers.remove(frameNumber);
			}
			bm.unpinPage(i + 6, filename, true);
			System.out.println("Page " + (i + 6) + " at frame " + frameNumber
					+ " is unpinned.");
		}

		// pin another set of pages
		for (int i = 2 * bm.getPoolSize(); i < 2 * bm.getPoolSize() + 5; i++) {
			f = bm.pinPage(i + 7, filename);
			if (f == null) {
				throw new TestException("Unable to pin page.");
			}
			int frameNumber = bm.findFrame(i + 7, filename);

			if (frameNumbers.contains(frameNumber)) {
				throw new TestException("Duplicate frame number!");
			} else {
				frameNumbers.add(frameNumber);
			}

			System.out.println("Page " + (i + 7) + " pinned in frame "
					+ frameNumber + ".");

		}

		// unpin the new set of pages
		for (int i = 2 * bm.getPoolSize(); i < 2 * bm.getPoolSize() + 5; i++) {
			int frameNumber = bm.findFrame(i + 7, filename);
			if (!frameNumbers.contains(frameNumber)) {
				throw new TestException("Invalid frame number!");
			} else {
				frameNumbers.remove(frameNumber);
			}
			bm.unpinPage(i + 7, filename, true);
			System.out.println("Page " + (i + 7) + " at frame " + frameNumber
					+ " is unpinned.");
		}

	}
}
