package info.rvin.mojo.flexmojo.compiler;

import java.io.File;

public class Stylesheet {

	/**
	 * The name in the archive.
	 */
	private String name;

	/**
	 * The file to be added
	 */
	private File path;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public File getPath() {
		return path;
	}

	public void setPath(File path) {
		this.path = path;
	}
}
