package org.jeecg.modules.system.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xkcoding.justauth.AuthRequestFactory;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.AuthStateUtils;
import me.zhyd.oauth.utils.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.enums.MessageTypeEnum;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.util.*;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysThirdAccount;
import org.jeecg.modules.system.entity.SysThirdAppConfig;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.model.ThirdLoginModel;
import org.jeecg.modules.system.service.ISysDictService;
import org.jeecg.modules.system.service.ISysThirdAccountService;
import org.jeecg.modules.system.service.ISysThirdAppConfigService;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.service.impl.ThirdAppDingtalkServiceImpl;
import org.jeecg.modules.system.service.impl.ThirdAppWechatEnterpriseServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

/**
 * @Author scott
 * @since 2018-12-17
 */
@Controller
@RequestMapping("/sys/thirdLogin")
@Slf4j
public class ThirdLoginController {
	@Autowired
	private ISysUserService sysUserService;
	@Autowired
	private ISysThirdAccountService sysThirdAccountService;
	@Autowired
	private ISysDictService sysDictService;
	@Autowired
	private BaseCommonService baseCommonService;
	@Autowired
    private RedisUtil redisUtil;
	@Autowired
	private AuthRequestFactory factory;
	@Autowired
	private ISysDepartService sysDepartService;

	@Autowired
	private ThirdAppWechatEnterpriseServiceImpl thirdAppWechatEnterpriseService;
	@Autowired
	private ThirdAppDingtalkServiceImpl thirdAppDingtalkService;

	@Autowired
	private ISysThirdAppConfigService appConfigService;

	@Autowired
	public ISysBaseAPI sysBaseAPI;

	@RequestMapping("/render/{source}")
    public void render(@PathVariable("source") String source, HttpServletResponse response) throws IOException {
        log.info("Third party loginrender：" + source);
        AuthRequest authRequest = factory.get(source);
        String authorizeUrl = authRequest.authorize(AuthStateUtils.createState());
        log.info("Third-party login authentication address：" + authorizeUrl);
        response.sendRedirect(authorizeUrl);
    }

	@RequestMapping("/{source}/callback")
    public String loginThird(@PathVariable("source") String source, AuthCallback callback,ModelMap modelMap) {
		log.info("Third party logincallback：" + source + " params：" + JSONObject.toJSONString(callback));
        AuthRequest authRequest = factory.get(source);
        AuthResponse response = authRequest.login(callback);
        log.info(JSONObject.toJSONString(response));
        Result<JSONObject> result = new Result<JSONObject>();
        if(response.getCode()==2000) {

        	JSONObject data = JSONObject.parseObject(JSONObject.toJSONString(response.getData()));
        	String username = data.getString("username");
        	String avatar = data.getString("avatar");
        	String uuid = data.getString("uuid");
        	//Construct third-party login information storage object
			ThirdLoginModel tlm = new ThirdLoginModel(source, uuid, username, avatar);
        	//Determine whether there is this person
			//update-begin-author:wangshuai date:20201118 for:Modify to query third-party account table
        	LambdaQueryWrapper<SysThirdAccount> query = new LambdaQueryWrapper<SysThirdAccount>();
        	query.eq(SysThirdAccount::getThirdType, source);
			//update-begin---author:wangshuai---date:2023-10-07---for:【QQYUN-6667】Knock knock cloud，I keep getting this prompt when unbinding and rebinding online.---
        	query.eq(SysThirdAccount::getTenantId, CommonConstant.TENANT_ID_DEFAULT_VALUE);
			//update-end---author:wangshuai---date:2023-10-07---for:【QQYUN-6667】Knock knock cloud，I keep getting this prompt when unbinding and rebinding online.---
			query.and(q -> q.eq(SysThirdAccount::getThirdUserUuid, uuid).or().eq(SysThirdAccount::getThirdUserId, uuid));
        	List<SysThirdAccount> thridList = sysThirdAccountService.list(query);
			SysThirdAccount user = null;
        	if(thridList==null || thridList.size()==0) {
				//Otherwise, create a new account directly
				user = sysThirdAccountService.saveThirdUser(tlm,CommonConstant.TENANT_ID_DEFAULT_VALUE);
        	}else {
        		//Already exists Just set username Do not set avatar
        		user = thridList.get(0);
        	}
        	// generatetoken
			//update-begin-author:wangshuai date:20201118 for:Query whether a user exists from third-party loginid，There is no mobile phone number bound
			if(oConvertUtils.isNotEmpty(user.getSysUserId())) {
				String sysUserId = user.getSysUserId();
				SysUser sysUser = sysUserService.getById(sysUserId);
				String token = saveToken(sysUser);
    			modelMap.addAttribute("token", token);
			}else{
				modelMap.addAttribute("token", "Bind mobile phone number,"+""+uuid);
			}
			//update-end-author:wangshuai date:20201118 for:Query whether a user exists from third-party loginid，There is no mobile phone number bound
		//update-begin--Author:wangshuai  Date:20200729 for：The interface returns a failed identification code when signature verification fails. issues#1441--------------------
        }else{
			modelMap.addAttribute("token", "Login failed");
		}
		//update-end--Author:wangshuai  Date:20200729 for：The interface returns a failed identification code when signature verification fails. issues#1441--------------------
        result.setSuccess(false);
        result.setMessage("Third-party login exception,Please contact the administrator");
        return "thirdLogin";
    }

	/**
	 * Create new account
	 * @param model
	 * @return
	 */
	@PostMapping("/user/create")
	@ResponseBody
	public Result<String> thirdUserCreate(@RequestBody ThirdLoginModel model) {
		log.info("third partyLog inCreate new account：" );
		Result<String> res = new Result<>();
		Object operateCode = redisUtil.get(CommonConstant.THIRD_LOGIN_CODE);
		if(operateCode==null || !operateCode.toString().equals(model.getOperateCode())){
			res.setSuccess(false);
			res.setMessage("Verification failed");
			return res;
		}
		//Create new account
		//update-begin-author:wangshuai date:20201118 for:Modified to check from third-party loginuser_id，When querying the user table, dotoken
		SysThirdAccount user = sysThirdAccountService.saveThirdUser(model,CommonConstant.TENANT_ID_DEFAULT_VALUE);
		if(oConvertUtils.isNotEmpty(user.getSysUserId())){
			String sysUserId = user.getSysUserId();
			SysUser sysUser = sysUserService.getById(sysUserId);
			// generatetoken
			String token = saveToken(sysUser);
			//update-end-author:wangshuai date:20201118 for:Modified to check from third-party loginuser_id，When querying the user table, dotoken
			res.setResult(token);
			res.setSuccess(true);
		}
		return res;
	}

	/**
	 * Bind account Need to set password Need to go through verification
	 * @param json
	 * @return
	 */
	@PostMapping("/user/checkPassword")
	@ResponseBody
	public Result<String> checkPassword(@RequestBody JSONObject json) {
		Result<String> result = new Result<>();
		Object operateCode = redisUtil.get(CommonConstant.THIRD_LOGIN_CODE);
		if(operateCode==null || !operateCode.toString().equals(json.getString("operateCode"))){
			result.setSuccess(false);
			result.setMessage("Verification failed");
			return result;
		}
		String username = json.getString("uuid");
		SysUser user = this.sysUserService.getUserByName(username);
		if(user==null){
			result.setMessage("User not found");
			result.setSuccess(false);
			return result;
		}
		String password = json.getString("password");
		String salt = user.getSalt();
		String passwordEncode = PasswordUtil.encrypt(user.getUsername(), password, salt);
		if(!passwordEncode.equals(user.getPassword())){
			result.setMessage("Password is incorrect");
			result.setSuccess(false);
			return result;
		}

		sysUserService.updateById(user);
		result.setSuccess(true);
		// generatetoken
		String token = saveToken(user);
		result.setResult(token);
		return result;
	}

	private String saveToken(SysUser user) {
		// generatetoken
		String token = JwtUtil.sign(user.getUsername(), user.getPassword());
		redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + token, token);
		// Set timeout
		redisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + token, JwtUtil.EXPIRE_TIME * 2 / 1000);
		return token;
	}

	/**
	 * Third-party login callback interface
	 * @param token
	 * @param thirdType
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/getLoginUser/{token}/{thirdType}/{tenantId}", method = RequestMethod.GET)
	@ResponseBody
	public Result<JSONObject> getThirdLoginUser(@PathVariable("token") String token,@PathVariable("thirdType") String thirdType,@PathVariable("tenantId") String tenantId) throws Exception {
		Result<JSONObject> result = new Result<JSONObject>();
		String username = JwtUtil.getUsername(token);
		//update-begin---author:chenrui ---date:20250210  for：[QQYUN-11021]Three-party login interface passedtokenObtain user information vulnerability fix------------
		if (!TokenUtils.verifyToken(token, sysBaseAPI, redisUtil)) {
			return Result.noauth("tokenAuthentication failed");
		}
		//update-end---author:chenrui ---date:20250210  for：[QQYUN-11021]Three-party login interface passedtokenObtain user information vulnerability fix------------
		//1. Verify whether the user is valid
		SysUser sysUser = sysUserService.getUserByName(username);
		result = sysUserService.checkUserIsEffective(sysUser);
		if(!result.isSuccess()) {
			return result;
		}
		//update-begin-author:wangshuai date:20201118 for:If the real name and avatar do not exist, the third-party login will be used.
		LambdaQueryWrapper<SysThirdAccount> query = new LambdaQueryWrapper<>();
		query.eq(SysThirdAccount::getSysUserId,sysUser.getId());
		query.eq(SysThirdAccount::getThirdType,thirdType);
		query.eq(SysThirdAccount::getTenantId,oConvertUtils.getInt(tenantId,CommonConstant.TENANT_ID_DEFAULT_VALUE));
		//update-begin---author:wangshuai ---date:20230328  for：[QQYUN-4883]DingTalkauthLog in to the same tenant and have the same userid------------
		List<SysThirdAccount> accountList = sysThirdAccountService.list(query);
		SysThirdAccount account = new SysThirdAccount();
		if(CollectionUtil.isNotEmpty(accountList)){
			account = accountList.get(0);
		}
		//update-end---author:wangshuai ---date:20230328  for：[QQYUN-4883]DingTalkauthLog in to the same tenant and have the same userid------------
		if(oConvertUtils.isEmpty(sysUser.getRealname())){
			sysUser.setRealname(account.getRealname());
		}
		if(oConvertUtils.isEmpty(sysUser.getAvatar())){
			sysUser.setAvatar(account.getAvatar());
		}
		//update-end-author:wangshuai date:20201118 for:If the real name and avatar do not exist, the third-party login will be used.
		JSONObject obj = new JSONObject();
		//Third-party login determines login tenant and department logic
		this.setUserTenantAndDepart(sysUser,obj,result);		
		//User login information
		obj.put("userInfo", sysUser);
		//Get dictionary cache【solve #jeecg-boot/issues/3998】
		obj.put("sysAllDictItems", sysDictService.queryAllDictItems());
		//token information
		obj.put("token", token);
		result.setResult(obj);
		result.setSuccess(true);
		result.setCode(200);
		baseCommonService.addLog("username: " + username + ",Login successful[third party users]！", CommonConstant.LOG_TYPE_1, null);
		return result;
	}
	/**
	 * third partyBind mobile phone number返回token
	 *
	 * @param jsonObject
	 * @return
	 */
	@Operation(summary="Mobile phone number login interface")
	@PostMapping("/bindingThirdPhone")
	@ResponseBody
	public Result<String> bindingThirdPhone(@RequestBody JSONObject jsonObject) {
		Result<String> result = new Result<String>();
		String phone = jsonObject.getString("mobile");
		String thirdUserUuid = jsonObject.getString("thirdUserUuid");
		// Verify verification code
		String captcha = jsonObject.getString("captcha");
		//update-begin-author:taoyan date:2022-9-13 for: VUEN-2245 【loopholes】发现新loopholes待处理20220906
		String redisKey = CommonConstant.PHONE_REDIS_KEY_PRE+phone;
		Object captchaCache = redisUtil.get(redisKey);
		//update-end-author:taoyan date:2022-9-13 for: VUEN-2245 【loopholes】发现新loopholes待处理20220906
		if (oConvertUtils.isEmpty(captcha) || !captcha.equals(captchaCache)) {
			result.setMessage("Verification code error");
			result.setSuccess(false);
			return result;
		}
		//Verify user validity
		SysUser sysUser = sysUserService.getUserByPhone(phone);
		if(sysUser != null){
			// User exists，direct binding
			sysThirdAccountService.updateThirdUserId(sysUser,thirdUserUuid);
		}else{
			// No mobile phone number exists，Create user
			sysUser = sysThirdAccountService.createUser(phone,thirdUserUuid,CommonConstant.TENANT_ID_DEFAULT_VALUE);
		}
		String token = saveToken(sysUser);
		result.setSuccess(true);
		result.setResult(token);
		return result;
	}

	/**
	 * Enterprise WeChat/DingTalk OAuth2Log in
	 *
	 * @param source
	 * @param state
	 * @return
	 */
	@ResponseBody
	@GetMapping("/oauth2/{source}/login")
	public String oauth2LoginCallback(@PathVariable("source") String source, @RequestParam("state") String state, HttpServletRequest request, HttpServletResponse response,
									  @RequestParam(value = "tenantId",required = false,defaultValue = "0") String tenantId) throws Exception {
		String url;
		//applicationidis empty，Description is not configuredlowAppId
		if(oConvertUtils.isEmpty(tenantId)){
			return "Tenant encoding is not configured";
		}
		if (CommonConstant.WECHAT_ENTERPRISE.equalsIgnoreCase(source)) {
			//Change to a third partyappconfiguration table
			SysThirdAppConfig config = appConfigService.getThirdConfigByThirdType(Integer.valueOf(tenantId), MessageTypeEnum.QYWX.getType());
			if(null == config){
				return "还未配置Enterprise WeChatapplication，请配置Enterprise WeChatapplication";
			}
			StringBuilder builder = new StringBuilder();
			// 构造Enterprise WeChatOAuth2Log in授权地址
			builder.append("https://open.weixin.qq.com/connect/oauth2/authorize");
			// corporateCorpID
			builder.append("?appid=").append(config.getClientId());
			// Callback link address redirected after authorization，Please useurlencodeProcess the link
			String redirectUri = CommonUtils.getBaseUrl(request)  + "/sys/thirdLogin/oauth2/wechat_enterprise/callback?tenantId="+tenantId;;
			builder.append("&redirect_uri=").append(URLEncoder.encode(redirectUri, "UTF-8"));
			// Return type，At this time it is fixed to：code
			builder.append("&response_type=code");
			// application授权作用域。
			// snsapi_base：Silent authorization，可获取成员的的基础information（UserIdandDeviceId）；
			builder.append("&scope=snsapi_base");
			// Will be brought after redirectionstateparameter，The length cannot exceed128bytes
			builder.append("&state=").append(state);
			// 终端使用此parameter判断是否需要带上身份information
			builder.append("#wechat_redirect");
			url = builder.toString();
		} else if (CommonConstant.DINGTALK.equalsIgnoreCase(source)) {
			//update-begin---author:wangshuai ---date:20230224  for：[QQYUN-3440]新建Enterprise WeChat和DingTalkconfiguration table，Isolation via tenant mode------------
			//Change to a third partyappconfiguration table
			SysThirdAppConfig appConfig = appConfigService.getThirdConfigByThirdType(Integer.valueOf(tenantId), MessageTypeEnum.DD.getType());
			if(null == appConfig){
				return "还未配置DingTalkapplication，请配置DingTalkapplication";
			}
			//update-end---author:wangshuai ---date:20230224  for：[QQYUN-3440]新建Enterprise WeChat和DingTalkconfiguration table，Isolation via tenant mode------------
			StringBuilder builder = new StringBuilder();
			// 构造DingTalkOAuth2Log in授权地址
			builder.append("https://login.dingtalk.com/oauth2/auth");
			// Authorization passed/Callback address after rejection。
			// Notice 需要and注册application时登记的域名保持一致。
			String redirectUri = CommonUtils.getBaseUrl(request) + "/sys/thirdLogin/oauth2/dingtalk/callback?tenantId="+tenantId;
			builder.append("?redirect_uri=").append(URLEncoder.encode(redirectUri, "UTF-8"));
			// The fixed value iscode。
			// Authorization passed后返回authCode。
			builder.append("&response_type=code");
			// 步骤一中创建的application详情中获取。
			// 企业内部application：client_id为application的AppKey。
			builder.append("&client_id=").append(appConfig.getClientId());
			// Authorization scope，授权页面显示的授权information以application注册时配置的为准。
			// openid：Users can be obtained after authorizationuserid
			builder.append("&scope=openid");
			// followauthCodeReturn as is。
			builder.append("&state=").append(state);
            //update-begin---author:wangshuai ---date:20220613  for：[issues/I5BOUF]oauth2 DingTalk无法Log in------------
            builder.append("&prompt=").append("consent");
            //update-end---author:wangshuai ---date:20220613  for：[issues/I5BOUF]oauth2 DingTalk无法Log in--------------
            url = builder.toString();
		} else {
			return "Not supportedsource";
		}
		log.info("oauth2 login url:" + url);
		response.sendRedirect(url);
		return "login…";
	}

    /**
     * Enterprise WeChat/DingTalk OAuth2Log in回调
     *
     * @param code
     * @param state
     * @param response
     * @return
     */
	@ResponseBody
	@GetMapping("/oauth2/{source}/callback")
	public String oauth2LoginCallback(
			@PathVariable("source") String source,
			// Enterprise WeChat返回的code
			@RequestParam(value = "code", required = false) String code,
			// DingTalk返回的code
			@RequestParam(value = "authCode", required = false) String authCode,
			@RequestParam("state") String state,
			@RequestParam(name = "tenantId",defaultValue = "0") String tenantId,
			HttpServletResponse response) {
        SysUser loginUser;
        if (CommonConstant.WECHAT_ENTERPRISE.equalsIgnoreCase(source)) {
            log.info("【Enterprise WeChat】OAuth2Log in进入callback：code=" + code + ", state=" + state);
            loginUser = thirdAppWechatEnterpriseService.oauth2Login(code,Integer.valueOf(tenantId));
            if (loginUser == null) {
                return "Login failed";
            }
        } else if (CommonConstant.DINGTALK.equalsIgnoreCase(source)) {
			log.info("【DingTalk】OAuth2Log in进入callback：authCode=" + authCode + ", state=" + state);
			loginUser = thirdAppDingtalkService.oauth2Login(authCode,Integer.valueOf(tenantId));
			if (loginUser == null) {
				return "Login failed";
			}
        } else {
            return "Not supportedsource";
        }
        try {

			//update-begin-author:taoyan date:2022-6-30 for: Workflow sends message Click the message link to jump to the processing page
			String redirect = "";
			if (state.indexOf("?") > 0) {
				String[] arr = state.split("\\?");
				state = arr[0];
				if(arr.length>1){
					redirect = arr[1];
				}
			}

			String token = saveToken(loginUser);
			state += "/oauth2-app/login?oauth2LoginToken=" + URLEncoder.encode(token, "UTF-8") + "&tenantId=" + URLEncoder.encode(tenantId, "UTF-8");
			//update-begin---author:wangshuai ---date:20220613  for：[issues/I5BOUF]oauth2 DingTalk无法Log in------------
			state += "&thirdType=" + source;
			//state += "&thirdType=" + "wechat_enterprise";
			if (redirect != null && redirect.length() > 0) {
				state += "&" + redirect;
			}
			//update-end-author:taoyan date:2022-6-30 for: Workflow sends message Click the message link to jump to the processing page

            //update-end---author:wangshuai ---date:20220613  for：[issues/I5BOUF]oauth2 DingTalk无法Log in------------
			log.info("OAuth2Log in重定向地址: " + state);
            try {
                response.sendRedirect(state);
                return "ok";
            } catch (IOException e) {
                e.printStackTrace();
                return "Redirect failed";
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "Decoding failed";
        }
    }

	/**
	 * Register an account and bind a third-party account 【低代码application专用接口】
	 * @param jsonObject
	 * @param user
	 * @return
	 */
	@ResponseBody
	@PutMapping("/registerBindThirdAccount")
	public Result<String> registerBindThirdAccount(@RequestBody JSONObject jsonObject, SysUser user) {
		//Phone number
		String phone = jsonObject.getString("phone");
		//Verification code
		String smscode = jsonObject.getString("smscode");
		String redisKey = CommonConstant.PHONE_REDIS_KEY_PRE + phone;
		Object code = redisUtil.get(redisKey);
		//third partyuuid
		String thirdUserUuid = jsonObject.getString("thirdUserUuid");
		String username = jsonObject.getString("username");
		//未设置username，则用Phone number作为username
		if (oConvertUtils.isEmpty(username)) {
			username = phone;
		}
		//No password set，则随机generate一个密码
		String password = jsonObject.getString("password");
		if (oConvertUtils.isEmpty(password)) {
			password = RandomUtil.randomString(8);
		}
		String email = jsonObject.getString("email");
		SysUser sysUser1 = sysUserService.getUserByName(username);
		if (sysUser1 != null) {
			return Result.error("username已注册");
		}
		SysUser sysUser2 = sysUserService.getUserByPhone(phone);
		if (sysUser2 != null) {
			return Result.error("该Phone number已注册");
		}
		if (oConvertUtils.isNotEmpty(email)) {
			SysUser sysUser3 = sysUserService.getUserByEmail(email);
			if (sysUser3 != null) {
				return Result.error("Email has been registered");
			}
		}
		if (null == code) {
			return Result.error("手机Verification code失效，Please get it again");
		}
		if (!smscode.equals(code.toString())) {
			return Result.error("手机Verification code error");
		}
		String realname = jsonObject.getString("realname");
		if (oConvertUtils.isEmpty(realname)) {
			realname = username;
		}
		try {
			//Save user table
			user.setCreateTime(new Date());
			String salt = oConvertUtils.randomGen(8);
			String passwordEncode = PasswordUtil.encrypt(username, password, salt);
			user.setSalt(salt);
			user.setUsername(username);
			user.setRealname(realname);
			user.setPassword(passwordEncode);
			user.setEmail(email);
			user.setPhone(phone);
			user.setStatus(CommonConstant.USER_UNFREEZE);
			user.setDelFlag(CommonConstant.DEL_FLAG_0);
			user.setActivitiSync(CommonConstant.ACT_SYNC_1);
			sysUserService.addUserWithRole(user, "");
			//保存third party users表
			sysThirdAccountService.updateThirdUserId(user, thirdUserUuid);
			String token = saveToken(user);
			return Result.ok(token);
		} catch (Exception e) {
			return Result.error("Registration failed");
		}
	}

	/**
	 * 设置用户租户和部门information
	 *
	 * @param sysUser
	 * @param obj
	 * @param result
	 */
	private void setUserTenantAndDepart(SysUser sysUser, JSONObject obj, Result<JSONObject> result) {
		//1.设置Log in租户
		sysUserService.setLoginTenant(sysUser, obj, sysUser.getUsername(), result);
		//2.设置Log in部门
		String orgCode = sysUser.getOrgCode();
		//部门不is empty还是用原来的部门code
		if(StringUtils.isEmpty(orgCode)){
			List<SysDepart> departs = sysDepartService.queryUserDeparts(sysUser.getId());
			//部门不is empty取第一个作为当前Log in部门
			if(CollectionUtil.isNotEmpty(departs)){
				orgCode = departs.get(0).getOrgCode();
				sysUser.setOrgCode(orgCode);
				this.sysUserService.updateUserDepart(sysUser.getUsername(), orgCode,null);
			}
		}
	}

	/**
	 * 新版DingTalkLog in
	 *
	 * @param authCode
	 * @param state
	 * @param tenantId
	 * @param response
	 * @return
	 */
	@ResponseBody
	@GetMapping("/oauth2/dingding/login")
	public String OauthDingDingLogin(@RequestParam(value = "authCode", required = false) String authCode,
									 @RequestParam("state") String state,
									 @RequestParam(name = "tenantId",defaultValue = "0") String tenantId,
									 HttpServletResponse response) {
		SysUser loginUser = thirdAppDingtalkService.oauthDingDingLogin(authCode,Integer.valueOf(tenantId));
		try {
			String redirect = "";
			if (state.indexOf("?") > 0) {
				String[] arr = state.split("\\?");
				state = arr[0];
				if(arr.length>1){
					redirect = arr[1];
				}
			}
			String token = saveToken(loginUser);
			state += "/oauth2-app/login?oauth2LoginToken=" + URLEncoder.encode(token, "UTF-8") + "&tenantId=" + URLEncoder.encode(tenantId, "UTF-8");
			state += "&thirdType=DINGTALK";
			if (redirect != null && redirect.length() > 0) {
				state += "&" + redirect;
			}
			log.info("OAuth2Log in重定向地址: " + state);
			try {
				response.sendRedirect(state);
				return "ok";
			} catch (IOException e) {
				log.error(e.getMessage(),e);
				return "Redirect failed";
			}
		} catch (UnsupportedEncodingException e) {
			log.error(e.getMessage(),e);
			return "Decoding failed";
		}
	}

	/**
	 * Get businessid和applicationid
	 * @param tenantId
	 * @return
	 */
	@ResponseBody
	@GetMapping("/get/corpId/clientId")
	public Result<SysThirdAppConfig> getCorpIdClientId(@RequestParam(value = "tenantId", defaultValue = "0") String tenantId){
		Result<SysThirdAppConfig> result = new Result<>();
		SysThirdAppConfig sysThirdAppConfig = thirdAppDingtalkService.getCorpIdClientId(Integer.valueOf(tenantId));
		result.setSuccess(true);
		result.setResult(sysThirdAppConfig);
		return result;
	}
}