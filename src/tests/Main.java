package tests;

import java.lang.reflect.InvocationTargetException;
import java.util.ServiceLoader;

import tests.exceptions.TestException;
import buffermanager.BufferManager;
import buffermanager.database.exceptions.BadFileException;
import buffermanager.database.exceptions.BadPageNumberException;
import buffermanager.database.exceptions.DBFileException;

public class Main {
	public static void main(String[] args) throws ClassNotFoundException,
			NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchFieldException, InstantiationException, DBFileException,
			BadFileException, BadPageNumberException, TestException {

		System.out.println(Test.class);

		ServiceLoader<Test> sl = ServiceLoader.loadInstalled(Test.class);
		for (Test test : sl) {
			System.out.println(test);
			test.execute(new BufferManager(10, "LRU"), "test");
		}
	}
}
