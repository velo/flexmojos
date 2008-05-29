/**
 * 
 */
package info.rvin.mojo.air;

import info.rvin.adt.ADTHelper;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Mojo to create an AIR package of a swf file and sign it with a certificate
 * Optionally, if the certificate does not exist, a self-signed certificate
 * is created.
 * 
 * The default file to package, the packageFile, is {build.finalName}.swf
 * 
 * @author Joost den Boer
 *
 * @goal package
 */
public class PackageMojo extends AbstractPackagerMojo {

	/**
	 * air file
	 *  
	 * @parameter expression="${project.build.directory}/${project.build.finalName}.air"
	 * @required
	 */
	private File airFile;

	/**
	 * Certificate options to use to create certificate
	 * 
	 * @parameter
	 */
	private Certificate certificate;

	/**
	 * Signing options for air package
	 * 
	 * @parameter
	 */
	private SigningOptions signingOptions;
	
	
	/**
	 * 
	 */
	public PackageMojo() {
		super();
	}

	/**
	 * Create air package
	 * 
	 * - Check if keystore exists. If not, first create a self-signed 
	 * 		certificate if Certificate information is provided. If not,
	 * 		display failure message.
	 * 
	 * - If keystore available, create air package.
	 * 
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {

		// setup and validate package options
		setup(airFile);
		
		// check signing options
		if(null == signingOptions) {
			throw new MojoFailureException("No signing options provided.");
		} else {
			getLog().debug("Signing Options provided");
			// if keystore not set or does not exist
			if(signingOptions.getKeystore() == null || 
					"".equals(signingOptions.getKeystore()) ||
					!(new File(signingOptions.getKeystore())).exists()
					) {
				
				getLog().debug("KeyStore not provided or file does not exist");
				// try to create self-signed certificate
				if(null != certificate) {
					
					File certFile = new File(certificate.getPfxFile());
					if(!certFile.exists()) {
						getLog().debug("Generate self-signed certificate");
						ADTHelper.getInstance(getLog()).createCertificate(certificate);
					}
					// copy keyStore and keyPass to signingOptions
					signingOptions.setKeystore(certificate.getPfxFile());
					signingOptions.setStorepass(certificate.getPassword());

					getLog().debug("Using keyStore: "+signingOptions.getKeystore());
					getLog().debug("Using storePass: "+signingOptions.getStorepass());
					getLog().info("Using self-signed certificate to sign air-package");
				} else {
					throw new MojoFailureException("No keyStore or Certificate provided. Cannot sign air package.");
				}
			}
		}
		
		// create air packager
		ADTHelper.getInstance(getLog()).createPackage(packageOptions, signingOptions);
	}

}
