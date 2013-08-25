package main;

import java.lang.reflect.InvocationTargetException;

import main.exceptions.TestException;

import buffermanager.BufferManager;
import buffermanager.database.exceptions.BadFileException;
import buffermanager.database.exceptions.BadPageNumberException;
import buffermanager.database.exceptions.DBFileException;
import buffermanager.exceptions.PagePinnedException;

public interface Test {
	public void execute()
			throws DBFileException, BadFileException, BadPageNumberException,
			TestException, NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchFieldException, InstantiationException, ClassNotFoundException, PagePinnedException;
}
