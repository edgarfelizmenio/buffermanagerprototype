package main;

import java.lang.reflect.InvocationTargetException;

import main.exceptions.TestException;

import buffermanager.BufferManager;
import buffermanager.database.exceptions.BadFileException;
import buffermanager.database.exceptions.BadPageNumberException;
import buffermanager.database.exceptions.DBFileException;

public interface Test {
	public void execute(BufferManager bm, String filename)
			throws DBFileException, BadFileException, BadPageNumberException,
			TestException, NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchFieldException;
}
