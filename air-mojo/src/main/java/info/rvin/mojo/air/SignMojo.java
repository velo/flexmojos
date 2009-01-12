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

import info.rvin.adt.ADTHelper;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Mojo to sign an unsigned airi package
 * 
 * @author Joost den Boer
 *
 * @goal sign
 * 
 */
public class SignMojo extends AbstractPackagerMojo {

	/**
	 * air file
	 *  
	 * @parameter expression="${project.build.directory}/${project.build.finalName}.air"
	 * @required
	 */
	private File airFile;

	/**
	 * airi file
	 *  
	 * @parameter expression="${project.build.directory}/${project.build.finalName}.airi"
	 * @required
	 */
	private File airiFile;
	
	/**
	 * Signing options for air package
	 * 
	 * @parameter
	 */
	private SigningOptions signingOptions;
	
	/**
	 * 
	 */
	public SignMojo() {
	}

	/**
	 * @see org.apache.maven.plugin.Mojo#execute()
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {

		// set input/output filesu
		packageOptions.setOutputFile(airFile);
		packageOptions.setInputFile(airiFile);
		
		getLog().debug("Using input file: "+airiFile.getAbsolutePath());
		getLog().debug("Creating air file:"+airFile.getAbsolutePath());
		
		ADTHelper.getInstance(getLog()).sign(packageOptions, signingOptions);
	}

}
