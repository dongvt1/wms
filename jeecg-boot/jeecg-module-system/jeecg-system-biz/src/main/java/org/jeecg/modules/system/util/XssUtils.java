package org.jeecg.modules.system.util;

import org.springframework.web.util.HtmlUtils;

import java.util.regex.Pattern;

/**
 * @Description: ToolsXSSUtils，The current approach is to replace it with a null character，CSDNis to escape，For example, the text begins with"<"Convert to&lt;
 * @author: lsq
 * @date: 2021Year07moon26day 19:13
 */
public class XssUtils {

    private static Pattern[] patterns = new Pattern[]{
        //Script fragments
        Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE),
        //src='...'
        Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        Pattern.compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        //script tags
        Pattern.compile("</script>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        //eval(...)
        Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        //expression(...)
        Pattern.compile("e­xpression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        //javascript:...
        Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
        //vbscript:...
        Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
        //onload(...)=...
        Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
    };

    public static String scriptXss(String value) {
        if (value != null) {
            value = value.replaceAll(" ", "");
            for(Pattern scriptPattern: patterns){
                value = scriptPattern.matcher(value).replaceAll("");
            }
        }
        return HtmlUtils.htmlEscape(value);
    }

    public static void main(String[] args) {
        String s = scriptXss("<img  src=x onload=alert(111).*?><script></script>javascript:eval()\\\\.");
        System.err.println("s======>" + s);
    }
}
