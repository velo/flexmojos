/**
 * 
 */
package info.rvin.mojo.air;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration options for the packager.
 * Includes both configuration and filenames to include in package.
 * 
 * With IncludeFiles the baseDirectory of the given fileNames can be set.
 * 
 * @author Joost den Boer
 *
 */
public class PackageOptions {

	/**
	 * Whether to validate the packager. Default <code>true</code>.
	 */
	private boolean validate = true;
	
	/**
	 * Debugging packager
	 */
	private boolean debug = false;
	
	/**
	 * Filenames to add to package
	 */
	private List<String> filenames;
	
	/**
	 * IncludeFiles to add to package
	 */
	private List<IncludeFile> includeFiles;
	
	/**
	 * File to which to output
	 */
	private File outputFile;
	
	/**
	 * File to use as input (for siging unsigned package)
	 */
	private File inputFile;
	
	/**
	 * Application description file
	 */
	private File descriptorFile;
	
	/**
	 * Base directory for files
	 */
	private File baseDirectory;
	
	/**
	 * 
	 */
	public PackageOptions() {
		
		filenames = new ArrayList<String>();
	}
	
	/**
	 * @return the includeFiles
	 */
	public List<IncludeFile> getIncludeFiles() {
		return includeFiles;
	}

	/**
	 * @param includeFiles the includeFiles to set
	 */
	public void setIncludeFiles(List<IncludeFile> includeFiles) {
		this.includeFiles = includeFiles;
	}

	/**
	 * @return the fileNames
	 */
	public List<String> getFilenames() {
		return filenames;
	}

	/**
	 * @param fileNames the fileNames to set
	 */
	public void setFilenames(List<String> fileNames) {
		this.filenames = fileNames;
	}


	/**
	 * @return the validate
	 */
	public boolean isValidate() {
		return validate;
	}

	/**
	 * @param validate the validate to set
	 */
	public void setValidate(boolean validate) {
		this.validate = validate;
	}
	
	/**
	 * @return the debug
	 */
	public boolean isDebug() {
		return debug;
	}

	/**
	 * @param debug the debug to set
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	/**
	 * @return the outputFile
	 */
	public File getOutputFile() {
		return outputFile;
	}

	/**
	 * @param outputFile the outputFile to set
	 */
	public void setOutputFile(File outputFile) {
		this.outputFile = outputFile;
	}

	/**
	 * @return the descriptorFile
	 */
	public File getDescriptorFile() {
		return descriptorFile;
	}

	/**
	 * @param descriptorFile the descriptorFile to set
	 */
	public void setDescriptorFile(File descriptorFile) {
		this.descriptorFile = descriptorFile;
	}

	/**
	 * @return the baseDirectory
	 */
	public File getBaseDirectory() {
		return baseDirectory;
	}

	/**
	 * @param baseDirectory the baseDirectory to set
	 */
	public void setBaseDirectory(File baseDirectory) {
		this.baseDirectory = baseDirectory;
	}

	/**
	 * @return the inputFile
	 */
	public File getInputFile() {
		return inputFile;
	}

	/**
	 * @param inputFile the inputFile to set
	 */
	public void setInputFile(File inputFile) {
		this.inputFile = inputFile;
	}

	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
	
		StringBuilder sb = new StringBuilder("PackageOptions:\n");
	
		sb.append("Base-directory:").append(baseDirectory).append("\n");
		sb.append("Validate:").append(validate).append("\n");
		sb.append("Debug:").append(debug).append("\n");
		sb.append("Output-file:").append(outputFile).append("\n");
		sb.append("Descriptor-file:").append(descriptorFile).append("\n");
		
		sb.append("FileNames:\n");
		for(String fileName : filenames) {
			sb.append("\t").append(fileName).append("\n");
		}
		if(null != includeFiles) {
			sb.append("IncludeFiles:\n");
			for(IncludeFile iFile : includeFiles) {
				sb.append("\t").append(iFile.toString());
			}
		}
		
		return sb.toString();
	}
}
