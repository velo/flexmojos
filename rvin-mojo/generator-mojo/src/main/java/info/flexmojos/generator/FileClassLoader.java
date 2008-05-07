/**
 * Flexmojos is a set of maven goals to allow maven users to compile, optimize and test Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008-2012  Marvin Froeder <marvin@flexmojos.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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

		Package p = c.getPackage();
		if(p == null) {
			String packageName = name.substring(0, name.lastIndexOf('.'));
			p = super.definePackage(packageName, null, null, null, null, null, null, null);
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