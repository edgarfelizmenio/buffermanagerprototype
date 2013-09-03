package testmodule.tests;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import testmodule.Test;
import testmodule.exceptions.TestException;


import dbms.buffermanager.BufferManager;
import dbms.buffermanager.exceptions.PageNotPinnedException;
import dbms.diskspacemanager.DiskSpaceManager;
import dbms.diskspacemanager.exceptions.BadFileException;
import dbms.diskspacemanager.exceptions.BadPageNumberException;
import dbms.diskspacemanager.exceptions.DBFileException;
import dbms.diskspacemanager.page.Page;


/**
 * Tests if the following cases are handled properly:
 * <ol>
 * <li>Pinning a page.</li>
 * <li>Unpinning a page.</li>
 * <li>Writing dirty pages to disk.</li>
 * </ol>
 */
public class Test2 implements Test {

	public void execute() throws DBFileException, BadFileException,
			BadPageNumberException, TestException, NoSuchMethodException,
			SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchFieldException, InstantiationException,
			ClassNotFoundException, PageNotPinnedException {

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
			NoSuchFieldException, PageNotPinnedException {

		int poolSize = 20;
		String filename = "test";
		DiskSpaceManager.getInstance().createFile(filename, 0);
		BufferManager bm = new BufferManager(poolSize, policy);

		System.err.println("Testing " + policy + "...");

		int first = 5;
		int last = first + bm.getPoolSize() + 5;

		bm.newPage(filename, last + 10);
		bm.unpinPage(filename, 0, false);

		// Pin the pages and modify the contents
		for (int i = first; i <= last; i++) {
			Page p = bm.pinPage(filename, i);
			if (p == null) {
				throw new TestException("Unable to pin page 1st time");
			}
			System.err.println("After pin page " + i);

			char[] data = ("This is test 1 for page " + i).toCharArray();


			p.setContents(data);

			bm.unpinPage(filename, i, true);

			System.err.println("After unpin page " + i);
		}

		System.err.println();

		// Check if the contents of a dirty page are written to disk
		for (int i = first; i <= last; i++) {
			Page p = bm.pinPage(filename, i);
			if (p == null) {
				throw new TestException("Unable to pin page the 2nd time");
			}

			Field contentsField = p.getClass().getDeclaredField("contents");
			contentsField.setAccessible(true);
			char[] contents = (char[]) contentsField.get(p);

			String original = "This is test 1 for page " + i;
			String readBack = new String(contents);

			if (!original.equals(readBack.substring(0, original.length()))) {
				throw new TestException("Page content incorrect"); // Contents
																	// of dirty
																	// page are
																	// not
																	// propagated
																	// to disk.
			}
			bm.unpinPage(filename, i, false);
		}
	}

}
