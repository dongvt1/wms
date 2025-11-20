package org.jeecg.common.system.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Collectors;

import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.DataBaseConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.constant.TenantConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.system.vo.SysUserCacheInfo;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;

/**
 * @Author Scott
 * @Date 2018-07-12 14:23
 * @Desc JWTTools
 **/
@Slf4j
public class JwtUtil {

	/**TokenValid for7sky（TokenexistreidsMedium cache time is doubled）*/
	public static final long EXPIRE_TIME = (7 * 12) * 60 * 60 * 1000;
	static final String WELL_NUMBER = SymbolConstant.WELL_NUMBER + SymbolConstant.LEFT_CURLY_BRACKET;

    /**
     *
     * @param response
     * @param code
     * @param errorMsg
     */
	public static void responseError(HttpServletResponse response, Integer code, String errorMsg) {
		try {
			Result jsonResult = new Result(code, errorMsg);
			jsonResult.setSuccess(false);
			
			// Set response headers and content type
			response.setStatus(code);
			response.setHeader("Content-type", "text/html;charset=UTF-8");
			response.setContentType("application/json;charset=UTF-8");
			// use ObjectMapper serialized to JSON string
			ObjectMapper objectMapper = new ObjectMapper();
			String json = objectMapper.writeValueAsString(jsonResult);
			response.getWriter().write(json);
			response.getWriter().flush();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * checktokenIs it correct?
	 *
	 * @param token  key
	 * @param secret User's password
	 * @return Is it correct?
	 */
	public static boolean verify(String token, String username, String secret) {
		try {
			// Generate based on passwordJWTvalidator
			Algorithm algorithm = Algorithm.HMAC256(secret);
			JWTVerifier verifier = JWT.require(algorithm).withClaim("username", username).build();
			// EffectivenessTOKEN
			DecodedJWT jwt = verifier.verify(token);
			return true;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return false;
		}
	}

	/**
	 * gettokenThe information in is not requiredsecret解密也能get
	 *
	 * @return tokenUsername contained in
	 */
	public static String getUsername(String token) {
		try {
			DecodedJWT jwt = JWT.decode(token);
			return jwt.getClaim("username").asString();
		} catch (JWTDecodeException e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Generate signature,5minexpires later
	 *
	 * @param username username
	 * @param secret   User's password
	 * @return encryptedtoken
	 */
	public static String sign(String username, String secret) {
		Date date = new Date(System.currentTimeMillis() + EXPIRE_TIME);
		Algorithm algorithm = Algorithm.HMAC256(secret);
		// Comes withusernameinformation
		return JWT.create().withClaim("username", username).withExpiresAt(date).sign(algorithm);

	}

	/**
	 * according torequestintokenGet user account
	 * 
	 * @param request
	 * @return
	 * @throws JeecgBootException
	 */
	public static String getUserNameByToken(HttpServletRequest request) throws JeecgBootException {
		String accessToken = request.getHeader("X-Access-Token");
		String username = getUsername(accessToken);
		if (oConvertUtils.isEmpty(username)) {
			throw new JeecgBootException("User not obtained");
		}
		return username;
	}
	
	/**
	  *  fromsessionGet variables in
	 * @param key
	 * @return
	 */
	public static String getSessionData(String key) {
		//${myVar}%
		//get${} The value behind
		String moshi = "";
		String wellNumber = WELL_NUMBER;

		if(key.indexOf(SymbolConstant.RIGHT_CURLY_BRACKET)!=-1){
			 moshi = key.substring(key.indexOf("}")+1);
		}
		String returnValue = null;
		if (key.contains(wellNumber)) {
			key = key.substring(2,key.indexOf("}"));
		}
		if (oConvertUtils.isNotEmpty(key)) {
			HttpSession session = SpringContextUtils.getHttpServletRequest().getSession();
			returnValue = (String) session.getAttribute(key);
		}
		//result plus${} The value behind
		if(returnValue!=null){returnValue = returnValue + moshi;}
		return returnValue;
	}
	
	/**
	  * from当前用户Get variables in
	 * @param key
	 * @param user
	 * @return
	 */
	public static String getUserSystemData(String key, SysUserCacheInfo user) {
		//1.Get priority SysUserCacheInfo
		if(user==null) {
			try {
				user = JeecgDataAutorUtils.loadUserInfo();
			} catch (Exception e) {
				log.warn("获取用户information异常：" + e.getMessage());
			}
		}
		//2.passshiro获取登录用户information
		LoginUser sysUser = null;
		try {
			sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		} catch (Exception e) {
			log.warn("SecurityUtils.getSubject() 获取用户information异常：" + e.getMessage());
		}

		//#{sys_user_code}%
		String moshi = "";
        String wellNumber = WELL_NUMBER;
		if(key.indexOf(SymbolConstant.RIGHT_CURLY_BRACKET)!=-1){
			 moshi = key.substring(key.indexOf("}")+1);
		}
		String returnValue = null;
		//Processing for special markings#{sysOrgCode}，Judgment replacement
		if (key.contains(wellNumber)) {
			key = key.substring(2,key.indexOf("}"));
		} else {
			key = key;
		}
		//update-begin---author:chenrui ---date:20250107  for：[QQYUN-10785]Data permissions，查看自己拥有部门的权限中存exist问题 #7288------------
		// 是否存existstring标志
		boolean multiStr;
		if(oConvertUtils.isNotEmpty(key) && key.trim().matches("^\\[\\w+]$")){
			key = key.substring(1,key.length()-1);
			multiStr = true;
		} else {
            multiStr = false;
        }
		//update-end---author:chenrui ---date:20250107  for：[QQYUN-10785]Data permissions，查看自己拥有部门的权限中存exist问题 #7288------------
		//Replace with current system time(year month day)
		if (key.equals(DataBaseConstant.SYS_DATE)|| key.toLowerCase().equals(DataBaseConstant.SYS_DATE_TABLE)) {
			returnValue = DateUtils.formatDate();
		}
		//Replace with current system time（year month day时分秒）
		else if (key.equals(DataBaseConstant.SYS_TIME)|| key.toLowerCase().equals(DataBaseConstant.SYS_TIME_TABLE)) {
			returnValue = DateUtils.now();
		}
		//Process status default value（Not initiated by default）
		else if (key.equals(DataBaseConstant.BPM_STATUS)|| key.toLowerCase().equals(DataBaseConstant.BPM_STATUS_TABLE)) {
			returnValue = "1";
		}

		//后台任务获取用户information异常，Cause program interruption
		if(sysUser==null && user==null){
			return null;
		}
		
		//Replace with system login user account
		if (key.equals(DataBaseConstant.SYS_USER_CODE)|| key.toLowerCase().equals(DataBaseConstant.SYS_USER_CODE_TABLE)) {
			if(user==null) {
				returnValue = sysUser.getUsername();
			}else {
				returnValue = user.getSysUserCode();
			}
		}

		// Replace with system login userID
		else if (key.equals(DataBaseConstant.SYS_USER_ID) || key.equalsIgnoreCase(DataBaseConstant.SYS_USER_ID_TABLE)) {
			if(user==null) {
				returnValue = sysUser.getId();
			}else {
				returnValue = user.getSysUserId();
			}
		}

		//Replace with system login user真实名字
		else if (key.equals(DataBaseConstant.SYS_USER_NAME)|| key.toLowerCase().equals(DataBaseConstant.SYS_USER_NAME_TABLE)) {
			if(user==null) {
				returnValue = sysUser.getRealname();
			}else {
				returnValue = user.getSysUserName();
			}
		}
		
		//替换为系统用户登录所use的机构编码
		else if (key.equals(DataBaseConstant.SYS_ORG_CODE)|| key.toLowerCase().equals(DataBaseConstant.SYS_ORG_CODE_TABLE)) {
			if(user==null) {
				returnValue = sysUser.getOrgCode();
			}else {
				returnValue = user.getSysOrgCode();
			}
		}

		// 替换为系统用户登录所use的机构ID
		else if (key.equals(DataBaseConstant.SYS_ORG_ID) || key.equalsIgnoreCase(DataBaseConstant.SYS_ORG_ID_TABLE)) {
			if (user == null) {
				returnValue = sysUser.getOrgId();
			} else {
				returnValue = user.getSysOrgId();
			}
		}

		//Replaced with all institution codes owned by system users
		else if (key.equals(DataBaseConstant.SYS_MULTI_ORG_CODE)|| key.toLowerCase().equals(DataBaseConstant.SYS_MULTI_ORG_CODE_TABLE)) {
			if(user==null){
				//TODO 暂时use用户登录部门，存exist逻辑缺陷，Not a department owned by the user
				returnValue = sysUser.getOrgCode();
				//update-begin---author:chenrui ---date:20250107  for：[QQYUN-10785]Data permissions，查看自己拥有部门的权限中存exist问题 #7288------------
				returnValue = multiStr ? "'" + returnValue + "'" : returnValue;
				//update-end---author:chenrui ---date:20250107  for：[QQYUN-10785]Data permissions，查看自己拥有部门的权限中存exist问题 #7288------------
			}else{
				if(user.isOneDepart()) {
					returnValue = user.getSysMultiOrgCode().get(0);
					//update-begin---author:chenrui ---date:20250107  for：[QQYUN-10785]Data permissions，查看自己拥有部门的权限中存exist问题 #7288------------
					returnValue = multiStr ? "'" + returnValue + "'" : returnValue;
					//update-end---author:chenrui ---date:20250107  for：[QQYUN-10785]Data permissions，查看自己拥有部门的权限中存exist问题 #7288------------
				}else {
					//update-begin---author:chenrui ---date:20250107  for：[QQYUN-10785]Data permissions，查看自己拥有部门的权限中存exist问题 #7288------------
					returnValue = user.getSysMultiOrgCode().stream()
							.filter(Objects::nonNull)
							//update-begin---author:chenrui ---date:20250224  for：[issues/7288]Data permissions，查看自己拥有部门的权限中存exist问题 #7288------------
							.map(orgCode -> {
								if (multiStr) {
									return "'" + orgCode + "'";
								} else {
									return orgCode;
								}
							})
							//update-end---author:chenrui ---date:20250224  for：[issues/7288]Data permissions，查看自己拥有部门的权限中存exist问题 #7288------------
							.collect(Collectors.joining(", "));
					//update-end---author:chenrui ---date:20250107  for：[QQYUN-10785]Data permissions，查看自己拥有部门的权限中存exist问题 #7288------------
				}
			}
		}

		// Replace with the role of the currently logged in usercode（Multiple commas separated）
		else if (key.equals(DataBaseConstant.SYS_ROLE_CODE) || key.equalsIgnoreCase(DataBaseConstant.SYS_ROLE_CODE_TABLE)) {
			if (user == null) {
				returnValue = sysUser.getRoleCode();
			} else {
				returnValue = user.getSysRoleCode();
			}
		}

		//update-begin-author:taoyan date:20210330 for:multi-tenantIDas system variable
		else if (key.equals(TenantConstant.TENANT_ID) || key.toLowerCase().equals(TenantConstant.TENANT_ID_TABLE)){
			try {
				returnValue = SpringContextUtils.getHttpServletRequest().getHeader(CommonConstant.TENANT_ID);
			} catch (Exception e) {
				log.warn("Get system tenant exception：" + e.getMessage());
			}
		}
		//update-end-author:taoyan date:20210330 for:multi-tenantIDas system variable
		if(returnValue!=null){returnValue = returnValue + moshi;}
		return returnValue;
	}
	
//	public static void main(String[] args) {
//		 String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE1NjUzMzY1MTMsInVzZXJuYW1lIjoiYWRtaW4ifQ.xjhud_tWCNYBOg_aRlMgOdlZoWFFKB_givNElHNw3X0";
//		 System.out.println(JwtUtil.getUsername(token));
//	}
}
