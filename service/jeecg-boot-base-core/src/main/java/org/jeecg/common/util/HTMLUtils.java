package org.jeecg.common.util;

import org.apache.commons.lang3.StringUtils;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.web.util.HtmlUtils;

/**
 * HTML Tools
 * @author: jeecg-boot
 * @date: 2022/3/30 14:43
 */
@SuppressWarnings("AlibabaClassNamingShouldBeCamel")
public class HTMLUtils {

    /**
     * GetHTMLtext within，Does not contain tags
     *
     * @param html HTML code
     */
    public static String getInnerText(String html) {
        if (StringUtils.isNotBlank(html)) {
            //remove html tags
            String content = html.replaceAll("</?[^>]+>", "");
            // Combine multiple spaces into one space
            content = content.replaceAll("(&nbsp;)+", "&nbsp;");
            // reverse escape character
            content = HtmlUtils.htmlUnescape(content);
            return content.trim();
        }
        return "";
    }

    /**
     * WillMarkdownparsed intoHtml
     * @param markdownContent
     * @return
     */
    public static String parseMarkdown(String markdownContent) {
        //update-begin---author:wangshuai---date:2024-06-26---for:【TV360X-1344】JDK17 Email sending failed，Need to change the spelling---
        /*PegDownProcessor pdp = new PegDownProcessor();
        return pdp.markdownToHtml(markdownContent);*/
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdownContent);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        return renderer.render(document);
        //update-end---author:wangshuai---date:2024-06-26---for:【TV360X-1344】JDK17 Email sending failed，Need to change the spelling---
    }

}
