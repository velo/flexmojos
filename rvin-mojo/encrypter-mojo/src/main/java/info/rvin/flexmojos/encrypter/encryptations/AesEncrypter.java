package info.rvin.flexmojos.encrypter.encryptations;

//other imports
import java.io.File;
import java.io.IOException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

public class AesEncrypter {

	boolean debug = false;

	private byte[] key;
	private byte[] iv;
	private int encryptionMode;
	private String paddingMode;

	private Log log;

	static final int CBC_MODE = 0;
	static final int ECB_MODE = 1;

	static final String NO_PADDING = "NoPadding";
	static final String ZERO_PADDING = "ZeroPadding";
	static final String PKCS7_PADDING = "PKCS5Padding";

	public AesEncrypter(Log log) {
		this.log = log;
	}

	protected byte[] encrypt(byte[] content) throws MojoExecutionException {

		byte[] cipherText = null;
		try {

			IvParameterSpec ivSpec = new IvParameterSpec(iv);
			SecretKey secretKey = new SecretKeySpec(key, "AES");
			Cipher aes = null;
			if (encryptionMode == ECB_MODE) {
				log.info("Cipher mode: " + "AES/ECB/" + paddingMode);
				aes = Cipher.getInstance("AES/ECB/" + paddingMode);
				aes.init(Cipher.ENCRYPT_MODE, secretKey);
			} else {
				log.info("Cipher mode: " + "AES/CBC/" + paddingMode);
				aes = Cipher.getInstance("AES/CBC/" + paddingMode);
				aes.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
			}
			cipherText = aes.doFinal(content);

		} catch (Exception e) {
			throw new MojoExecutionException("Error in encryption:", e);
		}
		return cipherText;

	}

	public byte[] hex2Bytes(String hex) {
		int len = hex.length();
		if (len % 2 == 1)
			return null;

		log.info("Bytes:" + len);
		byte[] b = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			b[i >> 1] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
		}

		return b;
	}

	public void encrypt(String key, String iv, File swf, File eswf)
			throws IOException, MojoExecutionException {
		this.key = hex2Bytes(key);
		this.iv = hex2Bytes(iv);
		this.encryptionMode = CBC_MODE;
		this.paddingMode = PKCS7_PADDING;
		byte[] encrypted = this.encrypt(FileUtils.readFileToByteArray(swf));
		FileUtils.writeByteArrayToFile(eswf, encrypted);
	}

}
