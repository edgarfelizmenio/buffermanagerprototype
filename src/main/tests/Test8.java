package main.tests;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import main.Test;
import main.exceptions.TestException;
import buffermanager.BufferManager;
import buffermanager.Frame;
import buffermanager.database.FileSystem;
import buffermanager.database.exceptions.BadFileException;
import buffermanager.database.exceptions.BadPageNumberException;
import buffermanager.database.exceptions.DBFileException;
import buffermanager.exceptions.PageNotPinnedException;
import buffermanager.exceptions.PagePinnedException;
import buffermanager.page.Page;

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

		char[] data = "This page is dirty!".toCharArray();

		Frame f = bm.newPage(1, filename);

		if (f == null) {
			throw new TestException("newPage failed!");
		}

		ClassLoader cl = Test8.class.getClassLoader();
		Class<Frame> frameClass = (Class<Frame>) cl.loadClass(Frame.class
				.getName());
		Field pageNumField = frameClass.getDeclaredField("pageNum");
		Field pageField = frameClass.getDeclaredField("page");
		
		pageNumField.setAccessible(true);
		pageField.setAccessible(true);
		
		int pageId = pageNumField.getInt(f);
		Page page = (Page) pageField.get(f);

		System.out.println("newPage successful");

		// Dirty page
		page.setContents(data);
		bm.unpinPage(pageId, filename, true);
		System.out.println("Unpinning of page successful");
		bm.flushPages();

		// Create a new buffer manager to see if it can handle it
		bm = new BufferManager(poolSize, policy);
		f = bm.pinPage(pageId, filename);
		if (f == null) {
			throw new TestException("Pinning of page failed!");
		}

		System.out.println("Pinning of page successful");

		page = (Page) pageField.get(f);

		if (!(new String(data).equals(new String(page.getContents()).substring(0, data.length)))) {
			throw new TestException("Dirtied page not written to disk!");
		}
		System.out.println("Dirtied page written to disk");
	}

}
