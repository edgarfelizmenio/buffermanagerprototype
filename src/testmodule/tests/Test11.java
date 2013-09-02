package testmodule.tests;

import java.lang.reflect.InvocationTargetException;

import testmodule.Test;
import testmodule.exceptions.TestException;


import dbms.buffermanager.BufferManager;
import dbms.buffermanager.exceptions.PageNotPinnedException;
import dbms.buffermanager.exceptions.PagePinnedException;
import dbms.diskspacemanager.DiskSpaceManager;
import dbms.diskspacemanager.exceptions.BadFileException;
import dbms.diskspacemanager.exceptions.BadPageNumberException;
import dbms.diskspacemanager.exceptions.DBFileException;
import dbms.diskspacemanager.page.Page;

/**
 * Tests if the following cases are handled properly:
 * <ol>
 * <li>Pinning an allocated page.</li>
 * <li>Flushing all the pages.</li>
 * <li>Erasing a file.</li>
 * <li>Flushing all the pages twice.</li>
 * </ol>
 */
public class Test11 implements Test {

	public void execute() throws DBFileException, BadFileException,
			BadPageNumberException, TestException, NoSuchMethodException,
			SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchFieldException, InstantiationException,
			ClassNotFoundException, PagePinnedException, PageNotPinnedException {

		String[] policies = { "LRUPolicy", "MRUPolicy", "ClockPolicy",
				"RandomPolicy" };

		for (String policy : policies) {
			testPolicy(policy);
		}
	}

	private void testPolicy(String policy) throws InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			SecurityException, InvocationTargetException,
			NoSuchMethodException, ClassNotFoundException, DBFileException,
			BadFileException, BadPageNumberException, TestException,
			NoSuchFieldException, PagePinnedException, PageNotPinnedException {

		int poolSize = 20;
		String filename = "test";
		DiskSpaceManager.getInstance().createFile(filename, 0);
		BufferManager bm = new BufferManager(poolSize, policy);

		System.err.println("Testing " + policy + "...");

		bm.newPage(filename, 15);
		bm.unpinPage(filename, 0, false);

		for (int i = 0; i < 13; i++) {
			Page p = bm.pinPage(filename, i);
			if (p == null) {
				throw new TestException("Pinning page failed!");
			}
			bm.unpinPage(filename, i, true);
		}

		bm.flushPages();
		DiskSpaceManager.getInstance().eraseFile(filename);
		bm.flushPages();

		System.err.println("Successfully deleted and flushed again.");
	}

}
