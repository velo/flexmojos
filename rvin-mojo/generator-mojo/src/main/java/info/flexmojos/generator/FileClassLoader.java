package info.flexmojos.generator;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileClassLoader extends ClassLoader {
	private String root;

	public FileClassLoader(String rootDir) {
		if (rootDir == null)
			throw new IllegalArgumentException("Null root directory");
		root = rootDir;
	}

	protected Class<?> loadClass(String name, boolean resolve)
			throws ClassNotFoundException {

		// Since all support classes of loaded class use same class loader
		// must check subclass cache of classes for things like Object
		Class<?> c = findLoadedClass(name);
		if (c == null) {
			try {
				c = findSystemClass(name);
			} catch (Exception e) {
				// Ignore these
			}
		}

		if (c == null) {
			// Convert class name argument to filename
			// Convert package names into subdirectories
			String filename = name.replace('.', File.separatorChar) + ".class";

			// Class loaded yet?

			try {

				byte data[] = loadClassData(filename);

				// Load class data from file and save in byte array
				c = defineClass(name, data, 0, data.length);
				if (c == null)
					throw new ClassNotFoundException(name);

				// Convert byte array to Class

				// If failed, throw exception

			} catch (IOException e) {
				throw new ClassNotFoundException("Error reading file: "
						+ filename);
			}
		}

		// Resolve class definition if approrpriate
		if (resolve)
			resolveClass(c);

		// Return class just created

		return c;
	}

	private byte[] loadClassData(String filename) throws IOException {

		// Create a file object relative to directory provided
		File f = new File(root, filename);

		// Get size of class file
		int size = (int) f.length();

		// Reserve space to read
		byte buff[] = new byte[size];

		// Get stream to read from
		FileInputStream fis = new FileInputStream(f);
		DataInputStream dis = new DataInputStream(fis);

		// Read in data
		dis.readFully(buff);

		// close stream
		dis.close();

		// return data
		return buff;
	}

	public static void main(String[] args) throws Exception {
		FileClassLoader loader = new FileClassLoader(
				"D:/flex/workspace/mojo/flex-mojo-IT/src/test/resources/simple-generation/target/classes");
		Class<?> c = loader.loadClass("com.acme.TestClass");

		Object tester = c.newInstance();

		System.out.println(tester);
	}
}