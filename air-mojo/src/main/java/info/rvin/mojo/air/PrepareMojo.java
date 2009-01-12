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
