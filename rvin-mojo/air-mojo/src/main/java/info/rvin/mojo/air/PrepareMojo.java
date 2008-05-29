/**
 * 
 */
package info.rvin.mojo.air;

import info.rvin.adt.ADTHelper;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Mojo to prepare and swf file and create an unsigned air package (airi).
 * The default file to package, the packageFile, is {build.finalName}.swf
 * 
 * @author Joost den Boer
 *
 * @goal prepare
 */
public class PrepareMojo extends AbstractPackagerMojo {

	/**
	 * airi file
	 *  
	 * @parameter expression="${project.build.directory}/${project.build.finalName}.airi"
	 * @required
	 */
	private File airiFile;
	
	/**
	 * 
	 */
	public PrepareMojo() {
		super();
	}

	/**
	 * Prepare an unsigned air package
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {

		// setup and validate package options
		setup(airiFile);
		
		// prepare air package
		ADTHelper.getInstance(getLog()).preparePackage(packageOptions);
	}

}
