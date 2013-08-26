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
 * Tests if the following cases are handled properly:
 * <ol>
 * <li>Pinning an allocated page.</li>
 * <li>Propagating changes to disk (Check if a dirty page is written to disk).</li>
 * <li>Pinning a page in wrong file/nonexistent file.</li>
 * </ol>
 */
public class Test10 implements Test {

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
		
		
		bm.newPage(15, filename);
		bm.unpinPage(0, filename, false);
		
		Page p;
		for (int i = 0; i < 13; i++) {
			p = bm.pinPage(i, filename);
			if (p == null) {
				throw new TestException("Pinning page failed!");
			}
			System.out.println("After pinPage " + i);
			char[] data = ("This is test 10 for page " + i).toCharArray();
			p.setContents(data);
			bm.flushPage(filename,i);
			System.out.println("After flushPage " + i);
			bm.unpinPage(i, filename, true);
		}
	}

}
