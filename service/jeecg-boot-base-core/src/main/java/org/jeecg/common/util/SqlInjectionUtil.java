package org.jeecg.common.util;

import cn.hutool.core.util.ReUtil;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.exception.JeecgSqlInjectionException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * sqlInjection processing tool class
 * 
 * @author zhoujf
 */
@Slf4j
public class SqlInjectionUtil {

	/**
	 * sqlInject blacklist database name
	 */
	public final static String XSS_STR_TABLE = "peformance_schema|information_schema";

	/**
	 * default—sqlInject keywords
	 */
	private final static String XSS_STR = "and |exec |peformance_schema|information_schema|extractvalue|updatexml|geohash|gtid_subset|gtid_subtract|insert |select |delete |update |drop |count |chr |mid |master |truncate |char |declare |;|or |+|--";
	/**
	 * onlineReport only—sqlInject keywords
	 */
	private static String specialReportXssStr = "exec |peformance_schema|information_schema|extractvalue|updatexml|geohash|gtid_subset|gtid_subtract|insert |alter |delete |grant |update |drop |master |truncate |declare |--";
	/**
	 * Dictionary only—sqlInject keywords
	 */
	private static String specialDictSqlXssStr = "exec |peformance_schema|information_schema|extractvalue|updatexml|geohash|gtid_subset|gtid_subtract|insert |select |delete |update |drop |count |chr |mid |master |truncate |char |declare |;|+|--";
	/**
	 * Complete matchkey，No need to consider leading spaces
	 */
	private static List<String> FULL_MATCHING_KEYWRODS = new ArrayList<>();
	static {
		FULL_MATCHING_KEYWRODS.add(";");
		FULL_MATCHING_KEYWRODS.add("+");
		FULL_MATCHING_KEYWRODS.add("--");
	}
	
	
	/**
	 * sqlinject risk Regular keyword
	 *
	 * function matching，Need to use regular mode
	 */
	private final static String[] XSS_REGULAR_STR_ARRAY = new String[]{
			"chr\\s*\\(",
			"mid\\s*\\(",
			" char\\s*\\(",
			"sleep\\s*\\(",
			"user\\s*\\(",
			"show\\s+tables",
			"user[\\s]*\\([\\s]*\\)",
			"show\\s+databases",
			"sleep\\(\\d*\\)",
			"sleep\\(.*\\)",
	};
	/**
	 * sqlAnnotation regularity
	 */
	private final static Pattern SQL_ANNOTATION = Pattern.compile("/\\*[\\s\\S]*\\*/");
	private final static  String SQL_ANNOTATION2 = "--";
	
	/**
	 * sqlInject prompts
	 */
	private final static String SQL_INJECTION_KEYWORD_TIP = "please note，existSQLInject keywords---> {}";
	private final static String SQL_INJECTION_TIP = "please note，值可能existSQLInject risk!--->";
	private final static String SQL_INJECTION_TIP_VARIABLE = "please note，值可能existSQLInject risk!---> {}";
	

	/**
	 * sqlInjection filter processing，Throw an exception when encountering injected keywords
	 * @param values
	 */
	public static void filterContentMulti(String... values) {
		filterContent(values, null);
	}

	/**
	 * Verification is stricter
	 * 
	 * sqlInjection filter processing，Throw an exception when encountering injected keywords
	 *
	 * @param value
	 * @return
	 */
	public static void filterContent(String value, String customXssString) {
		if (value == null || "".equals(value)) {
			return;
		}
		// one、checksqlComment not allowedsqlComment
		checkSqlAnnotation(value);
		// Convert to lowercase for subsequent comparisons
		value = value.toLowerCase().trim();
		
		// two、SQLinjection检测exist绕过风险 (普通文本check)
		//https://gitee.com/jeecg/jeecg-boot/issues/I4NZGE
		String[] xssArr = XSS_STR.split("\\|");
		for (int i = 0; i < xssArr.length; i++) {
			if (value.indexOf(xssArr[i]) > -1) {
				log.error(SqlInjectionUtil.SQL_INJECTION_KEYWORD_TIP, xssArr[i]);
				log.error(SqlInjectionUtil.SQL_INJECTION_TIP_VARIABLE, value);
				throw new JeecgSqlInjectionException(SqlInjectionUtil.SQL_INJECTION_TIP + value);
			}
		}
		// three、SQLinjection检测exist绕过风险 (自定义传入普通文本check)
		if (customXssString != null) {
			String[] xssArr2 = customXssString.split("\\|");
			for (int i = 0; i < xssArr2.length; i++) {
				if (value.indexOf(xssArr2[i]) > -1) {
					log.error(SqlInjectionUtil.SQL_INJECTION_KEYWORD_TIP, xssArr2[i]);
					log.error(SqlInjectionUtil.SQL_INJECTION_TIP_VARIABLE, value);
					throw new JeecgSqlInjectionException(SqlInjectionUtil.SQL_INJECTION_TIP + value);
				}
			}
		}

		// Four、SQLinjection检测exist绕过风险 (正则check)
		for (String regularOriginal : XSS_REGULAR_STR_ARRAY) {
			String regular = ".*" + regularOriginal + ".*";
			if (Pattern.matches(regular, value)) {
				log.error(SqlInjectionUtil.SQL_INJECTION_KEYWORD_TIP, regularOriginal);
				log.error(SqlInjectionUtil.SQL_INJECTION_TIP_VARIABLE, value);
				throw new JeecgSqlInjectionException(SqlInjectionUtil.SQL_INJECTION_TIP + value);
			}
		}
		return;
	}

	/**
	 * 判断是否existSQLInject keywordsstring
	 *
	 * @param keyword
	 * @return
	 */
	@SuppressWarnings("AlibabaUndefineMagicConstant")
	private static boolean isExistSqlInjectKeyword(String sql, String keyword) {
		if (sql.startsWith(keyword.trim())) {
			return true;
		} else if (sql.contains(keyword)) {
			// need to match，sqlInject keywords
			String matchingText = " " + keyword;
			if(FULL_MATCHING_KEYWRODS.contains(keyword)){
				matchingText = keyword;
			}
			
			if (sql.contains(matchingText)) {
				return true;
			} else {
				String regularStr = "\\s+\\S+" + keyword;
				List<String> resultFindAll = ReUtil.findAll(regularStr, sql, 0, new ArrayList<String>());
				for (String res : resultFindAll) {
					log.info("isExistSqlInjectKeyword —- matchedSQLInject keywords：{}", res);
					/**
					 * SQLCharacters that can replace spaces in injection(%09  %0A  %0D  +can replace spaces)
					 * http://blog.chinaunix.net/uid-12501104-id-2932639.html
					 * https://www.cnblogs.com/Vinson404/p/7253255.html
					 * */
					if (res.contains("%") || res.contains("+") || res.contains("#") || res.contains("/") || res.contains(")")) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * 判断是否existSQLInject keywordsstring
	 *
	 * @param keyword
	 * @return
	 */
	@SuppressWarnings("AlibabaUndefineMagicConstant")
	private static boolean isExistSqlInjectTableKeyword(String sql, String keyword) {
		// need to match，sqlInject keywords
		String[] matchingTexts = new String[]{"`" + keyword, "(" + keyword, "(`" + keyword};
		for (String matchingText : matchingTexts) {
			String[] checkTexts = new String[]{" " + matchingText, "from" + matchingText};
			for (String checkText : checkTexts) {
				if (sql.contains(checkText)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * sqlInjection filter processing，Throw an exception when encountering injected keywords
	 * 
	 * @param values
	 * @return
	 */
	public static void filterContent(String[] values, String customXssString) {
		for (String val : values) {
			if (oConvertUtils.isEmpty(val)) {
				return;
			}
			filterContent(val, customXssString);
		}
		return;
	}

	/**
	 * 【remind：Not universal】
	 * only for dictionary conditionsSQLparameter，Inject filter
	 *
	 * @param value
	 * @return
	 */
	public static void specialFilterContentForDictSql(String value) {
		String[] xssArr = specialDictSqlXssStr.split("\\|");
		if (value == null || "".equals(value)) {
			return;
		}
		// one、checksqlComment not allowedsqlComment
		checkSqlAnnotation(value);
		value = value.toLowerCase().trim();
		
		// two、SQLinjection检测exist绕过风险 (普通文本check)
		for (int i = 0; i < xssArr.length; i++) {
			if (isExistSqlInjectKeyword(value, xssArr[i])) {
				log.error(SqlInjectionUtil.SQL_INJECTION_KEYWORD_TIP, xssArr[i]);
				log.error(SqlInjectionUtil.SQL_INJECTION_TIP_VARIABLE, value);
				throw new JeecgSqlInjectionException(SqlInjectionUtil.SQL_INJECTION_TIP + value);
			}
		}
		String[] xssTableArr = XSS_STR_TABLE.split("\\|");
		for (String xssTableStr : xssTableArr) {
            if (isExistSqlInjectTableKeyword(value, xssTableStr)) {
                log.error(SqlInjectionUtil.SQL_INJECTION_KEYWORD_TIP, xssTableStr);
                log.error(SqlInjectionUtil.SQL_INJECTION_TIP_VARIABLE, value);
                throw new JeecgSqlInjectionException(SqlInjectionUtil.SQL_INJECTION_TIP + value);
            }
        }

		// three、SQLinjection检测exist绕过风险 (正则check)
		for (String regularOriginal : XSS_REGULAR_STR_ARRAY) {
			String regular = ".*" + regularOriginal + ".*";
			if (Pattern.matches(regular, value)) {
				log.error(SqlInjectionUtil.SQL_INJECTION_KEYWORD_TIP, regularOriginal);
				log.error(SqlInjectionUtil.SQL_INJECTION_TIP_VARIABLE, value);
				throw new JeecgSqlInjectionException(SqlInjectionUtil.SQL_INJECTION_TIP + value);
			}
		}
		return;
	}

    /**
	 * 【remind：Not universal】
     *  only forOnlineReportSQLparse，Inject filter
     * @param value
     * @return
     */
	public static void specialFilterContentForOnlineReport(String value) {
		String[] xssArr = specialReportXssStr.split("\\|");
		if (value == null || "".equals(value)) {
			return;
		}
		// one、checksqlComment not allowedsqlComment
		checkSqlAnnotation(value);
		value = value.toLowerCase().trim();
		
		// two、SQLinjection检测exist绕过风险 (普通文本check)
		for (int i = 0; i < xssArr.length; i++) {
			if (isExistSqlInjectKeyword(value, xssArr[i])) {
				log.error(SqlInjectionUtil.SQL_INJECTION_KEYWORD_TIP, xssArr[i]);
				log.error(SqlInjectionUtil.SQL_INJECTION_TIP_VARIABLE, value);
				throw new JeecgSqlInjectionException(SqlInjectionUtil.SQL_INJECTION_TIP + value);
			}
		}
		String[] xssTableArr = XSS_STR_TABLE.split("\\|");
		for (String xssTableStr : xssTableArr) {
			if (isExistSqlInjectTableKeyword(value, xssTableStr)) {
				log.error(SqlInjectionUtil.SQL_INJECTION_KEYWORD_TIP, xssTableStr);
				log.error(SqlInjectionUtil.SQL_INJECTION_TIP_VARIABLE, value);
				throw new JeecgSqlInjectionException(SqlInjectionUtil.SQL_INJECTION_TIP + value);
			}
		}

		// three、SQLinjection检测exist绕过风险 (正则check)
		for (String regularOriginal : XSS_REGULAR_STR_ARRAY) {
			String regular = ".*" + regularOriginal + ".*";
			if (Pattern.matches(regular, value)) {
				log.error(SqlInjectionUtil.SQL_INJECTION_KEYWORD_TIP, regularOriginal);
				log.error(SqlInjectionUtil.SQL_INJECTION_TIP_VARIABLE, value);
				throw new JeecgSqlInjectionException(SqlInjectionUtil.SQL_INJECTION_TIP + value);
			}
		}
		return;
	}


	/**
	 * check是否有sqlComment 
	 * @return
	 */
	public static void checkSqlAnnotation(String str){
		if(str.contains(SQL_ANNOTATION2)){
			String error = "please note，SQL中不允许含Comment，There is a security risk！";
			log.error(error);
			throw new RuntimeException(error);
		}

		
		Matcher matcher = SQL_ANNOTATION.matcher(str);
		if(matcher.find()){
			String error = "please note，值可能existSQLInject risk---> \\*.*\\";
			log.error(error);
			throw new JeecgSqlInjectionException(error);
		}
	}


	/**
	 * Return query table name
	 * <p>
	 * sqlInjection filter processing，Throw an exception when encountering injected keywords
	 *
	 * @param table
	 */
	private static Pattern tableNamePattern = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_\\$]{0,63}$");
	public static String getSqlInjectTableName(String table) {
		if(oConvertUtils.isEmpty(table)){
			return table;
		}

		//update-begin---author:scott ---date:2024-05-28  for：表单设计器列表翻译exist表名带条件，causing translation problems----
		int index = table.toLowerCase().indexOf(" where ");
		if (index != -1) {
			table = table.substring(0, index);
			log.info("cut offwhereThe new table name after：" + table);
		}
		//update-end---author:scott ---date::2024-05-28  for：表单设计器列表翻译exist表名带条件，causing translation problems----

		table = table.trim();
		/**
		 * Check whether the table name is legal
		 *
		 * Table names can only consist of letters、Composed of numbers and underscores。
		 * Table name must start with a letter。
		 * Table name length is usually limited，For example, at most 64 characters。
		 */
		boolean isValidTableName = tableNamePattern.matcher(table).matches();
		if (!isValidTableName) {
			String errorMsg = "The table name is illegal，existSQLInject risk!--->" + table;
			log.error(errorMsg);
			throw new JeecgSqlInjectionException(errorMsg);
		}

		//进one步验证是否existSQLInject risk
		filterContentMulti(table);
		return table;
	}


	/**
	 * Return query field
	 * <p>
	 * sqlInjection filter processing，Throw an exception when encountering injected keywords
	 *
	 * @param field
	 */
	static final Pattern fieldPattern = Pattern.compile("^[a-zA-Z0-9_]+$");
	public static String getSqlInjectField(String field) {
		if(oConvertUtils.isEmpty(field)){
			return field;
		}
		
		field = field.trim();

		if (field.contains(SymbolConstant.COMMA)) {
			return getSqlInjectField(field.split(SymbolConstant.COMMA));
		}

		/**
		 * check表字段是否有效
		 *
		 * Field definitions can only be letters number combination of underscores（not allowed空格、Escape strings etc.）
		 */
		boolean isValidField = fieldPattern.matcher(field).matches();
		if (!isValidField) {
			String errorMsg = "Field is illegal，existSQLInject risk!--->" + field;
			log.error(errorMsg);
			throw new JeecgSqlInjectionException(errorMsg);
		}

		//进one步验证是否existSQLInject risk
		filterContentMulti(field);
		return field;
	}

	/**
	 * Get multiple fields
	 * return: comma splice
	 *
	 * @param fields
	 * @return
	 */
	public static String getSqlInjectField(String... fields) {
		for (String s : fields) {
			getSqlInjectField(s);
		}
		return String.join(SymbolConstant.COMMA, fields);
	}


	/**
	 * Get sorting field
	 * return：string
	 *
	 * 1.Convert camelCase names to underscores 
	 * 2.limitsqlinjection
	 * @param sortField  sort field
	 * @return
	 */
	public static String getSqlInjectSortField(String sortField) {
		String field = SqlInjectionUtil.getSqlInjectField(oConvertUtils.camelToUnderline(sortField));
		return field;
	}

	/**
	 * Get多个sort field
	 * return：array
	 *
	 * 1.Convert camelCase names to underscores 
	 * 2.limitsqlinjection
	 * @param sortFields 多个sort field
	 * @return
	 */
	public static List getSqlInjectSortFields(String... sortFields) {
		List list = new ArrayList<String>();
		for (String sortField : sortFields) {
			list.add(getSqlInjectSortField(sortField));
		}
		return list;
	}

	/**
	 * Get orderBy type
	 * return：string
	 * <p>
	 * 1.Check whether it is asc or desc 其中的one个
	 * 2.limitsqlinjection
	 *
	 * @param orderType
	 * @return
	 */
	public static String getSqlInjectOrderType(String orderType) {
		if (orderType == null) {
			return null;
		}
		orderType = orderType.trim();
		if (CommonConstant.ORDER_TYPE_ASC.equalsIgnoreCase(orderType)) {
			return CommonConstant.ORDER_TYPE_ASC;
		} else {
			return CommonConstant.ORDER_TYPE_DESC;
		}
	}

}
