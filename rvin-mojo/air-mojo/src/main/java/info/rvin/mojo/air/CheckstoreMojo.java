/**
 * 
 */
package info.rvin.mojo.air;

import info.rvin.adt.ADTHelper;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Mojo to check the keystore
 * 
 * @author Joost den Boer
 *
 * @goal checkstore
 */
public class CheckstoreMojo extends AbstractMojo {

	/**
	 * Signing options to check
	 * 
	 * @parameter
	 */
	private SigningOptions signingOptions;
	
	/**
	 * 
	 */
	public CheckstoreMojo() {
	}

	/**
	 * @see org.apache.maven.plugin.Mojo#execute()
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {

		// check store
		ADTHelper.getInstance(getLog()).checkStore(signingOptions);
	}

}
