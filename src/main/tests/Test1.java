package main.tests;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import buffermanager.database.FileSystem;
import buffermanager.database.exceptions.BadFileException;
import buffermanager.database.exceptions.BadPageNumberException;
import buffermanager.database.exceptions.DBFileException;
import buffermanager.page.Page;
import main.Test;
import main.exceptions.TestException;

/**
 * Test stub for the FileSystem
 * 
 */
public class Test1 implements Test {

	@Override
	public void execute() throws DBFileException, BadFileException,
			BadPageNumberException, TestException, NoSuchMethodException,
			SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchFieldException, InstantiationException,
			ClassNotFoundException {

		FileSystem fs = FileSystem.getInstance();

		fs.createFile("testing", 5);

		fs.allocatePages("testing", 4); // file now has 9 pages (0-8)
		fs.deallocatePages("testing", 1, 2); // pages 1-2 will be deleted.
												// file now has 7 pages

		System.out.println(fs.allocatePages("testing", 2)); // page 9 and 10
		System.out.println(fs.allocatePages("testing", 1)); // page 11

		Page p = Page.makePage();

		p.setContents(1, "K".toCharArray());
		p.setContents(2, "R".toCharArray());

		fs.writePage("testing", 3, p);
		fs.readPage("testing", 0, p);
		System.out.println(Arrays.toString(Arrays.copyOfRange(p.getContents(),
				1, 10)));
		System.out.println();

		fs.readPage("testing", 3, p);
		System.out.println(Arrays.toString(Arrays.copyOfRange(p.getContents(),
				1, 10)));
		System.out.println();

		fs.createFile("testagain", 7);
		System.out.println(fs.allocatePages("testagain", 3));// pages 7,8,9
		fs.writePage("testagain", 2, p);

		fs.readPage("testagain", 1, p);
		System.out.println(Arrays.toString(Arrays.copyOfRange(p.getContents(),
				1, 10)));
		System.out.println();

		fs.readPage("testagain", 2, p);
		System.out.println(Arrays.toString(Arrays.copyOfRange(p.getContents(),
				1, 10)));
		System.out.println();

		// pages 1 and 2 of file testing are already deallocated by this time
		try {
			fs.readPage("testing", 1, p);
		} catch (DBFileException e) {
			System.out.println("Correctly caught unallocated page read.");
			System.out.println(e.getMessage());
		}

		try {
			fs.writePage("testing", 2, p);
		} catch (DBFileException e) {
			System.out.println("Correctly caught unallocated page write.");
			System.out.println(e.getMessage());
		}

		System.out.println(fs.erase("testing")); // should print true
		System.out.println(fs.erase("testagain")); // should print true
		fs.listFiles();
	}
}
