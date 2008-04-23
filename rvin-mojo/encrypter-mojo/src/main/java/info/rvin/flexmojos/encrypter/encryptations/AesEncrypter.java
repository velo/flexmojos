package info.rvin.flexmojos.encrypter.encryptations;

//other imports
import java.io.File;
import java.io.IOException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.FileUtils;

public class AesEncrypter {

	boolean debug = false;

	private byte[] key;
	private byte[] iv;
	private int encryptionMode;
	private String paddingMode;

	static final int CBC_MODE = 0;
	static final int ECB_MODE = 1;

	static final String NO_PADDING = "NoPadding";
	static final String ZERO_PADDING = "ZeroPadding";
	static final String PKCS7_PADDING = "PKCS5Padding";

	protected byte[] encrypt(byte[] content) {

		byte[] cipherText = null;
		try {

			IvParameterSpec ivSpec = new IvParameterSpec(iv);
			SecretKey secretKey = new SecretKeySpec(key, "AES");
			Cipher aes = null;
			if (encryptionMode == ECB_MODE) {
				log("Cipher mode: " + "AES/ECB/" + paddingMode);
				aes = Cipher.getInstance("AES/ECB/" + paddingMode);
				aes.init(Cipher.ENCRYPT_MODE, secretKey);
			} else {
				log("Cipher mode: " + "AES/CBC/" + paddingMode);
				aes = Cipher.getInstance("AES/CBC/" + paddingMode);
				aes.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
			}
			cipherText = aes.doFinal(content);

		} catch (Exception e) {
			log("Error in encryption:", e);
		}
		return cipherText;

	}

	protected byte[] decrypt(byte[] content) {

		byte[] plainText = null;
		try {

			IvParameterSpec ivSpec = new IvParameterSpec(iv);
			SecretKey secretKey = new SecretKeySpec(key, "AES");
			Cipher aes = null;
			if (encryptionMode == ECB_MODE) {
				aes = Cipher.getInstance("AES/ECB/" + paddingMode);
				aes.init(Cipher.DECRYPT_MODE, secretKey);
			} else {
				aes = Cipher.getInstance("AES/CBC/" + paddingMode);
				aes.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
			}
			plainText = aes.doFinal(content);

		} catch (Exception e) {
			log("Error in decryption:", e);
		}
		return plainText;
	}

	private static void log(String string, Object... o) {
		// TODO Auto-generated method stub

	}

	public static String bytes2Hex(byte[] bytes) {

		if (bytes == null)
			return null;

		StringBuffer b = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			b.append(String.format("%1$02x", bytes[i]));
		}
		return b.toString();
	}

	public static byte[] hex2Bytes(String hex) {
		int len = hex.length();
		if (len % 2 == 1)
			return null;

		log("Bytes:" + len);
		byte[] b = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			b[i >> 1] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
		}

		return b;
	}

	public static void encrypt(String key, String iv, File swf, File eswf)
			throws IOException {
		AesEncrypter aes = new AesEncrypter();
		aes.key = AesEncrypter.hex2Bytes(key);
		aes.iv = AesEncrypter.hex2Bytes(iv);
		aes.encryptionMode = CBC_MODE;
		aes.paddingMode = PKCS7_PADDING;
		byte[] encrypted = aes.encrypt(FileUtils.readFileToByteArray(swf));
		FileUtils.writeByteArrayToFile(eswf, encrypted);
	}

}
