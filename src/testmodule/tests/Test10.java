package testmodule.tests;

import java.lang.reflect.InvocationTargetException;

import testmodule.Test;
import testmodule.exceptions.TestException;


import dbms.buffermanager.BufferManager;
import dbms.buffermanager.exceptions.PageNotPinnedException;
import dbms.buffermanager.exceptions.PagePinnedException;
import dbms.diskspacemanager.DiskSpaceManager;
import dbms.diskspacemanager.exceptions.BadFileException;
import dbms.diskspacemanager.exceptions.BadPageIDException;
import dbms.diskspacemanager.exceptions.DBFileException;
import dbms.diskspacemanager.page.Page;


/**
 * Tests if the following cases are handled properly:
 * <ol>
 * <li>Pinning an allocated page.</li>
 * <li>Propagating changes to disk (Check if a dirty page is written to disk).</li>
 * <li>Pinning a page in wrong file/nonexistent file.</li>
 * </ol>
 */
public class Test10 implements Test {

	public void execute() throws DBFileException, BadFileException,
			BadPageIDException, TestException, NoSuchMethodException,
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
			BadFileException, BadPageIDException, TestException,
			NoSuchFieldException, PagePinnedException, PageNotPinnedException {

		int poolSize = 20;
		String filename = "test";
		DiskSpaceManager.getInstance().createFile(filename, 0);
		BufferManager bm = new BufferManager(poolSize, policy);

		System.err.println("Testing " + policy + "...");

		bm.newPage(filename, 15);
		bm.unpinPage(filename, 0, false);

		Page p;
		for (int i = 0; i < 13; i++) {
			p = bm.pinPage(filename, i);
			if (p == null) {
				throw new TestException("Pinning page failed!");
			}
			System.err.println("After pinPage " + i);
			char[] data = ("This is test 10 for page " + i).toCharArray();
			p.setContents(data);
			bm.flushPage(filename, i);
			System.err.println("After flushPage " + i);
			bm.unpinPage(filename, i, true);
		}

		for (int i = 0; i < 13; i++) {
			p = bm.pinPage(filename, i);
			if (p == null) {
				throw new TestException("Pinning page failed!");
			}
			String readBack = new String(p.getContents());
			String orig = "This is test 10 for page " + i;

			System.err.println("PAGE[ " + i + " ]: "
					+ readBack.substring(0, orig.length()));
			if (!readBack.substring(0, orig.length()).equals(orig)) {
				throw new TestException("Page content incorrect!");
			}
			bm.unpinPage(filename, i, false);
		}

		// Try to pin a page in a different file
		boolean success = false;
		try {
			p = bm.pinPage(filename + "bheb", 1);
		} catch (BadFileException bfe) {
			System.err.println("Successfully caught pinning wrong file");
			success = true;
		}

		if (!success) {
			throw new TestException("Pinned wrong file!");
		}
	}

}
