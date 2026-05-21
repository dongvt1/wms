package org.jeecg.common.util.encryption;


import lombok.Data;

/**
 * @Description: EncryptedString
 * @author: jeecg-boot
 */
@Data
public class  EncryptedString {

    /**
     * The length is16characters
     */
    public static  String key = "1234567890adbcde";

    /**
     * The length is16characters
     */
    public static  String iv  = "1234567890hjlkew";
}
