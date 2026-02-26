package org.jeecg.common.util;

import java.security.Key;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

/**
 * @Description: Password tools
 * @author: jeecg-boot
 */
public class PasswordUtil {

	/**
	 * JAVA6Support any of the following algorithms PBEWITHMD5ANDDES PBEWITHMD5ANDTRIPLEDES
	 * PBEWITHSHAANDDESEDE PBEWITHSHA1ANDRC2_40 PBKDF2WITHHMACSHA1
	 * */

    /**
     * The algorithm used is defined as:PBEWITHMD5andDESalgorithm
     * 加密algorithm
     */
	public static final String ALGORITHM = "PBEWithMD5AndDES";

    /**
     * The algorithm used is defined as:PBEWITHMD5andDESalgorithm
     * key
     */
	public static final String SALT = "63293188";

	/**
	 * Define the number of iterations as1000Second-rate
	 */
	private static final int ITERATIONCOUNT = 1000;

	/**
	 * 获取加密algorithm中使用的salt value,The salt used in decryption must be the same as that used in encryption to complete the operation. The salt length must be8byte
	 * 
	 * @return byte[] salt value
	 * */
	public static byte[] getSalt() throws Exception {
		// Instantiating a secure random number
		SecureRandom random = new SecureRandom();
		// produce salt
		return random.generateSeed(8);
	}

	public static byte[] getStaticSalt() {
		// produce salt
		return SALT.getBytes();
	}

	/**
	 * according toPBE密码生成一把key
	 * 
	 * @param password
	 *            生成key时所使用的密码
	 * @return Key PBEalgorithmkey
	 * */
	private static Key getPbeKey(String password) {
		// 实例化使用的algorithm
		SecretKeyFactory keyFactory;
		SecretKey secretKey = null;
		try {
			keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
			// set upPBEkey参数
			PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray());
			// 生成key
			secretKey = keyFactory.generateSecret(keySpec);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return secretKey;
	}

	/**
	 * Encrypt plaintext string
	 * 
	 * @param plaintext
	 *            Plain text string to be encrypted
	 * @param password
	 *            生成key时所使用的密码
	 * @param salt
	 *            salt value
	 * @return Encrypted ciphertext string
	 * @throws Exception
	 */
	public static String encrypt(String plaintext, String password, String salt) {

		Key key = getPbeKey(password);
		byte[] encipheredData = null;
		PBEParameterSpec parameterSpec = new PBEParameterSpec(salt.getBytes(), ITERATIONCOUNT);
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM);

			cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);
			//update-begin-author:sccott date:20180815 for:When Chinese is used as the username，encrypted passwordwindowsandlinuxwill get different results gitee/issues/IZUD7
			encipheredData = cipher.doFinal(plaintext.getBytes("utf-8"));
			//update-end-author:sccott date:20180815 for:When Chinese is used as the username，encrypted passwordwindowsandlinuxwill get different results gitee/issues/IZUD7
		} catch (Exception e) {
		}
		return bytesToHexString(encipheredData);
	}

	/**
	 * Decrypt ciphertext string
	 * 
	 * @param ciphertext
	 *            Ciphertext string to be decrypted
	 * @param password
	 *            生成key时所使用的密码(If you need to decrypt,This parameter needs to be consistent with the one used for encryption)
	 * @param salt
	 *            salt value(If you need to decrypt,This parameter needs to be consistent with the one used for encryption)
	 * @return Decrypted plaintext string
	 * @throws Exception
	 */
	public static String decrypt(String ciphertext, String password, String salt) {

		Key key = getPbeKey(password);
		byte[] passDec = null;
		PBEParameterSpec parameterSpec = new PBEParameterSpec(salt.getBytes(), ITERATIONCOUNT);
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM);

			cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

			passDec = cipher.doFinal(hexStringToBytes(ciphertext));
		}

		catch (Exception e) {
			// TODO: handle exception
		}
		return new String(passDec);
	}

	/**
	 * 将byte数组转换为hexadecimal string
	 * 
	 * @param src
	 *            byte数组
	 * @return
	 */
	public static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	/**
	 * 将hexadecimal string转换为byte数组
	 * 
	 * @param hexString
	 *            hexadecimal string
	 * @return
	 */
	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || "".equals(hexString)) {
			return null;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}


}