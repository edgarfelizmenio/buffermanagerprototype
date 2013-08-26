package main.tests;

import java.lang.reflect.InvocationTargetException;

import dbms.buffermanager.BufferManager;
import dbms.buffermanager.exceptions.PageNotPinnedException;
import dbms.buffermanager.exceptions.PagePinnedException;
import dbms.diskspacemanager.FileSystem;
import dbms.diskspacemanager.exceptions.BadFileException;
import dbms.diskspacemanager.exceptions.BadPageNumberException;
import dbms.diskspacemanager.exceptions.DBFileException;
import dbms.diskspacemanager.page.Page;

import main.Test;
import main.exceptions.TestException;

/**
 * Test newPage, pinPage, unpinPage, and whether a dirty page is written to
 * disk.
 * 
 */
public class Test8 implements Test {

	@Override
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
		FileSystem.getInstance().createFile(filename, 0);
		BufferManager bm = new BufferManager(poolSize, policy);

		System.out.println("Testing " + policy + "...");

		char[] data = "This page is dirty!".toCharArray();
		
		int pageNumber = bm.newPage(1, filename);

		if (pageNumber == Page.NO_PAGE_NUMBER) {
			throw new TestException("newPage failed!");
		}

		Page page = bm.findPage(pageNumber, filename);

		System.out.println("newPage successful");

		// Dirty page
		page.setContents(data);
		bm.unpinPage(pageNumber, filename, true);
		System.out.println("Unpinning of page successful");
		bm.flushPages();

		// Create a new buffer manager to see if it can handle it
		bm = new BufferManager(poolSize, policy);
		Page p = bm.pinPage(pageNumber, filename);
		if (p == null) {
			throw new TestException("Pinning of page failed!");
		}

		System.out.println("Pinning of page successful");

		if (!(new String(data).equals(new String(p.getContents()).substring(0, data.length)))) {
			throw new TestException("Dirtied page not written to disk!");
		}
		System.out.println("Dirtied page written to disk");
	}

}
