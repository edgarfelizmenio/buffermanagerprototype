package main;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import main.exceptions.TestException;
import buffermanager.database.exceptions.BadFileException;
import buffermanager.database.exceptions.BadPageNumberException;
import buffermanager.database.exceptions.DBFileException;
import buffermanager.exceptions.PagePinnedException;

public class Main {
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws URISyntaxException,
			ClassNotFoundException {
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

		int i = 1;
		for (Class<Test> c : tests) {
			Test t;
			try {
				System.out.println("Start of " + c.getName());
				t = c.getConstructor(null).newInstance();
				t.execute();
				System.out.println(c.getName() + " passed.\n");
			} catch (NoSuchMethodException | SecurityException
					| IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchFieldException
					| InstantiationException | DBFileException
					| BadFileException | BadPageNumberException
					| PagePinnedException | TestException e) {
				System.out.println(c.getName() + " failed.");
				e.printStackTrace();
				System.out.println(e);
			}
			i++;
		}

	}
}
