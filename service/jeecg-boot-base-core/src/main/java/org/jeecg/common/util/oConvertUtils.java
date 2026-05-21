package org.jeecg.common.util;

import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.springframework.beans.BeanUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.*;
import java.sql.Date;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @Author  Zhang Daihao
 *
 */
@Slf4j
public class oConvertUtils {
	public static boolean isEmpty(Object object) {
		if (object == null) {
			return (true);
		}
		if ("".equals(object)) {
			return (true);
		}
		if (CommonConstant.STRING_NULL.equals(object)) {
			return (true);
		}
		return (false);
	}
	
	public static boolean isNotEmpty(Object object) {
		if (object != null && !"".equals(object) && !object.equals(CommonConstant.STRING_NULL)) {
			return (true);
		}
		return (false);
	}

	
	/**
	 * returndecodeDecrypt string
	 * 
	 * @param inStr
	 * @return
	 */
	public static String decodeString(String inStr) {
		if (oConvertUtils.isEmpty(inStr)) {
			return null;
		}

		try {
			inStr = URLDecoder.decode(inStr, "UTF-8");
		} catch (Exception e) {
			// solve：URLDecoder: Illegal hex characters in escape (%) pattern - For input string: "automatic"
			//e.printStackTrace();
		}
		return inStr;
	}

	public static String decode(String strIn, String sourceCode, String targetCode) {
		String temp = code2code(strIn, sourceCode, targetCode);
		return temp;
	}

	@SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
    public static String StrToUTF(String strIn, String sourceCode, String targetCode) {
		strIn = "";
		try {
			strIn = new String(strIn.getBytes("ISO-8859-1"), "GBK");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return strIn;

	}

	private static String code2code(String strIn, String sourceCode, String targetCode) {
		String strOut = null;
		if (strIn == null || "".equals(strIn.trim())) {
			return strIn;
		}
		try {
			byte[] b = strIn.getBytes(sourceCode);
			for (int i = 0; i < b.length; i++) {
				System.out.print(b[i] + "  ");
			}
			strOut = new String(b, targetCode);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return strOut;
	}

	public static int getInt(String s, int defval) {
		if (s == null || "".equals(s)) {
			return (defval);
		}
		try {
			return (Integer.parseInt(s));
		} catch (NumberFormatException e) {
			return (defval);
		}
	}

	public static int getInt(String s) {
		if (s == null || "".equals(s)) {
			return 0;
		}
		try {
			return (Integer.parseInt(s));
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public static int getInt(String s, Integer df) {
		if (s == null || "".equals(s)) {
			return df;
		}
		try {
			return (Integer.parseInt(s));
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public static Integer[] getInts(String[] s) {
		if (s == null) {
			return null;
		}
		Integer[] integer = new Integer[s.length];
		for (int i = 0; i < s.length; i++) {
			integer[i] = Integer.parseInt(s[i]);
		}
		return integer;

	}

	public static double getDouble(String s, double defval) {
		if (s == null || "".equals(s)) {
			return (defval);
		}
		try {
			return (Double.parseDouble(s));
		} catch (NumberFormatException e) {
			return (defval);
		}
	}

	public static double getDou(Double s, double defval) {
		if (s == null) {
			return (defval);
		}
		return s;
	}

	/*public static Short getShort(String s) {
		if (StringUtil.isNotEmpty(s)) {
			return (Short.parseShort(s));
		} else {
			return null;
		}
	}*/

	public static int getInt(Object object, int defval) {
		if (isEmpty(object)) {
			return (defval);
		}
		try {
			return (Integer.parseInt(object.toString()));
		} catch (NumberFormatException e) {
			return (defval);
		}
	}
	
	public static Integer getInteger(Object object, Integer defval) {
		if (isEmpty(object)) {
			return (defval);
		}
		try {
			return (Integer.parseInt(object.toString()));
		} catch (NumberFormatException e) {
			return (defval);
		}
	}
	
	public static Integer getInt(Object object) {
		if (isEmpty(object)) {
			return null;
		}
		try {
			return (Integer.parseInt(object.toString()));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public static int getInt(BigDecimal s, int defval) {
		if (s == null) {
			return (defval);
		}
		return s.intValue();
	}

	public static Integer[] getIntegerArry(String[] object) {
		int len = object.length;
		Integer[] result = new Integer[len];
		try {
			for (int i = 0; i < len; i++) {
				result[i] = new Integer(object[i].trim());
			}
			return result;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public static String getString(String s) {
		return (getString(s, ""));
	}

	/**
	 * Escaped toUnicodecoding
	 * @param s
	 * @return
	 */
	/*public static String escapeJava(Object s) {
		return StringEscapeUtils.escapeJava(getString(s));
	}*/
	
	public static String getString(Object object) {
		if (isEmpty(object)) {
			return "";
		}
		return (object.toString().trim());
	}

	public static String getString(int i) {
		return (String.valueOf(i));
	}

	public static String getString(float i) {
		return (String.valueOf(i));
	}

	/**
	 * return常规string（Keep only numbers in string、letter、Chinese）
	 *
	 * @param input
	 * @return
	 */
	public static String getNormalString(String input) {
		if (oConvertUtils.isEmpty(input)) {
			return null;
		}
		String result = input.replaceAll("[^0-9a-zA-Z\\u4e00-\\u9fa5]", "");
		return result;
	}

	public static String getString(String s, String defval) {
		if (isEmpty(s)) {
			return (defval);
		}
		return (s.trim());
	}

	public static String getString(Object s, String defval) {
		if (isEmpty(s)) {
			return (defval);
		}
		return (s.toString().trim());
	}

	public static long stringToLong(String str) {
		Long test = new Long(0);
		try {
			test = Long.valueOf(str);
		} catch (Exception e) {
		}
		return test.longValue();
	}

	/**
	 * Get this machineIP
	 */
	public static String getIp() {
		String ip = null;
		try {
			InetAddress address = InetAddress.getLocalHost();
			ip = address.getHostAddress();

		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return ip;
	}

	/**
	 * Determine whether a class is a basic data type。
	 * 
	 * @param clazz
	 *            The class to be judged。
	 * @return true Represented as basic data type。
	 */
	private static boolean isBaseDataType(Class clazz) throws Exception {
		return (clazz.equals(String.class) || clazz.equals(Integer.class) || clazz.equals(Byte.class) || clazz.equals(Long.class) || clazz.equals(Double.class) || clazz.equals(Float.class) || clazz.equals(Character.class) || clazz.equals(Short.class) || clazz.equals(BigDecimal.class) || clazz.equals(BigInteger.class) || clazz.equals(Boolean.class) || clazz.equals(Date.class) || clazz.isPrimitive());
	}

	/**
	 * decodingbase64
	 *
	 * @param base64Str base64string
	 * @return 被加密后ofstring
	 */
	public static String decodeBase64Str(String base64Str) {
		byte[] byteContent = Base64.decodeBase64(base64Str);
		if (byteContent == null) {
			return null;
		}
		String decodedString = new String(byteContent);
		return decodedString;
	}
	
	
	/**
	 * @param request
	 *            IP
	 * @return IP Address
	 */
	public static String getIpAddrByRequest(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || CommonConstant.UNKNOWN.equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || CommonConstant.UNKNOWN.equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || CommonConstant.UNKNOWN.equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}

	/**
	 * @return local machineIP
	 * @throws SocketException
	 */
	public static String getRealIp() throws SocketException {
        // localIP，If no external network is configuredIP则return它
		String localip = null;
        // ExtranetIP
		String netip = null;

		Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
		InetAddress ip = null;
        // 是否找到ExtranetIP
		boolean finded = false;
		while (netInterfaces.hasMoreElements() && !finded) {
			NetworkInterface ni = netInterfaces.nextElement();
			Enumeration<InetAddress> address = ni.getInetAddresses();
			while (address.hasMoreElements()) {
				ip = address.nextElement();
                // ExtranetIP
				if (!ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1) {
					netip = ip.getHostAddress();
					finded = true;
					break;
				} else if (ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1) {
                    // IntranetIP
				    localip = ip.getHostAddress();
				}
			}
		}

		if (netip != null && !"".equals(netip)) {
			return netip;
		} else {
			return localip;
		}
	}

	/**
	 * java去除string中of空格、Enter、newline character、tab character
	 * 
	 * @param str
	 * @return
	 */
	public static String replaceBlank(String str) {
		String dest = "";
		if (str != null) {
		    String reg = "\\s*|\t|\r|\n";
			Pattern p = Pattern.compile(reg);
			Matcher m = p.matcher(str);
			dest = m.replaceAll("");
		}
		return dest;

	}

	/**
	 * Determine whether the element is in the array
	 * 
	 * @param child
	 * @param all
	 * @return
	 */
	public static boolean isIn(String child, String[] all) {
		if (all == null || all.length == 0) {
			return false;
		}
		for (int i = 0; i < all.length; i++) {
			String aSource = all[i];
			if (aSource.equals(child)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Determine whether the element is in the array
	 *
	 * @param childArray
	 * @param all
	 * @return
	 */
	public static boolean isArrayIn(String[] childArray, String[] all) {
		if (all == null || all.length == 0) {
			return false;
		}
		for (String v : childArray) {
			if (!isIn(v, all)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Determine whether the element is in the array
	 *
	 * @param childArray
	 * @param all
	 * @return
	 */
	public static boolean isJsonArrayIn(JSONArray childArray, String[] all) {
		if (all == null || all.length == 0) {
			return false;
		}

		List<String> childs = childArray.toJavaList(String.class);
		for (String v : childs) {
			if (!isIn(v, all)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * judgestring是否为JSONFormat
	 * @param str
	 * @return
	 */
	public static boolean isJson(String str) {
		if (str == null || str.trim().isEmpty()) {
			return false;
		}
		try {
			com.alibaba.fastjson.JSON.parse(str);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * GetMapobject
	 */
	public static Map<Object, Object> getHashMap() {
		return new HashMap<>(5);
	}

	/**
	 * SETConvertMAP
	 * 
	 * @param str
	 * @return
	 */
	public static Map<Object, Object> setToMap(Set<Object> setobj) {
		Map<Object, Object> map = getHashMap();
		for (Iterator iterator = setobj.iterator(); iterator.hasNext();) {
			Map.Entry<Object, Object> entry = (Map.Entry<Object, Object>) iterator.next();
			map.put(entry.getKey().toString(), entry.getValue() == null ? "" : entry.getValue().toString().trim());
		}
		return map;

	}

	public static boolean isInnerIp(String ipAddress) {
		boolean isInnerIp = false;
		long ipNum = getIpNum(ipAddress);
		/**
		 * privateIP：Akind 10.0.0.0-10.255.255.255 Bkind 172.16.0.0-172.31.255.255 Ckind 192.168.0.0-192.168.255.255 certainly，besides127This network segment is the loopback address
		 **/
		long aBegin = getIpNum("10.0.0.0");
		long aEnd = getIpNum("10.255.255.255");
		long bBegin = getIpNum("172.16.0.0");
		long bEnd = getIpNum("172.31.255.255");
		long cBegin = getIpNum("192.168.0.0");
		long cEnd = getIpNum("192.168.255.255");
		String localIp = "127.0.0.1";
		isInnerIp = isInner(ipNum, aBegin, aEnd) || isInner(ipNum, bBegin, bEnd) || isInner(ipNum, cBegin, cEnd) || localIp.equals(ipAddress);
		return isInnerIp;
	}

	private static long getIpNum(String ipAddress) {
		String[] ip = ipAddress.split("\\.");
		long a = Integer.parseInt(ip[0]);
		long b = Integer.parseInt(ip[1]);
		long c = Integer.parseInt(ip[2]);
		long d = Integer.parseInt(ip[3]);

		long ipNum = a * 256 * 256 * 256 + b * 256 * 256 + c * 256 + d;
		return ipNum;
	}

	private static boolean isInner(long userIp, long begin, long end) {
		return (userIp >= begin) && (userIp <= end);
	}
	
	/**
	 * Will下划线大写方式命名ofstringConvert为驼峰式。
	 * ifConvert前of下划线大写方式命名ofstring为空，则return空string。</br>
	 * For example：hello_world->helloWorld
	 * 
	 * @param name
	 *            Convert前of下划线大写方式命名ofstring
	 * @return Convert后of驼峰式命名ofstring
	 */
	public static String camelName(String name) {
		StringBuilder result = new StringBuilder();
		// quick check
		if (name == null || name.isEmpty()) {
			// 没必要Convert
			return "";
		} else if (!name.contains(SymbolConstant.UNDERLINE)) {
			// without underscore，仅Will首letter小写
			//update-begin--Author:zhoujf  Date:20180503 for：TASK #2500 【code generator】code generator开发一通用模板生成功能
			//update-begin--Author:zhoujf  Date:20180503 for：TASK #2500 【code generator】code generator开发一通用模板生成功能
			return name.substring(0, 1).toLowerCase() + name.substring(1).toLowerCase();
			//update-end--Author:zhoujf  Date:20180503 for：TASK #2500 【code generator】code generator开发一通用模板生成功能
		}
		// 用下划线Will原始string分割
		String[] camels = name.split("_");
		for (String camel : camels) {
			// 跳过原始string中开头、Trailing underscore or double underscore
			if (camel.isEmpty()) {
				continue;
			}
			// Handling real camel fragments
			if (result.length() == 0) {
				// first hump fragment，全部letter都小写
				result.append(camel.toLowerCase());
			} else {
				// Other hump clips，首letter大写
				result.append(camel.substring(0, 1).toUpperCase());
				result.append(camel.substring(1).toLowerCase());
			}
		}
		return result.toString();
	}
	
	/**
	 * Will下划线大写方式命名ofstringConvert为驼峰式。
	 * ifConvert前of下划线大写方式命名ofstring为空，则return空string。</br>
	 * For example：hello_world,test_id->helloWorld,testId
	 * 
	 * @param name
	 *            Convert前of下划线大写方式命名ofstring
	 * @return Convert后of驼峰式命名ofstring
	 */
	public static String camelNames(String names) {
		if(names==null||"".equals(names)){
			return null;
		}
		StringBuffer sf = new StringBuffer();
		String[] fs = names.split(",");
		for (String field : fs) {
			field = camelName(field);
			sf.append(field + ",");
		}
		String result = sf.toString();
		return result.substring(0, result.length() - 1);
	}
	
	//update-begin--Author:zhoujf  Date:20180503 for：TASK #2500 【code generator】code generator开发一通用模板生成功能
	/**
	 * Will下划线大写方式命名ofstringConvert为驼峰式。(首letter写)
	 * ifConvert前of下划线大写方式命名ofstring为空，则return空string。</br>
	 * For example：hello_world->HelloWorld
	 * 
	 * @param name
	 *            Convert前of下划线大写方式命名ofstring
	 * @return Convert后of驼峰式命名ofstring
	 */
	public static String camelNameCapFirst(String name) {
		StringBuilder result = new StringBuilder();
		// quick check
		if (name == null || name.isEmpty()) {
			// 没必要Convert
			return "";
		} else if (!name.contains(SymbolConstant.UNDERLINE)) {
			// without underscore，仅Will首letter小写
			return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
		}
		// 用下划线Will原始string分割
		String[] camels = name.split("_");
		for (String camel : camels) {
			// 跳过原始string中开头、Trailing underscore or double underscore
			if (camel.isEmpty()) {
				continue;
			}
			// Other hump clips，首letter大写
			result.append(camel.substring(0, 1).toUpperCase());
			result.append(camel.substring(1).toLowerCase());
		}
		return result.toString();
	}
	//update-end--Author:zhoujf  Date:20180503 for：TASK #2500 【code generator】code generator开发一通用模板生成功能
	
	/**
	 * Convert camelCase names to underscores
	 * @param para
	 * @return
	 */
	public static String camelToUnderline(String para){
	    int length = 3;
        if(para.length()<length){
        	return para.toLowerCase(); 
        }
        StringBuilder sb=new StringBuilder(para);
        //position
        int temp=0;
        //Starting from the third character Avoid naming irregularities 
        for(int i=2;i<para.length();i++){
            if(Character.isUpperCase(para.charAt(i))){
                sb.insert(i+temp, "_");
                temp+=1;
            }
        }
        return sb.toString().toLowerCase(); 
	}

	/**
	 * random number
	 * @param place 定义random numberof位数
	 */
	public static String randomGen(int place) {
		String base = "qwertyuioplkjhgfdsazxcvbnmQAZWSXEDCRFVTGBYHNUJMIKLOP0123456789";
		StringBuffer sb = new StringBuffer();
		Random rd = new Random();
		for(int i=0;i<place;i++) {
			sb.append(base.charAt(rd.nextInt(base.length())));
		}
		return sb.toString();
	}
	
	/**
	 * Getkindof所有属性，包括父kind
	 * 
	 * @param object
	 * @return
	 */
	public static Field[] getAllFields(Object object) {
		Class<?> clazz = object.getClass();
		List<Field> fieldList = new ArrayList<>();
		while (clazz != null) {
			fieldList.addAll(new ArrayList<>(Arrays.asList(clazz.getDeclaredFields())));
			clazz = clazz.getSuperclass();
		}
		Field[] fields = new Field[fieldList.size()];
		fieldList.toArray(fields);
		return fields;
	}
	
	/**
	  * WillmapofkeyConvert all to lowercase
	 * @param list
	 * @return
	 */
	public static List<Map<String, Object>> toLowerCasePageList(List<Map<String, Object>> list){
		List<Map<String, Object>> select = new ArrayList<>();
		for (Map<String, Object> row : list) {
			 Map<String, Object> resultMap = new HashMap<>(5);
			 Set<String> keySet = row.keySet(); 
			 for (String key : keySet) { 
				 String newKey = key.toLowerCase(); 
				 resultMap.put(newKey, row.get(key)); 
			 }
			 select.add(resultMap);
		}
		return select;
	}

	/**
	 * WillentityListConvert成modelList
	 * @param fromList
	 * @param tClass
	 * @param <F>
	 * @param <T>
	 * @return
	 */
	public static<F,T> List<T> entityListToModelList(List<F> fromList, Class<T> tClass){
		if(fromList == null || fromList.isEmpty()){
			return null;
		}
		List<T> tList = new ArrayList<>();
		for(F f : fromList){
			T t = entityToModel(f, tClass);
			tList.add(t);
		}
		return tList;
	}

	public static<F,T> T entityToModel(F entity, Class<T> modelClass) {
		log.debug("entityToModel : Entity属性of值赋值到Model");
		Object model = null;
		if (entity == null || modelClass ==null) {
			return null;
		}

		try {
			model = modelClass.newInstance();
		} catch (InstantiationException e) {
			log.error("entityToModel : instantiation exception", e);
		} catch (IllegalAccessException e) {
			log.error("entityToModel : Security permission exception", e);
		}
		BeanUtils.copyProperties(entity, model);
		return (T)model;
	}

	/**
	 * judge list Is it empty
	 *
	 * @param list
	 * @return true or false
	 * list == null		: true
	 * list.size() == 0	: true
	 */
	public static boolean listIsEmpty(Collection list) {
		return (list == null || list.size() == 0);
	}

	/**
	 * judge旧值与新值 Are they equal?
	 *
	 * @param oldVal
	 * @param newVal
	 * @return
	 */
	public static boolean isEqual(Object oldVal, Object newVal) {
		if (oldVal != null && newVal != null) {
			if (isArray(oldVal)) {
				return equalityOfArrays((Object[]) oldVal, (Object[]) newVal);
			}else if(oldVal instanceof JSONArray){
				if(newVal instanceof JSONArray){
					return equalityOfJSONArray((JSONArray) oldVal, (JSONArray) newVal);
				}else{
					if (isEmpty(newVal) && (oldVal == null || ((JSONArray) oldVal).size() == 0)) {
						return true;
					}
					List<Object> arrayStr = Arrays.asList(newVal.toString().split(","));
					JSONArray newValArray = new JSONArray(arrayStr);
					return equalityOfJSONArray((JSONArray) oldVal, newValArray);
				}
			}else{
				return oldVal.equals(newVal);
			}
			
		} else {
			if (oldVal == null && newVal == null) {
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * Method description judge一个object是否是一个数组
	 *
	 * @param obj
	 * @return
	 * @author yaomy
	 * @date 2018Year2moon5day afternoon5:03:00
	 */
	public static boolean isArray(Object obj) {
		if (obj == null) {
			return false;
		}
		return obj.getClass().isArray();
	}

	/**
	 * Get集合of大小
	 * 
	 * @param collection
	 * @return
	 */
	public static int getCollectionSize(Collection<?> collection) {
		return collection != null ? collection.size() : 0;
	}
	
	/**
	 * judge两个数组Are they equal?（Array elements are in no particular order）
	 *
	 * @param oldVal
	 * @param newVal
	 * @return
	 */
	public static boolean equalityOfJSONArray(JSONArray oldVal, JSONArray newVal) {
		if (oldVal != null && newVal != null) {
			Object[] oldValArray = oldVal.toArray();
			Object[] newValArray = newVal.toArray();
			return equalityOfArrays(oldValArray,newValArray);
		} else {
			if ((oldVal == null || oldVal.size() == 0) && (newVal == null || newVal.size() == 0)) {
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * 比较带逗号ofstring
	 * QQYUN-5212【Simple flow】按day期触发 Multiple choice People component When the selection order is inconsistent Not triggered，It should be a matter of unification 包括Multiple choice部门组件
	 * @param oldVal
	 * @param newVal
	 * @return
	 */
	public static boolean equalityOfStringArrays(String oldVal, String newVal) {
		if(oldVal.equals(newVal)){
			return true;
		}
		if(oldVal.indexOf(",")>=0 && newVal.indexOf(",")>=0){
			String[] arr1 = oldVal.split(",");
			String[] arr2 = newVal.split(",");
			if(arr1.length == arr2.length){
				boolean flag = true;
				Map<String, Integer> map = new HashMap<>();
				for(String s1: arr1){
					map.put(s1, 1);
				}
				for(String s2: arr2){
					if(map.get(s2) == null){
						flag = false;
						break;
					}
				}
				return flag;
			}
		}
		return false;
	}
	
	/**
	 * judge两个数组Are they equal?（Array elements are in no particular order）
	 *
	 * @param oldVal
	 * @param newVal
	 * @return
	 */
	public static boolean equalityOfArrays(Object[] oldVal, Object newVal[]) {
		if (oldVal != null && newVal != null) {
			Arrays.sort(oldVal);
			Arrays.sort(newVal);
			return Arrays.equals(oldVal, newVal);
		} else {
			if ((oldVal == null || oldVal.length == 0) && (newVal == null || newVal.length == 0)) {
				return true;
			} else {
				return false;
			}
		}
	}

//	public static void main(String[] args) {
////		String[] a = new String[]{"1", "2"};
////		String[] b = new String[]{"2", "1"};
//		Integer a = null;
//		Integer b = 1;
//		System.out.println(oConvertUtils.isEqual(a, b));
//	}
	
	/**
	 * judge list Whether it is not empty
	 *
	 * @param list
	 * @return true or false
	 * list == null		: false
	 * list.size() == 0	: false
	 */
	public static boolean listIsNotEmpty(Collection list) {
		return !listIsEmpty(list);
	}

	/**
	 * Read static text content
	 * @param url
	 * @return
	 */
	public static String readStatic(String url) {
		String json = "";
		try {
			//Change the way of writing，solvespringbootreadjar包Chinese件of问题
			InputStream stream = oConvertUtils.class.getClassLoader().getResourceAsStream(url.replace("classpath:", ""));
			json = IOUtils.toString(stream,"UTF-8");
		} catch (IOException e) {
			log.error(e.getMessage(),e);
		}
		return json;
	}

	/**
	 * WillList Convert to JSONArray
	 * @return
	 */
	public static JSONArray list2JSONArray(List<String> list){
		if(list==null || list.size()==0){
			return null;
		}
		JSONArray array = new JSONArray();
		for(String str: list){
			array.add(str);
		}
		return array;
	}

	/**
	 * judge两个list中of元素是否完全一致
	 * QQYUN-5326【Simple flow】Get组织人员 one/many Filter criteria No department filtering
	 * @return
	 */
	public static boolean isEqList(List<String> list1, List<String> list2){
		if(list1.size() != list2.size()){
			return false;
		}
		for(String str1: list1){
			boolean flag = false;
			for(String str2: list2){
				if(str1.equals(str2)){
					flag = true;
					break;
				}
			}
			if(!flag){
				return false;
			}
		}
		return true;
	}


	/**
	 * judge list1中of元素是否在list2appear in
	 * QQYUN-5326【Simple flow】Get组织人员 one/many Filter criteria No department filtering
	 * @param list1
	 * @param list2
	 * @return
	 */
	public static boolean isInList(List<String> list1, List<String> list2){
		for(String str1: list1){
			boolean flag = false;
			for(String str2: list2){
				if(str1.equals(str2)){
					flag = true;
					break;
				}
			}
			if(flag){
				return true;
			}
		}
		return false;
	}

	/**
	 * 计算文件大小Convert toMB
	 * @param uploadCount
	 * @return
	 */
	public static Double calculateFileSizeToMb(Long uploadCount){
		double count = 0.0;
		if(uploadCount>0) {
			BigDecimal bigDecimal = new BigDecimal(uploadCount);
			//converted toMB
			BigDecimal divide = bigDecimal.divide(new BigDecimal(1048576));
			count = divide.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
			return count;
		}
		return count;
	}

	/**
	 * mapchangestr
	 *
	 * @param map
	 * @return
	 */
	public static String mapToString(Map<String, String[]> map) {
		if (map == null || map.size() == 0) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String[]> entry : map.entrySet()) {
			String key = entry.getKey();
			String[] values = entry.getValue();
			sb.append(key).append("=");
			sb.append(values != null ? StringUtils.join(values, ",") : "");
			sb.append("&");
		}

		String result = sb.toString();
		if (result.endsWith("&")) {
			result = result.substring(0, sb.length() - 1);
		}
		return result;
	}

	/**
	 * judgeobjectIs it empty <br/>
	 * 支持各种kind型ofobject
	 * for for [QQYUN-10990]AIRAG
	 * @param obj
	 * @return
	 * @author chenrui
	 * @date 2025/2/13 18:34
	 */
	public static boolean isObjectEmpty(Object obj) {
		if (null == obj) {
			return true;
		}

		if (obj instanceof CharSequence) {
			return isEmpty(obj);
		} else if (obj instanceof Map) {
			return ((Map<?, ?>) obj).isEmpty();
		} else if (obj instanceof Iterable) {
			return isObjectEmpty(((Iterable<?>) obj).iterator());
		} else if (obj instanceof Iterator) {
			return !((Iterator<?>) obj).hasNext();
		} else if (isArray(obj)) {
			return 0 == Array.getLength(obj);
		}
		return false;
	}

	/**
	 * iterator Is it empty
	 * for for [QQYUN-10990]AIRAG
	 * @param iterator Iteratorobject
	 * @return Is it empty
	 */
	public static boolean isEmptyIterator(Iterator<?> iterator) {
		return null == iterator || false == iterator.hasNext();
	}


	/**
	 * judgeobjectWhether it is not empty
	 * for for [QQYUN-10990]AIRAG
	 * @param object
	 * @return
	 * @author chenrui
	 * @date 2025/2/13 18:35
	 */
	public static boolean isObjectNotEmpty(Object object) {
		return !isObjectEmpty(object);
	}

	/**
	 * ifsrcgreater thandesreturntrue
	 * for [QQYUN-10990]AIRAG
	 * @param src
	 * @param des
	 * @return
	 * @author: chenrui
	 * @date: 2018/9/19 15:30
	 */
	public static boolean isGt(Number src, Number des) {
		if (null == src || null == des) {
			throw new IllegalArgumentException("Parameter cannot be empty");
		}
		if (src.doubleValue() > des.doubleValue()) {
			return true;
		}
		return false;
	}

	/**
	 * ifsrcgreater than等于desreturntrue
	 * for [QQYUN-10990]AIRAG
	 * @param src
	 * @param des
	 * @return
	 * @author: chenrui
	 * @date: 2018/9/19 15:30
	 */
	public static boolean isGe(Number src, Number des) {
		if (null == src || null == des) {
			throw new IllegalArgumentException("Parameter cannot be empty");
		}
		if (src.doubleValue() < des.doubleValue()) {
			return false;
		}
		return true;
	}


	/**
	 * judge是否存在
	 * for [QQYUN-10990]AIRAG
	 * @param obj
	 * @param objs
	 * @param <T>
	 * @return
	 * @author chenrui
	 * @date 2020/9/12 15:50
	 */
	public static <T> boolean isIn(T obj, T... objs) {
		if (isEmpty(objs)) {
			return false;
		}
		for (T obj1 : objs) {
			if (isEqual(obj, obj1)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * judge租户IDIs it valid?
	 * @param tenantId
	 * @return
	 */
	public static boolean isEffectiveTenant(String tenantId) {
		return MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL && isNotEmpty(tenantId) && !("0").equals(tenantId);
	}
	
}
