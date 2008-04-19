/**
 * 
 */
package info.rvin.adt;

import java.util.ArrayList;
import java.util.List;

import com.adobe.air.ADT;
import com.adobe.argv.UsageError;

/**
 * Helper class to invoke the ADT tool with the correct arguments.
 * 
 * @author Joost den Boer
 *
 */
public class ADTInvoker {

	/**
	 * Private constructor.
	 * All methods static so no need to construct instance.
	 */
	private ADTInvoker() {
		super();
	}
	
	/**
	 * Execute ADT with all given arguments
	 * 
	 * @param args List of arguments
	 */
	static private void executeADT(List<String> args) {
		
		ADT.main(args.toArray(new String[args.size()]));
	}
	
	/**
	 * Package into AIR application
	 * Executes 'adt -package'
	 * 
	 * @param signOptions Sign options
	 * @param airFile name of file to create
	 * @param appDesc description of AIR application
	 * @param fileArgs arguments of file to include in AIR package
	 * @throws UsageError thrown if a problem occurs during packaging
	 */
	static public void doPackage(SigningOptions signOptions,
			String airFile, String appDesc, FileArgs fileArgs) throws UsageError {

		// prepare arguments
		List<String> args = new ArrayList<String>();
		args.add("-package");
		args.addAll(signOptions.getSigningOptions());
		args.add(airFile);
		args.add(appDesc);
		args.addAll(fileArgs.getFileArgsList());
		
		// execute adt
		executeADT(args);
	}
	
	/**
	 * Prepare an unsigned AIR package
	 * executes 'adt -prepare'
	 * 
	 * @param airiFile Name of airi file to create
	 * @param appDesc description of AIR application
	 * @param fileArgs arguments of file to include in AIR package
	 */
	static public void doPrepare(String airiFile, String appDesc, FileArgs fileArgs) {
		
		// prepare arguments
		List<String> args = new ArrayList<String>();
		args.add("-prepare");
		args.add(airiFile);
		args.add(appDesc);
		args.addAll(fileArgs.getFileArgsList());
		
		// execute adt
		executeADT(args);
	}
	
	/**
	 * Signs an unsigned AIR package
	 * executes 'adt -sign'
	 * 
	 * @param signOptions Sign options
	 * @param airiFile Name of airi file to sign
	 * @param airFile Name of airi file to create
	 * @throws UsageError thrown if an error occurs during signing
	 */
	static public void doSign(SigningOptions signOptions, String airiFile,
			String airFile) throws UsageError {

		// prepare arguments
		List<String> args = new ArrayList<String>();
		args.add("-sign");
		args.addAll(signOptions.getSigningOptions());
		args.add(airiFile);
		args.add(airFile);
		
		// execute adt
		executeADT(args);
	}
	
	/**
	 * Checks the certificate store
	 * Executes 'adt -checkstore'
	 * 
	 * @param signOptions Signing options
	 * @throws UsageError throw if an error occurs during checking store
	 */
	static public void doCheckStore(SigningOptions signOptions) throws UsageError {

		// prepare arguments
		List<String> args = new ArrayList<String>();
		args.add("-checkstore");
		args.addAll(signOptions.getSigningOptions());
		
		// execute adt
		executeADT(args);
		
	}
	
	/**
	 * Generates a self-signed certificate.
	 * Execute 'adt -certificate'
	 * 
	 * @param cArgs Certificate arguments
	 * @param keytype Type of key to apply on generated certificate
	 * @param pfxFile File in which to store the generated certificate
	 * @param password Password of the certificate store
	 * @throws UsageError throw if an error occurs during certificate creation
	 */
	static public void createCertificate(CertificateArgs certArgs, String keytype,
			String pfxFile, String password) throws UsageError {
		
		// prepare arguments
		List<String> args = new ArrayList<String>();
		args.add("-certificate");
		args.addAll(certArgs.getCertificateOptions());
		args.add(keytype);
		args.add(pfxFile);
		args.add(password);
		
		// execute adt
		executeADT(args);

	}
	
	/**
	 * Displays help info.
	 * Execute 'adt -help'
	 *
	 */
	static public void doHelp() {
		
		// prepare arguments
		List<String> args = new ArrayList<String>();
		args.add("-help");
		
		// execute adt
		executeADT(args);
	}
}
