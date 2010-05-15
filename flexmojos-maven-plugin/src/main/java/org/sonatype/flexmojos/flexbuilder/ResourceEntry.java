package org.sonatype.flexmojos.flexbuilder;

public class ResourceEntry
{
	private String destPath;
	private String sourcePath;
	
	public ResourceEntry(String destPath, String sourcePath)
	{
		this.destPath = destPath;
		this.sourcePath = sourcePath;
	}
	
	/**
	 * Returns the path to where the resource file will be added
	 * to the compiled SWC relative to the SWC root.
	 * @return
	 */
	public String getDestPath()
	{
		return destPath;
	}
	
	/**
	 * Sets the path to where the resource file will be added
	 * to the compiled SWC relative to the SWC root.
	 * @param path
	 */
	public void setDestPath(String path)
	{
		destPath = path;
	}
	
	/**
	 * Returns the absolute path to the resource file on the file system.
	 * The flex compiler uses this to resolve the resource file.
	 * @return
	 */
	public String getSourcePath()
	{
		return sourcePath;
	}
	
	/**
	 * Sets the absolute path to the resource file on the file system.
	 * The flex compiler uses this to resolve the resource file.
	 * @param path
	 */
	public void setSourcePath(String path)
	{
		sourcePath = path;
	}
}