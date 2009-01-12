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

import java.io.File;

import org.apache.maven.model.Build;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Abstract Packager Mojo provides some common properties and functionality
 * for both the PrepareMojo and PackagerMojo. 
 * 
 * The default file to package, the packageFile, is {build.finalName}.swf
 * 
 * @author Joost den Boer
 */
public abstract class AbstractPackagerMojo extends AbstractMojo {

	/**
	 * @parameter expression="${project.build}"
	 * @required
	 * @readonly
	 */
	protected Build build;
	
	/**
	 * AIR Application description file
	 * 
	 * @parameter expression="${project.build.sourceDirectory}/${project.artifactId}-app.xml"
	 * @required
	 */
	protected File applicationDescriptionFile;

	/**
	 * File to include in package
	 * 
	 * @parameter
	 */
	protected PackageOptions packageOptions;
	
	/**
	 * 
	 */
	public AbstractPackagerMojo() {
		super();
	}

	/**
	 * Setup packaging
	 * 
	 * @param outputFile output of packager
	 * @throws MojoFailureException
	 */
	protected void setup(File outputFile) throws MojoFailureException {

		// setting base directory for packageOptions to target directory
		if(null == packageOptions.getBaseDirectory()) {
			packageOptions.setBaseDirectory(new File(build.getDirectory()));
		}
		
		// set default packageFile if not provided.
		if(null == packageOptions.getFilenames() ||
				packageOptions.getFilenames().size() == 0) {
			String SwfFilename = build.getFinalName() + ".swf";
			packageOptions.getFilenames().add(SwfFilename);
		}

		// set airiFile and appDescFile in packageOptions
		packageOptions.setOutputFile(outputFile);
		packageOptions.setDescriptorFile(applicationDescriptionFile);
		
		// validate packageOptions
		if(!packageOptions.getOutputFile().exists()) {
			getLog().debug("Output file does not exist");
		} else {
			getLog().debug("Output file already exists");
		}
		if(!packageOptions.getDescriptorFile().exists()) {
			getLog().debug("Descriptor file does not exist");
		} else {
			getLog().debug("Descriptor file exists");
		}
		
		// Output info to debugger
		getLog().debug("outputFile: "+outputFile.getAbsolutePath());
		getLog().debug("applicationDescriptionFile: "+applicationDescriptionFile.getAbsolutePath());
		getLog().debug("packageOptions: "+packageOptions.toString());
	}
}
