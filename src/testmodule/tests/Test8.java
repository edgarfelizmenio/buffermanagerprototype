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
 * <li>Allocating a page.</li>
 * <li>Pinning a page.</li>
 * <li>Unpinning a page.</li>
 * <li>Writing a dirty page to disk.</li>
 * </ol>
 */
public class Test8 implements Test {

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

		char[] data = "This page is dirty!".toCharArray();
		
		int pageNumber = bm.newPage(filename, 1);

		if (pageNumber == Page.NO_PAGE_NUMBER) {
			throw new TestException("newPage failed!");
		}

		Page page = bm.findPage(filename, pageNumber);

		System.err.println("newPage successful");

		// Dirty page
		page.setContents(data);
		bm.unpinPage(filename, pageNumber, true);
		System.err.println("Unpinning of page successful");
		bm.flushPages();

		// Create a new buffer manager to see if it can handle it
		bm = new BufferManager(poolSize, policy);
		Page p = bm.pinPage(filename, pageNumber);
		if (p == null) {
			throw new TestException("Pinning of page failed!");
		}

		System.err.println("Pinning of page successful");

		if (!(new String(data).equals(new String(p.getContents()).substring(0, data.length)))) {
			throw new TestException("Dirtied page not written to disk!");
		}
		System.err.println("Dirtied page written to disk");
	}

}
