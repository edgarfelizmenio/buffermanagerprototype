package main.tests;

import java.lang.reflect.InvocationTargetException;

import buffermanager.BufferManager;
import buffermanager.database.exceptions.BadFileException;
import buffermanager.database.exceptions.BadPageNumberException;
import buffermanager.database.exceptions.DBFileException;
import main.Test;
import main.exceptions.TestException;

public class Test2 implements Test {

	@Override
	public void execute(BufferManager bm, String filename)
			throws DBFileException, BadFileException, BadPageNumberException,
			TestException, NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchFieldException {
		
		System.out.println("hi");
	}

}
