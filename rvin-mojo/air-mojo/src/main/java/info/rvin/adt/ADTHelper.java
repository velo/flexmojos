/**
 * 
 */
package info.rvin.adt;

import info.rvin.mojo.air.Certificate;
import info.rvin.mojo.air.IncludeFile;
import info.rvin.mojo.air.PackageOptions;
import info.rvin.mojo.air.SigningOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.util.List;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

import com.adobe.air.AIRPackager;
import com.adobe.air.CertificateCreator;
import com.adobe.air.Listener;

/**
 * Helper class for ADT functionality. Provides functionality which is share by
 * multiple mojos.
 * 
 * @author Joost den Boer
 */
public class ADTHelper {

	/* logging instance */
	private Log logger;

	/**
	 * 
	 */
	private ADTHelper(Log logInstance) {
		super();
		this.logger = logInstance;
	}

	/**
	 * Return new helper instance
	 * 
	 * @param logger
	 *            Log instance to be used by logger
	 * @return ADTHelper
	 */
	public static ADTHelper getInstance(Log logger) {
		return new ADTHelper(logger);
	}

	/**
	 * Create a self-signed certificate.
	 * 
	 * Setup the packager to be ready to create the self-signed certificate. -
	 * create new creator - parse certificate - create certificate
	 * 
	 * @param certificate
	 * @throws MojoFailureException
	 */
	public void createCertificate(Certificate certificate)
			throws MojoFailureException {

		logger.debug("Creating a self-signed certificate.");

		// new creator
		CertificateCreator creator = new CertificateCreator();

		// parse certificate
		parseCertificate(certificate, creator);

		// create certificate
		try {
			creator.create();
		} catch (IOException e) {
			throw new MojoFailureException(e.getMessage());
		}

		logger.debug("Done creating certificate.");
	}

	/**
	 * Prepare an unsigned air package - prepare packager - create intermediate
	 * 
	 * @param packageOptions
	 * @throws MojoFailureException
	 */
	public void preparePackage(PackageOptions packageOptions)
			throws MojoFailureException {

		logger.debug("Preparing package ...");

		// create packager
		PackagerListener listener = new PackagerListener(logger);
		AIRPackager packager = getAIRPackager(listener);

		// prepare packager
		parsePackageOrPrepare(packager, packageOptions, ".airi");

		// create intermediate
		try {
			logger.debug("Creating intermediate");
			packager.createIntermediate();
		} catch (IOException e) {
			logger.error("Error creating intermediate package.");
			throw new MojoFailureException(e.getMessage());
		} catch (Throwable t) {
			logger.error(t);
		} finally {
			packager.close();
		}

		logger.debug("Done preparing.");
	}

	/**
	 * create an (unsigned) air package - prepare packager - create self-signed
	 * certificate if needed - create air file
	 * 
	 * @param packageOptions
	 * @throws MojoFailureException
	 */
	public void createPackage(PackageOptions packageOptions,
			SigningOptions signingOptions) throws MojoFailureException {

		logger.debug("Start packaging ...");

		// create packager
		// PackagerListener listener = new PackagerListener(logger);
		MessageCollector listener = new MessageCollector();
		AIRPackager packager = getAIRPackager(listener);

		// check if signingOptions available
		processSignOptions(packager, signingOptions);

		// prepare packager
		parsePackageOrPrepare(packager, packageOptions, ".air");

		// create air package
		createAirPackage(packager);

		logger.debug("Done packaging.");
		
		processMessages(listener);
	}

	/**
	 * Process signing options and parse them
	 * 
	 * @param packager AIRPackager
	 * @param signingOptions SigningOptions to process
	 * @throws MojoFailureException 
	 */
	private void processSignOptions(AIRPackager packager, SigningOptions signingOptions) throws MojoFailureException {
		
		// check if signingOptions available
		if (null == signingOptions) {
			throw new MojoFailureException("No signing options provided");
		} else {
			// check if keystore exists
			File keystore = new File(signingOptions.getKeystore());
			logger.debug("Keystore " + keystore.getAbsolutePath() + " exists: "
					+ keystore.exists());

			if (!keystore.exists()) {
				throw new MojoFailureException(
						"Provided keystore does not exist");
			} else {
				parseSigningOptions(packager, signingOptions);
			}
		}
	}
	
	/**
	 * Process messages collected by MessageCollector
	 * 
	 * @param mc MessageCollector
	 * @throws MojoFailureException 
	 */
	private void processMessages(MessageCollector mc) throws MojoFailureException {

		// process message collected by packager listener
		if (mc.hasMessages) {
			boolean hasErrors = false;
			for (MessageCollector.ADTMessage msg : mc.messages) {
				System.out.println(msg.toString());
				if ("ERROR".equals(msg.type)) {
					hasErrors = true;
				}
			}

			if (hasErrors) {
				throw new MojoFailureException(
						"Detected ADT Error while creating package");
			}
		}
	}
	
	/**
	 * Create air package
	 * 
	 * @param packager AIRPackager to use
	 * @throws MojoFailureException 
	 */
	private void createAirPackage(AIRPackager packager) throws MojoFailureException {
		// create air package
		try {
			logger.debug("Creating air package");
			packager.createAIR();
		} catch (IOException e) {
			logger.error("Error creating air package.", e);
			throw new MojoFailureException(e.getMessage());
		} catch (Throwable t) {
			logger.error(t);
		} finally {
			packager.close();
		}
	}

	/**
	 * Check store
	 * 
	 * @param signingOptions
	 * @throws MojoFailureException
	 */
	public void checkStore(SigningOptions signingOptions)
			throws MojoFailureException {

		parseSigningOptions(getAIRPackager(null), signingOptions);

		logger.info("Valid password");
	}

	/**
	 * Sign unsigned airi package
	 * 
	 * - process signing options
	 * - set airi input file
	 * - set air output file
	 * - create air package
	 * - process messages
	 * 
	 * @throws MojoFailureException
	 */
	public void sign(PackageOptions packageOptions, SigningOptions signingOptions) 
			throws MojoFailureException {
	
		logger.debug("Start signing ...");

		// create packager
		MessageCollector listener = new MessageCollector();
		AIRPackager packager = getAIRPackager(listener);
		
		// check if signingOptions available
		processSignOptions(packager, signingOptions);
		
		// validate airi file
		File airiFile = packageOptions.getInputFile();
		if (!airiFile.exists()) {
			throw new MojoFailureException("no such file " + airiFile.getPath());
		}
		packager.setIntermediate(airiFile);
		
		// set output file
		try {
			packager.setOutput(packageOptions.getOutputFile());
		} catch (IOException ex) {
			throw new MojoFailureException("unable to create temp file in output directory");
		}

		// create air package
		createAirPackage(packager);

		logger.debug("Done signing.");
		
		processMessages(listener);
	}

	/**
	 * @return new AIRPackager instance
	 */
	private AIRPackager getAIRPackager(Listener listener) {

		AIRPackager packager = new AIRPackager();
		if (null != listener) {
			packager.setListener(listener);
		}
		return packager;
	}

	/**
	 * Parse certificate options
	 * 
	 * @param cert
	 * @param creator
	 * @throws MojoFailureException
	 */
	private void parseCertificate(Certificate cert, CertificateCreator creator)
			throws MojoFailureException {

		// (common) name
		if (cert.hasCn()) {
			creator.setName(cert.getCn());
		}

		// org. unit
		if (cert.hasO()) {
			creator.setOrganizationalUnit(cert.getOu());
		}

		// organisation
		if (cert.hasO()) {
			creator.setOrganizationName(cert.getO());
		}

		// Country
		if (cert.hasC()) {
			creator.setCountry(cert.getC());
		}

		// keyType
		if (!cert.hasKeyType()) {
			throw new MojoFailureException(
					"No keyType specified. Valid types are 1024-RSA and 2048-RSA");
		} else {
			try {
				creator.setKeyType(cert.getKeyType());
			} catch (IllegalArgumentException e) {
				// if keyType is wrong
				throw new MojoFailureException("key type not recognized: "
						+ cert.getKeyType()
						+ " -- valid types are 1024-RSA and 2048-RSA");
			}
		}

		// output file
		if (!cert.hasPfxFile()) {
			throw new MojoFailureException(
					"No output file for certificate (pfx) specified.");
		} else {
			creator.setOutput(new File(cert.getPfxFile()));
		}

		// password
		if (!cert.hasPassword()) {
			throw new MojoFailureException("No certificate password specified.");
		} else {
			creator.setPassword(cert.getPassword());
		}
	}

	/**
	 * Setup package for either preparing or packaging
	 * 
	 * @param packager
	 *            AIRPackager to use
	 * @param packageOptions
	 *            PackageOptions
	 * @param extension
	 *            File extension to use for targat package
	 * @throws MojoFailureException
	 */
	private void parsePackageOrPrepare(AIRPackager packager,
			PackageOptions packageOptions, String extension)
			throws MojoFailureException {

		// if not to validate packager
		if (!packageOptions.isValidate()) {
			logger.debug("Disabling packager validation");
			try {
				// use reflexion to enable setting methods
				Method setValidate = packager.getClass().getDeclaredMethod(
						"setValidate", Boolean.TYPE);
				setValidate.setAccessible(true);
				setValidate.invoke(packager, false);
			} catch (NoSuchMethodException e) {
				logger.debug("Error invoking setValidate method", e);
				throw new MojoFailureException(e.getMessage());
			} catch (IllegalAccessException e) {
				logger.debug("Error invoking setValidate method", e);
				throw new MojoFailureException(e.getMessage());
			} catch (InvocationTargetException e) {
				logger.debug("Error invoking setValidate method", e);
				throw new MojoFailureException(e.getMessage());
			}
		}

		// if debugging
		if (packageOptions.isDebug()) {
			logger.debug("Enabling debugging");
			try {
				Method setDebug = packager.getClass().getDeclaredMethod(
						"setDebug", Boolean.TYPE);
				setDebug.setAccessible(true);
				setDebug.invoke(packager, true);
			} catch (NoSuchMethodException e) {
				logger.debug("Error invoking setDebug method", e);
				throw new MojoFailureException(e.getMessage());
			} catch (IllegalAccessException e) {
				logger.debug("Error invoking setDebug method", e);
				throw new MojoFailureException(e.getMessage());
			} catch (InvocationTargetException e) {
				logger.debug("Error invoking setDebug method", e);
				throw new MojoFailureException(e.getMessage());
			}
		}

		// set output
		try {
			packager.setOutput(packageOptions.getOutputFile());
		} catch (IOException ex) {
			throw new MojoFailureException(
					"unable to create temp file in output directory");
		}

		// set app descriptor
		packager.setDescriptor(packageOptions.getDescriptorFile());

		// parse file arguments
		parseFileArguments(packager, packageOptions);
	}

	/**
	 * Parse file arguments
	 * 
	 * @param packager
	 * @param packageOptions
	 *            PackageOptions containing file options
	 * @throws MojoFailureException
	 */
	protected void parseFileArguments(AIRPackager packager,
			PackageOptions packageOptions) throws MojoFailureException {
		if (packageOptions.getFilenames().size() == 0)
			throw new MojoFailureException("at least one file is required");

		logger.debug("Parsing file arguments ...");

		parseOptionalFileList(packager, packageOptions.getBaseDirectory(),
				packageOptions.getFilenames());
		parseIncludeFileList(packager, packageOptions.getIncludeFiles());

		logger.debug("Done parsing files.");
	}

	/**
	 * Parse include-file list
	 * 
	 * @param packager
	 * @param includeFiles
	 * @throws MojoFailureException
	 */
	private void parseIncludeFileList(AIRPackager packager,
			List<IncludeFile> includeFiles) throws MojoFailureException {

		// process all include files
		if (null != includeFiles) {
			for (IncludeFile includeFile : includeFiles) {
				// get base directory
				File cwd = new File(includeFile.getBaseDirectory());

				// process all files
				for (String fileName : includeFile.getFileNames()) {
					File file = new File(fileName);

					logger.debug("Adding include-file "
							+ file.getAbsolutePath() + " with root "
							+ cwd.getAbsolutePath());

					addFileOrDir(packager, cwd, file);
				}
			}
		}
	}

	/**
	 * Parse signing options
	 * 
	 * @param p
	 *            AIRPackager
	 * @param so
	 *            SigningOptions
	 * @throws MojoFailureException
	 */
	private void parseSigningOptions(AIRPackager packager, SigningOptions so)
			throws MojoFailureException {

		logger.debug("Parse signing options ...");
		logger.debug(so.toString());

		// get data from signing options
		String storeType = so.getStoretype();
		String keyStoreFile = so.getKeystore();
		String storePass = so.getStorepass();
		String keyPass = so.getKeypass();
		String providerName = so.getProviderName();
		String timestampURL = so.getTsaUrl();
		String alias = null;

		// validate storeType
		if (storeType == null) {
			throw new MojoFailureException("storetype is required");
		} else {
			logger.debug("storeType is: " + storeType);
		}
		// validate storePass
		if (storePass == null) {
			throw new MojoFailureException("storePass is required");
		} else {
			logger.debug("storePass is: " + storePass);
		}

		// get keystore
		KeyStore keyStore = null;
		try {
			keyStore = providerName != null ? KeyStore.getInstance(storeType,
					providerName) : KeyStore.getInstance(storeType);
			logger.debug("Keystore: " + keyStore.toString());

		} catch (KeyStoreException ex) {
			throw new MojoFailureException(
					"requested keystore type is not available");
		} catch (NoSuchProviderException ex) {
			throw new MojoFailureException(
					"requested provider is not available");
		}

		try {
			// load keystore
			try {
				keyStore
						.load(
								keyStoreFile != null ? ((java.io.InputStream) (new FileInputStream(
										keyStoreFile)))
										: null, storePass.toCharArray());

				logger.debug("Keystore loaded (" + keyStore.toString() + ")");
			} catch (IOException ex) {
				throw new MojoFailureException(
						"could not load keystore file (password may be incorrect)");
			} catch (CertificateException ex) {
				throw new MojoFailureException(
						"unable to load a certificate in this file");
			}

			// determine alias
			if (alias == null) {
				if (!keyStore.aliases().hasMoreElements()) {
					throw new MojoFailureException(
							"The key could not be obtained. You may need to use the keystore argument with the specified keystore type.");
				}
				alias = (String) keyStore.aliases().nextElement();

				logger.debug("Alias is: " + alias);
			}

			// get private key
			try {
				String pass = keyPass == null ? storePass : keyPass;
				PrivateKey key = (PrivateKey) keyStore.getKey(alias, pass
						.toCharArray());
				if (packager != null) {
					packager.setPrivateKey(key);
				}
				logger.debug("Private key set");
			} catch (UnrecoverableKeyException ex) {
				throw new MojoFailureException(
						"unable to retrieve key (password may be incorrect)");
			}

			// set signer certificate
			try {
				if (packager != null) {
					packager.setSignerCertificate(keyStore
							.getCertificate(alias));
				}
				if (packager != null) {
					packager.setCertificateChain(keyStore
							.getCertificateChain(alias));
				}
				logger.debug("Certificates set in private key");
			} catch (CertificateExpiredException ex) {
				throw new MojoFailureException("certificate has expired");
			} catch (CertificateNotYetValidException ex) {
				throw new MojoFailureException("certificate is not yet valid");
			} catch (CertificateException ex) {
				throw new MojoFailureException(
						"not an X509 code-signing certificate");
			}

			// set timestamp url
			// if (timestampURL != null) {
			String tsUrl = "none".equals(timestampURL) ? null : timestampURL;
			packager.setTimestampURL(tsUrl);
			logger.debug("Timestamp: " + tsUrl);
			// }
		} catch (KeyStoreException ex) {
			throw new RuntimeException(
					"aliases accessed before keystore was loaded");
		} catch (NoSuchAlgorithmException ex) {
			throw new MojoFailureException(
					"required crypto algorithm not available");
		}

		logger.debug("Done with signing options");
	}

	/**
	 * Parse optional file list. Adds all files to the packager
	 * 
	 * @param packager
	 * @param root
	 * @param fileNames
	 * @throws MojoFailureException
	 */
	private void parseOptionalFileList(AIRPackager packager, File root,
			List<String> fileNames) throws MojoFailureException {

		for (String fileName : fileNames) {
			File file = new File(fileName);

			logger.debug("Adding file " + file + " with root " + root);

			addFileOrDir(packager, root, new File(fileName));
		}

	}

	/**
	 * Add file or directory to the packager
	 * 
	 * @param packager
	 * @param cwd
	 * @param file
	 * @throws MojoFailureException
	 */
	protected void addFileOrDir(AIRPackager packager, File cwd, File file)
			throws MojoFailureException {

		if (!file.isAbsolute())
			file = new File(cwd, file.getPath());

		if (!file.exists())
			throw new MojoFailureException("no such file " + file.getPath());

		file = new File(file.toURI().normalize().getPath());

		if (file.isHidden())
			return;

		if (file.isFile()) {
			try {
				packager.addSourceWithRoot(file, cwd);
			} catch (IllegalArgumentException e) {
				throw new MojoFailureException(e.getLocalizedMessage() + ": "
						+ file.getPath());
			}
			return;
		}

		if (file.isDirectory()) {
			File dirContents[] = file.listFiles();

			for (int idx = 0; idx < dirContents.length; idx++)
				addFileOrDir(packager, cwd, dirContents[idx]);
		}

	}
}
