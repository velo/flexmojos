/**
 * 
 */
package info.rvin.adt;

import java.util.ArrayList;
import java.util.List;

import com.adobe.argv.UsageError;

/**
 * File argument options for ADT tool
 * 
 * @author Joost den Boer
 *
 */
public class FileArgs {

	// Base directory for all files or directories in this object  
	private String baseDirectory = null;
	
	// List of files or directories
	private List<String> fileOrDirs = null;
	
	// List of sub file arguments
	public List<FileArgs> subFileArgs = null;
	
	/*
	 * List of packageDirFiles. 
	 * This is only allowed if the baseDirectory is null.
	 */ 
	private List<PackageDirFile> packageDirFiles = null;
	
	/**
	 * Create a default FileArgs instance
	 *
	 */
	public FileArgs() {
		super();
	}
	
	/**
	 * Create a FileArgs instance with given base directory
	 * 
	 * @param baseDir String name of base directory for all files and directory
	 * 			in this FileArgs instance
	 */
	public FileArgs(String baseDir) {
		super();
		this.baseDirectory = baseDir;
	}
	
	/**
	 * @return the fileOrDirs
	 */
	public List<String> getFileOrDirs() {
		return fileOrDirs;
	}

	/**
	 * Returns a list of a file arguments set in this instance
	 * 
	 * @return List of file arguments
	 */
	public List<String> getFileArgsList() {
		
		List<String> args = new ArrayList<String>();
		// first add all files or directories
		if(fileOrDirs != null) {
			args.addAll(getFileOrDirs());
		}
		
		// second add all sub file argument
		if(subFileArgs != null) {
			for(FileArgs fa : subFileArgs) {
				args.add("-C");
				args.addAll(fa.getFileOrDirs());
			}
		}
		
		// last add package dir files
		if(packageDirFiles != null) {
			for(PackageDirFile pdf : packageDirFiles) {
				args.add(pdf.toString());
			}
		}
		
		// return all arguments
		return args;
	}
	
	/**
	 * Add sub file arguments
	 * The sub FileArg instance must have a base directory set.
	 * All files or directories in this FileArg instance must exist
	 * relativly to this base directory.
	 * 
	 * @param fargs FileArg sub instance to add
	 */
	public void addSubFileArgs(FileArgs fargs) {
		
		if(subFileArgs == null) {
			subFileArgs = new ArrayList<FileArgs>();
		}
		subFileArgs.add(fargs);
	}

	/**
	 * Adds a file or directory
	 * @param fileOrDir String name of file or directory to add
	 */
	public void addFileOrDir(String fileOrDir) {
		
		// create list if doesn't exist yet
		if(fileOrDirs == null) {
			fileOrDirs = new ArrayList<String>();
		}
		// add to list
		fileOrDirs.add(fileOrDir);
	}

	/**
	 * Adds a file which must be placed in a special directory
	 * in the air package.
	 * 
	 * @param file String name of file which to put in a special directory
	 * @param dir String name of directory in which to put the special file
	 * @throws UsageError thrown if a packageFileDir is added to a FileArgs
	 * 			instance which has a non-null base directory
	 */
	public void addPackageFileDir(String file, String dir) throws UsageError {

		// test if baseDirectory is set
		if(baseDirectory == null) {
			// create list if doesn't exist yet
			if(packageDirFiles == null) {
				packageDirFiles = new ArrayList<PackageDirFile>();
			}
			// add to list
			packageDirFiles.add(new PackageDirFile(file, dir));
		} else {
			throw new UsageError("Only allowed to add a packageFileDir" +
					" to the main FileArgs object which has a" +
					" base directory which is null");
		}
	}

	/**
	 * Set all files and/or directories at once.
	 * 
	 * @param fileOrDirs the fileOrDirs to set
	 */
	public void setFileOrDirs(List<String> fileOrDirs) {
		this.fileOrDirs = fileOrDirs;
	}

	/**
	 * private class to specify a file which is to be placed in a
	 * special directory in the air-file.
	 * 
	 * See deg_guide_flex_air1.pdf, page 29.
	 * 
	 * @author Joost den Boer
	 */
	private class PackageDirFile {

		private String file;
		private String directory;
		
		public PackageDirFile(String f, String dir) {
			super();
			this.file = f;
			this.directory = dir;
		}

		/*
		 * Returns the String value of this PackageDirFile object:
		 * '-e <file> <directory>'
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			
			return new StringBuilder("-e ")
					.append(file)
					.append(" ")
					.append(directory)
					.toString(); 
		}
	}
}
