package org.jeecg.common.desensitization.util;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.desensitization.annotation.SensitiveField;
import org.jeecg.common.desensitization.enums.SensitiveEnum;
import org.jeecg.common.util.encryption.AesEncryptUtil;
import org.jeecg.common.util.oConvertUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.List;

/**
 * Sensitive information processing tools
 * @author taoYan
 * @date 2022/4/20 18:01
 **/
@Slf4j
public class SensitiveInfoUtil {

    /**
     * Handle nested objects
     * @param obj method return value
     * @param entity entityclass
     * @param isEncode Whether to encrypt（true: cryptographic operations / false:Decryption operation）
     * @throws IllegalAccessException
     */
    public static void handleNestedObject(Object obj, Class entity, boolean isEncode) throws IllegalAccessException {
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            if(field.getType().isPrimitive()){
                continue;
            }
            if(field.getType().equals(entity)){
                // Inside the object isentity
                field.setAccessible(true);
                Object nestedObject = field.get(obj);
                handlerObject(nestedObject, isEncode);
                break;
            }else{
                // Inside the object isList<entity>
                if(field.getGenericType() instanceof ParameterizedType){
                    ParameterizedType pt = (ParameterizedType)field.getGenericType();
                    if(pt.getRawType().equals(List.class)){
                        if(pt.getActualTypeArguments()[0].equals(entity)){
                            field.setAccessible(true);
                            Object nestedObject = field.get(obj);
                            handleList(nestedObject, entity, isEncode);
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * deal withObject
     * @param obj method return value
     * @param isEncode Whether to encrypt（true: cryptographic operations / false:Decryption operation）
     * @return
     * @throws IllegalAccessException
     */
    public static Object handlerObject(Object obj, boolean isEncode) throws IllegalAccessException {
        if (oConvertUtils.isEmpty(obj)) {
            return obj;
        }
        long startTime=System.currentTimeMillis();
        log.debug(" obj --> "+ obj.toString());
        
        // Determine whether it is an object
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            boolean isSensitiveField = field.isAnnotationPresent(SensitiveField.class);
            if(isSensitiveField){
                // Must haveSensitiveFieldannotation 才作deal with
                if(field.getType().isAssignableFrom(String.class)){
                    //Must be of type string 才作deal with
                    field.setAccessible(true);
                    String realValue = (String) field.get(obj);
                    if(realValue==null || "".equals(realValue)){
                        continue;
                    }
                    SensitiveField sf = field.getAnnotation(SensitiveField.class);
                    if(isEncode==true){
                        //encryption
                        String value = SensitiveInfoUtil.getEncodeData(realValue,  sf.type());
                        field.set(obj, value);
                    }else{
                        //解密只deal with encodetype
                        if(sf.type().equals(SensitiveEnum.ENCODE)){
                            String value = SensitiveInfoUtil.getDecodeData(realValue);
                            field.set(obj, value);
                        }
                    }
                }
            }
        }
        //long endTime=System.currentTimeMillis();
        //log.info((isEncode ? "cryptographic operations，" : "Decryption operation，") + "Current program takes time：" + (endTime - startTime) + "ms");
        return obj;
    }

    /**
     * deal with List<entity>
     * @param obj
     * @param entity
     * @param isEncode（true: cryptographic operations / false:Decryption operation）
     */
    public static void handleList(Object obj, Class entity, boolean isEncode){
        List list = (List)obj;
        if(list.size()>0){
            Object first = list.get(0);
            if(first.getClass().equals(entity)){
                for(int i=0; i<list.size(); i++){
                    Object temp = list.get(i);
                    try {
                        handlerObject(temp, isEncode);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    /**
     * deal with数据 Get decrypted data
     * @param data
     * @return
     */
    public static String getDecodeData(String data){
        String result = null;
        try {
            result = AesEncryptUtil.desEncrypt(data);
        } catch (Exception exception) {
            log.debug("Data decryption error，original data:"+data);
        }
        //solvedebugmode，Encryption and decryption failure causes Chinese characters to be decrypted and become empty.
        if(oConvertUtils.isEmpty(result) && oConvertUtils.isNotEmpty(data)){
            result = data;
        }
        return result;
    }

    /**
     * deal with数据 获取encryption后的数据 Or formatted data
     * @param data string
     * @param sensitiveEnum type
     * @return deal with后的string
     */
    public static String getEncodeData(String data, SensitiveEnum sensitiveEnum){
        String result;
        switch (sensitiveEnum){
            case ENCODE:
                try {
                    result = AesEncryptUtil.encrypt(data);
                } catch (Exception exception) {
                    log.error("数据encryption错误", exception.getMessage());
                    result = data;
                }
                break;
            case CHINESE_NAME:
                result = chineseName(data);
                break;
            case ID_CARD:
                result = idCardNum(data);
                break;
            case FIXED_PHONE:
                result = fixedPhone(data);
                break;
            case MOBILE_PHONE:
                result = mobilePhone(data);
                break;
            case ADDRESS:
                result = address(data, 3);
                break;
            case EMAIL:
                result = email(data);
                break;
            case BANK_CARD:
                result = bankCard(data);
                break;
            case CNAPS_CODE:
                result = cnapsCode(data);
                break;
            default:
                result = data;
        }
        return result;
    }


    /**
     * [Chinese name] Only display the first Chinese character，Others hidden as2asterisks
     * @param fullName full name
     * @return <example：plum**>
     */
    public static String chineseName(String fullName) {
        if (oConvertUtils.isEmpty(fullName)) {
            return "";
        }
        return formatRight(fullName, 1);
    }

    /**
     * [Chinese name] Only display the first Chinese character，Others hidden as2asterisks
     * @param familyName surname
     * @param firstName name
     * @return <example：plum**>
     */
    public static String chineseName(String familyName, String firstName) {
        if (oConvertUtils.isEmpty(familyName) || oConvertUtils.isEmpty(firstName)) {
            return "";
        }
        return chineseName(familyName + firstName);
    }

    /**
     * [ID number] Show last four digits，Other hidden。total18Bit or15Bit。
     * @param id ID number
     * @return <example：*************5762>
     */
    public static String idCardNum(String id) {
        if (oConvertUtils.isEmpty(id)) {
            return "";
        }
        return formatLeft(id, 4);

    }

    /**
     * [Landline] 后四Bit，Other hidden
     * @param num Landline
     * @return <example：****1234>
     */
    public static String fixedPhone(String num) {
        if (oConvertUtils.isEmpty(num)) {
            return "";
        }
        return formatLeft(num, 4);
    }

    /**
     * [phone number] 前三Bit，后四Bit，Other hidden
     * @param num phone number
     * @return <example:138******1234>
     */
    public static String mobilePhone(String num) {
        if (oConvertUtils.isEmpty(num)) {
            return "";
        }
        int len = num.length();
        if(len<11){
            return num;
        }
        return formatBetween(num, 3, 4);
    }

    /**
     * [address] Show only to regions，不显示详细address；We need to enhance protection of personal information
     * @param address address
     * @param sensitiveSize Sensitive information length
     * @return <example：Haidian District, Beijing****>
     */
    public static String address(String address, int sensitiveSize) {
        if (oConvertUtils.isEmpty(address)) {
            return "";
        }
        int len = address.length();
        if(len<sensitiveSize){
            return address;
        }
        return formatRight(address, sensitiveSize);
    }

    /**
     * [Email] Email prefix only shows first letter，前缀Other hidden，Replace with asterisk，@及后面的address显示
     * @param email Email
     * @return <example:g**@163.com>
     */
    public static String email(String email) {
        if (oConvertUtils.isEmpty(email)) {
            return "";
        }
        int index = email.indexOf("@");
        if (index <= 1){
            return email;
        }
        String begin = email.substring(0, 1);
        String end = email.substring(index);
        String stars = "**";
        return begin + stars + end;
    }

    /**
     * [Bank card number] 前六Bit，后四Bit，Others are hidden with asterisks每Bit1asterisks
     * @param cardNum Bank card number
     * @return <example:6222600**********1234>
     */
    public static String bankCard(String cardNum) {
        if (oConvertUtils.isEmpty(cardNum)) {
            return "";
        }
        return formatBetween(cardNum, 6, 4);
    }

    /**
     * [Company account opening bank number] Company bank account number,显示前两Bit，Others are hidden with asterisks，每Bit1asterisks
     * @param code Company account opening bank number
     * @return <example:12********>
     */
    public static String cnapsCode(String code) {
        if (oConvertUtils.isEmpty(code)) {
            return "";
        }
        return formatRight(code, 2);
    }


    /**
     * Format the one on the right into*
     * @param str string
     * @param reservedLength Reserved length
     * @return 格式化后的string
     */
    public static String formatRight(String str, int reservedLength){
        String name = str.substring(0, reservedLength);
        String stars = String.join("", Collections.nCopies(str.length()-reservedLength, "*"));
        return name + stars;
    }

    /**
     * Format the left one as*
     * @param str string
     * @param reservedLength Reserved length
     * @return 格式化后的string
     */
    public static String formatLeft(String str, int reservedLength){
        int len = str.length();
        String show = str.substring(len-reservedLength);
        String stars = String.join("", Collections.nCopies(len-reservedLength, "*"));
        return stars + show;
    }

    /**
     * Format the middle one into*
     * @param str string
     * @param beginLen 开始Reserved length
     * @param endLen 结尾Reserved length
     * @return 格式化后的string
     */
    public static String formatBetween(String str, int beginLen, int endLen){
        int len = str.length();
        String begin = str.substring(0, beginLen);
        String end = str.substring(len-endLen);
        String stars = String.join("", Collections.nCopies(len-beginLen-endLen, "*"));
        return begin + stars + end;
    }

}
