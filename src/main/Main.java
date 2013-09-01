package main;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Main {
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws URISyntaxException,
			ClassNotFoundException {

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

		for (Class<Test> c : tests) {
			Test t;
			try {
				System.out.println("Start of " + c.getName());
				t = c.getConstructor().newInstance();
				t.execute();
				System.out.println(c.getName() + " passed.\n");
			} catch (Exception e) {
				System.out.println(c.getName() + " failed.");
				e.printStackTrace();
				System.out.println(e);
			}
		}

	}
}
