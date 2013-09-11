package testmodule;

import java.lang.reflect.InvocationTargetException;

import testmodule.exceptions.TestException;

import dbms.buffermanager.exceptions.PageNotPinnedException;
import dbms.buffermanager.exceptions.PagePinnedException;
import dbms.diskspacemanager.exceptions.BadFileException;
import dbms.diskspacemanager.exceptions.BadPageIDException;
import dbms.diskspacemanager.exceptions.DBFileException;


public interface Test {
	public void execute() throws DBFileException, BadFileException,
	BadPageIDException, TestException, NoSuchMethodException,
	SecurityException, IllegalAccessException,
	IllegalArgumentException, InvocationTargetException,
	NoSuchFieldException, InstantiationException,
	ClassNotFoundException, PagePinnedException, PageNotPinnedException;
}
