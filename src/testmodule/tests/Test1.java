package testmodule.tests;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import testmodule.Test;
import testmodule.exceptions.TestException;


import dbms.buffermanager.exceptions.PageNotPinnedException;
import dbms.buffermanager.exceptions.PagePinnedException;
import dbms.diskspacemanager.DiskSpaceManager;
import dbms.diskspacemanager.exceptions.BadFileException;
import dbms.diskspacemanager.exceptions.BadPageNumberException;
import dbms.diskspacemanager.exceptions.DBFileException;
import dbms.diskspacemanager.page.Page;


/**
 * Test stub for the DiskSpaceManager
 * 
 */
public class Test1 implements Test {

	public void execute() throws DBFileException, BadFileException,
			BadPageNumberException, TestException, NoSuchMethodException,
			SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchFieldException, InstantiationException,
			ClassNotFoundException, PagePinnedException, PageNotPinnedException {

		DiskSpaceManager fs = DiskSpaceManager.getInstance();

		fs.createFile("testing", 5);

		fs.allocatePages("testing", 4); // file now has 9 pages (0-8)
		fs.deallocatePages("testing", 1, 2); // pages 1-2 will be deleted.
												// file now has 7 pages

		System.err.println(fs.allocatePages("testing", 2)); // page 9 and 10
		System.err.println(fs.allocatePages("testing", 1)); // page 11

		Page p = Page.makePage();

		p.setContents(1, "K".toCharArray());
		p.setContents(2, "R".toCharArray());

		fs.writePage("testing", 3, p);
		fs.readPage("testing", 0, p);
		System.err.println(Arrays.toString(Arrays.copyOfRange(p.getContents(),
				1, 10)));
		System.err.println();

		fs.readPage("testing", 3, p);
		System.err.println(Arrays.toString(Arrays.copyOfRange(p.getContents(),
				1, 10)));
		System.err.println();

		fs.createFile("testagain", 7);
		System.err.println(fs.allocatePages("testagain", 3));// pages 7,8,9
		fs.writePage("testagain", 2, p);

		fs.readPage("testagain", 1, p);
		System.err.println(Arrays.toString(Arrays.copyOfRange(p.getContents(),
				1, 10)));
		System.err.println();

		fs.readPage("testagain", 2, p);
		System.err.println(Arrays.toString(Arrays.copyOfRange(p.getContents(),
				1, 10)));
		System.err.println();

		// pages 1 and 2 of file testing are already deallocated by this time
		try {
			fs.readPage("testing", 1, p);
		} catch (DBFileException e) {
			System.err.println("Correctly caught unallocated page read.");
			System.err.println(e.getMessage());
		}

		try {
			fs.writePage("testing", 2, p);
		} catch (DBFileException e) {
			System.err.println("Correctly caught unallocated page write.");
			System.err.println(e.getMessage());
		}

		System.err.println(fs.eraseFile("testing")); // should print true
		System.err.println(fs.eraseFile("testagain")); // should print true
		System.err.println(fs.listFiles());

	}
}
