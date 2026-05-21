package org.jeecg.modules.system.util;


import cn.hutool.core.util.CharsetUtil;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import cn.hutool.crypto.symmetric.SymmetricCrypto;

/**
 * @Description: Password encryption and decryption
 * @author: lsq
 * @date: 2020Year09moon07day 14:26
 */
public class SecurityUtil {
    /**encryptionkey*/
    private static String key = "JEECGBOOT1423670";

    //---AESencryption---------begin---------
    /**encryption
     * @param content
     * @return
     */
    public static String jiami(String content) {
            SymmetricCrypto aes = new SymmetricCrypto(SymmetricAlgorithm.AES, key.getBytes());
            String encryptResultStr = aes.encryptHex(content);
            return encryptResultStr;
    }

    /**Decrypt
     * @param encryptResultStr
     * @return
     */
    public static String jiemi(String encryptResultStr){
        SymmetricCrypto aes = new SymmetricCrypto(SymmetricAlgorithm.AES, key.getBytes());
        //Decrypt为字符串
        String decryptResult = aes.decryptStr(encryptResultStr, CharsetUtil.CHARSET_UTF_8);
        return  decryptResult;
    }
    //---AESencryption---------end---------
    /**
     * main function
     */
    public static void main(String[] args) {
        String content="test1111";
        String encrypt = jiami(content);
        System.out.println(encrypt);
        //build
        String decrypt = jiemi(encrypt);
        //Decrypt为字符串
        System.out.println(decrypt);
    }
}
