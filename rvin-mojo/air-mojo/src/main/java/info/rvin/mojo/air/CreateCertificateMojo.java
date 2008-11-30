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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Mojo to create a self-signed certificate to be used to self-sign
 * air packages.
 * 
 * @author Joost den Boer
 *
 * TODO: add to package phase?
 * 
 * @goal create-certificate
 */
public class CreateCertificateMojo extends AbstractMojo {

	/**
	 * Certificate options to use to create certificate
	 * 
	 * @parameter
	 */
	private Certificate certificate;
	
	/**
	 * 
	 */
	public CreateCertificateMojo() {
	}

	/**
	 * Create a self-signed certificate.
	 * 
	 * Setup the packager to be ready to create the self-signed certificate.
	 * - create new creator
	 * - parse certificate
	 * - create certificate
	 * 
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {

		// delegate to helper
		ADTHelper.getInstance(getLog()).createCertificate(certificate);
	}

}
