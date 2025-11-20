package org.jeecg.modules.system.controller;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.exceptions.ClientException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.constant.enums.DySmsEnum;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.*;
import org.jeecg.common.util.encryption.EncryptedString;
import org.jeecg.config.JeecgBaseConfig;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysRoleIndex;
import org.jeecg.modules.system.entity.SysTenant;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.model.SysLoginModel;
import org.jeecg.modules.system.service.*;
import org.jeecg.modules.system.service.impl.SysBaseApiImpl;
import org.jeecg.modules.system.util.RandImageUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author scott
 * @since 2018-12-17
 */
@RestController
@RequestMapping("/sys")
@Tag(name="User login")
@Slf4j
public class LoginController {
	@Autowired
	private ISysUserService sysUserService;
	@Autowired
	private ISysPermissionService sysPermissionService;
	@Autowired
	private SysBaseApiImpl sysBaseApi;
	@Autowired
	private ISysLogService logService;
	@Autowired
    private RedisUtil redisUtil;
	@Autowired
    private ISysDepartService sysDepartService;
	@Autowired
    private ISysDictService sysDictService;
	@Resource
	private BaseCommonService baseCommonService;
	@Autowired
	private JeecgBaseConfig jeecgBaseConfig;
	private final String BASE_CHECK_CODES = "qwertyuiplkjhgfdsazxcvbnmQWERTYUPLKJHGFDSAZXCVBNM1234567890";
	/**
	 * Thread pool is used to send records asynchronously
	 */
	public static ExecutorService cachedThreadPool = new ShiroThreadPoolExecutor(0, 1024, 60L, TimeUnit.SECONDS, new SynchronousQueue<>());
	
	@Operation(summary="Login interface")
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public Result<JSONObject> login(@RequestBody SysLoginModel sysLoginModel, HttpServletRequest request){
		Result<JSONObject> result = new Result<JSONObject>();
		String username = sysLoginModel.getUsername();
		String password = sysLoginModel.getPassword();
		if(isLoginFailOvertimes(username)){
			return result.error500("该User login失败次数过多，Please at10Log in again in minutes！");
		}

		// step.1 Verification codecheck
        String captcha = sysLoginModel.getCaptcha();
        if(captcha==null){
            result.error500("Verification codeinvalid");
            return result;
        }
        String lowerCaseCaptcha = captcha.toLowerCase();
		// Add key as obfuscation，Avoid simple splicing，exploited by outsiders，The user can customize the key
		//update-begin---author:chenrui ---date:20250107  for：[QQYUN-10775]Verification code可以复用 #7674------------
		String keyPrefix = Md5Util.md5Encode(sysLoginModel.getCheckKey()+jeecgBaseConfig.getSignatureSecret(), "utf-8");
		String realKey = keyPrefix + lowerCaseCaptcha;
		//update-end---author:chenrui ---date:20250107  for：[QQYUN-10775]Verification code可以复用 #7674------------
		Object checkCode = redisUtil.get(realKey);
		//When entering the login page，有一定几率出现Verification code错误 #1714
		if(checkCode==null || !checkCode.toString().equals(lowerCaseCaptcha)) {
            log.warn("Verification code错误，key= {} , Ui checkCode= {}, Redis checkCode = {}", sysLoginModel.getCheckKey(), lowerCaseCaptcha, checkCode);
			result.error500("Verification code错误");
			// Change to specialcode Facilitates front-end judgment
			result.setCode(HttpStatus.PRECONDITION_FAILED.value());
			return result;
		}
		
		// step.2 Verify that the user exists and is valid
		LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(SysUser::getUsername,username);
		SysUser sysUser = sysUserService.getOne(queryWrapper);
		result = sysUserService.checkUserIsEffective(sysUser);
		if(!result.isSuccess()) {
			return result;
		}

		// step.3 Verify that the username or password is correct
		String userpassword = PasswordUtil.encrypt(username, password, sysUser.getSalt());
		String syspassword = sysUser.getPassword();
		if (!syspassword.equals(userpassword)) {
			addLoginFailOvertimes(username);
			result.error500("Wrong username or password");
			return result;
		}

		// step.4  Login successfully to obtain user information
		userInfo(sysUser, result, request);

		// step.5  Login successful删除Verification code
		redisUtil.del(realKey);
		redisUtil.del(CommonConstant.LOGIN_FAIL + username);

		// step.6  记录User login日志
		LoginUser loginUser = new LoginUser();
		BeanUtils.copyProperties(sysUser, loginUser);
		baseCommonService.addLog("username: " + username + ",Login successful！", CommonConstant.LOG_TYPE_1, null,loginUser);
		return result;
	}


	/**
	 * 【vue3dedicated】Get user information
	 */
	@GetMapping("/user/getUserInfo")
	public Result<JSONObject> getUserInfo(HttpServletRequest request){
		long start = System.currentTimeMillis();
		Result<JSONObject> result = new Result<JSONObject>();
		String  username = JwtUtil.getUserNameByToken(request);
		if(oConvertUtils.isNotEmpty(username)) {
			// 根据username查询User information
			SysUser sysUser = sysUserService.getUserByName(username);
			JSONObject obj=new JSONObject();
			log.info("1 Get user information耗时（User basic information）" + (System.currentTimeMillis() - start) + "millisecond");

			//update-begin---author:scott ---date:2022-06-20  for：vue3front end，Support customized home page-----------
			String vue3Version = request.getHeader(CommonConstant.VERSION);
			//update-begin---author:liusq ---date:2022-06-29  for：Interface return value modification，Synchronously modify the judgment logic here-----------
			SysRoleIndex roleIndex = sysUserService.getDynamicIndexByUserRole(username, vue3Version);
			if (oConvertUtils.isNotEmpty(vue3Version) && roleIndex != null && oConvertUtils.isNotEmpty(roleIndex.getUrl())) {
				String homePath = roleIndex.getUrl();
				if (!homePath.startsWith(SymbolConstant.SINGLE_SLASH)) {
					homePath = SymbolConstant.SINGLE_SLASH + homePath;
				}
				sysUser.setHomePath(homePath);
			}
			//update-begin---author:liusq ---date:2022-06-29  for：Interface return value modification，Synchronously modify the judgment logic here-----------
			//update-end---author:scott ---date::2022-06-20  for：vue3front end，Support customized home page--------------
			log.info("2 Get user information耗时 (Home page configuration)" + (System.currentTimeMillis() - start) + "millisecond");
			
			obj.put("userInfo",sysUser);
			obj.put("sysAllDictItems", sysDictService.queryAllDictItems());
			log.info("3 Get user information耗时 (dictionary data)" + (System.currentTimeMillis() - start) + "millisecond");
			
			result.setResult(obj);
			result.success("");
		}
		log.info("end Get user information耗时 " + (System.currentTimeMillis() - start) + "millisecond");
		return result;

	}
	
	/**
	 * Log out
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/logout")
	public Result<Object> logout(HttpServletRequest request,HttpServletResponse response) {
		//User exit logic
	    String token = request.getHeader(CommonConstant.X_ACCESS_TOKEN);
	    if(oConvertUtils.isEmpty(token)) {
	    	return Result.error("Log out失败！");
	    }
	    String username = JwtUtil.getUsername(token);
		LoginUser sysUser = sysBaseApi.getUserByName(username);
	    if(sysUser!=null) {
			asyncClearLogoutCache(token, sysUser); // Asynchronous cleanup
			SecurityUtils.getSubject().logout();
	    	return Result.ok("退出Login successful！");
	    }else {
	    	return Result.error("Tokeninvalid!");
	    }
	}

	/**
	 * Clear user cache
	 *
	 * @param token
	 * @param sysUser
	 */
	private void asyncClearLogoutCache(String token, LoginUser sysUser) {
		cachedThreadPool.execute(()->{
			//清空User loginTokencache
			redisUtil.del(CommonConstant.PREFIX_USER_TOKEN + token);
			//清空User loginShiro权限cache
			redisUtil.del(CommonConstant.PREFIX_USER_SHIRO_CACHE + sysUser.getId());
			//清空用户的cacheinformation（Include department information），For examplesys:cache:user::<username>
			redisUtil.del(String.format("%s::%s", CacheConstant.SYS_USERS_CACHE, sysUser.getUsername()));
			baseCommonService.addLog("username: "+sysUser.getRealname()+",Exit successfully！", CommonConstant.LOG_TYPE_1, null, sysUser);
			log.info("【Exit successfully操作】Asynchronous processing，After exiting，Clear user cache： "+sysUser.getRealname());
		});
	}
	
	
	/**
	 * Get visits
	 * @return
	 */
	@GetMapping("loginfo")
	public Result<JSONObject> loginfo() {
		Result<JSONObject> result = new Result<JSONObject>();
		JSONObject obj = new JSONObject();
		//update-begin--Author:zhangweijian  Date:20190428 for：Incoming start time，end time parameter
		// Get the start and end time of the day
		Calendar calendar = new GregorianCalendar();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date dayStart = calendar.getTime();
		calendar.add(Calendar.DATE, 1);
		Date dayEnd = calendar.getTime();
		// Get system access records
		Long totalVisitCount = logService.findTotalVisitCount();
		obj.put("totalVisitCount", totalVisitCount);
		Long todayVisitCount = logService.findTodayVisitCount(dayStart,dayEnd);
		obj.put("todayVisitCount", todayVisitCount);
		Long todayIp = logService.findTodayIp(dayStart,dayEnd);
		//update-end--Author:zhangweijian  Date:20190428 for：Incoming start time，end time parameter
		obj.put("todayIp", todayIp);
		result.setResult(obj);
		result.success("Login successful");
		return result;
	}
	
	/**
	 * Get visits
	 * @return
	 */
	@GetMapping("/visitInfo")
	public Result<List<Map<String,Object>>> visitInfo() {
		Result<List<Map<String,Object>>> result = new Result<List<Map<String,Object>>>();
		Calendar calendar = new GregorianCalendar();
		calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Date dayEnd = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        Date dayStart = calendar.getTime();
        List<Map<String,Object>> list = logService.findVisitCount(dayStart, dayEnd);
		result.setResult(oConvertUtils.toLowerCasePageList(list));
		return result;
	}
	
	
	/**
	 * Log in successfully and select the user’s current department.
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/selectDepart", method = RequestMethod.PUT)
	public Result<JSONObject> selectDepart(@RequestBody SysUser user) {
		Result<JSONObject> result = new Result<JSONObject>();
		String username = user.getUsername();
		if(oConvertUtils.isEmpty(username)) {
			LoginUser sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();
			username = sysUser.getUsername();
		}
		
		//Get login department
		String orgCode= user.getOrgCode();
		//Get login tenant
		Integer tenantId = user.getLoginTenantId();
		//set upUser login部门和Log in租户
		this.sysUserService.updateUserDepart(username, orgCode,tenantId);
		SysUser sysUser = sysUserService.getUserByName(username);
		JSONObject obj = new JSONObject();
		obj.put("userInfo", sysUser);
		result.setResult(obj);
		return result;
	}

	/**
	 * 短信Login interface
	 * 
	 * @param jsonObject
	 * @return
	 */
	@PostMapping(value = "/sms")
	public Result<String> sms(@RequestBody JSONObject jsonObject,HttpServletRequest request) {
		Result<String> result = new Result<String>();
		String clientIp = IpUtils.getIpAddr(request);
		String mobile = jsonObject.get("mobile").toString();
		//Mobile phone number pattern login mode: "2"  Registration mode: "1"
		String smsmode=jsonObject.get("smsmode").toString();
		log.info("-------- IP:{}, Phone number：{}，获取绑定Verification code", clientIp, mobile);
		
		if(oConvertUtils.isEmpty(mobile)){
			result.setMessage("Phone number不允许为空！");
			result.setSuccess(false);
			return result;
		}
		
		//update-begin-author:taoyan date:2022-9-13 for: VUEN-2245 【loopholes】发现新loopholes待处理20220906
		String redisKey = CommonConstant.PHONE_REDIS_KEY_PRE+mobile;
		Object object = redisUtil.get(redisKey);
		//update-end-author:taoyan date:2022-9-13 for: VUEN-2245 【loopholes】发现新loopholes待处理20220906
		
		if (object != null) {
			result.setMessage("Verification code10within minutes，still valid！");
			result.setSuccess(false);
			return result;
		}

		//-------------------------------------------------------------------------------------
		//Increase checkPrevent malicious brushing of SMS interfaces
		if(!DySmsLimit.canSendSms(clientIp)){
			log.warn("--------[warn] IPaddress:{}, Too many SMS interface requests-------", clientIp);
			result.setMessage("Too many SMS interface requests，Please try again later！");
			result.setCode(CommonConstant.PHONE_SMS_FAIL_CODE);
			result.setSuccess(false);
			return result;
		}
		//-------------------------------------------------------------------------------------

		//random number
		String captcha = RandomUtil.randomNumbers(6);
		JSONObject obj = new JSONObject();
    	obj.put("code", captcha);
		try {
			boolean b = false;
			//Registration template
			if (CommonConstant.SMS_TPL_TYPE_1.equals(smsmode)) {
				SysUser sysUser = sysUserService.getUserByPhone(mobile);
				if(sysUser!=null) {
					result.error500(" Phone number已经注册，Please log in directly！");
					baseCommonService.addLog("Phone number已经注册，Please log in directly！", CommonConstant.LOG_TYPE_1, null);
					return result;
				}
				b = DySmsHelper.sendSms(mobile, obj, DySmsEnum.REGISTER_TEMPLATE_CODE);
			}else {
				//login mode，Verify user validity
				SysUser sysUser = sysUserService.getUserByPhone(mobile);
				result = sysUserService.checkUserIsEffective(sysUser);
				if(!result.isSuccess()) {
					String message = result.getMessage();
					String userNotExist="This user does not exist，Please register";
					if(userNotExist.equals(message)){
						result.error500("This user does not exist或未绑定Phone number");
					}
					return result;
				}
				
				/**
				 * smsmode SMS template method  0 .Login template、1.Registration template、2.Forgot password template
				 */
				if (CommonConstant.SMS_TPL_TYPE_0.equals(smsmode)) {
					//Login template
					b = DySmsHelper.sendSms(mobile, obj, DySmsEnum.LOGIN_TEMPLATE_CODE);
				} else if(CommonConstant.SMS_TPL_TYPE_2.equals(smsmode)) {
					//Forgot password template
					b = DySmsHelper.sendSms(mobile, obj, DySmsEnum.FORGET_PASSWORD_TEMPLATE_CODE);
                    //update-begin---author:wangshuai---date:2025-07-15---for:【issues/8567】serious：There is a horizontal override problem when changing passwords。---
                    if(b){
                        String username = sysUser.getUsername();
                        obj.put("username",username);
                        redisUtil.set(redisKey, obj.toJSONString(), 600);
                        result.setSuccess(true);
                        return result;
                    }
                    //update-end---author:wangshuai---date:2025-07-15---for:【issues/8567】serious：There is a horizontal override problem when changing passwords。---
                }
			}

			if (b == false) {
				result.setMessage("短信Verification code发送失败,Please try again later");
				result.setSuccess(false);
				return result;
			}
			
			//update-begin-author:taoyan date:2022-9-13 for: VUEN-2245 【loopholes】发现新loopholes待处理20220906
			//Verification code10within minutesefficient
			redisUtil.set(redisKey, captcha, 600);
			//update-end-author:taoyan date:2022-9-13 for: VUEN-2245 【loopholes】发现新loopholes待处理20220906
			
			//update-begin--Author:scott  Date:20190812 for：issues#391
			//result.setResult(captcha);
			//update-end--Author:scott  Date:20190812 for：issues#391
			result.setSuccess(true);

		} catch (ClientException e) {
			e.printStackTrace();
			result.error500(" SMS interface is not configured，Please contact the administrator！");
			return result;
		}
		return result;
	}
	

	/**
	 * Phone numberLogin interface
	 * 
	 * @param jsonObject
	 * @return
	 */
	@Operation(summary="Phone numberLogin interface")
	@PostMapping("/phoneLogin")
	public Result<JSONObject> phoneLogin(@RequestBody JSONObject jsonObject, HttpServletRequest request) {
		Result<JSONObject> result = new Result<JSONObject>();
		String phone = jsonObject.getString("mobile");
		//update-begin-author:taoyan date:2022-11-7 for: issues/4109 平台User login失败锁定用户
		if(isLoginFailOvertimes(phone)){
			return result.error500("该User login失败次数过多，Please at10Log in again in minutes！");
		}
		//update-end-author:taoyan date:2022-11-7 for: issues/4109 平台User login失败锁定用户
		//Verify user validity
		SysUser sysUser = sysUserService.getUserByPhone(phone);
		result = sysUserService.checkUserIsEffective(sysUser);
		if(!result.isSuccess()) {
			return result;
		}
		
		String smscode = jsonObject.getString("captcha");

		//update-begin-author:taoyan date:2022-9-13 for: VUEN-2245 【loopholes】发现新loopholes待处理20220906
		String redisKey = CommonConstant.PHONE_REDIS_KEY_PRE+phone;
		Object code = redisUtil.get(redisKey);
		//update-end-author:taoyan date:2022-9-13 for: VUEN-2245 【loopholes】发现新loopholes待处理20220906

		if (!smscode.equals(code)) {
			//update-begin-author:taoyan date:2022-11-7 for: issues/4109 平台User login失败锁定用户
			addLoginFailOvertimes(phone);
			//update-end-author:taoyan date:2022-11-7 for: issues/4109 平台User login失败锁定用户
			return Result.error("手机Verification code错误");
		}
		//User information
		userInfo(sysUser, result, request);
		//add log
		baseCommonService.addLog("username: " + sysUser.getUsername() + ",Login successful！", CommonConstant.LOG_TYPE_1, null);
        redisUtil.removeAll(redisKey);
		return result;
	}


	/**
	 * User information
	 *
	 * @param sysUser
	 * @param result
	 * @return
	 */
	private Result<JSONObject> userInfo(SysUser sysUser, Result<JSONObject> result, HttpServletRequest request) {
		String username = sysUser.getUsername();
		String syspassword = sysUser.getPassword();
		// Get user department information
		JSONObject obj = new JSONObject(new LinkedHashMap<>());

		//1.generatetoken
		String token = JwtUtil.sign(username, syspassword);
		// set uptokencacheefficient时间
		redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + token, token);
		redisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + token, JwtUtil.EXPIRE_TIME * 2 / 1000);
		obj.put("token", token);

		//2.set upLog in租户
		Result<JSONObject> loginTenantError = sysUserService.setLoginTenant(sysUser, obj, username,result);
		if (loginTenantError != null) {
			return loginTenantError;
		}

		//3.set upLog inUser information
		obj.put("userInfo", sysUser);
		
		//4.set upLog in部门
		List<SysDepart> departs = sysDepartService.queryUserDeparts(sysUser.getId());
		obj.put("departs", departs);
		if (departs == null || departs.size() == 0) {
			obj.put("multi_depart", 0);
		} else if (departs.size() == 1) {
			sysUserService.updateUserDepart(username, departs.get(0).getOrgCode(),null);
			obj.put("multi_depart", 1);
		} else {
			//Check whether there is currently a logged-in department
			// update-begin--Author:wangshuai Date:20200805 for：If the user selects a department，The database contains the last login department，Then take one and save it
			SysUser sysUserById = sysUserService.getById(sysUser.getId());
			if(oConvertUtils.isEmpty(sysUserById.getOrgCode())){
				sysUserService.updateUserDepart(username, departs.get(0).getOrgCode(),null);
			}
			// update-end--Author:wangshuai Date:20200805 for：If the user selects a department，The database contains the last login department，Then take one and save it
			obj.put("multi_depart", 2);
		}

		//update-begin---author:scott ---date:2024-01-05  for：【QQYUN-7802】front endexistLog in时加载了两次数据字典，It is recommended to optimize，Avoid performance problems that may occur when there are too many data dictionaries #956---
		// logininterface，existvue3front end下不加载dictionary data，vue2Download dictionary
		String vue3Version = request.getHeader(CommonConstant.VERSION);
		if(oConvertUtils.isEmpty(vue3Version)){
			obj.put("sysAllDictItems", sysDictService.queryAllDictItems());
		}
		//end-begin---author:scott ---date:2024-01-05  for：【QQYUN-7802】front endexistLog in时加载了两次数据字典，It is recommended to optimize，Avoid performance problems that may occur when there are too many data dictionaries #956---
		
		result.setResult(obj);
		result.success("Login successful");
		return result;
	}

	/**
	 * Get encrypted string
	 * @return
	 */
	@GetMapping(value = "/getEncryptedString")
	public Result<Map<String,String>> getEncryptedString(){
		Result<Map<String,String>> result = new Result<Map<String,String>>();
		Map<String,String> map = new HashMap(5);
		map.put("key", EncryptedString.key);
		map.put("iv",EncryptedString.iv);
		result.setResult(map);
		return result;
	}

	/**
	 * 后台generate图形Verification code ：efficient
	 * @param response
	 * @param key
	 */
	@Operation(summary="获取Verification code")
	@GetMapping(value = "/randomImage/{key}")
	public Result<String> randomImage(HttpServletResponse response,@PathVariable("key") String key){
		Result<String> res = new Result<String>();
		try {
			//generateVerification code
			String code = RandomUtil.randomString(BASE_CHECK_CODES,4);
			//deposit toredismiddle
			String lowerCaseCode = code.toLowerCase();
			
			//update-begin-author:taoyan date:2022-9-13 for: VUEN-2245 【loopholes】发现新loopholes待处理20220906
			// Add key as obfuscation，Avoid simple splicing，exploited by outsiders，The user can customize the key
			//update-begin---author:chenrui ---date:20250107  for：[QQYUN-10775]Verification code可以复用 #7674------------
			String keyPrefix = Md5Util.md5Encode(key+jeecgBaseConfig.getSignatureSecret(), "utf-8");
			String realKey = keyPrefix + lowerCaseCode;
			//update-end-author:taoyan date:2022-9-13 for: VUEN-2245 【loopholes】发现新loopholes待处理20220906
			redisUtil.removeAll(keyPrefix);
			//update-end---author:chenrui ---date:20250107  for：[QQYUN-10775]Verification code可以复用 #7674------------
			redisUtil.set(realKey, lowerCaseCode, 60);
			log.info("获取Verification code，Redis key = {}，checkCode = {}", realKey, code);
			//returnfront end
			String base64 = RandImageUtil.generate(code);
			res.setSuccess(true);
			res.setResult(base64);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			res.error500("获取Verification code失败,Check, pleaseredisConfiguration!");
			return res;
		}
		return res;
	}

	/**
	 * Switch menu table tovue3table
	 */
	@RequiresRoles({"admin"})
	@GetMapping(value = "/switchVue3Menu")
	public Result<String> switchVue3Menu(HttpServletResponse response) {
		Result<String> res = new Result<String>();	
		sysPermissionService.switchVue3Menu();
		return res;
	}
	
	/**
	 * appLog in
	 * @param sysLoginModel
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/mLogin", method = RequestMethod.POST)
	public Result<JSONObject> mLogin(@RequestBody SysLoginModel sysLoginModel) throws Exception {
		Result<JSONObject> result = new Result<JSONObject>();
		String username = sysLoginModel.getUsername();
		String password = sysLoginModel.getPassword();
		JSONObject obj = new JSONObject();
		
		//update-begin-author:taoyan date:2022-11-7 for: issues/4109 平台User login失败锁定用户
		if(isLoginFailOvertimes(username)){
			return result.error500("该User login失败次数过多，Please at10Log in again in minutes！");
		}
		//update-end-author:taoyan date:2022-11-7 for: issues/4109 平台User login失败锁定用户
		//1. 校验用户是否efficient
		SysUser sysUser = sysUserService.getUserByName(username);
		result = sysUserService.checkUserIsEffective(sysUser);
		if(!result.isSuccess()) {
			return result;
		}
		
		//2. Verify that the username or password is correct
		String userpassword = PasswordUtil.encrypt(username, password, sysUser.getSalt());
		String syspassword = sysUser.getPassword();
		if (!syspassword.equals(userpassword)) {
			//update-begin-author:taoyan date:2022-11-7 for: issues/4109 平台User login失败锁定用户
			addLoginFailOvertimes(username);
			//update-end-author:taoyan date:2022-11-7 for: issues/4109 平台User login失败锁定用户
			result.error500("Wrong username or password");
			return result;
		}
		
		//3.set upLog in部门
		String orgCode = sysUser.getOrgCode();
		if(oConvertUtils.isEmpty(orgCode)) {
			//If the current user has no selected department View department related information
			
			List<SysDepart> departs = sysDepartService.queryUserDeparts(sysUser.getId());
			//update-begin-author:taoyan date:20220117 for: JTC-1068【app】Create new user，没有set up部门及角色，点击Log in提示暂未归属部，一直existLog in页面 使用Phone numberLog in Can be normal
			if (departs == null || departs.size() == 0) {
				/*result.error500("The user does not belong to the department yet,不可Log in!");
				
				return result;*/
			}else{
				orgCode = departs.get(0).getOrgCode();
				sysUser.setOrgCode(orgCode);
				this.sysUserService.updateUserDepart(username, orgCode,null);
			}
			//update-end-author:taoyan date:20220117 for: JTC-1068【app】Create new user，没有set up部门及角色，点击Log in提示暂未归属部，一直existLog in页面 使用Phone numberLog in Can be normal
		}

		//4. set upLog in租户
		Result<JSONObject> loginTenantError = sysUserService.setLoginTenant(sysUser, obj, username, result);
		if (loginTenantError != null) {
			return loginTenantError;
		}

		//5. set upLog inUser information
		obj.put("userInfo", sysUser);
		
		//6. generatetoken
		String token = JwtUtil.sign(username, syspassword);
		// set up超时时间
		redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + token, token);
		redisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + token, JwtUtil.EXPIRE_TIME*2 / 1000);

		//token information
		obj.put("token", token);
		result.setResult(obj);
		result.setSuccess(true);
		result.setCode(200);
		baseCommonService.addLog("username: " + username + ",Login successful[Mobile terminal]！", CommonConstant.LOG_TYPE_1, null);
		return result;
	}

	/**
	 * 图形Verification code
	 * @param sysLoginModel
	 * @return
	 */
	@RequestMapping(value = "/checkCaptcha", method = RequestMethod.POST)
	public Result<?> checkCaptcha(@RequestBody SysLoginModel sysLoginModel){
		String captcha = sysLoginModel.getCaptcha();
		String checkKey = sysLoginModel.getCheckKey();
		if(captcha==null){
			return Result.error("Verification codeinvalid");
		}
		String lowerCaseCaptcha = captcha.toLowerCase();
		String realKey = Md5Util.md5Encode(lowerCaseCaptcha+checkKey, "utf-8");
		Object checkCode = redisUtil.get(realKey);
		if(checkCode==null || !checkCode.equals(lowerCaseCaptcha)) {
			return Result.error("Verification code错误");
		}
		return Result.ok();
	}
	/**
	 * Log in二维码
	 */
	@Operation(summary = "Log in二维码")
	@GetMapping("/getLoginQrcode")
	public Result<?>  getLoginQrcode() {
		String qrcodeId = CommonConstant.LOGIN_QRCODE_PRE+IdWorker.getIdStr();
		//Define QR code parameters
		Map params = new HashMap(5);
		params.put("qrcodeId", qrcodeId);
		//Store the unique identifier of the QR code30秒efficient
		redisUtil.set(CommonConstant.LOGIN_QRCODE + qrcodeId, qrcodeId, 30);
		return Result.OK(params);
	}
	/**
	 * Scan the QR code
	 */
	@Operation(summary = "扫码Log in二维码")
	@PostMapping("/scanLoginQrcode")
	public Result<?> scanLoginQrcode(@RequestParam String qrcodeId, @RequestParam String token) {
		Object check = redisUtil.get(CommonConstant.LOGIN_QRCODE + qrcodeId);
		if (oConvertUtils.isNotEmpty(check)) {
			//storetokenRead to the front desk
			redisUtil.set(CommonConstant.LOGIN_QRCODE_TOKEN+qrcodeId, token, 60);
		} else {
			return Result.error("QR code has expired,Please refresh and try again");
		}
		return Result.OK("Scan code successfully");
	}


	/**
	 * Get the code saved by the user after scanning the codetoken
	 */
	@Operation(summary = "Get the code saved by the user after scanning the codetoken")
	@GetMapping("/getQrcodeToken")
	public Result getQrcodeToken(@RequestParam String qrcodeId) {
		Object token = redisUtil.get(CommonConstant.LOGIN_QRCODE_TOKEN + qrcodeId);
		Map result = new HashMap(5);
		Object qrcodeIdExpire = redisUtil.get(CommonConstant.LOGIN_QRCODE + qrcodeId);
		if (oConvertUtils.isEmpty(qrcodeIdExpire)) {
			//QR code expiration notification front desk refresh
			result.put("token", "-2");
			return Result.OK(result);
		}
		if (oConvertUtils.isNotEmpty(token)) {
			result.put("success", true);
			result.put("token", token);
		} else {
			result.put("token", "-1");
		}
		return Result.OK(result);
	}

	/**
	 * Log in失败超出次数5 returntrue
	 * @param username
	 * @return
	 */
	private boolean isLoginFailOvertimes(String username){
		String key = CommonConstant.LOGIN_FAIL + username;
		Object failTime = redisUtil.get(key);
		if(failTime!=null){
			Integer val = Integer.parseInt(failTime.toString());
			if(val>5){
				return true;
			}
		}
		return false;
	}

	/**
	 * 记录Log in失败次数
	 * @param username
	 */
	private void addLoginFailOvertimes(String username){
		String key = CommonConstant.LOGIN_FAIL + username;
		Object failTime = redisUtil.get(key);
		Integer val = 0;
		if(failTime!=null){
			val = Integer.parseInt(failTime.toString());
		}
		// 10minute，一minute为60s
		redisUtil.set(key, ++val, 600);
	}

	/**
	 * 发送短信Verification codeinterface(Change password)
	 *
	 * @param jsonObject
	 * @return
	 */
	@PostMapping(value = "/sendChangePwdSms")
	public Result<String> sendSms(@RequestBody JSONObject jsonObject) {
		Result<String> result = new Result<>();
		String mobile = jsonObject.get("mobile").toString();
		if (oConvertUtils.isEmpty(mobile)) {
			result.setMessage("Phone number不允许为空！");
			result.setSuccess(false);
			return result;
		}
		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		String username = sysUser.getUsername();
		LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<>();
		query.eq(SysUser::getUsername, username).eq(SysUser::getPhone, mobile);
		SysUser user = sysUserService.getOne(query);
		if (null == user) {
			return Result.error("当前Log in用户和绑定的Phone number不匹配，无法Change password！");
		}
		String redisKey = CommonConstant.PHONE_REDIS_KEY_PRE + mobile;
		Object object = redisUtil.get(redisKey);
		if (object != null) {
			result.setMessage("Verification code10within minutes，still valid！");
			result.setSuccess(false);
			return result;
		}
		//random number
		String captcha = RandomUtil.randomNumbers(6);
		JSONObject obj = new JSONObject();
		obj.put("code", captcha);
		try {
			boolean b = DySmsHelper.sendSms(mobile, obj, DySmsEnum.CHANGE_PASSWORD_TEMPLATE_CODE);
			if (!b) {
				result.setMessage("短信Verification code发送失败,Please try again later");
				result.setSuccess(false);
				return result;
			}
			//Verification code5within minutesefficient
            //update-begin---author:wangshuai---date:2025-07-15---for:【issues/8567】serious：There is a horizontal override problem when changing passwords。---
            obj.put("username",username);
            redisUtil.set(redisKey, obj.toJSONString(), 300);
            //update-end---author:wangshuai---date:2025-07-15---for:【issues/8567】serious：There is a horizontal override problem when changing passwords。---
			result.setSuccess(true);
		} catch (ClientException e) {
			e.printStackTrace();
			result.error500(" SMS interface is not configured，Please contact the administrator！");
			return result;
		}
		return result;
	}

	
	/**
	 * 图形Verification code
	 * @param sysLoginModel
	 * @return
	 */
	@RequestMapping(value = "/smsCheckCaptcha", method = RequestMethod.POST)
	public Result<?> smsCheckCaptcha(@RequestBody SysLoginModel sysLoginModel, HttpServletRequest request){
		String captcha = sysLoginModel.getCaptcha();
		String checkKey = sysLoginModel.getCheckKey();
		if(captcha==null){
			return Result.error("Verification codeinvalid");
		}
		String lowerCaseCaptcha = captcha.toLowerCase();
		String realKey = Md5Util.md5Encode(lowerCaseCaptcha+checkKey+jeecgBaseConfig.getSignatureSecret(), "utf-8");
		Object checkCode = redisUtil.get(realKey);
		if(checkCode==null || !checkCode.equals(lowerCaseCaptcha)) {
			return Result.error("Verification code错误");
		}
		String clientIp = IpUtils.getIpAddr(request);
		//Clear the number of SMS records
		DySmsLimit.clearSendSmsCount(clientIp);
		redisUtil.removeAll(realKey);
		return Result.ok();
	}
	
}