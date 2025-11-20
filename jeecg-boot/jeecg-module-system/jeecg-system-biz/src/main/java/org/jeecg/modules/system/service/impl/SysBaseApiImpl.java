package org.jeecg.modules.system.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.exceptions.ClientException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.base.Joiner;
import com.jeecg.dingtalk.api.core.response.Response;
import freemarker.core.TemplateClassResolver;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.dto.AiragFlowDTO;
import org.jeecg.common.api.dto.DataLogDTO;
import org.jeecg.common.api.dto.OnlineAuthDTO;
import org.jeecg.common.api.dto.message.*;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.UrlMatchEnum;
import org.jeecg.common.constant.*;
import org.jeecg.common.constant.enums.*;
import org.jeecg.common.desensitization.util.SensitiveInfoUtil;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.query.QueryCondition;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.query.QueryRuleEnum;
import org.jeecg.common.system.vo.*;
import org.jeecg.common.util.*;
import org.jeecg.common.util.dynamic.db.FreemarkerParseFactory;
import org.jeecg.config.firewall.SqlInjection.IDictTableWhiteListHandler;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.modules.airag.flow.service.IAiragFlowService;
import org.jeecg.modules.airag.flow.vo.api.FlowRunParams;
import org.jeecg.modules.message.entity.SysMessageTemplate;
import org.jeecg.modules.message.handle.impl.DdSendMsgHandle;
import org.jeecg.modules.message.handle.impl.EmailSendMsgHandle;
import org.jeecg.modules.message.handle.impl.QywxSendMsgHandle;
import org.jeecg.modules.message.handle.impl.SystemSendMsgHandle;
import org.jeecg.modules.message.service.ISysMessageTemplateService;
import org.jeecg.modules.message.websocket.WebSocket;
import org.jeecg.modules.system.entity.*;
import org.jeecg.modules.system.mapper.*;
import org.jeecg.modules.system.service.*;
import org.jeecg.modules.system.util.SecurityUtil;
import org.jeecg.modules.system.vo.lowapp.SysDictVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.PathMatcher;

import jakarta.annotation.Resource;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: Common underlying servicesAPI，Provide other independent module calls
 * @Author: scott
 * @Date:2019-4-20 
 * @Version:V1.0
 */
@Slf4j
@Service
public class SysBaseApiImpl implements ISysBaseAPI {
	/** Current system database type */
	private static String DB_TYPE = "";

	@Autowired
	private ISysMessageTemplateService sysMessageTemplateService;
	@Resource
	private SysUserMapper userMapper;
	@Resource
	private SysUserRoleMapper sysUserRoleMapper;
	@Autowired
	private ISysDepartService sysDepartService;
	@Autowired
	private ISysDictService sysDictService;
	@Resource
	private SysAnnouncementMapper sysAnnouncementMapper;
	@Resource
	private SysAnnouncementSendMapper sysAnnouncementSendMapper;
	@Resource
    private WebSocket webSocket;
	@Resource
	private SysRoleMapper roleMapper;
	@Resource
	private SysDepartMapper departMapper;
	@Resource
	private SysCategoryMapper categoryMapper;
	@Autowired
	private ISysDataSourceService dataSourceService;
	@Autowired
	private ISysUserDepartService sysUserDepartService;
	@Resource
	private SysPermissionMapper sysPermissionMapper;
	@Autowired
	private ISysPermissionDataRuleService sysPermissionDataRuleService;
	@Autowired
	private ThirdAppWechatEnterpriseServiceImpl wechatEnterpriseService;
	@Autowired
	private ThirdAppDingtalkServiceImpl dingtalkService;
	@Autowired
	ISysCategoryService sysCategoryService;
	@Autowired
	private ISysUserService sysUserService;
	@Autowired
	private ISysDataLogService sysDataLogService;
	@Autowired
	private ISysRoleService sysRoleService;
	@Autowired
	private ISysUserTenantService sysUserTenantService;

	@Autowired
	private ISysUserRoleService sysUserRoleService;

	@Autowired
	private ISysUserPositionService sysUserPositionService;

	@Autowired
	private IDictTableWhiteListHandler dictTableWhiteListHandler;

	@Autowired
	private ISysAnnouncementService sysAnnouncementService;

    @Autowired
    IAiragFlowService airagFlowService;

	@Override
	//@SensitiveDecode
	public LoginUser getUserByName(String username) {
		//update-begin-author:taoyan date:2022-6-6 for: VUEN-1276 【v3flow chart】testbug 1、Through the process or process instance initiated by me，View history，flow chart预览问题
		if (oConvertUtils.isEmpty(username)) {
			return null;
		}
		//update-end-author:taoyan date:2022-6-6 for: VUEN-1276 【v3flow chart】testbug 1、Through the process or process instance initiated by me，View history，flow chart预览问题
		LoginUser user = sysUserService.getEncodeUserInfo(username);

		//Desensitization and decryption when calling methods in the same class Aopwill fail，Obtaining user information is too important，The native decryption method is used here，Not adopted@SensitiveDecodeAbleAnnotation method
		try {
			SensitiveInfoUtil.handlerObject(user, false);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return user;
	}


	@Override
	@Cacheable(cacheNames=CommonConstant.SYS_USER_ID_MAPPING_CACHE, key="#username")
	public String getUserIdByName(String username) {
		if (oConvertUtils.isEmpty(username)) {
			return null;
		}
		String userId = userMapper.getUserIdByName(username);
		return userId;
	}
	

	@Override
	public String translateDictFromTable(String table, String text, String code, String key) {
		return sysDictService.queryTableDictTextByKey(table, text, code, key);
	}

	@Override
	public String translateDict(String code, String key) {
		return sysDictService.queryDictTextByKey(code, key);
	}

	@Override
	public List<SysPermissionDataRuleModel> queryPermissionDataRule(String component, String requestPath, String username) {
		List<SysPermission> currentSyspermission = null;
		if(oConvertUtils.isNotEmpty(component)) {
			//1.via annotation propertiespageComponent Get menu
			LambdaQueryWrapper<SysPermission> query = new LambdaQueryWrapper<SysPermission>();
			query.eq(SysPermission::getDelFlag,0);
			query.eq(SysPermission::getComponent, component);
			currentSyspermission = sysPermissionMapper.selectList(query);
		}else {
			//1.Directly request the address query menu through the front end
			LambdaQueryWrapper<SysPermission> query = new LambdaQueryWrapper<SysPermission>();
			query.eq(SysPermission::getMenuType,2);
			query.eq(SysPermission::getDelFlag,0);
			
			//update-begin-author:taoyan date:2023-2-21 for: Solving parameter order issues
			List<String> allPossiblePaths = this.getOnlinePossiblePaths(requestPath);
			log.info("Get the menu address= {}", allPossiblePaths.toString());
			if(allPossiblePaths.size()==1){
				query.eq(SysPermission::getUrl, requestPath);
			}else{
				query.in(SysPermission::getUrl, allPossiblePaths);	
			}
			//update-end-author:taoyan date:2023-2-21 for: Solving parameter order issues
			
			currentSyspermission = sysPermissionMapper.selectList(query);
			//2.not found Then through custom matchingURL Get menu
			if(currentSyspermission==null || currentSyspermission.size()==0) {
				//by customizingURLMatching rules Get menu（Implement data permission rules configuration through menu，In fact, data rule control is performed on the data acquisition interface.）
				String userMatchUrl = UrlMatchEnum.getMatchResultByUrl(requestPath);
				LambdaQueryWrapper<SysPermission> queryQserMatch = new LambdaQueryWrapper<SysPermission>();
				// update-begin-author:taoyan date:20211027 for: onlineIf the menu is configured as a first-level menu Unable to query permissions CancelmenuType = 1
				//queryQserMatch.eq(SysPermission::getMenuType, 1);
				// update-end-author:taoyan date:20211027 for: onlineIf the menu is configured as a first-level menu Unable to query permissions CancelmenuType = 1
				queryQserMatch.eq(SysPermission::getDelFlag, 0);
				queryQserMatch.eq(SysPermission::getUrl, userMatchUrl);
				if(oConvertUtils.isNotEmpty(userMatchUrl)){
					currentSyspermission = sysPermissionMapper.selectList(queryQserMatch);
				}
			}
			//3.not found 再通过正则matchGet menu
			if(currentSyspermission==null || currentSyspermission.size()==0) {
				//Permission configuration through regular matching
				String regUrl = getRegexpUrl(requestPath);
				if(regUrl!=null) {
					currentSyspermission = sysPermissionMapper.selectList(new LambdaQueryWrapper<SysPermission>().eq(SysPermission::getMenuType,2).eq(SysPermission::getUrl, regUrl).eq(SysPermission::getDelFlag,0));
				}
			}
		}
		if(currentSyspermission!=null && currentSyspermission.size()>0){
			List<SysPermissionDataRuleModel> dataRules = new ArrayList<SysPermissionDataRuleModel>();
			for (SysPermission sysPermission : currentSyspermission) {
				// update-begin--Author:scott Date:20191119 for：Data permission rule coding is not standardized，Projects have the same package name and class name #722
				List<SysPermissionDataRule> temp = sysPermissionDataRuleService.queryPermissionDataRules(username, sysPermission.getId());
				if(temp!=null && temp.size()>0) {
					//dataRules.addAll(temp);
					dataRules = oConvertUtils.entityListToModelList(temp,SysPermissionDataRuleModel.class);
				}
				// update-end--Author:scott Date:20191119 for：Data permission rule coding is not standardized，Projects have the same package name and class name #722
			}
			return dataRules;
		}
		return null;
	}

	/**
	 * Match the address passed by the front end If the match is successful, the regular address will be returned.
	 * AntPathMatcherMatch address
	 *()* match0or more characters
	 *()**match0or multiple directories
	 */
	private String getRegexpUrl(String url) {
		List<String> list = sysPermissionMapper.queryPermissionUrlWithStar();
		if(list!=null && list.size()>0) {
			for (String p : list) {
				PathMatcher matcher = new AntPathMatcher();
				if(matcher.match(p, url)) {
					return p;
				}
			}
		}
		return null;
	}

	@Override
	public SysUserCacheInfo getCacheUser(String username) {
		SysUserCacheInfo info = new SysUserCacheInfo();
		info.setOneDepart(true);
		LoginUser user = this.getUserByName(username);

//		try {
//			//Desensitization when calling between methods in the same class@SensitiveDecodeAbleDecrypt AopFailure handling
//			SensitiveInfoUtil.handlerObject(user, false);
//		} catch (IllegalAccessException e) {
//			e.printStackTrace();
//		}

		if(user!=null) {
			info.setSysUserId(user.getId());
			info.setSysUserCode(user.getUsername());
			info.setSysUserName(user.getRealname());
			info.setSysOrgCode(user.getOrgCode());
			info.setSysOrgId(user.getOrgId());
			info.setSysRoleCode(user.getRoleCode());
		}else{
			return null;
		}
		//Multi-department supportinQuery
		List<SysDepart> list = departMapper.queryUserDeparts(user.getId());
		List<String> sysMultiOrgCode = new ArrayList<String>();
		if(list==null || list.size()==0) {
			//The current user has no department
			//sysMultiOrgCode.add("0");
		}else if(list.size()==1) {
			sysMultiOrgCode.add(list.get(0).getOrgCode());
		}else {
			info.setOneDepart(false);
			for (SysDepart dpt : list) {
				sysMultiOrgCode.add(dpt.getOrgCode());
			}
		}
		info.setSysMultiOrgCode(sysMultiOrgCode);
		return info;
	}

	@Override
	public LoginUser getUserById(String id) {
		if(oConvertUtils.isEmpty(id)) {
			return null;
		}
		LoginUser loginUser = new LoginUser();
		SysUser sysUser = userMapper.selectById(id);
		if(sysUser==null) {
			return null;
		}
		BeanUtils.copyProperties(sysUser, loginUser);
		//Remove sensitive user information
		loginUser.setPassword(null);
		loginUser.setRelTenantIds(null);
		loginUser.setDepartIds(null);
		return loginUser;
	}

	@Override
	public List<String> getRolesByUsername(String username) {
		return sysUserRoleMapper.getRoleByUserName(username);
	}
	
	@Override
	public List<String> getRolesByUserId(String userId) {
		return sysUserRoleMapper.getRoleCodeByUserId(userId);
	}

	@Override
	public List<String> getDepartIdsByUsername(String username) {
		List<SysDepart> list = sysDepartService.queryDepartsByUsername(username);
		List<String> result = new ArrayList<>(list.size());
		for (SysDepart depart : list) {
			result.add(depart.getId());
		}
		return result;
	}
	
	@Override
	public List<String> getDepartIdsByUserId(String userId) {
		return sysDepartService.queryDepartsByUserId(userId);
	}

	@Override
	public Set<String> getDepartParentIdsByUsername(String username) {
		List<SysDepart> list = sysDepartService.queryDepartsByUsername(username);
		Set<String> result = new HashSet<>(list.size());
		for (SysDepart depart : list) {
			result.add(depart.getParentId());
		}
		return result;
	}

	@Override
	public Set<String> getDepartParentIdsByDepIds(Set<String> depIds) {
		LambdaQueryWrapper<SysDepart> departQuery = new LambdaQueryWrapper<SysDepart>().in(SysDepart::getId, depIds);
		List<SysDepart> departList = departMapper.selectList(departQuery);

		if(CollectionUtils.isEmpty(departList)){
			return null;
		}
		Set<String> parentIds = departList.stream()
				.map(SysDepart::getParentId)
				.collect(Collectors.toSet());
		return parentIds;
	}

	@Override
	public List<String> getDepartNamesByUsername(String username) {
		List<SysDepart> list = sysDepartService.queryDepartsByUsername(username);
		List<String> result = new ArrayList<>(list.size());
		for (SysDepart depart : list) {
			result.add(depart.getDepartName());
		}
		return result;
	}

	@Override
	public DictModel getParentDepartId(String departId) {
		SysDepart depart = departMapper.getParentDepartId(departId);
		DictModel model = new DictModel(depart.getId(),depart.getParentId());
		return model;
	}

	@Override
	@Cacheable(value = CacheConstant.SYS_DICT_CACHE,key = "#code", unless = "#result == null ")
	public List<DictModel> queryDictItemsByCode(String code) {
		return sysDictService.queryDictItemsByCode(code);
	}

	@Override
	@Cacheable(value = CacheConstant.SYS_ENABLE_DICT_CACHE,key = "#code", unless = "#result == null ")
	public List<DictModel> queryEnableDictItemsByCode(String code) {
		return sysDictService.queryEnableDictItemsByCode(code);
	}

	@Override
	public List<DictModel> queryTableDictItemsByCode(String tableFilterSql, String text, String code) {
		//【Online+system】Dictionary table plus permission control mechanism logic，Good idea LOWCOD-799
		if(tableFilterSql.indexOf(SymbolConstant.SYS_VAR_PREFIX)>=0){
			tableFilterSql = QueryGenerator.getSqlRuleValue(tableFilterSql);
		}
		return sysDictService.queryTableDictItemsByCode(tableFilterSql, text, code);
	}

	@Override
	public List<DictModel> queryAllDepartBackDictModel() {
		return sysDictService.queryAllDepartBackDictModel();
	}

	@Override
	public void sendSysAnnouncement(MessageDTO message) {
		this.sendSysAnnouncement(message.getFromUser(),
				message.getToUser(),
				message.getTitle(),
				message.getContent(),
				message.getCategory(),
                message.getNoticeType());
		try {
			// Send to third party simultaneouslyAPPinformation
			wechatEnterpriseService.sendMessage(message, true);
			dingtalkService.sendMessage(message, true);
		} catch (Exception e) {
			log.error("Send to third party simultaneouslyAPPinformation失败！", e);
		}
	}

	@Override
	public void sendBusAnnouncement(BusMessageDTO message) {
		sendBusAnnouncement(message.getFromUser(),
				message.getToUser(),
				message.getTitle(),
				message.getContent(),
				message.getCategory(),
				message.getBusType(),
				message.getBusId(),
				message.getNoticeType());
		try {
			// Send to third party simultaneouslyAPPinformation
			wechatEnterpriseService.sendMessage(message, true);
			dingtalkService.sendMessage(message, true);
		} catch (Exception e) {
			log.error("Send to third party simultaneouslyAPPinformation失败！", e);
		}
	}

	@Override
	public void sendTemplateAnnouncement(TemplateMessageDTO message) {
		String templateCode = message.getTemplateCode();
		String title = message.getTitle();
		Map<String,String> tmplateParam = message.getTemplateParam();
		String fromUser = message.getFromUser();
		String toUser = message.getToUser();

		List<SysMessageTemplate> sysSmsTemplates = sysMessageTemplateService.selectByCode(templateCode);
		if(sysSmsTemplates==null||sysSmsTemplates.size()==0){
			throw new JeecgBootException("information模板不存exist，template encoding："+templateCode);
		}
		SysMessageTemplate sysSmsTemplate = sysSmsTemplates.get(0);
		//Template title
		title = title==null?sysSmsTemplate.getTemplateName():title;
		//Template content
		String content = sysSmsTemplate.getTemplateContent();
		if(tmplateParam!=null) {
			for (Map.Entry<String, String> entry : tmplateParam.entrySet()) {
				String str = "${" + entry.getKey() + "}";
				if(oConvertUtils.isNotEmpty(title)){
					title = title.replace(str, entry.getValue());
				}
				content = content.replace(str, entry.getValue());
			}
		}
		String mobileOpenUrl = null;
		if(tmplateParam!=null && oConvertUtils.isNotEmpty(tmplateParam.get(CommonConstant.MSG_HREF_URL))){
			mobileOpenUrl = tmplateParam.get(CommonConstant.MSG_HREF_URL);
		}
		SysAnnouncement announcement = new SysAnnouncement();
		announcement.setTitile(title);
		announcement.setMsgContent(content);
		announcement.setSender(fromUser);
		announcement.setPriority(CommonConstant.PRIORITY_M);
		announcement.setMsgType(CommonConstant.MSG_TYPE_UESR);
		announcement.setSendStatus(CommonConstant.HAS_SEND);
		announcement.setSendTime(new Date());
		announcement.setMsgCategory(CommonConstant.MSG_CATEGORY_2);
		announcement.setDelFlag(String.valueOf(CommonConstant.DEL_FLAG_0));
       //update-begin-author:liusq---date:2025-07-01--for: [QQYUN-12999]systemnotify，systemnotify时间更新，But it's ranked lower
		announcement.setIzTop(CommonConstant.IZ_TOP_0);
		//update-end-author:liusq---date:2025-07-01--for: [QQYUN-12999]systemnotify，systemnotify时间更新，But it's ranked lower
  		sysAnnouncementMapper.insert(announcement);
		// 2.Insert user notification reading mark table record
		String userId = toUser;
		String[] userIds = userId.split(",");
		String anntId = announcement.getId();
		for(int i=0;i<userIds.length;i++) {
			if(oConvertUtils.isNotEmpty(userIds[i])) {
				SysUser sysUser = userMapper.getUserByName(userIds[i]);
				if(sysUser==null) {
					continue;
				}
				SysAnnouncementSend announcementSend = new SysAnnouncementSend();
				announcementSend.setAnntId(anntId);
				announcementSend.setUserId(sysUser.getId());
				announcementSend.setReadFlag(CommonConstant.NO_READ_FLAG);
				sysAnnouncementSendMapper.insert(announcementSend);
				JSONObject obj = new JSONObject();
				obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_USER);
				obj.put(WebsocketConst.MSG_USER_ID, sysUser.getId());
				obj.put(WebsocketConst.MSG_ID, announcement.getId());
				obj.put(WebsocketConst.MSG_TXT, announcement.getTitile());
				webSocket.sendMessage(sysUser.getId(), obj.toJSONString());
			}
		}
		try {
			// Synchronize enterprise WeChat、钉钉的informationnotify
			dingtalkService.sendActionCardMessage(announcement, mobileOpenUrl, true);
			wechatEnterpriseService.sendTextCardMessage(announcement, mobileOpenUrl, true);
		} catch (Exception e) {
			log.error("Send to third party simultaneouslyAPPinformation失败！", e);
		}

	}

	@Override
	public void sendBusTemplateAnnouncement(BusTemplateMessageDTO message) {
		String templateCode = message.getTemplateCode();
		String title = message.getTitle();
		Map<String,String> tmplateParam = message.getTemplateParam();
		String fromUser = message.getFromUser();
		String toUser = message.getToUser();
		String busId = message.getBusId();
		String busType = message.getBusType();

		List<SysMessageTemplate> sysSmsTemplates = sysMessageTemplateService.selectByCode(templateCode);
		if(sysSmsTemplates==null||sysSmsTemplates.size()==0){
			throw new JeecgBootException("information模板不存exist，template encoding："+templateCode);
		}
		SysMessageTemplate sysSmsTemplate = sysSmsTemplates.get(0);
		//Template title
		title = title==null?sysSmsTemplate.getTemplateName():title;
		//Template content
		String content = sysSmsTemplate.getTemplateContent();
		if(tmplateParam!=null) {
			for (Map.Entry<String, String> entry : tmplateParam.entrySet()) {
				String str = "${" + entry.getKey() + "}";
				if (entry.getValue() != null) {
					title = title.replace(str, entry.getValue());
					content = content.replace(str, entry.getValue());
				}
			}
		}
		SysAnnouncement announcement = new SysAnnouncement();
		announcement.setTitile(title);
		announcement.setMsgContent(content);
		announcement.setSender(fromUser);
		announcement.setPriority(CommonConstant.PRIORITY_M);
		announcement.setMsgType(CommonConstant.MSG_TYPE_UESR);
		announcement.setSendStatus(CommonConstant.HAS_SEND);
		announcement.setSendTime(new Date());
		//update-begin-author:liusq---date:2025-07-01--for: [QQYUN-12999]systemnotify，systemnotify时间更新，But it's ranked lower
		announcement.setIzTop(CommonConstant.IZ_TOP_0);
		//update-end-author:liusq---date:2025-07-01--for: [QQYUN-12999]systemnotify，systemnotify时间更新，But it's ranked lower
		if(tmplateParam!=null && oConvertUtils.isNotEmpty(tmplateParam.get(CommonSendStatus.MSG_ABSTRACT_JSON))){
			announcement.setMsgAbstract(tmplateParam.get(CommonSendStatus.MSG_ABSTRACT_JSON));
		}
		String mobileOpenUrl = null;
		if(tmplateParam!=null && oConvertUtils.isNotEmpty(tmplateParam.get(CommonConstant.MSG_HREF_URL))){
			mobileOpenUrl = tmplateParam.get(CommonConstant.MSG_HREF_URL);
		}

		// If passed extensionjson，Description is a personalized service，Have opinionsremarkthen set as notification content
		if(oConvertUtils.isJson(announcement.getMsgAbstract())) {
			// Getannouncement.getMsgAbstract()fieldsremark
			JSONObject jsonObject = JSON.parseObject(announcement.getMsgAbstract());
			String remark = jsonObject.containsKey("remark")? jsonObject.getString("remark"): null;
			if(oConvertUtils.isNotEmpty(remark)){
				announcement.setMsgContent(remark);
			}
		}

		announcement.setMsgCategory(CommonConstant.MSG_CATEGORY_2);
		announcement.setDelFlag(String.valueOf(CommonConstant.DEL_FLAG_0));
		announcement.setBusId(busId);
		announcement.setBusType(busType);
		announcement.setOpenType(SysAnnmentTypeEnum.getByType(busType).getOpenType());
		announcement.setOpenPage(SysAnnmentTypeEnum.getByType(busType).getOpenPage());
		sysAnnouncementMapper.insert(announcement);
		// 2.Insert user notification reading mark table record
		String userId = toUser;
		String[] userIds = userId.split(",");
		String anntId = announcement.getId();
		for(int i=0;i<userIds.length;i++) {
			if(oConvertUtils.isNotEmpty(userIds[i])) {
				SysUser sysUser = userMapper.getUserByName(userIds[i]);
				if(sysUser==null) {
					continue;
				}
				SysAnnouncementSend announcementSend = new SysAnnouncementSend();
				announcementSend.setAnntId(anntId);
				announcementSend.setUserId(sysUser.getId());
				announcementSend.setReadFlag(CommonConstant.NO_READ_FLAG);
				sysAnnouncementSendMapper.insert(announcementSend);
				JSONObject obj = new JSONObject();
				obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_USER);
				obj.put(WebsocketConst.MSG_USER_ID, sysUser.getId());
				obj.put(WebsocketConst.MSG_ID, announcement.getId());
				obj.put(WebsocketConst.MSG_TXT, announcement.getTitile());
				webSocket.sendMessage(sysUser.getId(), obj.toJSONString());
			}
		}
		try {
			// 钉钉的informationnotify
			dingtalkService.sendActionCardMessage(announcement, mobileOpenUrl, true);
			// Enterprise WeChat notification
			wechatEnterpriseService.sendTextCardMessage(announcement, mobileOpenUrl, true);
		} catch (Exception e) {
			log.error("Send to third party simultaneouslyAPPinformation失败！", e);
		}

	}

	@Override
	public String parseTemplateByCode(TemplateDTO templateDTO) {
		String templateCode = templateDTO.getTemplateCode();
		Map<String, String> map = templateDTO.getTemplateParam();
		List<SysMessageTemplate> sysSmsTemplates = sysMessageTemplateService.selectByCode(templateCode);
		if(sysSmsTemplates==null||sysSmsTemplates.size()==0){
			throw new JeecgBootException("information模板不存exist，template encoding："+templateCode);
		}
		SysMessageTemplate sysSmsTemplate = sysSmsTemplates.get(0);
		//Template content
		String content = sysSmsTemplate.getTemplateContent();
		if(map!=null) {
			for (Map.Entry<String, String> entry : map.entrySet()) {
				String str = "${" + entry.getKey() + "}";
				content = content.replace(str, entry.getValue());
			}
		}
		return content;
	}

	@Override
	public void updateSysAnnounReadFlag(String busType, String busId) {
		SysAnnouncement announcement = sysAnnouncementMapper.selectOne(new QueryWrapper<SysAnnouncement>().eq("bus_type",busType).eq("bus_id",busId));
		if(announcement != null){
			LoginUser sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();
			String userId = sysUser.getId();
			LambdaUpdateWrapper<SysAnnouncementSend> updateWrapper = new UpdateWrapper().lambda();
			updateWrapper.set(SysAnnouncementSend::getReadFlag, CommonConstant.HAS_READ_FLAG);
			updateWrapper.set(SysAnnouncementSend::getReadTime, new Date());
			updateWrapper.eq(SysAnnouncementSend::getAnntId,announcement.getId());
			updateWrapper.eq(SysAnnouncementSend::getUserId,userId);
			//updateWrapper.last("where annt_id ='"+announcement.getId()+"' and user_id ='"+userId+"'");
			SysAnnouncementSend announcementSend = new SysAnnouncementSend();
			sysAnnouncementSendMapper.update(announcementSend, updateWrapper);
		}
	}

	/**
	 * GetDatabase type
	 * @param dataSource
	 * @return
	 * @throws SQLException
	 */
	private String getDatabaseTypeByDataSource(DataSource dataSource) throws SQLException{
		if("".equals(DB_TYPE)) {
			Connection connection = dataSource.getConnection();
			try {
				DatabaseMetaData md = connection.getMetaData();
				String dbType = md.getDatabaseProductName().toLowerCase();
				if(dbType.indexOf(DataBaseConstant.DB_TYPE_MYSQL.toLowerCase())>=0) {
					DB_TYPE = DataBaseConstant.DB_TYPE_MYSQL;
				}else if(dbType.indexOf(DataBaseConstant.DB_TYPE_ORACLE.toLowerCase())>=0) {
					DB_TYPE = DataBaseConstant.DB_TYPE_ORACLE;
				}else if(dbType.indexOf(DataBaseConstant.DB_TYPE_SQLSERVER.toLowerCase())>=0||dbType.indexOf(DataBaseConstant.DB_TYPE_SQL_SERVER_BLANK)>=0) {
					DB_TYPE = DataBaseConstant.DB_TYPE_SQLSERVER;
				}else if(dbType.indexOf(DataBaseConstant.DB_TYPE_POSTGRESQL.toLowerCase())>=0) {
					DB_TYPE = DataBaseConstant.DB_TYPE_POSTGRESQL;
				}else if(dbType.indexOf(DataBaseConstant.DB_TYPE_MARIADB.toLowerCase())>=0) {
					DB_TYPE = DataBaseConstant.DB_TYPE_MARIADB;
				}else {
					log.error("Database type:[" + dbType + "]Not recognized!");
					//throw new JeecgBootException("Database type:["+dbType+"]Not recognized!");
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}finally {
				connection.close();
			}
		}
		return DB_TYPE;

	}

	@Override
	public List<DictModel> queryAllDict() {
		// Query并排序
		QueryWrapper<SysDict> queryWrapper = new QueryWrapper<SysDict>();
		queryWrapper.orderByAsc("create_time");
		List<SysDict> dicts = sysDictService.list(queryWrapper);
		// packaged into model
		List<DictModel> list = new ArrayList<DictModel>();
		for (SysDict dict : dicts) {
			list.add(new DictModel(dict.getDictCode(), dict.getDictName()));
		}

		return list;
	}

	@Override
	public List<SysCategoryModel> queryAllSysCategory() {
		List<SysCategory> ls = categoryMapper.selectList(null);
		List<SysCategoryModel> res = oConvertUtils.entityListToModelList(ls,SysCategoryModel.class);
		return res;
	}

	@Override
	public List<DictModel> queryFilterTableDictInfo(String table, String text, String code, String filterSql) {
		return sysDictService.queryTableDictItemsByCodeAndFilter(table,text,code,filterSql);
	}

	@Override
	public List<String> queryTableDictByKeys(String table, String text, String code, String[] keyArray) {
		return sysDictService.queryTableDictByKeys(table,text,code,Joiner.on(",").join(keyArray));
	}

	@Override
	public List<ComboModel> queryAllUserBackCombo() {
		List<ComboModel> list = new ArrayList<ComboModel>();
		List<SysUser> userList = userMapper.selectList(new QueryWrapper<SysUser>().eq("status",1).eq("del_flag",0));
		for(SysUser user : userList){
			ComboModel model = new ComboModel();
			model.setTitle(user.getRealname());
			model.setId(user.getId());
			model.setUsername(user.getUsername());
			list.add(model);
		}
		return list;
	}

	@Override
	public JSONObject queryAllUser(String userIds, Integer pageNo, Integer pageSize) {
		JSONObject json = new JSONObject();
		QueryWrapper<SysUser> queryWrapper = new QueryWrapper<SysUser>().eq("status",1).eq("del_flag",0);
		List<ComboModel> list = new ArrayList<ComboModel>();
		Page<SysUser> page = new Page<SysUser>(pageNo, pageSize);
		IPage<SysUser> pageList = userMapper.selectPage(page, queryWrapper);
		for(SysUser user : pageList.getRecords()){
			ComboModel model = new ComboModel();
			model.setUsername(user.getUsername());
			model.setTitle(user.getRealname());
			model.setId(user.getId());
			model.setEmail(user.getEmail());
			if(oConvertUtils.isNotEmpty(userIds)){
				String[] temp = userIds.split(",");
				for(int i = 0; i<temp.length;i++){
					if(temp[i].equals(user.getId())){
						model.setChecked(true);
					}
				}
			}
			list.add(model);
		}
		json.put("list",list);
		json.put("total",pageList.getTotal());
		return json;
	}

	public List<ComboModel> queryAllRole() {
		List<ComboModel> list = new ArrayList<ComboModel>();
		List<SysRole> roleList = roleMapper.selectList(new QueryWrapper<SysRole>());
		for(SysRole role : roleList){
			ComboModel model = new ComboModel();
			model.setTitle(role.getRoleName());
			model.setId(role.getId());
			list.add(model);
		}
		return list;
	}

    @Override
    public List<ComboModel> queryAllRole(String[] roleIds) {
        List<ComboModel> list = new ArrayList<ComboModel>();
        List<SysRole> roleList = roleMapper.selectList(new QueryWrapper<SysRole>());
        for(SysRole role : roleList){
            ComboModel model = new ComboModel();
            model.setTitle(role.getRoleName());
            model.setId(role.getId());
            model.setRoleCode(role.getRoleCode());
            if(oConvertUtils.isNotEmpty(roleIds)) {
                for (int i = 0; i < roleIds.length; i++) {
                    if (roleIds[i].equals(role.getId())) {
                        model.setChecked(true);
                    }
                }
            }
            list.add(model);
        }
        return list;
    }

	@Override
	public List<String> getRoleIdsByUsername(String username) {
		return sysUserRoleMapper.getRoleIdByUserName(username);
	}

	@Override
	public String getDepartIdsByOrgCode(String orgCode) {
		return departMapper.queryDepartIdByOrgCode(orgCode);
	}

	@Override
	public List<SysDepartModel> getAllSysDepart() {
		List<SysDepartModel> departModelList = new ArrayList<SysDepartModel>();
		List<SysDepart> departList = departMapper.selectList(new QueryWrapper<SysDepart>().eq("del_flag","0"));
		for(SysDepart depart : departList){
			SysDepartModel model = new SysDepartModel();
			BeanUtils.copyProperties(depart,model);
			departModelList.add(model);
		}
		return departModelList;
	}

	@Override
	public DynamicDataSourceModel getDynamicDbSourceById(String dbSourceId) {
		SysDataSource dbSource = dataSourceService.getById(dbSourceId);
		if(dbSource!=null && StringUtils.isNotBlank(dbSource.getDbPassword())){
			String dbPassword = dbSource.getDbPassword();
			String decodedStr = SecurityUtil.jiemi(dbPassword);
			dbSource.setDbPassword(decodedStr);
		}
		return new DynamicDataSourceModel(dbSource);
	}

	@Override
	public DynamicDataSourceModel getDynamicDbSourceByCode(String dbSourceCode) {
		SysDataSource dbSource = dataSourceService.getOne(new LambdaQueryWrapper<SysDataSource>().eq(SysDataSource::getCode, dbSourceCode));
		if(dbSource!=null && StringUtils.isNotBlank(dbSource.getDbPassword())){
			String dbPassword = dbSource.getDbPassword();
			String decodedStr = SecurityUtil.jiemi(dbPassword);
			dbSource.setDbPassword(decodedStr);
		}
		return new DynamicDataSourceModel(dbSource);
	}

	@Override
	public List<String> getDeptHeadByDepId(String deptId) {
		QueryWrapper<SysUser> queryWrapper = new QueryWrapper<SysUser>().eq("status", 1).eq("del_flag", 0);
		//Supports comma-separated delivery of multiple departmentsid
		if (oConvertUtils.isNotEmpty(deptId) && deptId.contains(SymbolConstant.COMMA)) {
			String[] vals = deptId.split(SymbolConstant.COMMA);
			queryWrapper.and(andWrapper -> {
				for (int i = 0; i < vals.length; i++) {
					andWrapper.like("depart_ids", vals[i]);
					andWrapper.or();
				}
			});
		} else {
			queryWrapper.like("depart_ids", deptId);
		}
		
		List<SysUser> userList = userMapper.selectList(queryWrapper);
		List<String> list = new ArrayList<>();
		for(SysUser user : userList){
			list.add(user.getUsername());
		}
		return list;
	}

	@Override
	public void sendWebSocketMsg(String[] userIds, String cmd) {
		JSONObject obj = new JSONObject();
		obj.put(WebsocketConst.MSG_CMD, cmd);
		webSocket.sendMessage(userIds, obj.toJSONString());
	}

	@Override
	public List<UserAccountInfo> queryAllUserByIds(String[] userIds) {
		QueryWrapper<SysUser> queryWrapper = new QueryWrapper<SysUser>().eq("status",1).eq("del_flag",0);
		queryWrapper.in("id",userIds);
		List<UserAccountInfo> loginUsers = new ArrayList<>();
		List<SysUser> sysUsers = userMapper.selectList(queryWrapper);
		for (SysUser user:sysUsers) {
			UserAccountInfo loginUser=new UserAccountInfo();
			BeanUtils.copyProperties(user, loginUser);
			loginUsers.add(loginUser);
		}
		return loginUsers;
	}

	/**
	 * Push check-in personnel information
	 * @param userId
	 */
	@Override
	public void meetingSignWebsocket(String userId) {
		JSONObject obj = new JSONObject();
		obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_SIGN);
		obj.put(WebsocketConst.MSG_USER_ID,userId);
		//TODO Currently pushing all，Modify later
		webSocket.sendMessage(obj.toJSONString());
	}

	@Override
	public List<UserAccountInfo> queryUserByNames(String[] userNames) {
		QueryWrapper<SysUser> queryWrapper = new QueryWrapper<SysUser>().eq("status",1).eq("del_flag",0);
		queryWrapper.in("username",userNames);
		List<UserAccountInfo> loginUsers = new ArrayList<>();
		List<SysUser> sysUsers = userMapper.selectList(queryWrapper);
		for (SysUser user:sysUsers) {
			UserAccountInfo loginUser=new UserAccountInfo();
			BeanUtils.copyProperties(user, loginUser);
			loginUsers.add(loginUser);
		}
		return loginUsers;
	}

	@Override
	public List<JSONObject> queryUserBySuperQuery(String superQuery,String matchType) {
		List<JSONObject> result=new ArrayList<>();
		Map<String, String[]> parameterMap=new HashMap<>();
		parameterMap.put("superQueryMatchType",new String[]{matchType});
		parameterMap.put("superQueryParams",new String[]{superQuery});
		SysUser sysUser=new SysUser();
		QueryWrapper<SysUser> queryWrapper = QueryGenerator.initQueryWrapper(sysUser, parameterMap);
		List<SysUser> list= sysUserService.list(queryWrapper);
		if(ObjectUtils.isNotEmpty(list)){
		
			//update-begin-author:taoyan date:2023-5-19 for: QQYUN-5326【Simple flow】Get组织人员 one/many Filter criteria No department filtering
			String departKey = "depart";
			QueryCondition departCondition = null;
			try {
				String temp = URLDecoder.decode(superQuery, "UTF-8");
				List<QueryCondition> conditions = JSON.parseArray(temp, QueryCondition.class);
				for(QueryCondition condition: conditions){
					if(departKey.equals(condition.getField())){
						departCondition = condition;
						break;
					}
				}
			} catch (UnsupportedEncodingException e) {
				log.error("Query用户information，Query条件jsonConversion failed", e);
			}
			
			for (SysUser user : list) {
				JSONObject userJson = JSONObject.parseObject(JSONObject.toJSONString(user));
				List<SysDepart> departList = sysDepartService.queryDepartsByUsername(user.getUsername());
				List<String> departIds = null;
				if(departList!=null && departList.size()>0){
					departIds = departList.stream().map(i->i.getId()).collect(Collectors.toList());
					List<String> departNames = departList.stream().map(i->i.getDepartName()).collect(Collectors.toList());
					userJson.put(departKey, oConvertUtils.list2JSONArray(departIds));
					userJson.put(departKey+"_dictText", String.join(",", departNames));
				}
				boolean flag = getDepartConditionResult(departCondition, departIds);
				if(flag){
					result.add(userJson);
				}
				//update-end-author:taoyan date:2023-5-19 for: QQYUN-5326【Simple flow】Get组织人员 one/many Filter criteria No department filtering
				
			}
		}
		return result;
	}

	/**
	 * Determine whether the user's department meets the conditions -equal/不equal/exist--middle/不exist--middle/is empty/不is empty
	 * QQYUN-5326【Simple flow】Get组织人员 one/many Filter criteria No department filtering
	 * @param departCondition
	 * @param departIds
	 * @return
	 */
	private boolean getDepartConditionResult(QueryCondition departCondition, List<String> departIds){
		if(departCondition == null){
			return true;
		}
		QueryRuleEnum rule = QueryRuleEnum.getByValue(departCondition.getRule());
		String conditionVal = departCondition.getVal();
		if(rule == QueryRuleEnum.EMPTY){
			if(departIds==null || departIds.size()==0){
				return true;
			}
		}else if (rule == QueryRuleEnum.NOT_EMPTY){
			if(departIds!=null && departIds.size()>0){
				return true;
			}
		}else{
			if(oConvertUtils.isEmpty(conditionVal)){
				return false;
			}
			if(departIds==null || departIds.size()==0){
				return false;
			}
			List<String> conditionList;
			if(conditionVal.startsWith("[") && conditionVal.endsWith("]")){
				conditionList = JSONArray.parseArray(conditionVal, String.class);
			}else{
				conditionList = new ArrayList<String>(Arrays.asList(conditionVal.split(",")));
			}
			if(rule == QueryRuleEnum.EQ){
				if(oConvertUtils.isEqList(conditionList, departIds)){
					return true;
				}
			}else if(rule == QueryRuleEnum.NE){
				if(!oConvertUtils.isEqList(conditionList, departIds)){
					return true;
				}
			}else if(rule == QueryRuleEnum.IN){
				if(oConvertUtils.isInList(departIds, conditionList)){
					return true;
				}
			}else if(rule == QueryRuleEnum.NOT_IN){
				if(!oConvertUtils.isInList(departIds, conditionList)){
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public JSONObject queryUserById(String id) {
		QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(true, SysUser::getId, id);
		SysUser sysUser = sysUserService.getOne(queryWrapper);
		if (ObjectUtils.isNotEmpty(sysUser)) {
			return JSONObject.parseObject(JSONObject.toJSONString(sysUser));
		}
		return null;
	}

	@Override
	public List<JSONObject> queryDeptBySuperQuery(String superQuery,String matchType) {
		List<JSONObject> result=new ArrayList<>();
		Map<String, String[]> parameterMap=new HashMap<>();
		parameterMap.put("superQueryMatchType",new String[]{matchType});
		parameterMap.put("superQueryParams",new String[]{superQuery});
		SysDepart sysDepart=new SysDepart();
		QueryWrapper<SysDepart> queryWrapper = QueryGenerator.initQueryWrapper(sysDepart, parameterMap);
		List<SysDepart> list= sysDepartService.list(queryWrapper);
		if(ObjectUtils.isNotEmpty(list)){
			for (SysDepart depart: list) {
				result.add(JSONObject.parseObject(JSONObject.toJSONString(depart)));
			}
		}
		return result;
	}

	@Override
	public List<JSONObject> queryRoleBySuperQuery(String superQuery,String matchType) {
		List<JSONObject> result=new ArrayList<>();
		Map<String, String[]> parameterMap=new HashMap<>();
		parameterMap.put("superQueryMatchType",new String[]{matchType});
		parameterMap.put("superQueryParams",new String[]{superQuery});
		SysRole sysDepart=new SysRole();
		QueryWrapper<SysRole> queryWrapper = QueryGenerator.initQueryWrapper(sysDepart, parameterMap);
		List<SysRole> list= sysRoleService.list(queryWrapper);
		if(ObjectUtils.isNotEmpty(list)){
			for (SysRole role: list) {
				result.add(JSONObject.parseObject(JSONObject.toJSONString(role)));
			}
		}
		return result;
	}

	@Override
	public List<String> selectUserIdByTenantId(String tenantId) {
		QueryWrapper queryWrapper=new QueryWrapper<SysUserTenant>();
		queryWrapper.select("user_id");
		queryWrapper.eq("tenant_id",tenantId);
		return sysUserTenantService.listObjs(queryWrapper,e->e.toString());
	}

	@Override
	public SysDepartModel selectAllById(String id) {
		SysDepart sysDepart = sysDepartService.getById(id);
		SysDepartModel sysDepartModel = new SysDepartModel();
		BeanUtils.copyProperties(sysDepart,sysDepartModel);
		return sysDepartModel;
	}

	@Override
	public List<String> queryDeptUsersByUserId(String userId) {
		List<String> userIds = new ArrayList<>();
		List<SysUserDepart> userDepartList = sysUserDepartService.list(new QueryWrapper<SysUserDepart>().eq("user_id",userId));
		if(userDepartList != null){
			//Find affiliated companies
			String orgCodes = "";
			StringBuilder orgCodesBuilder = new StringBuilder();
            orgCodesBuilder.append(orgCodes);
			for(SysUserDepart userDepart : userDepartList){
				//Query所属公司编码
				SysDepart depart = sysDepartService.getById(userDepart.getDepId());
				int length = YouBianCodeUtil.ZHANWEI_LENGTH;
				String compyOrgCode = "";
				if(depart != null && depart.getOrgCode() != null){
					compyOrgCode = depart.getOrgCode().substring(0,length);
					if(orgCodes.indexOf(compyOrgCode) == -1){
                        orgCodesBuilder.append(SymbolConstant.COMMA).append(compyOrgCode);
					}
				}
			}
            orgCodes = orgCodesBuilder.toString();
			if(oConvertUtils.isNotEmpty(orgCodes)){
				orgCodes = orgCodes.substring(1);
				List<String> listIds = departMapper.getSubDepIdsByOrgCodes(orgCodes.split(","));
				List<SysUserDepart> userList = sysUserDepartService.list(new QueryWrapper<SysUserDepart>().in("dep_id",listIds));
				for(SysUserDepart userDepart : userList){
					if(!userIds.contains(userDepart.getUserId())){
						userIds.add(userDepart.getUserId());
					}
				}
			}
		}
		return userIds;
	}

	/**
	 * Query用户拥有的Role集合
	 * @param username
	 * @return
	 */
	@Override
	public Set<String> getUserRoleSet(String username) {
		// Query用户拥有的Role集合
		List<String> roles = sysUserRoleMapper.getRoleByUserName(username);
		log.info("-------Read the roles owned by the user from the databaseRules------username： " + username + ",Roles size: " + (roles == null ? 0 : roles.size()));
		return new HashSet<>(roles);
	}
	
	
	/**
	 * Query用户拥有的Role集合
	 * @param useId
	 * @return
	 */
	@Override
	public Set<String> getUserRoleSetById(String useId) {
		// Query用户拥有的Role集合
		List<String> roles = sysUserRoleMapper.getRoleCodeByUserId(useId);
		log.info("-------Read the roles owned by the user from the databaseRules------useId： " + useId + ",Roles size: " + (roles == null ? 0 : roles.size()));
		return new HashSet<>(roles);
	}

	/**
	 * Query用户拥有的权限集合
	 * @param userId
	 * @return
	 */
	@Override
	public Set<String> getUserPermissionSet(String userId) {
		Set<String> permissionSet = new HashSet<>();
		List<SysPermission> permissionList = sysPermissionMapper.queryByUser(userId);
		//================= begin When opening a tenant if nottestRole，Join by defaulttestRole================
		if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
			if (permissionList == null) {
				permissionList = new ArrayList<>();
			}
			List<SysPermission> testRoleList = sysPermissionMapper.queryPermissionByTestRoleId();
			permissionList.addAll(testRoleList);
		}
		//================= end When opening a tenant if nottestRole，Join by defaulttestRole================
		for (SysPermission po : permissionList) {
//			// TODO URLThere is a problem with the rules？
//			if (oConvertUtils.isNotEmpty(po.getUrl())) {
//				permissionSet.add(po.getUrl());
//			}
			if (oConvertUtils.isNotEmpty(po.getPerms())) {
				permissionSet.add(po.getPerms());
			}
		}
		log.info("-------Read the permissions owned by the user from the databasePerms------userId： "+ userId+",Perms size: "+ (permissionSet==null?0:permissionSet.size()) );
		return permissionSet;
	}

	/**
	 * judgeonline菜one是否有权限
	 * @param onlineAuthDTO
	 * @return
	 */
	@Override
	public boolean hasOnlineAuth(OnlineAuthDTO onlineAuthDTO) {
		String username = onlineAuthDTO.getUsername();
		List<String> possibleUrl = onlineAuthDTO.getPossibleUrl();
		String onlineFormUrl = onlineAuthDTO.getOnlineFormUrl();
		//Query菜one
		LambdaQueryWrapper<SysPermission> query = new LambdaQueryWrapper<SysPermission>();
		query.eq(SysPermission::getDelFlag, 0);
		query.in(SysPermission::getUrl, possibleUrl);
		List<SysPermission> permissionList = sysPermissionMapper.selectList(query);
		if (permissionList == null || permissionList.size() == 0) {
			//没有配置菜one try to findonline表one菜one地址
			SysPermission sysPermission = new SysPermission();
			sysPermission.setUrl(onlineFormUrl);
			int count = sysPermissionMapper.queryCountByUsername(username, sysPermission);
			if(count<=0){
				//update-begin---author:chenrui ---date:20240123  for：[QQYUN-7992]【online】工one申请下的online表one，Not configuredonline表one开发菜one，Operation error: No permissions------------
				sysPermission.setUrl(onlineAuthDTO.getOnlineWorkOrderUrl());
				count = sysPermissionMapper.queryCountByUsername(username, sysPermission);
				if(count<=0) {
					return false;
				}
				//update-end---author:chenrui ---date:20240123  for：[QQYUN-7992]【online】工one申请下的online表one，Not configuredonline表one开发菜one，Operation error: No permissions------------
			}
		} else {
			//try to find到菜one了
			boolean has = false;
			for (SysPermission p : permissionList) {
				int count = sysPermissionMapper.queryCountByUsername(username, p);
				has = has || (count>0);
			}
			if (!has) {
				//没有配置菜one try to findonline表one菜one地址
				SysPermission sysPermission = new SysPermission();
				sysPermission.setUrl(onlineFormUrl);
				int count = sysPermissionMapper.queryCountByUsername(username, sysPermission);
				if (count <= 0) {
					//update-begin---author:chenrui ---date:20240123  for：[QQYUN-7992]【online】工one申请下的online表one，Not configuredonline表one开发菜one，Operation error: No permissions------------
					sysPermission.setUrl(onlineAuthDTO.getOnlineWorkOrderUrl());
					count = sysPermissionMapper.queryCountByUsername(username, sysPermission);
					if (count > 0) {
						has = true;
					}
					//update-end---author:chenrui ---date:20240123  for：[QQYUN-7992]【online】工one申请下的online表one，Not configuredonline表one开发菜one，Operation error: No permissions------------
				} else {
					has = true;
				}
			}
			return has;
		}
		return true;
	}

	/**
	 * Query用户拥有的Role集合 common api The interface implementation inside
	 * @param username
	 * @return
	 */
	@Override
	public Set<String> queryUserRoles(String username) {
		return getUserRoleSet(username);
	}

	@Override
	public Set<String> queryUserRolesById(String userId) {
		return getUserRoleSetById(userId);
	}

	/**
	 * Query用户拥有的权限集合 common api The interface implementation inside
	 * @param userId
	 * @return
	 */
	@Override
	public Set<String> queryUserAuths(String userId) {
		return getUserPermissionSet(userId);
	}

	/**
	 * 36according tomany个用户账号(comma separated)，Query返回many个用户information
	 * @param usernames
	 * @return
	 */
	@Override
	public List<JSONObject> queryUsersByUsernames(String usernames) {
		LambdaQueryWrapper<SysUser> queryWrapper =  new LambdaQueryWrapper<>();
		queryWrapper.in(SysUser::getUsername,usernames.split(","));
		return JSON.parseArray(JSON.toJSONString(userMapper.selectList(queryWrapper))).toJavaList(JSONObject.class);
	}

	@Override
	public List<JSONObject> queryUsersByIds(String ids) {
		LambdaQueryWrapper<SysUser> queryWrapper =  new LambdaQueryWrapper<>();
		queryWrapper.in(SysUser::getId,ids.split(","));
		return JSON.parseArray(JSON.toJSONString(userMapper.selectList(queryWrapper))).toJavaList(JSONObject.class);
	}

	/**
	 * 37according tomany个Department code(comma separated)，Query返回many个departmentinformation
	 * @param orgCodes
	 * @return
	 */
	@Override
	public List<JSONObject> queryDepartsByOrgcodes(String orgCodes) {
		LambdaQueryWrapper<SysDepart> queryWrapper =  new LambdaQueryWrapper<>();
		queryWrapper.in(SysDepart::getOrgCode,orgCodes.split(","));
		return JSON.parseArray(JSON.toJSONString(sysDepartService.list(queryWrapper))).toJavaList(JSONObject.class);
	}

	@Override
	public List<JSONObject> queryDepartsByIds(String ids) {
		LambdaQueryWrapper<SysDepart> queryWrapper =  new LambdaQueryWrapper<>();
		queryWrapper.in(SysDepart::getId,ids.split(","));
		return JSON.parseArray(JSON.toJSONString(sysDepartService.list(queryWrapper))).toJavaList(JSONObject.class);
	}

	/**
     * 发information
     *
     * @param fromUser
     * @param toUser
     * @param title
     * @param msgContent
     * @param setMsgCategory
     * @param noticeType
     */
	private void sendSysAnnouncement(String fromUser, String toUser, String title, String msgContent, String setMsgCategory, String noticeType) {
		SysAnnouncement announcement = new SysAnnouncement();
		announcement.setTitile(title);
		announcement.setMsgContent(msgContent);
		announcement.setSender(fromUser);
		announcement.setPriority(CommonConstant.PRIORITY_M);
		announcement.setMsgType(CommonConstant.MSG_TYPE_UESR);
		announcement.setSendStatus(CommonConstant.HAS_SEND);
		announcement.setSendTime(new Date());
		announcement.setMsgCategory(setMsgCategory);
		announcement.setDelFlag(String.valueOf(CommonConstant.DEL_FLAG_0));
		//update-begin-author:liusq---date:2025-07-01--for: [QQYUN-12999]systemnotify，systemnotify时间更新，But it's ranked lower
		announcement.setIzTop(CommonConstant.IZ_TOP_0);
		//update-end-author:liusq---date:2025-07-01--for: [QQYUN-12999]systemnotify，systemnotify时间更新，But it's ranked lower
		if(oConvertUtils.isEmpty(noticeType)){
            noticeType = NoticeTypeEnum.NOTICE_TYPE_SYSTEM.getValue();
        }
        announcement.setNoticeType(noticeType);
		sysAnnouncementMapper.insert(announcement);
		// 2.Insert user notification reading mark table record
		String userId = toUser;
		String[] userIds = userId.split(",");
		String anntId = announcement.getId();
		for(int i=0;i<userIds.length;i++) {
			if(oConvertUtils.isNotEmpty(userIds[i])) {
				SysUser sysUser = userMapper.getUserByName(userIds[i]);
				if(sysUser==null) {
					continue;
				}
				SysAnnouncementSend announcementSend = new SysAnnouncementSend();
				announcementSend.setAnntId(anntId);
				announcementSend.setUserId(sysUser.getId());
				announcementSend.setReadFlag(CommonConstant.NO_READ_FLAG);
				sysAnnouncementSendMapper.insert(announcementSend);
				JSONObject obj = new JSONObject();
				obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_USER);
				obj.put(WebsocketConst.MSG_USER_ID, sysUser.getId());
				obj.put(WebsocketConst.MSG_ID, announcement.getId());
				obj.put(WebsocketConst.MSG_TXT, announcement.getTitile());
				obj.put(CommonConstant.NOTICE_TYPE, noticeType);
				webSocket.sendMessage(sysUser.getId(), obj.toJSONString());
			}
		}

	}

	/**
	 * 发information With business parameters
	 * @param fromUser
	 * @param toUser
	 * @param title
	 * @param msgContent
	 * @param setMsgCategory
	 * @param busType
	 * @param busId
	 * @param noticeType information类型,for [JHHB-136]【vue3】协同工作systeminformation需要添加一个类型
	 */
	private void sendBusAnnouncement(String fromUser, String toUser, String title, String msgContent, String setMsgCategory, String busType, String busId, String noticeType) {
		SysAnnouncement announcement = new SysAnnouncement();
		announcement.setTitile(title);
		announcement.setMsgContent(msgContent);
		announcement.setSender(fromUser);
		announcement.setPriority(CommonConstant.PRIORITY_M);
		announcement.setMsgType(CommonConstant.MSG_TYPE_UESR);
		announcement.setSendStatus(CommonConstant.HAS_SEND);
		announcement.setSendTime(new Date());
		announcement.setMsgCategory(setMsgCategory);
		announcement.setDelFlag(String.valueOf(CommonConstant.DEL_FLAG_0));
		announcement.setBusId(busId);
		announcement.setBusType(busType);
		announcement.setOpenType(SysAnnmentTypeEnum.getByType(busType).getOpenType());
		announcement.setOpenPage(SysAnnmentTypeEnum.getByType(busType).getOpenPage());
		//update-begin---author:chenrui ---date:20250807  for：[JHHB-136]【vue3】协同工作systeminformation需要添加一个类型------------
		if(oConvertUtils.isEmpty(noticeType)){
			noticeType = NoticeTypeEnum.NOTICE_TYPE_FLOW.getValue();
		}
		announcement.setNoticeType(noticeType);
		//update-end---author:chenrui ---date:20250807  for：[JHHB-136]【vue3】协同工作systeminformation需要添加一个类型------------
		//update-begin-author:liusq---date:2025-07-01--for: [QQYUN-12999]systemnotify，systemnotify时间更新，But it's ranked lower
		announcement.setIzTop(CommonConstant.IZ_TOP_0);
		//update-end-author:liusq---date:2025-07-01--for: [QQYUN-12999]systemnotify，systemnotify时间更新，But it's ranked lower
		sysAnnouncementMapper.insert(announcement);
		// 2.Insert user notification reading mark table record
		String userId = toUser;
		String[] userIds = userId.split(",");
		String anntId = announcement.getId();
		for(int i=0;i<userIds.length;i++) {
			if(oConvertUtils.isNotEmpty(userIds[i])) {
				SysUser sysUser = userMapper.getUserByName(userIds[i]);
				if(sysUser==null) {
					continue;
				}
				SysAnnouncementSend announcementSend = new SysAnnouncementSend();
				announcementSend.setAnntId(anntId);
				announcementSend.setUserId(sysUser.getId());
				announcementSend.setReadFlag(CommonConstant.NO_READ_FLAG);
				sysAnnouncementSendMapper.insert(announcementSend);
				JSONObject obj = new JSONObject();
				obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_USER);
				obj.put(WebsocketConst.MSG_USER_ID, sysUser.getId());
				obj.put(WebsocketConst.MSG_ID, announcement.getId());
				obj.put(WebsocketConst.MSG_TXT, announcement.getTitile());
				obj.put(CommonConstant.NOTICE_TYPE, NoticeTypeEnum.NOTICE_TYPE_FLOW.getValue());
				webSocket.sendMessage(sysUser.getId(), obj.toJSONString());
			}
		}
	}

	/**
	 * send邮件information
	 * @param email
	 * @param title
	 * @param content
	 */
	@Override
	public void sendEmailMsg(String email, String title, String content) {
			EmailSendMsgHandle emailHandle=new EmailSendMsgHandle();
			emailHandle.sendMsg(email, title, content);
	}
	/**
	 * send短信information
	 * @param phone Phone number
	 * @param param Template parameters
	 * @param dySmsEnum SMS template
	 */
	@Override
	public void sendSmsMsg(String phone, JSONObject param,DySmsEnum dySmsEnum) {
        try {
			log.info(" send短信information phone = {}", phone);
			log.info(" send短信information param = {}", param);
            DySmsHelper.sendSms(phone, param,dySmsEnum);
        } catch (ClientException e) {
			e.printStackTrace();
        }
    }

	/**
	 * sendhtml模版邮件information
	 * @param email
	 * @param title
	 * @param emailTemplateEnum
	 * @param params
	 */
	@Override
	public void sendHtmlTemplateEmail(String email, String title, EmailTemplateEnum emailTemplateEnum,JSONObject params) {
		EmailSendMsgHandle emailHandle=new EmailSendMsgHandle();
		String htmlText="";
		try{
			//Get模板实例
			Configuration configuration = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
			//Set the directory for template files
			String url = emailTemplateEnum.getUrl();
			configuration.setClassForTemplateLoading(this.getClass(), url.substring(0,url.lastIndexOf("/")));
			configuration.setDefaultEncoding("UTF-8");
			//Null value error setting
			configuration.setClassicCompatible(true);
			configuration.setNewBuiltinClassResolver(TemplateClassResolver.SAFER_RESOLVER);
			Template template = configuration.getTemplate(url.substring(url.lastIndexOf("/")));
			//Parse template files
			htmlText= FreeMarkerTemplateUtils.processTemplateIntoString(template, params);
		}catch(IOException e){
			e.printStackTrace();
		}catch(TemplateException e){
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}

		log.debug("Email Html Text：{}", htmlText);
		emailHandle.sendMsg(email, title, htmlText);
	}

	/**
	 * Get公司下级department和所有用户idinformation
	 * @param orgCode
	 * @return
	 */
	@Override
	public List<Map> getDeptUserByOrgCode(String orgCode) {
		//1.Get公司information
		SysDepart comp=sysDepartService.queryCompByOrgCode(orgCode);
		if(comp!=null){
			//2.Get公司下级department
			List<SysDepart> departs=sysDepartService.queryDeptByPid(comp.getId());
			//3.Getdepartment下的人员information
			 List<Map> list=new ArrayList();
			 //4.Processing of departmental and subordinate user data
			for (SysDepart dept:departs) {
				Map map=new HashMap(5);
				//Department name
				String departName = dept.getDepartName();
				//according toDepartment codeGet下级departmentid
				List<String> listIds = departMapper.getSubDepIdsByDepId(dept.getId());
				//According to subordinate departmentsidsGet下级department的所有用户
				List<SysUserDepart> userList = sysUserDepartService.list(new QueryWrapper<SysUserDepart>().in("dep_id",listIds));
				List<String> userIds = new ArrayList<>();
				for(SysUserDepart userDepart : userList){
					if(!userIds.contains(userDepart.getUserId())){
						userIds.add(userDepart.getUserId());
					}
				}
				map.put("name",departName);
				map.put("ids",userIds);
				list.add(map);
			}
			return list;
		}
		return null;
	}

	/**
	 * Query分类dictionary翻译
	 *
	 * @param ids Classification dictionary tableid
	 * @return
	 */
	@Override
	public List<String> loadCategoryDictItem(String ids) {
		return sysCategoryService.loadDictItem(ids, false);
	}

	@Override
	public List<String> loadCategoryDictItemByNames(String names, boolean delNotExist) {
		return sysCategoryService.loadDictItemByNames(names, delNotExist);
	}

	/**
	 * according to dictionarycodeLoad dictionarytext
	 *
	 * @param dictCode order：tableName,text,code
	 * @param keys     要Query的key
	 * @return
	 */
	@Override
	public List<String> loadDictItem(String dictCode, String keys) {
		String[] params = dictCode.split(",");
		return sysDictService.queryTableDictByKeys(params[0], params[1], params[2], keys, false);
	}

	@Override
	public Map<String, String> copyLowAppDict(String originalAppId, String appId, String tenantId) {
		Map<String, String> dictCodeMapping = new HashMap<String, String>();
		List<SysDictVo> ls = sysDictService.getDictListByLowAppId(originalAppId);
		for (SysDictVo vo : ls) {
			vo.setId(null);
			vo.setLowAppId(appId);
			vo.setTenantId(oConvertUtils.getInt(tenantId, null));
			String newDictCode = sysDictService.addDictByLowAppId(vo);
			dictCodeMapping.put(vo.getDictCode(), newDictCode);
		}

		log.info(" --- Batch copy the dictionaries under the application to the new tenant —— Original applicationID:{}，new applicationID:{}，new tenantID：{}，Number of dictionaries：{} --- ", originalAppId, appId, tenantId, dictCodeMapping.size());
		return dictCodeMapping;
	}

	/**
	 * according to dictionarycodeQuerydictionary项
	 *
	 * @param dictCode order：tableName,text,code
	 * @param dictCode 要Query的key
	 * @return
	 */
	@Override
	public List<DictModel> getDictItems(String dictCode) {
		List<DictModel> ls = sysDictService.getDictItems(dictCode);
		if (ls == null) {
			ls = new ArrayList<>();
		}
		return ls;
	}

	/**
	 * according tomany个dictionarycodeQuerymany个dictionary项
	 *
	 * @param dictCodeList
	 * @return key = dictCode ； value=corresponding dictionary entry
	 */
	@Override
	public Map<String, List<DictModel>> getManyDictItems(List<String> dictCodeList) {
		return sysDictService.queryDictItemsByCodeList(dictCodeList);
	}

	/**
	 * 【Drop down search】
	 * Dictionary table with large data volume Go asynchronous loading，That is, the front-end input content filters the data
	 *
	 * @param dictCode dictionarycodeFormat：table,text,code
	 * @param keyword  filter keywords
	 * @return
	 */
	@Override
	public List<DictModel> loadDictItemByKeyword(String dictCode, String keyword, Integer pageNo, Integer pageSize) {
		return sysDictService.loadDict(dictCode, keyword,pageNo, pageSize);
	}

	@Override
	public Map<String, List<DictModel>> translateManyDict(String dictCodes, String keys) {
		List<String> dictCodeList = Arrays.asList(dictCodes.split(","));
		List<String> values = Arrays.asList(keys.split(","));
		//update-begin---author:chenrui ---date:20231221  for：[issues/#5643]解决分布式下表dictionary跨库无法Query问题------------
		return sysDictService.queryManyDictByKeys(dictCodeList, values);
		//update-end---author:chenrui ---date:20231221  for：[issues/#5643]解决分布式下表dictionary跨库无法Query问题------------
	}

	//update-begin---author:chenrui ---date:20231221  for：[issues/#5643]解决分布式下表dictionary跨库无法Query问题------------
	@Override
	public List<DictModel> translateDictFromTableByKeys(String table, String text, String code, String keys, String dataSource) {
		return sysDictService.queryTableDictTextByKeys(table, text, code, Arrays.asList(keys.split(",")), dataSource);
	}
	//update-end---author:chenrui ---date:20231221  for：[issues/#5643]解决分布式下表dictionary跨库无法Query问题------------

	//-------------------------------------process节点send模板information-----------------------------------------------
	@Autowired
	private QywxSendMsgHandle qywxSendMsgHandle;

	@Autowired
	private SystemSendMsgHandle systemSendMsgHandle;

	@Autowired
	private EmailSendMsgHandle emailSendMsgHandle;

	@Autowired
	private DdSendMsgHandle ddSendMsgHandle;

	@Override
	public void sendTemplateMessage(MessageDTO message) {
		String messageType = message.getType();
		log.debug(" 【universal】pushinformation messageType = {}", messageType);
		//update-begin-author:taoyan date:2022-7-9 for: 将模板parse代码移至informationsend, rather than the place where it is called
		String templateCode = message.getTemplateCode();
		if(oConvertUtils.isNotEmpty(templateCode)){
			SysMessageTemplate templateEntity = getTemplateEntity(templateCode);
			boolean isMarkdown = CommonConstant.MSG_TEMPLATE_TYPE_MD.equals(templateEntity.getTemplateType());
			String content = templateEntity.getTemplateContent();
			if(oConvertUtils.isNotEmpty(content) && null!=message.getData()){
				content = FreemarkerParseFactory.parseTemplateContent(content, message.getData(), isMarkdown);
			}
			message.setIsMarkdown(isMarkdown);
			message.setContent(content);
		}
		if(oConvertUtils.isEmpty(message.getContent())){
			log.error("sendinformation失败,information内容is empty！");
			throw new JeecgBootException("sendinformation失败,information内容is empty！");
		}

		//update-end-author:taoyan date:2022-7-9 for: 将模板parse代码移至informationsend, rather than the place where it is called
		if(MessageTypeEnum.XT.getType().equals(messageType)){
			if (message.isMarkdown()) {
				// systeminformation要parseMarkdown
				message.setContent(HTMLUtils.parseMarkdown(message.getContent()));
			}
			systemSendMsgHandle.sendMessage(message);
		}else if(MessageTypeEnum.YJ.getType().equals(messageType)){
			if (message.isMarkdown()) {
				// 邮件information要parseMarkdown
				message.setContent(HTMLUtils.parseMarkdown(message.getContent()));
			}
			//update-begin---author:wangshuai---date:2024-11-20---for:【QQYUN-8523】Send email notification via KnockKou Cloud，unstable---
			if(message.getIsTimeJob() != null && message.getIsTimeJob()){
				emailSendMsgHandle.sendEmailMessage(message);
			}else{
				emailSendMsgHandle.sendMessage(message);
			}
			//update-end---author:wangshuai---date:2024-11-20---for:【QQYUN-8523】Send email notification via KnockKou Cloud，unstable---
		}else if(MessageTypeEnum.DD.getType().equals(messageType)){
			ddSendMsgHandle.sendMessage(message);
		}else if(MessageTypeEnum.QYWX.getType().equals(messageType)){
			qywxSendMsgHandle.sendMessage(message);
		}
	}

	@Override
	public String getTemplateContent(String code) {
		List<SysMessageTemplate> list = sysMessageTemplateService.selectByCode(code);
		if(list==null || list.size()==0){
			return null;
		}
		return list.get(0).getTemplateContent();
	}

	/**
	 * GetTemplate content，parsemarkdown
	 *
	 * @param code
	 * @return
	 */
	public SysMessageTemplate getTemplateEntity(String code) {
		List<SysMessageTemplate> list = sysMessageTemplateService.selectByCode(code);
		if (list == null || list.size() == 0) {
			return null;
		}
		return list.get(0);
	}

	//-------------------------------------process节点send模板information-----------------------------------------------

	@Override
	public void saveDataLog(DataLogDTO dataLogDto) {
		try {
			SysDataLog entity = new SysDataLog();
			entity.setDataTable(dataLogDto.getTableName());
			entity.setDataId(dataLogDto.getDataId());
			entity.setDataContent(dataLogDto.getContent());
			entity.setType(dataLogDto.getType());
			entity.setDataVersion("1");
			if (oConvertUtils.isNotEmpty(dataLogDto.getCreateName())) {
				entity.setCreateBy(dataLogDto.getCreateName());
			} else {
				entity.autoSetCreateName();
			}
			sysDataLogService.save(entity);
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
			//e.printStackTrace();
		}
	}

    @Override
    public void updateAvatar(LoginUser loginUser) {
		SysUser sysUser = new SysUser();
		// createUpdateWrapperobject
		UpdateWrapper<SysUser> updateWrapper = new UpdateWrapper<>();
		updateWrapper.eq("id", loginUser.getId()); // Set update conditions
		sysUser.setAvatar(loginUser.getAvatar()); // 设置要更新fields
		sysUserService.update(sysUser, updateWrapper);
    }

	@Override
	public void sendAppChatSocket(String userId) {
		JSONObject obj = new JSONObject();
		obj.put(WebsocketConst.MSG_CMD, WebsocketConst.MSG_CHAT);
		obj.put(WebsocketConst.MSG_USER_ID, userId);
		webSocket.sendMessage(userId, obj.toJSONString());
	}

	@Override
	public String getRoleCodeById(String id) {
		SysRole role = roleMapper.selectById(id);
		if(role!=null){
			return role.getRoleCode();
		}
		return null;
	}

	@Override
	public List<DictModel> queryRoleDictByCode(String roleCodes) {
		if (oConvertUtils.isEmpty(roleCodes)) {
			return new ArrayList<>();
		}
		List<String> codeList = Arrays.asList(roleCodes.split(","));
		LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.in(SysRole::getRoleCode, codeList);
		List<SysRole> list = roleMapper.selectList(queryWrapper);
		// converted toSysRoleVo
		return list.stream().map(sysRole -> {
			DictModel model = new DictModel();
			model.setText(sysRole.getRoleName());
			model.setValue(sysRole.getRoleCode());
			return model;
		}).collect(Collectors.toList());
	}

	@Override
	public List<String> queryUserIdsByDeptIds(List<String> deptIds) {
		QueryWrapper<SysUserDepart> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().select(SysUserDepart::getUserId).in(true,SysUserDepart::getDepId,deptIds);
		return sysUserDepartService.listObjs(queryWrapper,e->e.toString());
	}

    @Override
    public List<String> queryUserIdsByCascadeDeptIds(List<String> deptIds) {
        Set<String> userIds = new HashSet<>();
        List<SysDepart> departs = sysDepartService.list(Wrappers.lambdaQuery(SysDepart.class)
                .select(SysDepart::getOrgCode)
                .in(SysDepart::getId, deptIds));
        departs.forEach(depart -> {
            List<SysUser> sysUsers = sysUserDepartService.queryUserByDepCode(depart.getOrgCode(), null);
            if(oConvertUtils.isObjectNotEmpty(sysUsers)){
                userIds.addAll(sysUsers.stream().map(SysUser::getId).collect(Collectors.toSet()));
            }
        });
        return new ArrayList<>(userIds);
    }

    @Override
	public List<String> queryUserAccountsByDeptIds(List<String> deptIds) {
		return departMapper.queryUserAccountByDepartIds(deptIds);
	}

	@Override
	public List<String> queryUserIdsByRoleds(List<String> roleCodes) {
		LambdaQueryWrapper<SysRole> query = new LambdaQueryWrapper<SysRole>()
				.in(SysRole::getRoleCode, roleCodes);
		List<SysRole> roleList = sysRoleService.list(query);
		if(roleList!=null && roleList.size()>0){
			List<String> idList = roleList.stream().map(role->role.getId()).collect(Collectors.toList());
			QueryWrapper<SysUserRole> queryWrapper = new QueryWrapper<>();
			queryWrapper.lambda().select(SysUserRole::getUserId).in(true,SysUserRole::getRoleId, idList);
			return sysUserRoleService.listObjs(queryWrapper,e->e.toString());
		}
		return null;
	}

	@Override
	public List<String> queryUserIdsByPositionIds(List<String> positionIds) {
		QueryWrapper<SysUserPosition> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().select(SysUserPosition::getUserId).in(true,SysUserPosition::getPositionId,positionIds);
		return sysUserPositionService.listObjs(queryWrapper,e->e.toString());
	}

	//update-begin-author:taoyan date:2023-2-21 for: Solving parameter order issues
	/**
	 * Get带参数的报表地址，因为many个参数可能order会变，所以要将参数order重排，Get所有可能的地址集合
	 * as follows：参数order调整使用inQuery，就能Query出菜one数据
	 *  /online/cgreport/1624393012494286850?name=1&age=16
	 *  /online/cgreport/1624393012494286850?age=16&name=1
	 * @param path
	 * @return
	 */
	private List<String> getOnlinePossiblePaths(String path){
		List<String> result = new ArrayList<>();
		log.info(" path = "+ path);
		if (path.indexOf("?") >= 0 && (path.contains("/online/cgreport/") || path.contains("/online/cgformList/") || path.contains("/online/graphreport/"))) {
			//Include?说明有many个参数
			String[] pathArray = path.split("\\?");
			if(oConvertUtils.isNotEmpty(pathArray[1])){
				String[] params = pathArray[1].split("&");
				if(params.length==1){
					result.add(path);
				}else{
					result = anm(pathArray[0], Arrays.asList(params));
				}
			}else{
				result.add(path);
			}
		}else{
			result.add(path);
		}
		return result;
	}


	/**
	 * Get数组元素的 Different arrangements a(n,m)
	 * @param list
	 * @return
	 */
	private List<String> anm(String baseUrl, List<String> list) {
		int len = list.size();
		int[] destArray = new int[len];
		for (int i = 0; i < len; i++) {
			destArray[i] = i;
		}
		int[] temp = new int[len];
		List<String> result = new ArrayList<>();
		while (temp[0] < len) {
			temp[temp.length - 1]++;
			for (int i = temp.length - 1; i > 0; i--) {
				if (temp[i] == len) {
					temp[i] = 0;
					temp[i - 1]++;
				}
			}
			int[] tt = temp.clone();
			Arrays.sort(tt);
			if (!Arrays.equals(tt, destArray)) {
				continue;
			}
			String str = "";
			for (int i = 0; i < len; i++) {
				if(i>0){
					str += "&";
				}
				str += list.get(temp[i]);
			}
			result.add(baseUrl+"?"+str);
		}
		return result;
	}
	//update-end-author:taoyan date:2023-2-21 for: Solving parameter order issues

	@Override
	public List<String> getUserAccountsByDepCode(String orgCode) {
		return userMapper.getUserAccountsByDepCode(orgCode);
	}

	@Override
	public boolean dictTableWhiteListCheckBySql(String selectSql) {
		return dictTableWhiteListHandler.isPassBySql(selectSql);
	}

	@Override
	public boolean dictTableWhiteListCheckByDict(String tableOrDictCode, String... fields) {
		if (fields == null || fields.length == 0) {
			return dictTableWhiteListHandler.isPassByDict(tableOrDictCode);
		} else {
			return dictTableWhiteListHandler.isPassByDict(tableOrDictCode, fields);
		}
	}

	/**
	 * Automated publishing process
	 * @param dataId
	 * @param currentUserName
	 */
	@Override
	public void announcementAutoRelease(String dataId, String currentUserName) {
		//according toIDQuerynotify公告
		SysAnnouncement sysAnnouncement = sysAnnouncementService.getById(dataId);
		//Automatically publish announcements after passing the process
		sysAnnouncement.setSendStatus(CommonSendStatus.PUBLISHED_STATUS_1);
		sysAnnouncement.setSendTime(new Date());
		sysAnnouncement.setSender(currentUserName);
		boolean ok = sysAnnouncementService.updateById(sysAnnouncement);
		//pushnotifyinformation
		if(ok) {
			if(sysAnnouncement.getMsgType().equals(CommonConstant.MSG_TYPE_ALL)) {
				// Complete the previous relationship between the announcement and the user
				sysAnnouncementService.batchInsertSysAnnouncementSend(sysAnnouncement.getId(), sysAnnouncement.getTenantId());

				// pushwebsocketnotify
				JSONObject obj = new JSONObject();
				obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_TOPIC);
				obj.put(WebsocketConst.MSG_ID, sysAnnouncement.getId());
				obj.put(WebsocketConst.MSG_TXT, sysAnnouncement.getTitile());
				webSocket.sendMessage(obj.toJSONString());
			}else {
				// 2.Insert user notification reading mark table record
				String userId = sysAnnouncement.getUserIds();
				String[] userIds = userId.substring(0, (userId.length()-1)).split(",");
				JSONObject obj = new JSONObject();
				obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_USER);
				obj.put(WebsocketConst.MSG_ID, sysAnnouncement.getId());
				obj.put(WebsocketConst.MSG_TXT, sysAnnouncement.getTitile());
				webSocket.sendMessage(userIds, obj.toJSONString());
			}
			try {
				// Synchronize enterprise WeChat、钉钉的informationnotify
				Response<String> dtResponse = dingtalkService.sendActionCardMessage(sysAnnouncement, null, true);
				wechatEnterpriseService.sendTextCardMessage(sysAnnouncement, null,true);

				if (dtResponse != null && dtResponse.isSuccess()) {
					String taskId = dtResponse.getResult();
					sysAnnouncement.setDtTaskId(taskId);
					sysAnnouncementService.updateById(sysAnnouncement);
				}
			} catch (Exception e) {
				log.error("Send to third party simultaneouslyAPPinformation失败：", e);
			}
		}
	}

	@Override
	public SysDepartModel queryCompByOrgCode(String orgCode) {
		AssertUtils.assertNotEmpty("Please enter department code",orgCode);
		SysDepart comp = sysDepartService.queryCompByOrgCode(orgCode);
		if(comp == null) {
			log.error("未Query到对应的公司information");
			return null;
		}
		SysDepartModel respData = new SysDepartModel();
		BeanUtils.copyProperties(comp, respData);
		return respData;
	}

    /**
     * according toDepartment code和层次Query上级公司
     * 
     * @param orgCode Department code
     * @param level Can pass empty Default is1 The minimum value is1
     * @return
     */
    @Override
    public SysDepartModel queryCompByOrgCodeAndLevel(String orgCode, Integer level) {
        if (null == level || 0 == level) {
            level = 1;
        }
        int codeNum = YouBianCodeUtil.ZHANWEI_LENGTH;

        //先judge父级code
        String parendCode = "";
        if (orgCode.length() > codeNum) {
            parendCode = orgCode.substring(0, codeNum);
        } else {
            return null;
        }
        //according toDepartment codeQuery公司和子公司的数据
        List<String> categoryList = new ArrayList<>();
        categoryList.add(DepartCategoryEnum.DEPART_CATEGORY_COMPANY.getValue());
        categoryList.add(DepartCategoryEnum.DEPART_CATEGORY_SUB_COMPANY.getValue());
        LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<>();
        query.like(SysDepart::getOrgCode, parendCode);
        query.in(SysDepart::getOrgCategory, categoryList);
        query.orderByAsc(SysDepart::getOrgType);
        List<SysDepart> sysDepartList = sysDepartService.list(query);
        if (!CollectionUtils.isEmpty(sysDepartList)) {
            //Get上级公司
            SysDepart depart = getParentCompanyByOrgCode(orgCode, sysDepartList, level, 1);
            if(depart == null){
                depart = sysDepartList.get(0);
            }
            SysDepartModel respData = new SysDepartModel();
            BeanUtils.copyProperties(depart, respData);
            return respData;
        }
        return null;
    }

    @Override
    public Object runAiragFlow(AiragFlowDTO airagFlowDTO) {
        if(oConvertUtils.isEmpty(airagFlowDTO.getFlowId())) {
            throw new JeecgBootException("processID不能is empty");
        }
        FlowRunParams params = new FlowRunParams();
        params.setFlowId(airagFlowDTO.getFlowId());
        params.setInputParams(airagFlowDTO.getInputParams());
        params.setResponseMode("blocking");
        Result<Object> o = (Result<Object>)airagFlowService.runFlow(params);
        return o.getResult();
    }

    /**
     * according toorgCodetry to find上级
     * 
     * @param orgCode
     * @param sysDepartList
     * @param level Specify which level from bottom to top
     * @param nowLevel
     * @return
     */
    public SysDepart getParentCompanyByOrgCode(String orgCode,List<SysDepart> sysDepartList, int level, int nowLevel) {
        //Get上一级公司的编码
        String code = this.getPrefix(orgCode);
        if(oConvertUtils.isEmpty(code)) {
            return null;
        }
        List<SysDepart> list = sysDepartList.stream().filter(sysDepart -> sysDepart.getOrgCode().equals(code)).toList();
        //judge去上级的级别
        if(!CollectionUtils.isEmpty(list) && nowLevel == level) {
            return list.get(0);
        } else {
            if(!CollectionUtils.isEmpty(list)) {
                nowLevel++;
            }
            return getParentCompanyByOrgCode(code, sysDepartList, level, nowLevel);
        }
    }

    /**
     * according to指定值Get编码前缀（Fixed at each levelYouBianCodeUtil.ZHANWEI_LENGTHBit）
     *
     * @param fullCode full encoding（like"A01A01A01"）
     * @return Extracted prefix encoding（like"A01A01"）
     */
    private String getPrefix(String fullCode) {
        if(fullCode.length() < YouBianCodeUtil.ZHANWEI_LENGTH){
            return "";
        }
        // Calculate the total number of levels,according toZHANWEI_LENGTH
        int totalLevels = fullCode.length() / YouBianCodeUtil.ZHANWEI_LENGTH;
        int keepLevels = totalLevels - 1;
        // Calculate the length to be cut（Keep the number of levels × YouBianCodeUtil.ZHANWEI_LENGTH）
        int prefixLength = keepLevels * YouBianCodeUtil.ZHANWEI_LENGTH;
        return prefixLength == 0 ? "" : fullCode.substring(0, prefixLength);
    }

    /**
     * according todepartmentcodeor departmentidGetDepartment name(Current and parent departments)
     *
     * @param orgCode Department code
     * @param depId departmentid
     * @return String Department name
     */
    @Override
    public String getDepartPathNameByOrgCode(String orgCode, String depId) {
        return sysDepartService.getDepartPathNameByOrgCode(orgCode, depId);
    }
}