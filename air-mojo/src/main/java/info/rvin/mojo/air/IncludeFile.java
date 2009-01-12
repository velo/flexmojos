/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
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
