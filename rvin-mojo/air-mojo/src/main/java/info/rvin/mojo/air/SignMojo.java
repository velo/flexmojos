/**
 * Flexmojos is a set of maven goals to allow maven users to compile, optimize and test Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008-2012  Marvin Froeder <marvin@flexmojos.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
