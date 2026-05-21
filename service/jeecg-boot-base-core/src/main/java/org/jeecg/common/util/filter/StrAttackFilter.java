package org.jeecg.common.util.filter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * File upload string filter special characters
 * @author: jeecg-boot
 */
public class StrAttackFilter {

    public static String filter(String str) throws PatternSyntaxException {
        // Remove all special characters
        String regEx = "[`_《》~!@#$%^&*()+=|{}':;',\\[\\].<>?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("").trim();
    }

//    public static void main(String[] args) {
//        String filter = filter("@#jeecg/《》【bo】￥%……&*（o）)))！@t<>,.,/?'\'~~`");
//        System.out.println(filter);
//    }
}
