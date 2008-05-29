/**
 * 
 */
package info.rvin.mojo.air;

import java.util.ArrayList;
import java.util.List;

/**
 * File to include in AIR package
 * 
 * @author Joost den Boer
 *
 */
public class IncludeFile {

	private String baseDirectory;
	
	private List<String> fileNames;
	
	/**
	 * 
	 */
	public IncludeFile() {
		
		fileNames = new ArrayList<String>();
		fileNames.add("*");
	}
	
	/**
	 * Construct an IncludeFile with the given file
	 * @param fileName String name of file to include
	 */
	public IncludeFile(String fileName) {
		
		fileNames = new ArrayList<String>();
		fileNames.add(fileName);
	}

	/**
	 * @return the baseDirectory
	 */
	public String getBaseDirectory() {
		return baseDirectory;
	}

	/**
	 * @param baseDirectory the baseDirectory to set
	 */
	public void setBaseDirectory(String baseDirectory) {
		this.baseDirectory = baseDirectory;
	}

	/**
	 * @return the fileNames
	 */
	public List<String> getFileNames() {
		return fileNames;
	}

	/**
	 * @param fileNames the fileNames to set
	 */
	public void setFileNames(List<String> fileNames) {
		this.fileNames = fileNames;
	}

	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();
		if(null != baseDirectory) {
			sb.append("baseDirectory=").append(baseDirectory).append("\n");
		}
		sb.append("FileNames:\n");
		for(String fileName : fileNames) {
			sb.append("\t").append(fileName).append("\n");
		}
		
		return sb.toString();
	}

}
