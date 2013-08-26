package main.tests;

import java.lang.reflect.InvocationTargetException;

import buffermanager.BufferManager;
import buffermanager.Frame;
import buffermanager.database.FileSystem;
import buffermanager.database.exceptions.BadFileException;
import buffermanager.database.exceptions.BadPageNumberException;
import buffermanager.database.exceptions.DBFileException;
import buffermanager.exceptions.PageNotPinnedException;
import buffermanager.exceptions.PagePinnedException;
import buffermanager.page.Page;
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

	@SuppressWarnings("unchecked")
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
			System.out.println("after pinPage " + i);
		}
	}

}
