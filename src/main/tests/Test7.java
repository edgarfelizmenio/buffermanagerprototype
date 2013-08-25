package main.tests;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import buffermanager.BufferManager;
import buffermanager.Frame;
import buffermanager.database.FileSystem;
import buffermanager.database.exceptions.BadFileException;
import buffermanager.database.exceptions.BadPageNumberException;
import buffermanager.database.exceptions.DBFileException;
import buffermanager.exceptions.PagePinnedException;
import buffermanager.page.Page;
import main.Test;
import main.exceptions.TestException;

public class Test7 implements Test {

	@Override
	public void execute() throws DBFileException, BadFileException,
			BadPageNumberException, TestException, NoSuchMethodException,
			SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchFieldException, InstantiationException,
			ClassNotFoundException, PagePinnedException {

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
			NoSuchFieldException, PagePinnedException {

		int poolSize = 20;
		String filename = "test";
		FileSystem.getInstance().createFile(filename, 0);
		BufferManager bm = new BufferManager(poolSize, policy);

		System.out.println("Testing " + policy + "...");

		int[] pageIds = new int[30];
		Frame[] frames = new Frame[30];

		Arrays.fill(pageIds, 0);
		Arrays.fill(frames, null);

		ClassLoader cl = Test7.class.getClassLoader();

		Class<Frame> frameClass = (Class<Frame>) cl.loadClass(Frame.class
				.getName());

		Field pageIdField = frameClass.getDeclaredField("pageNum");
//		Field pageField = frameClass.getDeclaredField("page");
		pageIdField.setAccessible(true);
//		pageField.setAccessible(true);

		// Class<Page> pageField = (Class<Page>)
		// cl.loadClass(Page.class.getName());

		// Allocate 10 pages from database
		for (int i = 0; i < 10; i++) {
			Frame f = bm.newPage(1, filename);
			if (f == null) {
				throw new TestException("BufferManager.newPage failed!");
			}
			pageIds[i] = pageIdField.getInt(f);
			frames[i] = f;
		}
		
		// Pin first 10 pages a second time
		for (int i = 0; i < 10; i++) {
			System.out.println("Pinning page " + i +" " + pageIds[i]);
			Frame f = bm.pinPage(pageIds[i], filename);
			if (f == null) {
				throw new TestException("Unable to pin page");
			}
			
			// Checking pointers: after pinning once (with newPage), and pinning again, pointers should be the same.
			if (f != frames[i]) {
				throw new TestException("Error in pinning for the 2nd time.");
			}
		}
		
		// Try to free pinned pages
		for (int i = 5; i < 10; i++) {
			System.out.println("Freeing page " + pageIds[i]);
			boolean succeeded = true;
			try {
				bm.freePage(filename, pageIds[i]);
			} catch (PagePinnedException ppe) {
				succeeded = false;
			}
			
			if (succeeded) {
				throw new TestException("Pinned page freed.");
			}
		}
		
		// Free pages 0 to 9 by first unpinning each page twice
		for (int i = 0; i < 10; i++) {
			bm.unpinPage(pageIds[i], filename, false);
			bm.unpinPage(pageIds[i], filename, false);
			bm.freePage(filename, pageIds[i]);
			System.out.println("Freed page " + pageIds[i]);
		}
		
		// Get 14 more pages
		for (int i = 10; i < 24; i++) {
			Frame f = bm.newPage(1, filename);
			if (f == null) {
				throw new TestException("BufferManager.newPage failed!");
			}
			int pageId = pageIdField.getInt(f);
			System.out.println("New page " + i + "," + pageId);
		}
		
		
	}

}
