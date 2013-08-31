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

public class Test12 implements Test {

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
		
		bm.newPage(bm.getPoolSize() * 2, filename);
		bm.unpinPage(0, filename, false);
		
		for (int i = 0; i < bm.getPoolSize() + 1; i++) {
			Page p = bm.pinPage(i, filename);
			if (p == null) {
				throw new TestException("Pinning page failed!");
			}
			System.out.println("after pinPage " + i);
			char[] data = ("This is test 8 for page " + i).toCharArray();
			p.setContents(data);
			bm.flushPage(filename, i);
			System.out.println("after flushPage " + i);
			bm.unpinPage(i, filename, true);
		}
		
		int pageNumber = bm.newPage(1, filename);
		if (pageNumber == Page.NO_PAGE_NUMBER) {
			throw new TestException("newPage failed!");
		}
		Page p = bm.findPage(pageNumber, filename);
		
		// Verify that page is empty
		boolean empty = true;
		char contents[] = p.getContents();
		for (int i = 0; i < Page.PAGE_SIZE; i++) {
			if (contents[i] != '\0') {
				empty = false;
				break;
		}}
		
		if (!empty) {
			System.out.println("Test failed: page is not empty.");
			for (int i = 0; i < Page.PAGE_SIZE; i++) {
				System.out.println(contents[i]);
			}
			throw new TestException("Page is not empty.");
		}
	}

}
