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
