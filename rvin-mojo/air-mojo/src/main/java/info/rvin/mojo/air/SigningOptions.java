/**
 * 
 */
package info.rvin.mojo.air;


/**
 * Class to provide signing options for the ADT tool.
 * 
 * @author Joost den Boer
 *
 */
public class SigningOptions {
	
	private static String SEPERATOR = ", ";

	// store type
	private String storetype = null;

	// store password
	private String storepass = null;
	
	// keystore
	private String keystore = null;

	// keystore password
	private String keypass = null;
	
	// provider name
	private String providerName = null;
	
	// tsa url
	private String tsaUrl = null;
	
	/**
	 * Default empty constructor
	 *
	 */
	public SigningOptions() {
		super();
	}

	/**
	 * @return the keypass
	 */
	public String getKeypass() {
		return keypass;
	}

	/**
	 * @param keypass the keypass to set
	 */
	public void setKeypass(String keypass) {
		this.keypass = keypass;
	}

	/**
	 * @return the keystore
	 */
	public String getKeystore() {
		return keystore;
	}

	/**
	 * @param keystore the keystore to set
	 */
	public void setKeystore(String keystore) {
		this.keystore = keystore;
	}

	/**
	 * @return the providerName
	 */
	public String getProviderName() {
		return providerName;
	}

	/**
	 * @param providerName the providerName to set
	 */
	public void setProviderName(String providerName) {
		this.providerName = providerName;
	}

	/**
	 * @return the storepass
	 */
	public String getStorepass() {
		return storepass;
	}

	/**
	 * @param storepass the storepass to set
	 */
	public void setStorepass(String storepass) {
		this.storepass = storepass;
	}

	/**
	 * @return the storetype
	 */
	public String getStoretype() {
		return storetype;
	}

	/**
	 * @param storetype the storetype to set
	 */
	public void setStoretype(String storetype) {
		this.storetype = storetype;
	}

	/**
	 * @return the tsaUrl
	 */
	public String getTsaUrl() {
		return tsaUrl;
	}

	/**
	 * @param tsaUrl the tsaUrl to set
	 */
	public void setTsaUrl(String tsaUrl) {
		this.tsaUrl = tsaUrl;
	}
	
	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();
		if(storetype != null) {
			sb.append("storetype=").append(storetype).append(SEPERATOR);
		}
		if(keystore != null) {
			sb.append("keystore=").append(keystore).append(SEPERATOR);
		}
		if(storepass != null) {
			sb.append("storepass=").append(storepass).append(SEPERATOR);
		}
		if(keypass != null) {
			sb.append("keypass=").append(keypass).append(SEPERATOR);
		}
		if(providerName != null) {
			sb.append("providerName=").append(providerName).append(SEPERATOR);
		}
		if(tsaUrl != null) {
			sb.append("tsaUrl=").append(tsaUrl).append(SEPERATOR);
		}
		return sb.toString();
	}
	
}
