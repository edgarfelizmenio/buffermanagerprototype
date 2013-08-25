package main;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.ServiceLoader;

import main.exceptions.TestException;

import buffermanager.BufferManager;
import buffermanager.database.FileSystem;
import buffermanager.database.exceptions.BadFileException;
import buffermanager.database.exceptions.BadPageNumberException;
import buffermanager.database.exceptions.DBFileException;

public class Main {
	public static void main(String[] args) throws ClassNotFoundException,
			NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchFieldException, InstantiationException, DBFileException,
			BadFileException, BadPageNumberException, TestException,
			IOException, URISyntaxException {
		int poolSize = 20;
		
		String packagename = "main.tests";
		String path = packagename.replace('.', '/');
		System.out.println(path);

		URL resource = ClassLoader.getSystemClassLoader().getResource(path);
		System.out.println(resource);

		String fullpath = resource.getFile();
		System.out.println(fullpath);

		File directory = new File(resource.toURI());
		System.out.println(directory);
		
		List<Class<Test>> tests = new ArrayList<Class<Test>>();

		for (String f : directory.list()) {
			String className = packagename + '.'
					+ f.substring(0, f.length() - 6);
			System.out.println(className);
			tests.add((Class<Test>) Class.forName(className));
		}
		
		for (Class<Test> c: tests) {
			Test t = c.getConstructor(null).newInstance();
			FileSystem.getInstance().createFile("test", 0);
			t.execute(new BufferManager(poolSize, "Clock"), "test");
		}


	}
}
