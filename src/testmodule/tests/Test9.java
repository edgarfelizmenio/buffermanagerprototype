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
 * <li>Unpinning a page twice.</li>
 * <li>Pinning a nonexistent page.</li>
 * <li>Unpinning a nonexistent page.</li>
 * <li>Freeing a page that is still pinned.</li>
 * <li>Filling the buffer pool with pinned pages and pinning 1 more page.</li>
 * </ol>
 */
public class Test9 implements Test {

	public void execute() throws DBFileException, BadFileException,
			BadPageNumberException, TestException, NoSuchMethodException,
			SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchFieldException, InstantiationException,
			ClassNotFoundException, PagePinnedException, PageNotPinnedException {

		String[] policies = { "LRUPolicy", "MRUPolicy", "ClockPolicy",
				"RandomPolicy" };

		for (String policy : policies) {
			System.err.println("Testing " + policy + "...");
			testPolicy(policy);
			System.err.println("End of test for policy " + policy + ".");
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

		int[] pageIds = new int[30];

		// Allocate 10 pages to database
		for (int i = 0; i < 10; i++) {
			int pageNumber = bm.newPage(filename, 1);
			if (pageNumber == Page.NO_PAGE_NUMBER) {
				throw new TestException("newPage failed");
			}
			pageIds[i] = pageNumber;
		}

		System.err.println("Allocated 10 pages successful");

		// Try to unpin a pinned page twice
		bm.unpinPage(filename, pageIds[0], false);
		System.err.println("Unpinning of a pinned page successful");

		// Try to unpin an unpinned page
		boolean success = true;
		try {
			bm.unpinPage(filename, pageIds[0], false);
		} catch (PageNotPinnedException pnpe) {
			success = false;
		}

		if (success) {
			throw new TestException("Unpinning of an unpinned page succeeded!");
		}
		System.err
				.println("Unpinning of an unpinned page failed (as it should)");

		// Pin a nonexistent page
		success = true;
		Page p;
		try {
			p = bm.pinPage(filename, 999);
		} catch (BadPageNumberException bpne) {
			success = false;
		}

		if (success) {
			throw new TestException("Pinning of a non-existent page succeeded!");
		}
		System.err
				.println("Pinning of a non-existent page failed (as it should)");

		// Unpin a nonexistent page
		success = true;
		try {
			bm.unpinPage(filename, 999, false);
		} catch (PageNotPinnedException pnpe) {
			success = false;
		}

		if (success) {
			throw new TestException(
					"Unpinning of a non-existent page succeeded!");
		}
		System.err
				.println("Unpinning of a non-existent page failed (as it should)");

		// Free a page that is still pinned
		p = bm.pinPage(filename, pageIds[0]);
		if (p == null) {
			throw new TestException("Unable to pin page!");
		}
		System.err.println("Pinning of page successful");
		
		success = true;
		try {
			bm.freePage(filename, pageIds[0]);
		} catch(PagePinnedException ppe) {
			success = false;
		}
		if (success) {
			throw new TestException("Freeing a pinned page succeeded!");
		}
		System.err.println("Freeing a pinned page failed (as it should)");
		
		// Free all allocated pages
		for (int i = 0; i < 10; i++) {
			bm.unpinPage(filename, pageIds[i],false);
			bm.freePage(filename, pageIds[i]);
		}
		System.err.println("Freeing allocated pages successful");
		
		// Allocate new buffer manager
		bm = new BufferManager(poolSize, policy);
		
		// Fill up buffer with pinned pages
		for (int i = 0; i < bm.getPoolSize(); i++) {
			int pageNumber = bm.newPage(filename, 1);
			if (pageNumber == Page.NO_PAGE_NUMBER) {
				throw new TestException("newPage failed");
			}
			pageIds[i] = pageNumber;
		}
		System.err.println("Allocate pages successful");

		// Try to pin one more page
		int pageNumber = bm.newPage(filename, 1);
		if (pageNumber != Page.NO_PAGE_NUMBER) {
			throw new TestException("Pinning a page in a full buffer succeeded!");
		}
		System.err.println("Pinning a page in a full buffer failed (as it should)");
	
		// Free all allocated pages
		for (int i = 0; i < bm.getPoolSize(); i++) {
			bm.unpinPage(filename, pageIds[i], false);
			bm.freePage(filename, pageIds[i]);
		}
		System.err.println("Freeing allocated pages successful");
		
	}

}
