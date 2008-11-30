/**
 * Flex-mojos is a set of maven plugins to allow maven users to compile, optimize, test and ... Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008 Marvin Herman Froeder
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
