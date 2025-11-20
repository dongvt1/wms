package org.jeecg.common.util.encryption;

import org.apache.shiro.lang.codec.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @Description: AES encryption
 * @author: jeecg-boot
 * @date: 2022/3/30 11:48
 */
public class AesEncryptUtil {

    /**
     * useAES-128-CBCencryptionmodel keyandivCan be the same
     */
    private static String KEY = EncryptedString.key;
    private static String IV = EncryptedString.iv;

    /**
     * encryption方法
     * @param data  要encryption的data
     * @param key encryptionkey
     * @param iv encryptioniv
     * @return encryption的结果
     * @throws Exception
     */
    public static String encrypt(String data, String key, String iv) throws Exception {
        try {

            //"algorithm/model/two's complement method"NoPadding PkcsPadding
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            int blockSize = cipher.getBlockSize();

            byte[] dataBytes = data.getBytes();
            int plaintextLength = dataBytes.length;
            if (plaintextLength % blockSize != 0) {
                plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
            }

            byte[] plaintext = new byte[plaintextLength];
            System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);

            SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());

            cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
            byte[] encrypted = cipher.doFinal(plaintext);

            return Base64.encodeToString(encrypted);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Decryption method
     * @param data Data to be decrypted
     * @param key  Decryptkey
     * @param iv Decryptiv
     * @return Decrypt的结果
     * @throws Exception
     */
    public static String desEncrypt(String data, String key, String iv) throws Exception {
        //update-begin-author:taoyan date:2022-5-23 for:VUEN-1084 【vue3】onlineNew issues discovered by form testing 6、Decrypt报错 ---If decoding fails, an exception should be thrown.，Process outside
        byte[] encrypted1 = Base64.decode(data);

        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
        IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());

        cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);

        byte[] original = cipher.doFinal(encrypted1);
        String originalString = new String(original);
        //encryption解码后的字符串会出现\u0000
        return originalString.replaceAll("\\u0000", "");
        //update-end-author:taoyan date:2022-5-23 for:VUEN-1084 【vue3】onlineNew issues discovered by form testing 6、Decrypt报错 ---If decoding fails, an exception should be thrown.，Process outside
    }

    /**
     * use默认的keyandivencryption
     * @param data
     * @return
     * @throws Exception
     */
    public static String encrypt(String data) throws Exception {
        return encrypt(data, KEY, IV);
    }

    /**
     * use默认的keyandivDecrypt
     * @param data
     * @return
     * @throws Exception
     */
    public static String desEncrypt(String data) throws Exception {
        return desEncrypt(data, KEY, IV);
    }



//    /**
//     * test
//     */
//    public static void main(String args[]) throws Exception {
//        String test1 = "sa";
//        String test =new String(test1.getBytes(),"UTF-8");
//        String data = null;
//        String key =  KEY;
//        String iv = IV;
//        // /g2wzfqvMOeazgtsUVbq1kmJawROa6mcRAzwG1/GeJ4=
//        data = encrypt(test, key, iv);
//        System.out.println("data："+test);
//        System.out.println("encryption："+data);
//        String jiemi =desEncrypt(data, key, iv).trim();
//        System.out.println("Decrypt："+jiemi);
//    }

}
