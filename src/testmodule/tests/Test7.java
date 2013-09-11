package testmodule.tests;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

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
 * <ul>
 * <li>Allocating a new page.</li>
 * <li>Pinning a page.</li>
 * <li>Deallocating a page.</li>
 * </ul>
 */
public class Test7 implements Test {

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

		int[] pageIds = new int[30];
		Page[] pages = new Page[30];

		Arrays.fill(pageIds, 0);
		Arrays.fill(pages, null);

		// Allocate 10 pages from database
		for (int i = 0; i < 10; i++) {
			int pageNumber = bm.newPage(filename, 1);
			if (pageNumber == Page.NO_PAGE_NUMBER) {
				throw new TestException("BufferManager.newPage failed!");
			}
			pageIds[i] = pageNumber;
			pages[i] = bm.findPage(filename, pageNumber);
		}

		// Pin first 10 pages a second time
		for (int i = 0; i < 10; i++) {
			System.err.println("Pinning page " + i + " " + pageIds[i]);
			Page p = bm.pinPage(filename, pageIds[i]);
			if (p == null) {
				throw new TestException("Unable to pin page");
			}

			// Checking pointers: after pinning once (with newPage), and pinning
			// again, pointers should be the same.
			if (p != pages[i]) {
				throw new TestException("Error in pinning for the 2nd time.");
			}
		}

		// Try to free pinned pages
		for (int i = 5; i < 10; i++) {
			System.err.println("Freeing page " + pageIds[i]);
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
			bm.unpinPage(filename, pageIds[i], false);
			bm.unpinPage(filename, pageIds[i], false);
			bm.freePage(filename, pageIds[i]);
			System.err.println("Freed page " + pageIds[i]);
		}

		// Get 14 more pages
		for (int i = 10; i < 24; i++) {
			int pageNumber = bm.newPage(filename, 1);
			if (pageNumber == Page.NO_PAGE_NUMBER) {
				throw new TestException("BufferManager.newPage failed!");
			}
			System.err.println("New page " + i + "," + pageNumber);
		}

	}

}
