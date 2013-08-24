package tests;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import tests.exceptions.TestException;
import buffermanager.BufferManager;
import buffermanager.Frame;
import buffermanager.database.exceptions.BadFileException;
import buffermanager.database.exceptions.BadPageNumberException;
import buffermanager.database.exceptions.DBFileException;
import buffermanager.page.Page;

public class Test1 implements Test {

	@Override
	public void execute(BufferManager bm, String filename)
			throws DBFileException, BadFileException, BadPageNumberException,
			TestException, NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchFieldException {
		
		int first = 5;
		int last = first + bm.getPoolSize() + 5;

		bm.newPage(last + 10, filename);
		bm.unpinPage(0, filename, false);

		System.out.println("TEST 1");

		// Pin the pages and modify the contents
		for (int i = first; i <= last; i++) {
			Frame f = bm.pinPage(i, filename);
			if (f == null) {
				throw new TestException("Unable to pin page 1st time");
			}
			System.out.println("After pin page " + i);

			char[] data = ("This is test 1 for page " + i).toCharArray();

			Method method = f.getClass().getDeclaredMethod("getPage");
			method.setAccessible(true);
			Page p = (Page) method.invoke(f);

			p.setContents(data);

			bm.unpinPage(i, filename, true);

			System.out.println("After unpin page " + i);
		}

		System.out.println();
		
		// Check if the contents of a dirty page are written to disk
		for (int i = first; i <= last + 1; i++) {
			Frame f = bm.pinPage(i, filename);
			if (f == null) {
				throw new TestException("Unable to pin page the 2nd time");	
			}
			
			Method method = f.getClass().getDeclaredMethod("getPage");
			method.setAccessible(true);
			Page p = (Page) method.invoke(f);
			
			Field contentsField = p.getClass().getField("contents");
			contentsField.setAccessible(true);
			char[] contents = (char[])contentsField.get(p);
			
			String original = "This is test 1 for page " + i;
			String readBack = new String(contents);
			
			if (readBack.substring(0, original.length()) != original) {
				throw new TestException("Page content incorrect"); // Contents of dirty page are not propagated to disk.
			}
			bm.unpinPage(i, filename, false);
		}
	}

}
