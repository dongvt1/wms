package org.jeecg.common.util.dynamic.db;

import freemarker.cache.StringTemplateLoader;
import freemarker.core.ParseException;
import freemarker.core.TemplateClassResolver;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.constant.DataBaseConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecgframework.codegenerate.generate.util.SimpleFormat;

import java.io.StringWriter;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Zhao Junfu
 * @version V1.0
 * @Title:FreemarkerHelper
 * @description:FreemarkerEngine assistance class
 * @date Jul 5, 2013 2:58:29 PM
 */
@Slf4j
public class FreemarkerParseFactory {

    private static final String ENCODE = "utf-8";
    /**
     * Parameter formatting tool class
     */
    private static final String MINI_DAO_FORMAT = "DaoFormat";

    /**
     * File cache
     */
    private static final Configuration TPL_CONFIG = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
    /**
     * SQL cache
     */
    private static final Configuration SQL_CONFIG = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);

    private static StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();

    /**Use the embedded(?ms)Turn on single-line and multi-line modes*/
    private final static Pattern NOTES_PATTERN = Pattern
            .compile("(?ms)/\\*.*?\\*/|^\\s*//.*?$");

    static {
        TPL_CONFIG.setClassForTemplateLoading(new FreemarkerParseFactory().getClass(), "/");
        TPL_CONFIG.setNumberFormat("0.#####################");
        SQL_CONFIG.setTemplateLoader(stringTemplateLoader);
        SQL_CONFIG.setNumberFormat("0.#####################");
        //classic_compatibleset up，Solve the null pointer error
        SQL_CONFIG.setClassicCompatible(true);

        //update-begin-author:taoyan date:2022-8-10 for: freemarkerTemplate injection problem Disable parsingObjectConstructor，Executeandfreemarker.template.utility.JythonRuntime。
        //https://ackcent.com/in-depth-freemarker-template-injection/
        TPL_CONFIG.setNewBuiltinClassResolver(TemplateClassResolver.SAFER_RESOLVER);
        SQL_CONFIG.setNewBuiltinClassResolver(TemplateClassResolver.SAFER_RESOLVER);
        //update-end-author:taoyan date:2022-8-10 for: freemarkerTemplate injection problem Disable parsingObjectConstructor，Executeandfreemarker.template.utility.JythonRuntime。
    }

    /**
     * Determine whether the template exists
     *
     * @throws Exception
     */
    public static boolean isExistTemplate(String tplName) throws Exception {
        try {
            Template mytpl = TPL_CONFIG.getTemplate(tplName, "UTF-8");
            if (mytpl == null) {
                return false;
            }
        } catch (Exception e) {
            //update-begin--Author:scott  Date:20180320 for：solve problems - Error messagesqlFile does not exist，The actual problem issql freemarkerUsage error-----
            if (e instanceof ParseException) {
                log.error(e.getMessage(), e.fillInStackTrace());
                throw new Exception(e);
            }
            log.debug("----isExistTemplate----" + e.toString());
            //update-end--Author:scott  Date:20180320 for：solve problems - Error messagesqlFile does not exist，The actual problem issql freemarkerUsage error------
            return false;
        }
        return true;
    }

    /**
     * parseftltemplate
     *
     * @param tplName template名
     * @param paras   parameter
     * @return
     */
    public static String parseTemplate(String tplName, Map<String, Object> paras) {
        try {
            log.debug(" minidao sql templdate : " + tplName);
            StringWriter swriter = new StringWriter();
            Template mytpl = TPL_CONFIG.getTemplate(tplName, ENCODE);
            if (paras.containsKey(MINI_DAO_FORMAT)) {
                throw new RuntimeException("DaoFormat yes minidao reserved keywords，Not allowed ，请更改parameter定义！");
            }
            paras.put(MINI_DAO_FORMAT, new SimpleFormat());
            mytpl.process(paras, swriter);
            String sql = getSqlText(swriter.toString());
            paras.remove(MINI_DAO_FORMAT);
            return sql;
        } catch (Exception e) {
            log.error(e.getMessage(), e.fillInStackTrace());
            log.error("发送一次的templatekey:{ " + tplName + " }");
            //System.err.println(e.getMessage());
            //System.err.println("template名:{ "+ tplName +" }");
            throw new RuntimeException("parseSQLtemplate异常");
        }
    }

    /**
     * parseftl
     *
     * @param tplContent template内容
     * @param paras      parameter
     * @return String templateparse后内容
     */
    public static String parseTemplateContent(String tplContent,Map<String, Object> paras) {
        return parseTemplateContent(tplContent, paras, false);
    }
    public static String parseTemplateContent(String tplContent, Map<String, Object> paras, boolean keepSpace) {
        try {
            String sqlUnderline="sql_";
            StringWriter swriter = new StringWriter();
            if (stringTemplateLoader.findTemplateSource(sqlUnderline + tplContent.hashCode()) == null) {
                stringTemplateLoader.putTemplate(sqlUnderline + tplContent.hashCode(), tplContent);
            }
            Template mytpl = SQL_CONFIG.getTemplate(sqlUnderline + tplContent.hashCode(), ENCODE);
            if (paras.containsKey(MINI_DAO_FORMAT)) {
                throw new RuntimeException("DaoFormat yes minidao reserved keywords，Not allowed ，请更改parameter定义！");
            }
            paras.put(MINI_DAO_FORMAT, new SimpleFormat());
            mytpl.process(paras, swriter);
            String sql = getSqlText(swriter.toString(), keepSpace);
            paras.remove(MINI_DAO_FORMAT);
            return sql;
        } catch (Exception e) {
            log.error(e.getMessage(), e.fillInStackTrace());
            log.error("发送一次的templatekey:{ " + tplContent + " }");
            //System.err.println(e.getMessage());
            //System.err.println("template内容:{ "+ tplContent +" }");
            throw new RuntimeException("parseSQLtemplate异常");
        }
    }

    /**
     * Remove invalid fields，Remove comments Otherwise, batch processing may report errors. Remove invalid equals
     */
    private static String getSqlText(String sql) {
        return getSqlText(sql, false);
    }

    private static String getSqlText(String sql, boolean keepSpace) {
        // Replace the comment with""
        sql = NOTES_PATTERN.matcher(sql).replaceAll("");
        if (!keepSpace) {
            sql = sql.replaceAll("\\n", " ").replaceAll("\\t", " ")
                    .replaceAll("\\s{1,}", " ").trim();
        }
        // remove 最后yes whereSuch a question
        //wherespace "where "
        String whereSpace = DataBaseConstant.SQL_WHERE+" ";
        //"where and"
        String whereAnd = DataBaseConstant.SQL_WHERE+" and";
        //", where"
        String commaWhere = SymbolConstant.COMMA+" "+DataBaseConstant.SQL_WHERE;
        //", "
        String commaSpace = SymbolConstant.COMMA + " ";
        if (sql.endsWith(DataBaseConstant.SQL_WHERE) || sql.endsWith(whereSpace)) {
            sql = sql.substring(0, sql.lastIndexOf("where"));
        }
        // removewhere and Such a question
        int index = 0;
        while ((index = StringUtils.indexOfIgnoreCase(sql, whereAnd, index)) != -1) {
            sql = sql.substring(0, index + 5)
                    + sql.substring(index + 9, sql.length());
        }
        // remove , where Such a question
        index = 0;
        while ((index = StringUtils.indexOfIgnoreCase(sql, commaWhere, index)) != -1) {
            sql = sql.substring(0, index)
                    + sql.substring(index + 1, sql.length());
        }
        // remove 最后yes ,Such a question
        if (sql.endsWith(SymbolConstant.COMMA) || sql.endsWith(commaSpace)) {
            sql = sql.substring(0, sql.lastIndexOf(","));
        }
        return sql;
    }
}