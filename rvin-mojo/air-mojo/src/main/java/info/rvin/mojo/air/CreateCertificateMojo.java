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
