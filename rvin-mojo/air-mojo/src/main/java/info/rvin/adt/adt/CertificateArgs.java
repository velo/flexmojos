/**
 * 
 */
package info.rvin.adt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import com.adobe.argv.UsageError;

/**
 * Class to hold all certificate options.
 * At least the common name (cn) attribute is required.
 * 
 * @author Joost den Boer
 *
 */
public class CertificateArgs {

	private static final String CommonName = "cn";
	private static final String OrganisationUnit = "ou";
	private static final String Organisation  = "o";
	private static final String Country = "c";
	
	private Properties certArgs = new Properties();
	
	/**
	 * Create a new CertificateArgs instance and set commonName
	 * attribute
	 * 
	 * @param cn Common name value of certificate arguments
	 * @throws UsageError thrown if the provided commonName value is invalid
	 */
	public CertificateArgs(String cn) throws UsageError {
		super();
		if(cn == null || "".equals(cn)) {
			throw new UsageError("CN value is not valid");
		}
		// set common name
		setCN(cn);
	}

	/**
	 * Construct a new CertificateArgs instance with all possible
	 * arguments
	 * 
	 * @param cn Common name
	 * @param ou Organisation unit
	 * @param o Organisation
	 * @param c Country
	 * @throws UsageError thrown if the provided commonName value is invalid
	 */
	public CertificateArgs(String cn, String ou, String o, String c)
			throws UsageError {
		
		this(cn);
		// set arguments
		setOU(ou);
		setO(o);
		setC(c);
	}
	
	/**
	 * Set common name
	 * @param cn Common Name
	 */
	private void setCN(String cn) {
		certArgs.setProperty(CommonName, cn);
	}

	/**
	 * Return common name (cn)
	 * @return cn
	 */
	public String getCN() {
		return certArgs.getProperty(CommonName);
	}

	/**
	 * Set organisational unit
	 * @param ou Organisational unit
	 */
	private void setOU(String ou) {
		certArgs.setProperty(OrganisationUnit, ou);
	}

	/**
	 * Return organisational unit
	 * @return ou
	 */
	public String getOU() {
		return certArgs.getProperty(OrganisationUnit);
	}

	/**
	 * Set organisation
	 * @param o organisation
	 */
	private void setO(String o) {
		certArgs.setProperty(Organisation, o);
	}

	/**
	 * Return organisation (o)
	 * @return o
	 */
	public String getO() {
		return certArgs.getProperty(Organisation);
	}

	/**
	 * Set country
	 * @param c Country
	 */
	private void setC(String c) {
		certArgs.setProperty(Country, c);
	}

	/**
	 * Return country c
	 * @return c
	 */
	public String getC() {
		return certArgs.getProperty(Country);
	}

	/**
	 * Returns a list of all certificate arguments
	 * @return List of all certificate arguments
	 * @throws UsageError thrown if cn value is invalid
	 */
	public Collection<String> getCertificateOptions() throws UsageError {

		List<String> args = new ArrayList<String>();
		
		// CN
		String cn = getCN();
		if(isValid(cn)) {
			args.add("-cn");
			args.add(cn);
		} else {
			throw new UsageError("CN value is not valid");
		}
		
		// OU
		String ou = getOU();
		if(isValid(ou)) {
			args.add("-ou");
			args.add(ou);
		}

		// O
		String o = getO();
		if(isValid(o)) {
			args.add("-o");
			args.add(o);
		}

		// C
		String c = getC();
		if(isValid(c)) {
			args.add("-c");
			args.add(c);
		}
		
		// return args
		return args;
	}
	
	/**
	 * Checks if given string value is valid.
	 * A value is valid if it's not-null and not "".
	 * 
	 * @param value String value to validate
	 * @return <code>true</code> if value is valid. Otherwise returns <code>false</code>.
	 */
	private boolean isValid(String value) {
		
		return value != null && !"".equals(value);
	}
}
