package org.jeecg.modules.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.exceptions.ClientException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.dto.message.MessageDTO;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.FillRuleConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.constant.PasswordConstant;
import org.jeecg.common.constant.enums.*;
import org.jeecg.common.desensitization.annotation.SensitiveEncode;
import org.jeecg.common.exception.JeecgBootBizTipException;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.system.vo.SysUserCacheInfo;
import org.jeecg.common.util.*;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.modules.jmreport.common.util.OkConvertUtils;
import org.jeecg.modules.message.handle.impl.SystemSendMsgHandle;
import org.jeecg.modules.system.entity.*;
import org.jeecg.modules.system.mapper.*;
import org.jeecg.modules.system.model.SysUserSysDepPostModel;
import org.jeecg.modules.system.model.SysUserSysDepartModel;
import org.jeecg.modules.system.service.*;
import org.jeecg.modules.system.util.ImportSysUserCache;
import org.jeecg.modules.system.vo.*;
import org.jeecg.modules.system.vo.lowapp.AppExportUserVo;
import org.jeecg.modules.system.vo.lowapp.DepartAndUserInfo;
import org.jeecg.modules.system.vo.lowapp.DepartInfo;
import org.jeecg.modules.system.vo.lowapp.UpdateDepartInfo;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * User table Service implementation class
 * </p>
 *
 * @Author: scott
 * @Date: 2018-12-20
 */
@Service
@Slf4j
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {
	
	@Autowired
	private SysUserMapper userMapper;
	@Autowired
	private SysPermissionMapper sysPermissionMapper;
	@Autowired
	private SysUserRoleMapper sysUserRoleMapper;
	@Autowired
	private SysUserDepartMapper sysUserDepartMapper;
	@Autowired
	private SysDepartMapper sysDepartMapper;
	@Autowired
	private SysRoleMapper sysRoleMapper;
	@Autowired
	private SysDepartRoleUserMapper departRoleUserMapper;
	@Autowired
	private SysDepartRoleMapper sysDepartRoleMapper;
	@Resource
	private BaseCommonService baseCommonService;
	@Autowired
	private SysThirdAccountMapper sysThirdAccountMapper;
	@Autowired
	ThirdAppWechatEnterpriseServiceImpl wechatEnterpriseService;
	@Autowired
	ThirdAppDingtalkServiceImpl dingtalkService;
	@Autowired
	ISysRoleIndexService sysRoleIndexService;
	@Autowired
	SysTenantMapper sysTenantMapper;
	@Autowired
    private SysUserTenantMapper relationMapper;
	@Autowired
    private SysUserTenantMapper userTenantMapper;
	@Autowired
	private SysUserPositionMapper sysUserPositionMapper;
	@Autowired
	private SysPositionMapper sysPositionMapper;
	@Autowired
	private SystemSendMsgHandle systemSendMsgHandle;
	
	@Autowired
	private ISysThirdAccountService sysThirdAccountService;
	@Autowired
	private RedisUtil redisUtil;
    
    @Autowired
    private SysTenantPackUserMapper packUserMapper;
    
    @Autowired
    private SysUserDepPostMapper depPostMapper;
	
	@Override
	public Result<IPage<SysUser>> queryPageList(HttpServletRequest req, QueryWrapper<SysUser> queryWrapper, Integer pageSize, Integer pageNo) {
		Result<IPage<SysUser>> result = new Result<IPage<SysUser>>();
		//update-begin-Author:wangshuai--Date:20211119--for:【vue3】by departmentidQuery user，passcodeQueryid
		//departmentID
		String departId = req.getParameter("departId");
		if (oConvertUtils.isNotEmpty(departId)) {
			LambdaQueryWrapper<SysUserDepart> query = new LambdaQueryWrapper<>();
			query.eq(SysUserDepart::getDepId, departId);
			List<SysUserDepart> list = sysUserDepartMapper.selectList(query);
			List<String> userIds = list.stream().map(SysUserDepart::getUserId).collect(Collectors.toList());
			//update-begin---author:wangshuai ---date:20220322  for：[issues/I4XTYB]Query user时，当departmentid The interface reports an error when no user is assigned------------
			if (oConvertUtils.listIsNotEmpty(userIds)) {
				queryWrapper.in("id", userIds);
			} else {
				return Result.OK();
			}
			//update-end---author:wangshuai ---date:20220322  for：[issues/I4XTYB]Query user时，当departmentid The interface reports an error when no user is assigned------------
		}
		//userID
		String code = req.getParameter("code");
		if (oConvertUtils.isNotEmpty(code)) {
			queryWrapper.in("id", Arrays.asList(code.split(",")));
			pageSize = code.split(",").length;
		}
		//update-end-Author:wangshuai--Date:20211119--for:【vue3】by departmentidQuery user，passcodeQueryid

		//update-begin-author:taoyan--date:20220104--for: JTC-372 【user冻结问题】 onlineAuthorize、user组件，选择user都能看到被冻结的user
		String status = req.getParameter("status");
		if (oConvertUtils.isNotEmpty(status)) {
			queryWrapper.eq("status", Integer.parseInt(status));
		}
		//update-end-author:taoyan--date:20220104--for: JTC-372 【user冻结问题】 onlineAuthorize、user组件，选择user都能看到被冻结的user

		//update-begin---author:wangshuai---date:2024-03-08---for:【QQYUN-8110】Online address book supports setting permissions(You can only view assigned technical support)---
		String tenantId = TokenUtils.getTenantIdByRequest(req);
		String lowAppId = TokenUtils.getLowAppIdByRequest(req);
//		Object bean = ResourceUtil.getImplementationClass(DataEnhanceEnum.getClassPath(tenantId,lowAppId));
//		if(null != bean){
//			UserFilterEnhance userEnhanceService = (UserFilterEnhance) bean;
//			LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
//			List<String> userIds = userEnhanceService.getUserIds(sysUser.getId());
//			if(CollectionUtil.isNotEmpty(userIds)){
//				queryWrapper.in("id", userIds);
//			}
//		}
		//update-end---author:wangshuai---date:2024-03-08---for:【QQYUN-8110】Online address book supports setting permissions(You can only view assigned technical support)---
		
		//TODO External simulated login temporary account，List is not displayed
		queryWrapper.ne("username", "_reserve_user_external");
		Page<SysUser> page = new Page<SysUser>(pageNo, pageSize);
		IPage<SysUser> pageList = this.page(page, queryWrapper);

		//批量Query user的所属department
		//step.1 Get them all first useids
		//step.2 pass useids，一次性Query user的所属department名字
		List<String> userIds = pageList.getRecords().stream().map(SysUser::getId).collect(Collectors.toList());
		if (userIds != null && userIds.size() > 0) {
			Map<String, String> useDepNames = this.getDepNamesByUserIds(userIds);
			pageList.getRecords().forEach(item -> {
				item.setOrgCodeTxt(useDepNames.get(item.getId()));
				//Query user的tenantids
				List<Integer> list = userTenantMapper.getTenantIdsByUserId(item.getId());
				if (oConvertUtils.isNotEmpty(list)) {
					item.setRelTenantIds(StringUtils.join(list.toArray(), SymbolConstant.COMMA));
				} else {
					item.setRelTenantIds("");
				}
				Integer posTenantId = null;
				if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
					posTenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);;		
				}
				//Query userPosition关系表(Get the tenant below)
				//update-begin---author:wangshuai---date:2023-11-15---for:【QQYUN-7028】user职务保存后未回显---
				List<String> positionList =  sysUserPositionMapper.getPositionIdByUserTenantId(item.getId(),posTenantId);
				//update-end---author:wangshuai---date:2023-11-15---for:【QQYUN-7028】user职务保存后未回显---
				//update-end---author:wangshuai ---date:20230228  for：[QQYUN-4354]Add more fields：The current joining time should be that of the current tenant/The position is also under the current tenant------------
				item.setPost(CommonUtils.getSplitText(positionList,SymbolConstant.COMMA));
				
				//update-begin---author:wangshuai---date:2023-10-08---for:【QQYUN-6668】钉钉department和user同步，我怎么知道哪些user是双向绑定成功的---
				//Whether to segregate based on tenant(敲敲云user列表专用，Used to show whether nailing is synchronized)
				if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
					//Queryaccount表是否already同步钉钉
					LambdaQueryWrapper<SysThirdAccount> query = new LambdaQueryWrapper<>();
					query.eq(SysThirdAccount::getSysUserId,item.getId());
					query.eq(SysThirdAccount::getTenantId, tenantId);
					//Currently there is only synchronized DingTalk
					query.eq(SysThirdAccount::getThirdType, MessageTypeEnum.DD.getType());
					//Not empty means DingTalk has been synchronized
					List<SysThirdAccount> account = sysThirdAccountService.list(query);
					if(CollectionUtil.isNotEmpty(account)){
						item.setIzBindThird(true);
					}
				}
				//update-end---author:wangshuai---date:2023-10-08---for:【QQYUN-6668】钉钉department和user同步，我怎么知道哪些user是双向绑定成功的---
                //update-begin---author:wangshuai---date:2025-09-06---for: How to transform part-time positions into intermediate positions---
                //Querydepartment的兼职post
                List<String> depPostList = depPostMapper.getDepPostByUserId(item.getId());
                if(CollectionUtil.isNotEmpty(depPostList)){
                    item.setOtherDepPostId(StringUtils.join(depPostList.toArray(), SymbolConstant.COMMA));
                }
                //update-end---author:wangshuai---date:2025-09-06---for:How to transform part-time positions into intermediate positions---
            });
		}

		result.setSuccess(true);
		result.setResult(pageList);
		//log.info(pageList.toString());
		return result;
	}


    @Override
    @CacheEvict(value = {CacheConstant.SYS_USERS_CACHE}, allEntries = true)
    public Result<?> resetPassword(String username, String oldpassword, String newpassword, String confirmpassword) {
        SysUser user = userMapper.getUserByName(username);
        String passwordEncode = PasswordUtil.encrypt(username, oldpassword, user.getSalt());
        if (!user.getPassword().equals(passwordEncode)) {
            return Result.error("Old password entered incorrectly!");
        }
        if (oConvertUtils.isEmpty(newpassword)) {
            return Result.error("New password is not allowed to be empty!");
        }
        if (!newpassword.equals(confirmpassword)) {
            return Result.error("The password entered twice is inconsistent!");
        }
        String password = PasswordUtil.encrypt(username, newpassword, user.getSalt());
        this.userMapper.update(new SysUser().setPassword(password), new LambdaQueryWrapper<SysUser>().eq(SysUser::getId, user.getId()));
        return Result.ok("Password reset successful!");
    }

    @Override
    @CacheEvict(value = {CacheConstant.SYS_USERS_CACHE}, allEntries = true)
    public Result<?> changePassword(SysUser sysUser) {
        String salt = oConvertUtils.randomGen(8);
        sysUser.setSalt(salt);
        String password = sysUser.getPassword();
        String passwordEncode = PasswordUtil.encrypt(sysUser.getUsername(), password, salt);
        sysUser.setPassword(passwordEncode);
        this.userMapper.updateById(sysUser);
        return Result.ok("Password changed successfully!");
    }

    @Override
    @CacheEvict(value={CacheConstant.SYS_USERS_CACHE}, allEntries=true)
	@Transactional(rollbackFor = Exception.class)
	public boolean deleteUser(String userId) {
		//update-begin---author:wangshuai---date:2024-01-16---for:【QQYUN-7974】adminuser禁止删除---
		//1.验证当前user是管理员account admin
		//验证user是否for管理员
		this.checkUserAdminRejectDel(userId);
		//update-end---author:wangshuai---date:2024-01-16---for:【QQYUN-7974】adminuser禁止删除---
		
		//2.删除user
		this.removeById(userId);
		return false;
	}

	@Override
    @CacheEvict(value={CacheConstant.SYS_USERS_CACHE}, allEntries=true)
	@Transactional(rollbackFor = Exception.class)
	public boolean deleteBatchUsers(String userIds) {
		//1.验证当前user是管理员account admin
		this.checkUserAdminRejectDel(userIds);
		//2.删除user
		this.removeByIds(Arrays.asList(userIds.split(",")));
		return false;
	}

	@Override
	public SysUser getUserByName(String username) {
		SysUser sysUser = userMapper.getUserByName(username);
		//Query user的tenantids
		if(sysUser!=null){
			List<Integer> list = userTenantMapper.getTenantIdsByUserId(sysUser.getId());
			if (oConvertUtils.isNotEmpty(list)) {
				sysUser.setRelTenantIds(StringUtils.join(list.toArray(), SymbolConstant.COMMA));
			} else {
				sysUser.setRelTenantIds("");
			}
		}
		return sysUser;
	}
	
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void addUserWithRole(SysUser user, String roles) {
		this.save(user);
		if(oConvertUtils.isNotEmpty(roles)) {
			String[] arr = roles.split(",");
			for (String roleId : arr) {
				SysUserRole userRole = new SysUserRole(user.getId(), roleId);
				sysUserRoleMapper.insert(userRole);
			}
		}
	}

	@Override
	@CacheEvict(value= {CacheConstant.SYS_USERS_CACHE}, allEntries=true)
	@Transactional(rollbackFor = Exception.class)
	public void editUserWithRole(SysUser user, String roles) {
		this.updateById(user);
		//Delete first and then add
		sysUserRoleMapper.delete(new QueryWrapper<SysUserRole>().lambda().eq(SysUserRole::getUserId, user.getId()));
		if(oConvertUtils.isNotEmpty(roles)) {
			String[] arr = roles.split(",");
			for (String roleId : arr) {
				SysUserRole userRole = new SysUserRole(user.getId(), roleId);
				sysUserRoleMapper.insert(userRole);
			}
		}
	}


	@Override
	public List<String> getRole(String username) {
		return sysUserRoleMapper.getRoleByUserName(username);
	}

	/**
	 * Get dynamic homepage routing configuration
	 *
	 * @param username
	 * @param version
	 * @return
	 */
	@Override
	public SysRoleIndex getDynamicIndexByUserRole(String username, String version) {
		SysRoleIndex roleIndex = new SysRoleIndex();
		//only X-Version=v3 when，Just readsys_role_indexTable to obtain role homepage configuration
		boolean isV3 = CommonConstant.VERSION_V3.equals(version);
		if (isV3) {
			//update-begin-author:liusq---date:2025-07-01--for: [QQYUN-12980] 【Home page configuration】Home page custom configuration function page
			//1.先Query userUSERlevel 的所有Home page configuration
			if(oConvertUtils.isNotEmpty(username)){
				LambdaQueryWrapper<SysRoleIndex> routeIndexUserQuery = new LambdaQueryWrapper<>();
				//Role home page status0：Not turned on  1：turn on
				routeIndexUserQuery.eq(SysRoleIndex::getStatus, CommonConstant.STATUS_1);
				routeIndexUserQuery.eq(SysRoleIndex::getRelationType, CommonConstant.HOME_RELATION_USER);
				routeIndexUserQuery.eq(SysRoleIndex::getRoleCode, username);
				//Priority sorting
				routeIndexUserQuery.orderByAsc(SysRoleIndex::getPriority);
				List<SysRoleIndex> list = sysRoleIndexService.list(routeIndexUserQuery);
				if (CollectionUtils.isNotEmpty(list)) {
					roleIndex = list.get(0);
				}else{
				   //2.user没有配置，再Query RoleROLElevel 的所有Home page configuration
					LambdaQueryWrapper<SysRoleIndex> routeIndexQuery = new LambdaQueryWrapper<>();
					//Role home page status0：Not turned on  1：turn on
					routeIndexQuery.eq(SysRoleIndex::getStatus, CommonConstant.STATUS_1);
					//Role所有Home page configuration
					routeIndexQuery.eq(SysRoleIndex::getRelationType, CommonConstant.HOME_RELATION_ROLE);
					//当前userRole
					List<String> roles = sysUserRoleMapper.getRoleByUserName(username);
					String componentUrl = RoleIndexConfigEnum.getIndexByRoles(roles);
					roleIndex = new SysRoleIndex(componentUrl);
					//user所有Role
					//update-begin-author:liusq---date:2025-07-21--for: [QQYUN-13187]【新user登录Report an error】没有添加Role时 Report an error
					if(CollectionUtil.isNotEmpty(roles)){
						routeIndexQuery.in(SysRoleIndex::getRoleCode, roles);
					}
					//update-end-author:liusq---date:2025-07-21--for: [QQYUN-13187]【新user登录Report an error】没有添加Role时 Report an error
					//Priority sorting
					routeIndexQuery.orderByAsc(SysRoleIndex::getPriority);
					list = sysRoleIndexService.list(routeIndexQuery);
					if (CollectionUtils.isNotEmpty(list)) {
						roleIndex = list.get(0);
					}
				}
			}
			//update-end-author:liusq---date:2025-07-01--for: [QQYUN-12980] 【Home page configuration】Home page custom configuration function page
		}

		if (oConvertUtils.isEmpty(roleIndex.getComponent())) {
			if (isV3) {
				// like果Role没有配置首页，then use the default homepage
				return sysRoleIndexService.queryDefaultIndex();
			} else {
				// Nov3returnnull
				return null;
			}
		}
		return roleIndex;
	}

	/**
	 * passuser名获取userRole集合
	 * @param username user名
     * @return Role集合
	 */
	@Override
	public Set<String> getUserRolesSet(String username) {
		// Query user拥有的Role集合
		List<String> roles = sysUserRoleMapper.getRoleByUserName(username);
		log.info("-------passdata库读取user拥有的RoleRules------username： " + username + ",Roles size: " + (roles == null ? 0 : roles.size()));
		return new HashSet<>(roles);
	}
	
	/**
	 * passuser名获取userRole集合
	 * @param userId userID
     * @return Role集合
	 */
	@Override
	public Set<String> getUserRoleSetById(String userId) {
		// Query user拥有的Role集合
		List<String> roles = sysUserRoleMapper.getRoleCodeByUserId(userId);
		log.info("-------passdata库读取user拥有的RoleRules------userId： " + userId + ",Roles size: " + (roles == null ? 0 : roles.size()));
		return new HashSet<>(roles);
	}

	/**
	 * passuser名获取userPermission set
	 *
	 * @param userId userID
	 * @return Permission set
	 */
	@Override
	public Set<String> getUserPermissionsSet(String userId) {
		Set<String> permissionSet = new HashSet<>();
		List<SysPermission> permissionList = sysPermissionMapper.queryByUser(userId);
		//================= begin turn ontenantwhen if nottestRole，Join by defaulttestRole================
		if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
			if (permissionList == null) {
				permissionList = new ArrayList<>();
			}
			List<SysPermission> testRoleList = sysPermissionMapper.queryPermissionByTestRoleId();
			permissionList.addAll(testRoleList);
		}
		//================= end turn ontenantwhen if nottestRole，Join by defaulttestRole================
		for (SysPermission po : permissionList) {
//			// TODO URLThere is a problem with the rules？
//			if (oConvertUtils.isNotEmpty(po.getUrl())) {
//				permissionSet.add(po.getUrl());
//			}
			if (oConvertUtils.isNotEmpty(po.getPerms())) {
				permissionSet.add(po.getPerms());
			}
		}
		log.info("-------passdata库读取user拥有的权限Perms------userId： "+ userId+",Perms size: "+ (permissionSet==null?0:permissionSet.size()) );
		return permissionSet;
	}

	/**
	 * upgradeSpringBoot2.6.6,Circular dependencies are not allowed
	 * @author:qinfeng
	 * @update: 2022-04-07
	 * @param username
	 * @return
	 */
	@Override
	public SysUserCacheInfo getCacheUser(String username) {
		SysUserCacheInfo info = new SysUserCacheInfo();
		info.setOneDepart(true);
		if(oConvertUtils.isEmpty(username)) {
			return null;
		}

		//Query user信息
		SysUser sysUser = userMapper.getUserByName(username);
		if(sysUser!=null) {
			info.setSysUserCode(sysUser.getUsername());
			info.setSysUserName(sysUser.getRealname());
			info.setSysOrgCode(sysUser.getOrgCode());
		}
		
		//多department支持inQuery
		List<SysDepart> list = sysDepartMapper.queryUserDeparts(sysUser.getId());
		List<String> sysMultiOrgCode = new ArrayList<String>();
		if(list==null || list.size()==0) {
			//当前user无department
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

    /**
     * 根据departmentIdQuery
     * @param page
     * @param departId departmentid
     * @param username user账户名称
     * @return
     */
	@Override
	public IPage<SysUser> getUserByDepId(Page<SysUser> page, String departId,String username) {
		return userMapper.getUserByDepId(page, departId,username);
	}

	@Override
	public IPage<SysUser> getUserByDepIds(Page<SysUser> page, List<String> departIds, String username) {
		return userMapper.getUserByDepIds(page, departIds,username);
	}

	@Override
	public Map<String, String> getDepNamesByUserIds(List<String> userIds) {
		List<SysUserDepVo> list = this.baseMapper.getDepNamesByUserIds(userIds);

		Map<String, String> res = new HashMap(5);
		list.forEach(item -> {
					if (res.get(item.getUserId()) == null) {
						res.put(item.getUserId(), item.getDepartName());
					} else {
						res.put(item.getUserId(), res.get(item.getUserId()) + "," + item.getDepartName());
					}
				}
		);
		return res;
	}

	//update-begin-author:taoyan date:2022-9-13 for: VUEN-2245【loopholes】发现新loopholes待处理20220906 ----sqlinjection  Method not used，Note out
/*	@Override
	public IPage<SysUser> getUserByDepartIdAndQueryWrapper(Page<SysUser> page, String departId, QueryWrapper<SysUser> queryWrapper) {
		LambdaQueryWrapper<SysUser> lambdaQueryWrapper = queryWrapper.lambda();

		lambdaQueryWrapper.eq(SysUser::getDelFlag, CommonConstant.DEL_FLAG_0);
        lambdaQueryWrapper.inSql(SysUser::getId, "SELECT user_id FROM sys_user_depart WHERE dep_id = '" + departId + "'");

        return userMapper.selectPage(page, lambdaQueryWrapper);
	}*/
	//update-end-author:taoyan date:2022-9-13 for: VUEN-2245【loopholes】发现新loopholes待处理20220906 ----sqlinjection Method not used，Note out

	@Override
	public IPage<SysUserSysDepartModel> queryUserByOrgCode(String orgCode, SysUser userParams, IPage page) {
		List<SysUserSysDepartModel> list = baseMapper.getUserByOrgCode(page, orgCode, userParams);
		//根据departmentorgCodeQuerydepartment，Need to place the positionidmake a transfer
		for (SysUserSysDepartModel model:list) {
			List<String> positionList = sysUserPositionMapper.getPositionIdByUserId(model.getId());
			model.setPost(CommonUtils.getSplitText(positionList,SymbolConstant.COMMA));
		}
		Integer total = baseMapper.getUserByOrgCodeTotal(orgCode, userParams);

		IPage<SysUserSysDepartModel> result = new Page<>(page.getCurrent(), page.getSize(), total);
		result.setRecords(list);

		return result;
	}

    /**
     * 根据RoleIdQuery
     * @param page
     * @param roleId Roleid
     * @param username user账户名称
     * @param realname userName
     * @return
     */
	@Override
	public IPage<SysUser> getUserByRoleId(Page<SysUser> page, String roleId, String username, String realname) {
		//update-begin---author:wangshuai ---date:20230220  for：[QQYUN-3980]Under organization management Position function Job listings plus tenantsid Add position-user关联表------------
		IPage<SysUser> userRoleList = userMapper.getUserByRoleId(page, roleId, username,realname);
		List<SysUser> records = userRoleList.getRecords();
		if (null != records && records.size() > 0) {
			List<String> userIds = records.stream().map(SysUser::getId).collect(Collectors.toList());
			Map<String, String> useDepNames = this.getDepNamesByUserIds(userIds);
			for (SysUser sysUser : userRoleList.getRecords()) {
				//设置department
				sysUser.setOrgCodeTxt(useDepNames.get(sysUser.getId()));
				//设置userPositionid
				this.userPositionId(sysUser);
			}
		}
		return userRoleList;
		//update-end---author:wangshuai ---date:20230220  for：[QQYUN-3980]Under organization management Position function Job listings plus tenantsid Add position-user关联表------------
	}


	@Override
	@CacheEvict(value= {CacheConstant.SYS_USERS_CACHE}, key="#username")
	public void updateUserDepart(String username,String orgCode,Integer loginTenantId) {
		baseMapper.updateUserDepart(username, orgCode,loginTenantId);
	}


	@Override
	public SysUser getUserByPhone(String phone) {
		return userMapper.getUserByPhone(phone);
	}


	@Override
	public SysUser getUserByEmail(String email) {
		return userMapper.getUserByEmail(email);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void addUserWithDepart(SysUser user, String selectedParts) {
//		this.save(user);  //保存Rolewhenalready经添加过一次了
		if(oConvertUtils.isNotEmpty(selectedParts)) {
			String[] arr = selectedParts.split(",");
			for (String deaprtId : arr) {
				SysUserDepart userDeaprt = new SysUserDepart(user.getId(), deaprtId);
				sysUserDepartMapper.insert(userDeaprt);
			}
		}
	}


	@Override
	@Transactional(rollbackFor = Exception.class)
	@CacheEvict(value={CacheConstant.SYS_USERS_CACHE}, allEntries=true)
	public void editUserWithDepart(SysUser user, String departs) {
        //更新Rolewhenalready经更新了一次了，You can follow me again
		this.updateById(user);
		String[] arr = {};
		if(oConvertUtils.isNotEmpty(departs)){
			arr = departs.split(",");
		}
		//Queryalready关联department
		List<SysUserDepart> userDepartList = sysUserDepartMapper.selectList(new QueryWrapper<SysUserDepart>().lambda().eq(SysUserDepart::getUserId, user.getId()));
		if(userDepartList != null && userDepartList.size()>0){
			for(SysUserDepart depart : userDepartList ){
				//修改already关联department删除departmentuserRole关系
				if(!Arrays.asList(arr).contains(depart.getDepId())){
					List<SysDepartRole> sysDepartRoleList = sysDepartRoleMapper.selectList(
							new QueryWrapper<SysDepartRole>().lambda().eq(SysDepartRole::getDepartId,depart.getDepId()));
					List<String> roleIds = sysDepartRoleList.stream().map(SysDepartRole::getId).collect(Collectors.toList());
					if(roleIds != null && roleIds.size()>0){
						departRoleUserMapper.delete(new QueryWrapper<SysDepartRoleUser>().lambda().eq(SysDepartRoleUser::getUserId, user.getId())
								.in(SysDepartRoleUser::getDroleId,roleIds));
					}
				}
			}
		}
		//Delete first and then add
		sysUserDepartMapper.delete(new QueryWrapper<SysUserDepart>().lambda().eq(SysUserDepart::getUserId, user.getId()));
		if(oConvertUtils.isNotEmpty(departs)) {
			for (String departId : arr) {
				SysUserDepart userDepart = new SysUserDepart(user.getId(), departId);
				sysUserDepartMapper.insert(userDepart);
			}
		}
	}


	/**
	   * 校验user是否有效
	 * @param sysUser
	 * @return
	 */
	@Override
	public Result<?> checkUserIsEffective(SysUser sysUser) {
		Result<?> result = new Result<Object>();
		//Condition1：根据user信息Query，该user不存在
		if (sysUser == null) {
			result.error500("该user不存在，Please register");
			baseCommonService.addLog("user登录失败，user不存在！", CommonConstant.LOG_TYPE_1, null);
			return result;
		}
		//Condition2：根据user信息Query，该userLogged out
		//update-begin---author:Wang Shuai   Date:20200601  for：ifThe condition is alwaysfalsebug------------
		if (CommonConstant.DEL_FLAG_1.equals(sysUser.getDelFlag())) {
		//update-end---author:Wang Shuai   Date:20200601  for：ifThe condition is alwaysfalsebug------------
			baseCommonService.addLog("user登录失败，user名:" + sysUser.getUsername() + "Logged out！", CommonConstant.LOG_TYPE_1, null);
			result.error500("该userLogged out");
			return result;
		}
		//Condition3：根据user信息Query，该userfrozen
		if (CommonConstant.USER_FREEZE.equals(sysUser.getStatus())) {
			baseCommonService.addLog("user登录失败，user名:" + sysUser.getUsername() + "frozen！", CommonConstant.LOG_TYPE_1, null);
			result.error500("该userfrozen");
			return result;
		}
		return result;
	}

	@Override
	public List<SysUser> queryLogicDeleted() {
        //update-begin---author:wangshuai ---date:20221116  for：回收站Query未离职的------------
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(SysUser::getStatus, CommonConstant.USER_QUIT);
        return this.queryLogicDeleted(wrapper);
        //update-end---author:wangshuai ---date:20221116  for：回收站Query未离职的--------------
    }

	@Override
	public List<SysUser> queryLogicDeleted(LambdaQueryWrapper<SysUser> wrapper) {
		if (wrapper == null) {
			wrapper = new LambdaQueryWrapper<>();
		}
		wrapper.eq(SysUser::getDelFlag, CommonConstant.DEL_FLAG_1);
		return userMapper.selectLogicDeleted(wrapper);
	}

	@Override
	@CacheEvict(value={CacheConstant.SYS_USERS_CACHE}, allEntries=true)
	public boolean revertLogicDeleted(List<String> userIds, SysUser updateEntity) {
		return userMapper.revertLogicDeleted(userIds, updateEntity) > 0;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean removeLogicDeleted(List<String> userIds) {
		// 1. 删除user
		int line = userMapper.deleteLogicDeleted(userIds);
		// 2. 删除userdepartment关系
		line += sysUserDepartMapper.delete(new LambdaQueryWrapper<SysUserDepart>().in(SysUserDepart::getUserId, userIds));
		//3. 删除userRole关系
		line += sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getUserId, userIds));
		//4.Synchronously delete third partiesApp的user
		try {
			dingtalkService.removeThirdAppUser(userIds);
			wechatEnterpriseService.removeThirdAppUser(userIds);
		} catch (Exception e) {
			log.error("Synchronously delete third partiesApp的user失败：", e);
		}
		//5. 删除No.三方User table（Because the first4步需要用到No.三方User table，So delete it after him）
		line += sysThirdAccountMapper.delete(new LambdaQueryWrapper<SysThirdAccount>().in(SysThirdAccount::getSysUserId, userIds));

		//6. 删除tenantuser中间表的data
		line += userTenantMapper.delete(new LambdaQueryWrapper<SysUserTenant>().in(SysUserTenant::getUserId,userIds));
		
		return line != 0;
	}

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateNullPhoneEmail() {
        userMapper.updateNullByEmptyString("email");
        userMapper.updateNullByEmptyString("phone");
        return true;
    }

	@Override
	public void saveThirdUser(SysUser sysUser) {
		//保存user
		String userid = UUIDGenerator.generate();
		sysUser.setId(userid);
		baseMapper.insert(sysUser);
		//获取No.三方Role
		SysRole sysRole = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, "third_role"));
		//保存userRole
		SysUserRole userRole = new SysUserRole();
		userRole.setRoleId(sysRole.getId());
		userRole.setUserId(userid);
		sysUserRoleMapper.insert(userRole);
	}

	@Override
	public List<SysUser> queryByDepIds(List<String> departIds, String username) {
		return userMapper.queryByDepIds(departIds,username);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveUser(SysUser user, String selectedRoles, String selectedDeparts, String relTenantIds, boolean izSyncPack) {
		//step.1 保存user
		this.save(user);
		//获取user保存前台传过来的tenantidand add to tenant
		this.saveUserTenant(user.getId(),relTenantIds, izSyncPack);
		//step.2 保存Role
		if(oConvertUtils.isNotEmpty(selectedRoles)) {
			String[] arr = selectedRoles.split(",");
			for (String roleId : arr) {
				SysUserRole userRole = new SysUserRole(user.getId(), roleId);
				sysUserRoleMapper.insert(userRole);
			}
		}
		
		//step.3 保存所属department
		if(oConvertUtils.isNotEmpty(selectedDeparts)) {
			String[] arr = selectedDeparts.split(",");
			for (String deaprtId : arr) {
				SysUserDepart userDeaprt = new SysUserDepart(user.getId(), deaprtId);
				sysUserDepartMapper.insert(userDeaprt);
			}
		}

		//step.4 Save post
		this.saveUserPosition(user.getId(),user.getPost());
        //step5 Save part-time job
        this.saveUserOtherDepPost(user.getId(),user.getOtherDepPostId());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	@CacheEvict(value={CacheConstant.SYS_USERS_CACHE}, allEntries=true)
	public void editUser(SysUser user, String roles, String departs, String relTenantIds, String updateFromPage) {
		//获取user编辑前台传过来的tenantid
        this.editUserTenants(user.getId(),relTenantIds);
		//step.1 修改user基础信息
		this.updateById(user);
		//step.2 修改Role
		if (oConvertUtils.isEmpty(updateFromPage) || !"deptUsers".equalsIgnoreCase(updateFromPage)) {
			// 处理userRole Delete first and then add , like果是在departmentuser页面修改user,不处理userRole,因for该页面无法编辑userRole.
			sysUserRoleMapper.delete(new QueryWrapper<SysUserRole>().lambda().eq(SysUserRole::getUserId, user.getId()));
			if (oConvertUtils.isNotEmpty(roles)) {
				String[] arr = roles.split(",");
				for (String roleId : arr) {
					SysUserRole userRole = new SysUserRole(user.getId(), roleId);
					sysUserRoleMapper.insert(userRole);
				}
			}
		}

		//step.3 修改department
		String[] arr = {};
		if(oConvertUtils.isNotEmpty(departs)){
			arr = departs.split(",");
		}
		//Queryalready关联department
		List<SysUserDepart> userDepartList = sysUserDepartMapper.selectList(new QueryWrapper<SysUserDepart>().lambda().eq(SysUserDepart::getUserId, user.getId()));
		if(userDepartList != null && userDepartList.size()>0){
			for(SysUserDepart depart : userDepartList ){
				//修改already关联department删除departmentuserRole关系
				if(!Arrays.asList(arr).contains(depart.getDepId())){
					List<SysDepartRole> sysDepartRoleList = sysDepartRoleMapper.selectList(
							new QueryWrapper<SysDepartRole>().lambda().eq(SysDepartRole::getDepartId,depart.getDepId()));
					List<String> roleIds = sysDepartRoleList.stream().map(SysDepartRole::getId).collect(Collectors.toList());
					if(roleIds != null && roleIds.size()>0){
						departRoleUserMapper.delete(new QueryWrapper<SysDepartRoleUser>().lambda().eq(SysDepartRoleUser::getUserId, user.getId())
								.in(SysDepartRoleUser::getDroleId,roleIds));
					}
				}
			}
		}
		//Delete first and then add
		sysUserDepartMapper.delete(new QueryWrapper<SysUserDepart>().lambda().eq(SysUserDepart::getUserId, user.getId()));
		if(oConvertUtils.isNotEmpty(departs)) {
			for (String departId : arr) {
				SysUserDepart userDepart = new SysUserDepart(user.getId(), departId);
				sysUserDepartMapper.insert(userDepart);
			}
		}
		//step.4 Modify mobile phone number and email address
		// Update mobile phone number、The empty mailbox string is null
		userMapper.updateNullByEmptyString("email");
		userMapper.updateNullByEmptyString("phone");

		//step.5 Modify position
		this.editUserPosition(user.getId(),user.getPost());

        //update-begin---author:wangshuai---date:2025-09-06---for:How to transform part-time positions into intermediate positions---
        //step6 Modify part-time position
        //Delete first and then add
        depPostMapper.delete(new QueryWrapper<SysUserDepPost>().lambda().eq(SysUserDepPost::getUserId, user.getId()));
        this.saveUserOtherDepPost(user.getId(),user.getOtherDepPostId());
        //update-end---author:wangshuai---date:2025-09-06---for:How to transform part-time positions into intermediate positions---
	}


    /**
     * Save part-time job
     *
     * @param userId
     * @param otherDepPostId
     */
    private void saveUserOtherDepPost(String userId, String otherDepPostId) {
        if (oConvertUtils.isNotEmpty(otherDepPostId)) {
            String[] depPostId = otherDepPostId.split(SymbolConstant.COMMA);
            for (String postId : depPostId) {
                SysUserDepPost userPosition = new SysUserDepPost(userId, postId);
                depPostMapper.insert(userPosition);
            }
        }
    }

	@Override
	public List<String> userIdToUsername(Collection<String> userIdList) {
		LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.in(SysUser::getId, userIdList);
		List<SysUser> userList = super.list(queryWrapper);
		return userList.stream().map(SysUser::getUsername).collect(Collectors.toList());
	}

	@Override
	@Cacheable(cacheNames=CacheConstant.SYS_USERS_CACHE, key="#username")
	@SensitiveEncode
	public LoginUser getEncodeUserInfo(String username){
		if(oConvertUtils.isEmpty(username)) {
			return null;
		}
		LoginUser loginUser = new LoginUser();
		SysUser sysUser = userMapper.getUserByName(username);
		//Query user的tenantids
		this.setUserTenantIds(sysUser);
		//Set positionid
		this.userPositionId(sysUser);
		if(sysUser==null) {
			return null;
		}
		BeanUtils.copyProperties(sysUser, loginUser);
		// Query当前登录user的departmentid
		loginUser.setOrgId(this.getDepartIdByOrCode(sysUser.getOrgCode()));
		// Query当前登录user的Rolecode（Multiple commas separated）
		loginUser.setRoleCode(this.getJoinRoleCodeByUserId(sysUser.getId()));
		return loginUser;
	}

    @Override
    @CacheEvict(value={CacheConstant.SYS_USERS_CACHE}, allEntries=true)
    @Transactional(rollbackFor = Exception.class)
    public void userQuit(String username) {
        SysUser sysUser = userMapper.getUserByName(username);
        if(null == sysUser){
            throw new JeecgBootException("Failed to resign，该useralready不存在");
        }
		//update-begin---author:wangshuai ---date:20230111  for：[QQYUN-3951]tenantuser离职重构------------
		int tenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);
		//更新usertenant表的状态for离职状态
		if(tenantId==0){
			throw new JeecgBootException("Failed to resign，Tenant does not exist");
		}
		LambdaQueryWrapper<SysUserTenant> query = new LambdaQueryWrapper<>();
		query.eq(SysUserTenant::getUserId,sysUser.getId());
		query.eq(SysUserTenant::getTenantId,tenantId);
		SysUserTenant userTenant = new SysUserTenant();
		userTenant.setStatus(CommonConstant.USER_TENANT_QUIT);
		userTenantMapper.update(userTenant,query);
		//update-end---author:wangshuai ---date:20230111  for：[QQYUN-3951]tenantuser离职重构------------
    }

    @Override
    public List<SysUser> getQuitList(Integer tenantId) {
        return userMapper.getTenantQuitList(tenantId);
    }

    @Override
    public void updateStatusAndFlag(List<String> userIds, SysUser sysUser) {
        userMapper.updateStatusAndFlag(userIds,sysUser);
    }

	/**
	 * Set up login tenant
	 * @param sysUser
	 * @return
	 */
	@Override
	public Result<JSONObject>  setLoginTenant(SysUser sysUser, JSONObject obj, String username, Result<JSONObject> result){
		// update-begin--Author:sunjianlei Date:20210802 for：获取usertenant信息
		//user有哪些tenant
//		List<SysTenant> tenantList = null;
        //update-begin---author:wangshuai ---date:20221223  for：[QQYUN-3371]Tenant logic transformation，Change to relational table------------
		//update-begin---author:wangshuai ---date:20230427  for：【QQYUN-5270】After all the tenants under my name have withdrawn，Tenant frozen when logging in again------------
		List<SysTenant> tenantList = relationMapper.getTenantNoCancel(sysUser.getId());
		obj.put("tenantList", tenantList);
		//update-end---author:wangshuai ---date:20230427  for：【QQYUN-5270】After all the tenants under my name have withdrawn，Tenant frozen when logging in again------------
//		if (null!=tenantIdList && tenantIdList.size()>0) {
//			//update-end---author:wangshuai ---date:20221223  for：[QQYUN-3371]Tenant logic transformation，Change to relational table--------------
//			//-------------------------------------------------------------------------------------
//			//Query有效的tenant集合
//			LambdaQueryWrapper<SysTenant> queryWrapper = new LambdaQueryWrapper<>();
//			queryWrapper.in(SysTenant::getId, tenantIdList);
//			queryWrapper.eq(SysTenant::getStatus, Integer.valueOf(CommonConstant.STATUS_1));
//			tenantList = sysTenantMapper.selectList(queryWrapper);
//			//-------------------------------------------------------------------------------------
//
//			if (tenantList.size() == 0) {
//				return result.error500("与该user关联的tenant均already被冻结，Unable to log in！");
//			} else {
//				obj.put("tenantList", tenantList);
//			}
//		}
        //update-end---author:wangshuai ---date:20221223  for：[QQYUN-3371]Tenant logic transformation，Change to relational table--------------
		// update-end--Author:sunjianlei Date:20210802 for：获取usertenant信息


		//Login session tenantID，Validity reset
		if (tenantList != null && tenantList.size() > 0) {
			if (tenantList.size() == 1) {
				sysUser.setLoginTenantId(tenantList.get(0).getId());
			} else {
				List<SysTenant> listAfterFilter = tenantList.stream().filter(s -> s.getId().equals(sysUser.getLoginTenantId())).collect(Collectors.toList());
				if (listAfterFilter == null || listAfterFilter.size() == 0) {
					//If the last login to the tenantID，在user拥有的tenant集合里面没有了，则随机取user拥有的No.一个tenantID
					sysUser.setLoginTenantId(tenantList.get(0).getId());
				}
			}
		} else {
			//无tenantwhen，set to 0
			sysUser.setLoginTenantId(0);
		}
		//设置user登录缓存tenant
		this.updateUserDepart(username, null,sysUser.getLoginTenantId());
		log.info(" 登录接口user的tenantID = {}", sysUser.getLoginTenantId());
		if(sysUser.getLoginTenantId()!=null){
			//登录when需要手工设置下会话中的tenantID,不然登录接口无法passtenant隔离Query到data
			TenantContext.setTenant(sysUser.getLoginTenantId()+"");
		}
		return null;
	}


    /**
     * Get tenantsid
     * @param sysUser
     */
    private void setUserTenantIds(SysUser sysUser) {
		if(ObjectUtils.isNotEmpty(sysUser)) {
			List<Integer> list  = relationMapper.getTenantIdsNoStatus(sysUser.getId());
			if(null!=list && list.size()>0){
				sysUser.setRelTenantIds(StringUtils.join(list.toArray(), ","));
			}else{
				sysUser.setRelTenantIds("");
			}
		}
    }

    /**
     * save tenant
     *
     * @param userId
     * @param relTenantIds
     * @param izSyncPack 是否需要将user同步当前产品包下
     */
    private void saveUserTenant(String userId, String relTenantIds, boolean izSyncPack) {
        if (oConvertUtils.isNotEmpty(relTenantIds)) {
            String[] tenantIds = relTenantIds.split(SymbolConstant.COMMA);
            for (String tenantId : tenantIds) {
                SysUserTenant relation = new SysUserTenant();
                relation.setUserId(userId);
                relation.setTenantId(Integer.valueOf(tenantId));
                relation.setStatus(CommonConstant.STATUS_1);
                
				LambdaQueryWrapper sysUserTenantQueryWrapper = new LambdaQueryWrapper<SysUserTenant>()
						.eq(SysUserTenant::getUserId, userId)
						.eq(SysUserTenant::getTenantId,Integer.valueOf(tenantId));
				SysUserTenant tenantPresent = relationMapper.selectOne(sysUserTenantQueryWrapper);
				if (tenantPresent != null) {
					tenantPresent.setStatus(CommonConstant.STATUS_1);
					relationMapper.updateById(tenantPresent);
				}else{
					relationMapper.insert(relation);
                    ISysTenantService currentService = SpringContextUtils.getApplicationContext().getBean(ISysTenantService.class);
                    //default添加当前user到tenant套餐中
                    currentService.addPackUser(userId,tenantId);
				}
            }
        }else{
			//是否turn on系统管理模块的多tenantdata隔离【SAASMulti-tenant model】
			if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
				//update-begin---author:wangshuai ---date:20230220  for：judge当前user是否在当前tenant里面，If it does not exist, add it------------
				String tenantId = TenantContext.getTenant();
				if(oConvertUtils.isNotEmpty(tenantId)){
					Integer count = relationMapper.userTenantIzExist(userId, Integer.parseInt(tenantId));
					if(count == 0){
						SysUserTenant relation = new SysUserTenant();
						relation.setUserId(userId);
						relation.setTenantId(Integer.parseInt(tenantId));
						relation.setStatus(CommonConstant.STATUS_1);
						relationMapper.insert(relation);
                        if(izSyncPack){
                            ISysTenantService currentService = SpringContextUtils.getApplicationContext().getBean(ISysTenantService.class);
                            //自动foruser，Add all packages under tenant
                            currentService.addPackUser(userId,tenantId);
                        }
					}
				}
				//update-end---author:wangshuai ---date:20230220  for：judge当前user是否在当前tenant里面，If it does not exist, add it------------
			}
		}
    }

    /**
     * Edit tenant
     * @param userId
     * @param relTenantIds
     */
    private void editUserTenants(String userId, String relTenantIds) {
        LambdaQueryWrapper<SysUserTenant> query = new LambdaQueryWrapper<>();
        query.eq(SysUserTenant::getUserId, userId);
        //Database tenantid
		List<Integer> oldTenantIds = relationMapper.getTenantIdsByUserId(userId);
        //If the tenant passedidis empty，Then delete the tenant
        if (oConvertUtils.isEmpty(relTenantIds) && CollectionUtils.isNotEmpty(oldTenantIds)) {
            this.deleteTenantByUserId(userId, null);
        } else if (oConvertUtils.isNotEmpty(relTenantIds) && CollectionUtils.isEmpty(oldTenantIds)) {
            //If the tenant passedid不is empty但是Database tenantidis empty，Then add
            this.saveUserTenant(userId, relTenantIds, false);
        } else {
			//都不is empty，Need to compare，Add or delete
			if(oConvertUtils.isNotEmpty(relTenantIds) && CollectionUtils.isNotEmpty(oldTenantIds)){
				//Find new tenantsidwith original tenantidDifferences，Delete
				String[] relTenantIdArray = relTenantIds.split(SymbolConstant.COMMA);
				List<String> relTenantIdList = Arrays.asList(relTenantIdArray);
				
				List<Integer> deleteTenantIdList = oldTenantIds.stream().filter(item -> !relTenantIdList.contains(item.toString())).collect(Collectors.toList());
				for (Integer tenantId : deleteTenantIdList) {
					this.deleteTenantByUserId(userId, tenantId);
				}
				//找到原来tenant的useridwith new tenantsidDifferences，Add new
				String tenantIds = relTenantIdList.stream().filter(item -> !oldTenantIds.contains(Integer.valueOf(item))).collect(Collectors.joining(","));
				this.saveUserTenant(userId, tenantIds, false);
			}
        }
    }

    /**
     * 删除tenantpassuserid
     * @param tenantId
     * @param userId
     */
    private void deleteTenantByUserId(String userId,Integer tenantId){
        LambdaQueryWrapper<SysUserTenant> query = new LambdaQueryWrapper<>();
        query.eq(SysUserTenant::getUserId, userId);
        if(oConvertUtils.isNotEmpty(tenantId)){
            query.eq(SysUserTenant::getTenantId, tenantId);
        }
        relationMapper.delete(query);
        //删除产品包user关联
        LambdaQueryWrapper<SysTenantPackUser> packUserQuery = new LambdaQueryWrapper<>();
        packUserQuery.eq(SysTenantPackUser::getUserId, userId);
        if(oConvertUtils.isNotEmpty(tenantId)){
            packUserQuery.eq(SysTenantPackUser::getTenantId, tenantId);
        }
        packUserMapper.delete(packUserQuery);
    }



	@Override
	public void batchEditUsers(JSONObject json) {
		String userIds = json.getString("userIds");
		List<String> idList = JSONArray.parseArray(userIds, String.class);
		//department
		String selecteddeparts = json.getString("selecteddeparts");
		//Position
		String post = json.getString("post");
		//work place? There is no such field
		String workAddress = json.getString("workAddress");
		//批量修改userPosition
		if(oConvertUtils.isNotEmpty(post)) {
			//update-begin---author:wangshuai ---date:20230220  for：[QQYUN-3980]Under organization management Position function Job listings plus tenantsid Add position-user关联表------------
			//Modify positionuser关系表
			for (String userId:idList) {
				this.editUserPosition(userId,post);
			}
			//update-end---author:wangshuai ---date:20230220  for：[QQYUN-3980]Under organization management Position function Job listings plus tenantsid Add position-user关联表------------
		}
		if(oConvertUtils.isNotEmpty(selecteddeparts)) {
			//Query当前tenant的department列表
			Integer currentTenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);
			LambdaQueryWrapper<SysDepart> departQuery = new LambdaQueryWrapper<SysDepart>()
					.eq(SysDepart::getTenantId, currentTenantId);
			List<SysDepart> departList = sysDepartMapper.selectList(departQuery);
			if(departList==null || departList.size()==0){
				log.error("batchEditUsers According to tenantID没有找到department>"+currentTenantId);
				return;
			}
			List<String> departIdList = new ArrayList<String>();
			for(SysDepart depart: departList){
				if(depart!=null){
					String id = depart.getId();
					if(oConvertUtils.isNotEmpty(id)){
						departIdList.add(id);
					}
				}
			}
			//删除人员的department关联
			LambdaQueryWrapper<SysUserDepart> query = new LambdaQueryWrapper<SysUserDepart>()
					.in(SysUserDepart::getUserId, idList)
					.in(SysUserDepart::getDepId, departIdList);
			sysUserDepartMapper.delete(query);
			
			String[] arr = selecteddeparts.split(",");
			
			//Add again
			for (String deaprtId : arr) {
				for(String userId: idList){
					SysUserDepart userDepart = new SysUserDepart(userId, deaprtId);
					sysUserDepartMapper.insert(userDepart);
				}
			}
		}
	}

	@Override
	public DepartAndUserInfo searchByKeyword(String keyword) {
		DepartAndUserInfo departAndUserInfo = new DepartAndUserInfo();
		if(oConvertUtils.isNotEmpty(keyword)){
			LambdaQueryWrapper<SysUser> query1 = new LambdaQueryWrapper<SysUser>()
					.like(SysUser::getRealname, keyword);
			String str = oConvertUtils.getString(TenantContext.getTenant(), "0");
			Integer tenantId = Integer.valueOf(str);
			if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
				List<String> userIds = userTenantMapper.getUserIdsByTenantId(tenantId);
				if (oConvertUtils.listIsNotEmpty(userIds)) {
					query1.in(SysUser::getId, userIds);
				}else{
					query1.eq(SysUser::getId, "");
				}
			}
			List<SysUser> list1 = this.baseMapper.selectList(query1);
			if(list1!=null && list1.size()>0){
				List<UserAvatar> userList = list1.stream().map(v -> new UserAvatar(v)).collect(Collectors.toList());
				departAndUserInfo.setUserList(userList);
			}

			LambdaQueryWrapper<SysDepart> query2 = new LambdaQueryWrapper<SysDepart>()
					.like(SysDepart::getDepartName, keyword);
			if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
				query2.eq(SysDepart::getTenantId, tenantId);
			}
			List<SysDepart> list2 = sysDepartMapper.selectList(query2);
			if(list2!=null && list2.size()>0){
				List<DepartInfo> departList = new ArrayList<>();
				for(SysDepart depart: list2){
					List<String> orgName = new ArrayList<>();
					List<String> orgId = new ArrayList<>();
					getParentDepart(depart, orgName, orgId);
					DepartInfo departInfo = new DepartInfo();
					departInfo.setId(depart.getId());
					departInfo.setOrgId(orgId);
					departInfo.setOrgName(orgName);
					departList.add(departInfo);
				}
				departAndUserInfo.setDepartList(departList);
			}
		}
		return departAndUserInfo;
	}

	@Override
	public UpdateDepartInfo getUpdateDepartInfo(String departId) {
		SysDepart depart = sysDepartMapper.selectById(departId);
		if(depart!=null){
			UpdateDepartInfo info = new UpdateDepartInfo(depart);
			List<SysDepart> subList = sysDepartMapper.queryDeptByPid(departId);
			if(subList!=null && subList.size()>0){
				info.setHasSub(true);
			}
			//获取department负责人信息
			LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<SysUser>()
					.eq(SysUser::getUserIdentity, 2)
					.like(SysUser::getDepartIds, depart.getId());
			List<SysUser> userList = this.baseMapper.selectList(query);
			if(userList!=null && userList.size()>0){
				List<String> idList = userList.stream().map(i -> i.getId()).collect(Collectors.toList());
				info.setChargePersonList(idList);
			}
			return info;
		}
		return null;
	}

	@Override
	public void doUpdateDepartInfo(UpdateDepartInfo info) {
		String departId = info.getDepartId();
		SysDepart depart = sysDepartMapper.selectById(departId);
		if(depart!=null){
			//修改department信息-上级和department名称
			if(!depart.getParentId().equals(info.getParentId())){
				String pid = info.getParentId();
				SysDepart parentDepart = sysDepartMapper.selectById(pid);
				if(parentDepart!=null){
					String orgCode = getNextOrgCode(pid);
					depart.setOrgCode(orgCode);
					depart.setParentId(pid);
				}
			}
			depart.setDepartName(info.getDepartName());
			sysDepartMapper.updateById(depart);
			//先Query这个department的负责人
			List<SysUser> departChargeUsers = queryDepartChargePersons(departId);
			List<String> departChargeUserIdList = departChargeUsers.stream().map(i -> i.getId()).collect(Collectors.toList());
			//修改department负责人
			List<String> userIdList = info.getChargePersonList();
			if(userIdList!=null && userIdList.size()>0){
				for(String userId: userIdList){
					SysUser user = this.baseMapper.selectById(userId);
					if(user!=null){
						departChargeUserIdList.remove(user.getId());
						user.setUserIdentity(2);
						String departIds = user.getDepartIds();
						if(oConvertUtils.isEmpty(departIds)){
							user.setDepartIds(departId);
						}else{
							List<String> list = new ArrayList<String>(Arrays.asList(departIds.split(",")));
							if(list.indexOf(departId)>=0){
								continue;
							}else{
								list.add(departId);
								String newDepartIds = String.join(",", list);
								user.setDepartIds(newDepartIds);
							}
						}
						this.baseMapper.updateById(user);
					}
				}
			//update-begin---author:wangshuai ---date:20230303  for：department负责人不能被删除------------
				this.removeDepartmentManager(departChargeUserIdList,departChargeUsers,departId);
			}else{
				if(CollectionUtil.isNotEmpty(departChargeUsers)){
					//前端传过来user列表idis empty，说明data库的负责department人员均需要删除
					this.removeDepartmentManager(departChargeUserIdList,departChargeUsers,departId);
				}
			//update-end---author:wangshuai ---date:20230303  for：department负责人不能被删除------------
				
			}
		}
	}

	private List<SysUser> queryDepartChargePersons(String departId){
    	List<SysUser> result = new ArrayList<>();
		//update-begin---author:wangshuai ---date:20230303  for：department负责人不能被删除------------
		LambdaQueryWrapper<SysUser> userQuery = new LambdaQueryWrapper<>();
		userQuery.like(SysUser::getDepartIds,departId);
		List<SysUser> userList = userMapper.selectList(userQuery);
		if(userList!=null && userList.size()>0){
			for(SysUser user: userList){
				Integer identity = user.getUserIdentity();
				String deps = user.getDepartIds();
				if(identity!=null && identity==2){
					if(oConvertUtils.isNotEmpty(deps)){
						if(deps.indexOf(departId)>=0){
							result.add(user);
						}
		//update-end---author:wangshuai ---date:20230303  for：department负责人不能被删除------------
					}
				}
			}
		}
    	return result;
	}
	
	/**
	 * 变更parentdepartment Modify encoding
	 * @param parentId
	 * @return
	 */
	private String getNextOrgCode(String parentId){
		JSONObject formData = new JSONObject();
		formData.put("parentId",parentId);
		String[] codeArray = (String[]) FillRuleUtil.executeRule(FillRuleConstant.DEPART, formData);
		return codeArray[0];
	}

	@Override
	public void changeDepartChargePerson(JSONObject json) {
		String userId = json.getString("userId");
		String departId = json.getString("departId");
		boolean status  = json.getBoolean("status");
		SysUser user = this.getById(userId);
		if(user!=null){
			String ids = user.getDepartIds();
			if(status==true){
				//设置department负责人
				if(oConvertUtils.isEmpty(ids)){
					//set to上级
					user.setUserIdentity(CommonConstant.USER_IDENTITY_2);
					user.setDepartIds(departId);
				}else{
					List<String> list = new ArrayList<String>(Arrays.asList(ids.split(",")));
					if(list.indexOf(departId)>=0){
						//Do nothing
					}else{
						list.add(departId);
						String newIds = String.join(",", list);
						//set to上级
						user.setUserIdentity(CommonConstant.USER_IDENTITY_2);
						user.setDepartIds(newIds);
					}
				}
			}else{
				// Cancel the person in charge
				if(oConvertUtils.isNotEmpty(ids)){
					List<String> list = new ArrayList<String>();
					for(String temp: ids.split(",")){
						if(oConvertUtils.isEmpty(temp)){
							continue;
						}
						if(!temp.equals(departId)){
							list.add(temp);
						}
					}
					String newIds = "";
					if(list.size()>0){
						newIds = String.join(",", list);
					}else{
						//负责departmentis empty时，说明already经是普通user
						user.setUserIdentity(CommonConstant.USER_IDENTITY_1);
					}
					user.setDepartIds(newIds);
				}
			}
			this.updateById(user);
		}
	}

	/**
	 * 找上级department
	 * @param depart
	 * @param orgName
	 * @param orgId
	 */
	private void getParentDepart(SysDepart depart,List<String> orgName,List<String> orgId){
		String pid = depart.getParentId();
		orgName.add(0, depart.getDepartName());
		orgId.add(0, depart.getId());
		if(oConvertUtils.isNotEmpty(pid)){
			SysDepart temp = sysDepartMapper.selectById(pid);
			getParentDepart(temp, orgName, orgId);
		}
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	@CacheEvict(value={CacheConstant.SYS_USERS_CACHE}, allEntries=true)
	public void editTenantUser(SysUser sysUser, String tenantId, String departs, String roles) {
		SysUser user = new SysUser();
		user.setWorkNo(sysUser.getWorkNo());
		user.setId(sysUser.getId());
		this.updateById(user);
		//update-begin---author:wangshuai ---date:20230424  for：【QQYUN-5251】人员与department：department删除不掉------------
		if(oConvertUtils.isEmpty(departs)){
			//直接删除user下的的tenantdepartment
			sysUserDepartMapper.deleteUserDepart(user.getId(),tenantId);
		}else{
			//修改tenantuser下的department
			this.updateTenantDepart(user, tenantId, departs);
		}
		//update-end---author:wangshuai ---date:20230424  for：【QQYUN-5251】人员与department：department删除不掉------------
		//修改user下的Position
		this.editUserPosition(sysUser.getId(),sysUser.getPost());
	}

	/**
	 * Modify account status
	 * @param id accountid
	 * @param status account状态
	 */
	@Override
	@CacheEvict(value={CacheConstant.SYS_USERS_CACHE}, allEntries=true)
	public void updateStatus(String id, String status) {
		userMapper.update(new SysUser().setStatus(Integer.parseInt(status)),
				new UpdateWrapper<SysUser>().lambda().eq(SysUser::getId,id));
	}

	/**
	 * 修改tenant下的department
	 * @param departs
	 */
	public void updateTenantDepart(SysUser user, String tenantId, String departs) {
		List<String> departList = new ArrayList<>();
		long startTime = System.currentTimeMillis();
		if (oConvertUtils.isNotEmpty(departs)) {
			//获取当前tenant下的departmentid,According to the front desk
			departList = sysUserDepartMapper.getTenantDepart(Arrays.asList(departs.split(SymbolConstant.COMMA)), tenantId);
		}
		long endTime = System.currentTimeMillis();
		System.out.println("Query userdepartment用时：" + (endTime - startTime) + "ms");
		//Query当前tenant下department和useralready关联的department
		List<SysUserDepart> userDepartList = sysUserDepartMapper.getTenantUserDepart(user.getId(), tenantId);
		if (userDepartList != null && userDepartList.size() > 0 && departList.size() > 0) {
			for (SysUserDepart depart : userDepartList) {
				//修改already关联department删除departmentuserRole关系
				if (!departList.contains(depart.getDepId())) {
					List<SysDepartRole> sysDepartRoleList = sysDepartRoleMapper.selectList(
							new QueryWrapper<SysDepartRole>().lambda().eq(SysDepartRole::getDepartId, depart.getDepId()));
					List<String> roleIds = sysDepartRoleList.stream().map(SysDepartRole::getId).collect(Collectors.toList());
					if (roleIds.size() > 0) {
						departRoleUserMapper.delete(new QueryWrapper<SysDepartRoleUser>().lambda().eq(SysDepartRoleUser::getUserId, user.getId())
								.in(SysDepartRoleUser::getDroleId, roleIds));
					}
				}
			}
		}
		long endTime1 = System.currentTimeMillis();
		System.out.println("修改departmentRole用时：" + (endTime1 - startTime) + "ms");
		
		if (departList.size() > 0) {
			//删除user下的department
			sysUserDepartMapper.deleteUserDepart(user.getId(), tenantId);
			for (String departId : departList) {
				//添加department
				SysUserDepart userDepart = new SysUserDepart(user.getId(), departId);
				sysUserDepartMapper.insert(userDepart);
			}
		}
		long endTime2 = System.currentTimeMillis();
		System.out.println("修改userdepartment用时：" + (endTime2 - startTime) + "ms");
	}

	/**
	 * 保存userPosition
	 *
	 * @param userId
	 * @param positionIds
	 */
	private void saveUserPosition(String userId, String positionIds) {
		if (oConvertUtils.isNotEmpty(positionIds)) {
			String[] positionIdArray = positionIds.split(SymbolConstant.COMMA);
			for (String postId : positionIdArray) {
				SysUserPosition userPosition = new SysUserPosition();
				userPosition.setUserId(userId);
				userPosition.setPositionId(postId);
				sysUserPositionMapper.insert(userPosition);
			}
		}
	}

	/**
	 * 编辑userPosition
	 *
	 * @param userId
	 * @param positionIds
	 */
	private void editUserPosition(String userId, String positionIds) {
		//Delete first
		LambdaQueryWrapper<SysUserPosition> query = new LambdaQueryWrapper<>();
		query.eq(SysUserPosition::getUserId, userId);
		sysUserPositionMapper.delete(query);
		//Add data later
		this.saveUserPosition(userId, positionIds);
	}

	/**
	 * 设置userPositionid(Concatenated with commas)
	 * @param sysUser
	 */
	private void userPositionId(SysUser sysUser) {
		if(null != sysUser){
			List<String> positionList = sysUserPositionMapper.getPositionIdByUserId(sysUser.getId());
			sysUser.setPost(CommonUtils.getSplitText(positionList,SymbolConstant.COMMA));
		}
	}

	/**
	 * Query user当前登录department的id
	 *
	 * @param orgCode
	 */
	private @Nullable String getDepartIdByOrCode(String orgCode) {
		if (oConvertUtils.isEmpty(orgCode)) {
			return null;
		}
		LambdaQueryWrapper<SysDepart> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(SysDepart::getOrgCode, orgCode);
		queryWrapper.select(SysDepart::getId);
		SysDepart depart = sysDepartMapper.selectOne(queryWrapper);
		if (depart == null || oConvertUtils.isEmpty(depart.getId())) {
			return null;
		}
		return depart.getId();
	}

	/**
	 * Query user的Rolecode（Multiple commas separated）
	 *
	 * @param userId
	 */
	private @Nullable String getJoinRoleCodeByUserId(String userId) {
		if (oConvertUtils.isEmpty(userId)) {
			return null;
		}
		// judge是否turn onsaasmodel，According to tenantidfilter
		Integer tenantId = null;
		if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
			// turn on了但是没有tenantID，default-1，使其Query不到任何data
			tenantId = oConvertUtils.getInt(TenantContext.getTenant(), -1);
		}
		List<SysRole> roleList = sysRoleMapper.getRoleCodeListByUserId(userId, tenantId);
		if (CollectionUtils.isEmpty(roleList)) {
			return null;
		}
		return roleList.stream().map(SysRole::getRoleCode).collect(Collectors.joining(SymbolConstant.COMMA));
	}

	/**
	 * 移除department负责人
	 * @param departChargeUserIdList
	 * @param departChargeUsers
	 * @param departId
	 */
	private void removeDepartmentManager(List<String> departChargeUserIdList,List<SysUser> departChargeUsers,String departId){
		//移除department负责人
		for(String chargeUserId: departChargeUserIdList){
			for(SysUser chargeUser: departChargeUsers){
				if(chargeUser.getId().equals(chargeUserId)){
					String departIds = chargeUser.getDepartIds();
					List<String> list = new ArrayList<String>(Arrays.asList(departIds.split(",")));
					list.remove(departId);
					String newDepartIds = String.join(",", list);
					chargeUser.setDepartIds(newDepartIds);
					this.baseMapper.updateById(chargeUser);
					break;
				}
			}
		}
	}

	//======================================= begin user与department user列表导出 =========================================
	@Override
	public ModelAndView exportAppUser(HttpServletRequest request) {
		Integer tenantId = oConvertUtils.getInt(TenantContext.getTenant());
		// Step.1 组装Query条件，导出选中的departmentiddata
		String departIds = request.getParameter("departIds");
		List<String> list = new ArrayList<>();
		if(oConvertUtils.isNotEmpty(departIds)){
			list = Arrays.asList(departIds.split(SymbolConstant.COMMA));
		}
		//Query userdata
		List<SysUser> userList = userMapper.getUserByDepartsTenantId(list, tenantId);
        //获取department名称
        List<SysUserDepVo> userDepVos = new ArrayList<>();
        if(CollectionUtil.isNotEmpty(userList)){
            userDepVos = sysDepartMapper.getUserDepartByTenantUserId(userList, tenantId);
        }
		//获取Position
		List<SysUserPositionVo> positionVos = sysUserPositionMapper.getPositionIdByUsersTenantId(userList, tenantId);
		// step2 根据useridClassify
		//cycleuserdata将data整合导出
		List<AppExportUserVo> exportUserVoList = new ArrayList<>();
		for (SysUser sysUser : userList) {
			AppExportUserVo exportUserVo = new AppExportUserVo();
			BeanUtils.copyProperties(sysUser, exportUserVo);
            //update-begin---author:wangshuai---date:2025-01-17---for:【QQYUN-10926】Organizational management——user导出时，department没有导出上下级关系---
            Map<String, String> departMap = this.getDepartNamesAndCategory(userDepVos, sysUser);
            String departNames = departMap.get("departNames");
            exportUserVo.setDepart(departNames.toString());
            //update-end---author:wangshuai---date:2025-01-17---for:【QQYUN-10926】Organizational management——user导出时，department没有导出上下级关系---
			String posNames = positionVos.stream().filter(item -> item.getUserId().equals(sysUser.getId())).map(SysUserPositionVo::getName).collect(Collectors.joining(SymbolConstant.SEMICOLON));
			exportUserVo.setPosition(posNames);
			exportUserVoList.add(exportUserVo);
		}
		//step3 Package exportexcelparameter
		ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
		//Export file name
		mv.addObject(NormalExcelConstants.FILE_NAME, "user列表");
		mv.addObject(NormalExcelConstants.CLASS, AppExportUserVo.class);
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		ExportParams exportParams = new ExportParams("Import rules：\n" +
				"1、存在user编号时，data会根据user编号进OK匹配，匹配成功后只会更新Position和工号;\n" +
				"2、不存在user编号时，Support mobile phone number、Mail、Name、ministries、Position、Import job number,The mobile phone number is required;\n" +
				"3、上下级department用英文字符 / connect，like Finance Department/Finance Department，多个department或者Position用英文字符 ; 进OKconnect,like Finance Department;R&D Department", "Exporter:" + user.getRealname(), "Export information");
		mv.addObject(NormalExcelConstants.PARAMS, exportParams);
		mv.addObject(NormalExcelConstants.DATA_LIST, exportUserVoList);
		return mv;
	}

    /**
     * 获取department名称和department类型
     * for：【QQYUN-10926】Organizational management——user导出时，department没有导出上下级关系
     *
     * @param userDepVos
     * @param sysUser
     * @return
     */
    private Map<String,String> getDepartNamesAndCategory(List<SysUserDepVo> userDepVos, SysUser sysUser) {
        List<SysUserDepVo> SysUserDepVoList = userDepVos.stream().filter(item -> item.getUserId().equals(sysUser.getId()))
                .map(item -> {
                    SysUserDepVo userDepVo = new SysUserDepVo();
                    userDepVo.setUserId(item.getUserId());
                    userDepVo.setDeptId(item.getDeptId());
                    userDepVo.setDepartName(item.getDepartName());
                    userDepVo.setParentId(item.getParentId());
                    userDepVo.setOrgCategory(DepartCategoryEnum.getNameByValue(item.getOrgCategory()));
                    return userDepVo;
                }).collect(Collectors.toList());
        //cycleSysUserDepVoList,like果存在parentid的Condition下，Need to parentid的department名称Query出来
        StringBuilder departNames = new StringBuilder();
        StringBuilder departOrgCategorys = new StringBuilder();
        for (SysUserDepVo sysUserDepVo : SysUserDepVoList) {
            if(oConvertUtils.isEmpty(sysUserDepVo.getDepartName())){
                continue;
            }
            //用于Queryparent的department名称
            List<String> departNameList = new LinkedList<>();
            //用于Queryparent的department类型
            List<String> departOrgCategoryList = new LinkedList<>();
            departNameList.add(sysUserDepVo.getDepartName());
            departOrgCategoryList.add(sysUserDepVo.getOrgCategory());
            if (StringUtils.isNotEmpty(sysUserDepVo.getParentId())) {
                //递归Querydepartment名称
                this.getDepartNameByParentId(sysUserDepVo.getParentId(), departNameList, departOrgCategoryList);
            }
            Collections.reverse(departNameList);
            Collections.reverse(departOrgCategoryList);
            String departName = departNameList.stream().collect(Collectors.joining(SymbolConstant.SINGLE_SLASH));
            if (StringUtils.isNotEmpty(departNames.toString())) {
                departNames.append(SymbolConstant.SEMICOLON);
            }
            departNames.append(departName);
            String orgCatrgory = departOrgCategoryList.stream().collect(Collectors.joining(SymbolConstant.SINGLE_SLASH));
            if (StringUtils.isNotEmpty(departOrgCategorys.toString())) {
                departOrgCategorys.append(SymbolConstant.SEMICOLON);
            }
            departOrgCategorys.append(orgCatrgory);
        }
        //update-begin---author:wangshuai---date:2025-08-27---for:【QQYUN-13617】When importing department添加层级不对了---
        Map<String,String> map = new HashMap<>();
        map.put("departNames", departNames.toString());
        map.put("departOrgCategorys",departOrgCategorys.toString());
        return map;
        //update-end---author:wangshuai---date:2025-08-27---for:【QQYUN-13617】When importing department添加层级不对了---
    }

    /**
     * According to parentidQueryparent的department名称和department类型
     * for：【QQYUN-10926】Organizational management——user导出时，department没有导出上下级关系
     *
     * @param parentId
     * @param departNameList
     * @param departOrgCategoryList
     */
    private void getDepartNameByParentId(String parentId, List<String> departNameList, List<String> departOrgCategoryList) {
        SysDepart parentDepartId = sysDepartMapper.getDepartById(parentId);
        if (null != parentDepartId) {
            departNameList.add(parentDepartId.getDepartName());
            departOrgCategoryList.add(DepartCategoryEnum.getNameByValue(parentDepartId.getOrgCategory()));
            if (StringUtils.isNotEmpty(parentDepartId.getParentId())) {
                this.getDepartNameByParentId(parentDepartId.getParentId(), departNameList, departOrgCategoryList);
            }
        }
    }

    //======================================= end user与department user列表导出 =========================================

	//======================================= begin user与department user列表导入 =========================================
	@Override
	public Result<?> importAppUser(HttpServletRequest request) {
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
		Integer tenantId = oConvertUtils.getInt(TenantContext.getTenant());
		SysTenant sysTenant = sysTenantMapper.selectById(tenantId);
		// error message
		List<String> errorMessage = new ArrayList<>();
		int successLines = 0, errorLines = 0;
		for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
			MultipartFile file = entity.getValue();
			ImportParams params = new ImportParams();
			params.setTitleRows(2);
			params.setHeadRows(1);
			params.setNeedSave(true);
			//存放Position的map;keyfor name valueforPositionid。避免多次导入和Query
			Map<String, String> positionMap = new HashMap<>();
			//存放department的map;keyfor name valueforSysDepartobject。避免多次导入和Query
			Map<String, SysDepart> departMap = new HashMap<>();
			try {
				List<AppExportUserVo> listSysUsers = ExcelImportUtil.importExcel(file.getInputStream(), AppExportUserVo.class, params);
				for (int i = 0; i < listSysUsers.size(); i++) {
					//Record how many lines it is now
					int lineNumber = i + 1;
					//Whether the record is edited or added
					boolean isEdit = false;
					AppExportUserVo sysUserExcel = listSysUsers.get(i);
					String id = sysUserExcel.getId();
					String workNo = sysUserExcel.getWorkNo();
					String email = sysUserExcel.getEmail();
					String phone = sysUserExcel.getPhone();
					String realname = sysUserExcel.getRealname();
					String depart = sysUserExcel.getDepart();
					String position = sysUserExcel.getPosition();
					SysUser sysUser = new SysUser();
					//judgeidexists，like果存在的话就是更新
					if (oConvertUtils.isNotEmpty(id)) {
						SysUser user = userMapper.selectById(id);
						if (null == user) {
							errorLines++;
							errorMessage.add("No. " + lineNumber + " OK：user不存在，Please check if the number has been modified，Ignore import。");
							continue;
						}
						isEdit = true;
						sysUser.setId(id);
					} else {
						//Handle whether it already exists in the tenant，user是否already存在，already存在的user直接更新
						isEdit = false;
					}
					if (oConvertUtils.isNotEmpty(workNo)) {
						sysUser.setWorkNo(workNo);
					}
					try {
						if (isEdit) {
							userMapper.updateById(sysUser);
						} else {
							if (oConvertUtils.isEmpty(phone)) {
								errorMessage.add("No. " + lineNumber + " OK：Phone numberis empty，Ignore import。");
								errorLines++;
								continue;
							}
							SysUser userByPhone = userMapper.getUserByPhone(phone);
							if (null != userByPhone) {
								//Check to see if it already exists in this tenant，Import prohibited，Otherwise, just update directly
								Integer tenantCount = userTenantMapper.userTenantIzExist(userByPhone.getId(), tenantId);
								if (tenantCount > 0) {
									errorMessage.add("No. " + lineNumber + " OK：Members already exist in the organization，like果列表中不存在，Please confirm whether the member is under review or has resigned，Ignore import。");
									errorLines++;
									continue;
								}
								sysUser.setId(userByPhone.getId());
								userMapper.updateById(sysUser);
								this.addUserTenant(sysUser.getId(), tenantId, userByPhone.getUsername(),sysTenant.getName());
							} else {
								// 密码defaultfor “Tenant house number+Phone number”
								String password = sysTenant.getHouseNumber()+phone;
								String salt = oConvertUtils.randomGen(8);
								sysUser.setSalt(salt);
								// Password encryption and salting
								String passwordEncode = PasswordUtil.encrypt(phone, password, salt);
								sysUser.setPassword(passwordEncode);
								sysUser.setUsername(phone);
								sysUser.setRealname(oConvertUtils.getString(realname,phone));
								sysUser.setEmail(email);
								sysUser.setPhone(phone);
								sysUser.setStatus(CommonConstant.DEL_FLAG_1);
								sysUser.setDelFlag(CommonConstant.DEL_FLAG_0);
								sysUser.setCreateTime(new Date());
								userMapper.insert(sysUser);
								this.addUserTenant(sysUser.getId(), tenantId, sysUser.getUsername(),sysTenant.getName());
							}
						}
						//Add or editPosition
						if (oConvertUtils.isNotEmpty(position)) {
							this.addOrEditPosition(sysUser.getId(), position, isEdit, tenantId, positionMap);
						}
						//新增when才可以添加department
						if (!isEdit) {
							//Add or editdepartment
							this.addOrEditDepart(sysUser.getId(), depart, tenantId, departMap);
						}
						successLines++;
					} catch (Exception e) {
						errorLines++;
						String message = e.getMessage().toLowerCase();

						// pass索引名judge出错信息
						if (message.contains(CommonConstant.SQL_INDEX_UNIQ_SYS_USER_USERNAME)) {
							errorMessage.add("No. " + lineNumber + " OK：user名already经存在，Ignore import。");
						} else if (message.contains(CommonConstant.SQL_INDEX_UNIQ_SYS_USER_WORK_NO)) {
							errorMessage.add("No. " + lineNumber + " OK：Job number already exists，Ignore import。");
						} else if (message.contains(CommonConstant.SQL_INDEX_UNIQ_SYS_USER_PHONE)) {
							errorMessage.add("No. " + lineNumber + " OK：Phone numberalready经存在，Ignore import。");
						} else if (message.contains(CommonConstant.SQL_INDEX_UNIQ_SYS_USER_EMAIL)) {
							errorMessage.add("No. " + lineNumber + " OK：Email already exists，Ignore import。");
						} else if (message.contains(CommonConstant.SQL_INDEX_UNIQ_SYS_USER)) {
							errorMessage.add("No. " + lineNumber + " OK：Violation of table uniqueness constraint。");
						} else {
							errorMessage.add("No. " + lineNumber + " OK：unknown error，Ignore import");
							log.error(e.getMessage(), e);
						}
					}
				}
			} catch (Exception e) {
				errorMessage.add("Exception occurred：" + e.getMessage());
				log.error(e.getMessage(), e);
			} finally {
				try {
					file.getInputStream().close();
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}
			}
		}
		try {
			return ImportExcelUtil.imporReturnRes(errorLines, successLines, errorMessage);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 新增或者编辑Position
	 *
	 * @param userId      userid
	 * @param position    Position名称 already/Splicing
	 * @param isEdit      Add or edit
	 * @param positionMap Positionmap keyforname，valueforPositionid
	 */
	private void addOrEditPosition(String userId, String position, Boolean isEdit, Integer tenantId, Map<String, String> positionMap) {
		Page<SysPosition> page = new Page<>(1, 1);
		String[] positions = position.split(SymbolConstant.SEMICOLON);
		List<String> positionList = Arrays.asList(positions);
		positionList = positionList.stream().distinct().collect(Collectors.toList());
		//删除当前tenant下的Position，根据Position名称、tenantid、userid
		sysUserPositionMapper.deleteUserPosByNameAndTenantId(positionList, tenantId, userId);
		//cycle需要添加或修改的data
		for (String pos : positionList) {
			String posId = "";
			if (positionMap.containsKey(pos)) {
				posId = positionMap.get(pos);
			} else {
				List<String> namePage = sysPositionMapper.getPositionIdByName(pos, tenantId, page);
				if (CollectionUtil.isNotEmpty(namePage)) {
					posId = namePage.get(0);
					positionMap.put(pos, posId);
				}
			}

			//Positionid不is empty直接新增
			if (oConvertUtils.isNotEmpty(posId)) {
				this.addSysUserPosition(userId, posId);
				continue;
			}

			//不是编辑的Condition下Position才会新增
			if (!isEdit) {
				//新增Position和userPosition关系
				SysPosition sysPosition = new SysPosition();
				sysPosition.setName(pos);
				sysPosition.setCode(RandomUtil.randomString(10));
				sysPosition.setTenantId(tenantId);
				sysPositionMapper.insert(sysPosition);
				positionMap.put(pos, sysPosition.getId());
				this.addSysUserPosition(userId, sysPosition.getId());
			}
		}
	}

	/**
	 * 添加userPosition
	 */
	private void addSysUserPosition(String userId, String positionId) {
		Long count = sysUserPositionMapper.getUserPositionCount(userId, positionId);
		if(count == 0){
			SysUserPosition userPosition = new SysUserPosition();
			userPosition.setUserId(userId);
			userPosition.setPositionId(positionId);
			sysUserPositionMapper.insert(userPosition);
		}
	}

	/**
	 * Add or editdepartment
	 *
	 * @param userId    userid
	 * @param depart    department名称
	 * @param tenantId  tenantid
	 * @param departMap 存放department的map;keyfor name valueforSysDepartobject。
	 */
	private void addOrEditDepart(String userId, String depart, Integer tenantId, Map<String, SysDepart> departMap) {
		//批量将department和user信息建立关联关系
		if (StringUtils.isNotEmpty(depart)) {
			Page<SysDepart> page = new Page<>(1, 1);
			//多个department分离开
			String[] departNames = depart.split(SymbolConstant.SEMICOLON);
			List<String> departNameList = Arrays.asList(departNames);
			departNameList = departNameList.stream().distinct().collect(Collectors.toList());
			for (String departName : departNameList) {
                //departmentid
                String parentId = "";
				String[] names = departName.split(SymbolConstant.SINGLE_SLASH);
				//department名称Splicing
				String nameStr = "";
				for (int i = 0; i < names.length; i++) {
					String name = names[i];
					//Splicingname
					if (oConvertUtils.isNotEmpty(nameStr)) {
						nameStr = nameStr + SymbolConstant.SINGLE_SLASH + name;
					} else {
						nameStr = name;
					}
					SysDepart sysDepart = null;
					//judgemap中exists该department名称
					if (departMap.containsKey(nameStr)) {
						sysDepart = departMap.get(nameStr);
					} else {
						//不存在需要去Query
						List<SysDepart> departPageByName = sysDepartMapper.getDepartPageByName(page, name, tenantId, parentId);
						//departmentis empty需要新增department
						if (CollectionUtil.isEmpty(departPageByName)) {
							JSONObject formData = new JSONObject();
							formData.put("parentId", parentId);
							String[] codeArray = (String[]) FillRuleUtil.executeRule(FillRuleConstant.DEPART, formData);
							sysDepart = new SysDepart();
							sysDepart.setParentId(parentId);
							sysDepart.setOrgCode(codeArray[0]);
							sysDepart.setOrgType(codeArray[1]);
							sysDepart.setTenantId(tenantId);
							sysDepart.setDepartName(name);
							sysDepart.setIzLeaf(CommonConstant.IS_LEAF);
							sysDepart.setDelFlag(String.valueOf(CommonConstant.DEL_FLAG_0));
							sysDepart.setStatus(CommonConstant.STATUS_1);
							sysDepartMapper.insert(sysDepart);
						} else {
							sysDepart = departPageByName.get(0);
						}
						//parentid不is empty那么就将parentdepartment改成不是叶子节点
						if (oConvertUtils.isNotEmpty(parentId)) {
							sysDepartMapper.setMainLeaf(parentId, CommonConstant.NOT_LEAF);
						}
						parentId = sysDepart.getId();
						departMap.put(nameStr, sysDepart);
					}
					//最后一位新增departmentuser关系表
					if (i == names.length - 1) {
						Long count = sysUserDepartMapper.getCountByDepartIdAndUserId(userId, sysDepart.getId());
						if(count == 0){
							SysUserDepart userDepart = new SysUserDepart(userId, sysDepart.getId());
							sysUserDepartMapper.insert(userDepart);
						}
					}
				}
			}
		}

	}

	/**
	 * 添加usertenant
	 *
	 * @param userId
	 * @param tenantId
	 * @param invitedUsername 被邀请人的account
	 * @param tenantName tenant名称 
	 */
	private void addUserTenant(String userId, Integer tenantId, String invitedUsername, String tenantName) {
		SysUserTenant userTenant = new SysUserTenant();
		userTenant.setTenantId(tenantId);
		userTenant.setUserId(userId);
		userTenant.setStatus(CommonConstant.USER_TENANT_INVITE);
		userTenantMapper.insert(userTenant);
		//update-begin---author:wangshuai ---date:20230710  for：【QQYUN-5731】导入user时，no reminder------------
		//Send system message notification
		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		MessageDTO messageDTO = new MessageDTO();
		String title = sysUser.getRealname() + " invite you to join " + tenantName + "。";
		messageDTO.setTitle(title);
		Map<String, Object> data = new HashMap<>();
		//update-begin---author:wangshuai---date:2024-03-11---for:【QQYUN-8425】user导入成功后 Message reminder Jump to consent page---
		data.put(CommonConstant.NOTICE_MSG_BUS_TYPE,SysAnnmentTypeEnum.TENANT_INVITE.getType());
		//update-end---author:wangshuai---date:2024-03-11---for:【QQYUN-8425】user导入成功后 Message reminder Jump to consent page---
		messageDTO.setData(data);
		messageDTO.setContent(title);
		messageDTO.setToUser(invitedUsername);
		messageDTO.setFromUser("system");
		systemSendMsgHandle.sendMessage(messageDTO);
		//update-end---author:wangshuai ---date:20230710  for：【QQYUN-5731】导入user时，no reminder------------
	}
	//======================================= end user与department user列表导入 =========================================
	
	@Override
	public void checkUserAdminRejectDel(String userIds) {
		LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<>();
		query.in(SysUser::getId,Arrays.asList(userIds.split(SymbolConstant.COMMA)));
		query.eq(SysUser::getUsername,"admin");
		Long adminRoleCount = this.baseMapper.selectCount(query);
		//greater than0说明存在管理员user，Delete not allowed
		if(adminRoleCount>0){
			throw new JeecgBootException("adminuser，Delete not allowed！");
		}
	}

	@Override
	public void changePhone(JSONObject json, String username) {
		String smscode = json.getString("smscode");
		String phone = json.getString("phone");
		String type = json.getString("type");
		if(oConvertUtils.isEmpty(phone)){
			throw new JeecgBootException("请填写原Phone number！");
		}
		if(oConvertUtils.isEmpty(smscode)){
			throw new JeecgBootException("Please fill in the verification code！");
		}
		//step1 验证原Phone number是否和当前user匹配
		SysUser sysUser = userMapper.getUserByNameAndPhone(phone,username);
		if (null == sysUser){
			throw new JeecgBootException("原Phone number不匹配，Unable to change password！");
		}
		//step2 根据类型judge是验证原Phone number的Verification code还是新Phone number的Verification code
		//验证原Phone number
		if(CommonConstant.VERIFY_ORIGINAL_PHONE.equals(type)){
			this.verifyPhone(phone, smscode);
		}else if(CommonConstant.UPDATE_PHONE.equals(type)){
			//修改Phone number
			String newPhone = json.getString("newPhone");
			//需要验证新Phone number和原Phone number是否一致，Unanimous and not allowed to be modified
			if(newPhone.equals(phone)){
				throw new JeecgBootException("新Phone number与原Phone number一致，Cannot modify！");
			}
			this.verifyPhone(newPhone, smscode);
			//step3 新Phone numberVerification code验证成功之后即可修改Phone number
			sysUser.setPhone(newPhone);
			userMapper.updateById(sysUser);
		}
	}
	
	/**
	 * 验证Phone number
	 *
	 * @param phone
	 * @param smsCode
	 * @return
	 */
	public void verifyPhone(String phone, String smsCode){
		String phoneKey = CommonConstant.CHANGE_PHONE_REDIS_KEY_PRE + phone;
		Object phoneCode = redisUtil.get(phoneKey);
		if(null == phoneCode){
			throw new JeecgBootException("Verification code invalid，Please resend verification code！");
		}
		if(!smsCode.equals(phoneCode.toString())) {
			throw new JeecgBootException("SMS verification code does not match！");
		}
		//After verification is completed, clear the verification code on your mobile phone
		redisUtil.removeAll(phoneKey);
	}
	
	@Override
	public void sendChangePhoneSms(JSONObject jsonObject, String username, String ipAddress) {
		String type = jsonObject.getString("type");
		String phone = jsonObject.getString("phone");
		if(oConvertUtils.isEmpty(phone)){
			throw new JeecgBootException("请填写Phone number！");
		}
		//step1 根据类型judge是发送旧Phone numberVerification code还是新的Phone numberVerification code
		if(CommonConstant.VERIFY_ORIGINAL_PHONE.equals(type)){
			//step2 旧Phone numberVerification code需要验证Phone number是否匹配
			SysUser sysUser = userMapper.getUserByNameAndPhone(phone, username);
			if(null == sysUser){
				throw new JeecgBootException("旧Phone number不匹配，Cannot modifyPhone number！");
			}
		}else if(CommonConstant.UPDATE_PHONE.equals(type)){
			//step3 新Phone number需要验证Phone number码是否already注册过
			SysUser userByPhone = userMapper.getUserByPhone(phone);
			if(null != userByPhone){
				throw new JeecgBootException("Phone numberalready被注册，请尝试其他Phone number！");
			}
		}
		//step4 Send SMS verification code
		String redisKey = CommonConstant.CHANGE_PHONE_REDIS_KEY_PRE+phone;
		this.sendPhoneSms(phone, ipAddress,redisKey);
	}

	@Override
	public void sendLogOffPhoneSms(JSONObject jsonObject, String username, String ipAddress) {
		String phone = jsonObject.getString("phone");
		//passuser名Querydata库中的Phone number
		SysUser userByNameAndPhone = userMapper.getUserByNameAndPhone(phone, username);
		if (null == userByNameAndPhone) {
			throw new JeecgBootException("当前userPhone number不匹配，Cannot modify！");
		}
		String code = CommonConstant.LOG_OFF_PHONE_REDIS_KEY_PRE + phone;
		this.sendPhoneSms(phone, ipAddress, code);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void userLogOff(JSONObject jsonObject, String username) {
		String phone = jsonObject.getString("phone");
		String smsCode = jsonObject.getString("smscode");
		//passuser名Querydata库中的Phone number
		SysUser userByNameAndPhone = userMapper.getUserByNameAndPhone(phone, username);
		if (null == userByNameAndPhone) {
			throw new JeecgBootException("当前userPhone number不匹配，Unable to log out！");
		}
		String code = CommonConstant.LOG_OFF_PHONE_REDIS_KEY_PRE + phone;
		Object redisSmdCode = redisUtil.get(code);
		if (null == redisSmdCode) {
			throw new JeecgBootException("Verification code invalid，Unable to log out！");
		}
		if (!redisSmdCode.toString().equals(smsCode)) {
			throw new JeecgBootException("Verification code does not match，Unable to log out！");
		}
		this.deleteUser(userByNameAndPhone.getId());
		redisUtil.removeAll(code);
		redisUtil.removeAll(CacheConstant.SYS_USERS_CACHE + phone);
	}

	/**
	 * Send SMS verification code
	 * @param phone
	 */
	private void sendPhoneSms(String phone, String clientIp,String redisKey) {
		Object object = redisUtil.get(redisKey);

		if (object != null) {
			throw new JeecgBootException("Verification code10within minutes，still valid！");
		}

		//Increase checkPrevent malicious brushing of SMS interfaces
		if(!DySmsLimit.canSendSms(clientIp)){
			log.warn("--------[warn] IPaddress:{}, Too many SMS interface requests-------", clientIp);
			throw new JeecgBootException("Too many SMS interface requests，Please try again later！", CommonConstant.PHONE_SMS_FAIL_CODE);
		}
		
		//random number
		String captcha = RandomUtil.randomNumbers(6);
		JSONObject obj = new JSONObject();
		obj.put("code", captcha);
		try {
			boolean sendSmsSuccess = DySmsHelper.sendSms(phone, obj, DySmsEnum.LOGIN_TEMPLATE_CODE);
			if(!sendSmsSuccess){
				throw new JeecgBootException("短信Verification code发送失败,Please try again later！");
			}
			//Verification code10within minutes有效
			redisUtil.set(redisKey, captcha, 600);
		} catch (ClientException e) {
			log.error(e.getMessage(),e);
			throw new JeecgBootException("SMS interface is not configured，Please contact the administrator！");
		}
	}

    //================================================= begin 低代码department导入导出 ================================================================
    @Override
    public List<SysUserExportVo> getDepartAndRoleExportMsg(List<SysUser> userList) {
        List<SysUserExportVo> list = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(userList)) {
            //获取department
            List<SysUserDepVo> userDepVos = sysDepartMapper.getUserDepartByUserId(userList);
            //获取Role
            List<SysUserPositionVo> sysRoles = sysRoleMapper.getUserRoleByUserId(userList);
            //存放Position名称的map，key：main positionid value: Name of rank
            Map<String, String> postNameMap = new HashMap<>();
            //组装data并return
            for (SysUser sysUser : userList) {
                SysUserExportVo userExportVo = new SysUserExportVo();
                BeanUtils.copyProperties(sysUser, userExportVo);
                //update-begin---author:wangshuai---date:2025-08-27---for:【QQYUN-13617】When importing department添加层级不对了---
                Map<String, String> departMap = this.getDepartNamesAndCategory(userDepVos, sysUser);
                String departNames = departMap.get("departNames");
                userExportVo.setDepartNames(departNames);
                userExportVo.setOrgCategorys(departMap.get("departOrgCategorys"));
                //update-end---author:wangshuai---date:2025-08-27---for:【QQYUN-13617】When importing department添加层级不对了---
                String departIds = sysUser.getDepartIds();
                if (oConvertUtils.isNotEmpty(departIds)) {
                    List<SysUserDepVo> depVoList = sysDepartMapper.getDepartByIds(Arrays.asList(departIds.split(",")));
                    Map<String, String> departMaps = this.getDepartNamesAndCategory(userDepVos, sysUser);
                    userExportVo.setDepartIds(departMaps.get("departNames"));
                }
                String posNames = sysRoles.stream().filter(item -> item.getUserId().equals(sysUser.getId())).map(SysUserPositionVo::getName).collect(Collectors.joining(SymbolConstant.SEMICOLON));
                userExportVo.setRoleNames(posNames);
                if (null != sysUser.getMainDepPostId()) {
                    String postName = "";
                    if (null != postNameMap && postNameMap.containsKey(sysUser.getMainDepPostId())) {
                        postName = postNameMap.get(sysUser.getMainDepPostId());
                    } else {
                        postName = sysDepartMapper.getPostNameByPostId(sysUser.getMainDepPostId());
                    }
                    userExportVo.setPostName(postName);
                    postNameMap.put(sysUser.getMainDepPostId(), postName);
                }
                //update-begin---author:wangshuai---date:2025-09-06---for:How to transform part-time positions into intermediate positions---
                List<String> depPost = depPostMapper.getDepPostByUserId(sysUser.getId());
                if(CollectionUtil.isNotEmpty(depPost)){
                    userExportVo.setOtherDepPostId(String.join(SymbolConstant.COMMA, depPost));
                }
                //update-end---author:wangshuai---date:2025-09-06---for:How to transform part-time positions into intermediate positions---
                list.add(userExportVo);
            }
        }
        return list;
    }

    @Override
    public Result<?> importSysUser(HttpServletRequest request) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        String fileKey = multipartRequest.getParameter("fileKey");
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        // error message
        List<String> errorMessage = new ArrayList<>();
        int successLines = 0, errorLines = 0;
        //存放department的map;keyfor name valueforSysDepartobject。避免多次导入和Query
        Map<String, SysDepart> departMap = new HashMap<>();
        //Rankmap key: Rank名称 value: Rankid
        Map<String, String> positionMap = new HashMap<>();
        //postmap key：post名称 + departmentid value：post（departmentid）
        Map<String,String> postMap = new HashMap<>();
        String tenantId = TokenUtils.getTenantIdByRequest(request);
        for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            MultipartFile file = entity.getValue();// 获取上传文件object
            ImportParams params = new ImportParams();
            params.setTitleRows(2);
            params.setHeadRows(1);
            params.setNeedSave(true);
            try {
                List<SysUserImportVo> listSysUsers = ExcelImportUtil.importExcel(file.getInputStream(), SysUserImportVo.class, params);
                ImportSysUserCache.setImportSysUserMap(fileKey,0,listSysUsers.size(),"user");
                for (int i = 0; i < listSysUsers.size(); i++) {
                    SysUserImportVo sysUserExcel = listSysUsers.get(i);
                    SysUser sysUser = new SysUser();
                    BeanUtils.copyProperties(sysUserExcel, sysUser);
                    if (OkConvertUtils.isEmpty(sysUser.getUsername())) {
                        errorLines += 1;
                        int lineNumber = i + 1;
                        errorMessage.add("No. " + lineNumber + " OK：useraccountis empty，Ignore import。");
                        continue;
                    }
                    try {
                        String username = sysUser.getUsername();
                        //根据user名程序，is empty则添加user
                        SysUser userByName = userMapper.getUserByName(username);
                        if (null != userByName) {
                            errorLines += 1;
                            int lineNumber = i + 1;
                            errorMessage.add("No. " + lineNumber + " OK：user名already经存在，Ignore import。");
                            continue;
                        } else {
                            // 密码defaultfor “123456”
                            sysUser.setPassword(PasswordConstant.DEFAULT_PASSWORD);
                            // Password encryption and salting
                            String salt = oConvertUtils.randomGen(8);
                            sysUser.setSalt(salt);
                            String passwordEncode = PasswordUtil.encrypt(sysUserExcel.getUsername(), sysUser.getPassword(), salt);
                            sysUser.setPassword(passwordEncode);
                            sysUser.setActivitiSync(CommonConstant.ACT_SYNC_1);
                            this.save(sysUser);
                        }
                        //添加department
                        String departNames = sysUserExcel.getDepartNames();
                        String orgCategorys = sysUserExcel.getOrgCategorys();
                        //Add or editdepartment
                        Integer tenantIdInt = 0;
                        if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
                            tenantIdInt = OkConvertUtils.getInt(tenantId, 0);
                        }
                        this.lowAddOrEditDepart(sysUser.getId(), departNames, tenantIdInt, departMap, orgCategorys, sysUserExcel.getPostName(), sysUserExcel.getMainDepPostId(),postMap,positionMap);
                        //Add or editRole
                        String roleNames = sysUserExcel.getRoleNames();
                        this.saveOrEditRole(sysUser.getId(), roleNames, tenantIdInt);
                        //Add or editPosition
                       /* String position = sysUserExcel.getPost();
                        if (oConvertUtils.isNotEmpty(position)) {
                            this.addOrEditPosition(sysUser.getId(), position, false, tenantIdInt, positionMap);
                        }*/
                        //添加负责department
                        this.saveChargeDepart(sysUser, sysUserExcel.getDepartIds(), departMap);
                        successLines++;
                    } catch (Exception e) {
                        errorLines++;
                        String message = e.getMessage().toLowerCase();
                        int lineNumber = i + 1;
                        // pass索引名judge出错信息
                        if (message.contains(CommonConstant.SQL_INDEX_UNIQ_SYS_USER_USERNAME)) {
                            errorMessage.add("No. " + lineNumber + " OK：user名already经存在，Ignore import。");
                        } else if (message.contains(CommonConstant.SQL_INDEX_UNIQ_SYS_USER_WORK_NO)) {
                            errorMessage.add("No. " + lineNumber + " OK：Job number already exists，Ignore import。");
                        } else if (message.contains(CommonConstant.SQL_INDEX_UNIQ_SYS_USER_PHONE)) {
                            errorMessage.add("No. " + lineNumber + " OK：Phone numberalready经存在，Ignore import。");
                        } else if (message.contains(CommonConstant.SQL_INDEX_UNIQ_SYS_USER_EMAIL)) {
                            errorMessage.add("No. " + lineNumber + " OK：Email already exists，Ignore import。");
                        } else if (message.contains(CommonConstant.SQL_INDEX_UNIQ_SYS_USER)) {
                            errorMessage.add("No. " + lineNumber + " OK：Violation of table uniqueness constraint。");
                        } else {
                            errorMessage.add("No. " + lineNumber + " OK：unknown error，Ignore import");
                            log.error(e.getMessage(), e);
                        }
                    }
                    ImportSysUserCache.setImportSysUserMap(fileKey,i,listSysUsers.size(),"user");
                }
            } catch (Exception e) {
                ImportSysUserCache.removeImportLowAppMap(fileKey);
                errorMessage.add("Exception occurred：" + e.getMessage());
                log.error(e.getMessage(), e);
            } finally {
                try {
                    file.getInputStream().close();
                } catch (IOException e) {
                    ImportSysUserCache.removeImportLowAppMap(fileKey);
                    log.error(e.getMessage(), e);
                }
            }
        }
        try {
            departMap.clear();
            departMap = null;
            //Final import completed
            ImportSysUserCache.setImportSysUserMap(fileKey,1,1,"user");
            return ImportExcelUtil.imporReturnRes(errorLines, successLines, errorMessage);
        } catch (IOException e) {
            ImportSysUserCache.removeImportLowAppMap(fileKey);
            throw new RuntimeException(e);
        }
    }

    //================================================================ begin 【user导入】When importing department添加层级不对了======================================================================
    /**
     * 低代码下添加department和user
     * 
     * @Description and Knockout Cloud segmentation processing，reason：因低代码post等改造，有level，故添加department分开处理
     * @param userId          userid
     * @param depart          department名称
     * @param tenantId        tenantid
     * @param departMap       存放department的map;keyfor name valueforSysDepartobject。
     * @param orgCategorys    department类型
     * @param postName        Rank名称
     * @param mainDepPostName post名称
     * @param postMap         key: post名称 + departmentid value：post（departmentid）
     * @param positionMap     key: Rank名称  value: Rankid
     */
    private void lowAddOrEditDepart(String userId, String depart, Integer tenantId, Map<String, SysDepart> departMap, String orgCategorys, String postName, String mainDepPostName, Map<String, String> postMap, Map<String, String> positionMap) {
        //批量将department和user信息建立关联关系
        if (StringUtils.isNotEmpty(depart)) {
            Page<SysDepart> page = new Page<>(1, 1);
            //多个department分离开
            String[] departNames = depart.split(SymbolConstant.SEMICOLON);
            List<String> departNameList = Arrays.asList(departNames);
            //department类型
            List<String> categoryList = new ArrayList<>();
            if (oConvertUtils.isNotEmpty(orgCategorys)) {
                categoryList = Arrays.asList(orgCategorys.split(SymbolConstant.SEMICOLON));
            }
            departNameList = departNameList.stream().distinct().collect(Collectors.toList());
            //当下departmentcycle下标
            int index = 0;
            for (String departName : departNameList) {
                //departmentid
                String parentId = "";
                String[] names = departName.split(SymbolConstant.SINGLE_SLASH);
                //department名称Splicing
                String nameStr = "";
                //department类型
                String[] orgCategory = null;
                if (categoryList != null && categoryList.size() > index) {
                    orgCategory = categoryList.get(index).split(SymbolConstant.SINGLE_SLASH);
                }
                for (int i = 0; i < names.length; i++) {
                    String name = names[i];
                    //Splicingname
                    if (oConvertUtils.isNotEmpty(nameStr)) {
                        nameStr = nameStr + SymbolConstant.SINGLE_SLASH + name;
                    } else {
                        nameStr = name;
                    }
                    SysDepart sysDepart = null;
                    //defaultdepartment
                    String category = DepartCategoryEnum.DEPART_CATEGORY_DEPART.getValue();
                    if (null != orgCategory && orgCategory.length > i) {
                        category = orgCategory[i];
                    }
                    //judgemap中exists该department名称
                    if (departMap.containsKey(nameStr)) {
                        sysDepart = departMap.get(nameStr);
                        parentId = sysDepart.getId();
                    } else {
                        //不存在需要去Query
                        List<SysDepart> departPageByName = sysDepartMapper.getDepartPageByName(page, name, tenantId, parentId);
                        //departmentis empty需要新增department
                        if (CollectionUtil.isEmpty(departPageByName)) {
                            JSONObject formData = new JSONObject();
                            formData.put("parentId", parentId);
                            String[] codeArray = (String[]) FillRuleUtil.executeRule(FillRuleConstant.DEPART, formData);
                            sysDepart = new SysDepart();
                            sysDepart.setParentId(parentId);
                            sysDepart.setOrgCode(codeArray[0]);
                            sysDepart.setOrgType(codeArray[1]);
                            sysDepart.setTenantId(tenantId);
                            sysDepart.setDepartName(name);
                            sysDepart.setIzLeaf(CommonConstant.IS_LEAF);
                            sysDepart.setDelFlag(String.valueOf(CommonConstant.DEL_FLAG_0));
                            sysDepart.setStatus(CommonConstant.STATUS_1);
                            sysDepart.setOrgCategory(DepartCategoryEnum.getValueByName(category));
                            sysDepartMapper.insert(sysDepart);
                        } else {
                            sysDepart = departPageByName.get(0);
                        }
                        //parentid不is empty那么就将parentdepartment改成不是叶子节点
                        if (oConvertUtils.isNotEmpty(parentId)) {
                            sysDepartMapper.setMainLeaf(parentId, CommonConstant.NOT_LEAF);
                        }
                        parentId = sysDepart.getId();
                        departMap.put(nameStr, sysDepart);
                    }
                    //最后一位新增departmentuser关系表
                    if (i == names.length - 1) {
                        Long count = sysUserDepartMapper.getCountByDepartIdAndUserId(userId, sysDepart.getId());
                        if (count == 0) {
                            SysUserDepart userDepart = new SysUserDepart(userId, sysDepart.getId());
                            sysUserDepartMapper.insert(userDepart);
                        }
                        //添加post
                        if (oConvertUtils.isNotEmpty(mainDepPostName)) {
                            this.insertDepartPost(userId, parentId ,postName, mainDepPostName, postMap, tenantId, positionMap);
                        }
                    }
                }
                index++;
            }
        }
    }

    /**
     * 添加departmentpost
     *
     * @param mainDepPost post名称
     * @param userId      userid
     * @param departId    departmentid【上级departmentid】
     * @param postName    Rank名称
     * @param mainDepPostName    post名称
     * @param postMap     postmap key：post名称 + departmentid value：post（departmentid）
     * @param tenantId    tenantid
     * @param postionMap  Rankmap key: Rank名称 value: Rankid
     */
    private void insertDepartPost(String userId, String depId, String postName, String mainDepPostName, Map<String, String> postMap, Integer tenantId, Map<String, String> postionMap) {
        if(mainDepPostName.contains(SymbolConstant.COMMA)){
            mainDepPostName = mainDepPostName.split(SymbolConstant.COMMA)[0];
        }
        //当前department下already经存在post就不需要再次添加post了
        if (null == postMap || !postMap.containsKey(mainDepPostName + depId)) {
            //According to parentdepartmentid和职务名称查找postid
            String departId = sysDepartMapper.getDepIdByDepIdAndPostName(depId, postName);
            //不存在新增post
            if (oConvertUtils.isEmpty(departId) ) {
                //新增post
                SysDepart sysDepart = new SysDepart();
                JSONObject formData = new JSONObject();
                formData.put("parentId", depId);
                String[] codeArray = (String[]) FillRuleUtil.executeRule(FillRuleConstant.DEPART, formData);
                sysDepart.setParentId(depId);
                sysDepart.setOrgCode(codeArray[0]);
                sysDepart.setOrgType(codeArray[1]);
                sysDepart.setTenantId(tenantId);
                sysDepart.setDepartName(mainDepPostName);
                sysDepart.setIzLeaf(CommonConstant.IS_LEAF);
                sysDepart.setDelFlag(String.valueOf(CommonConstant.DEL_FLAG_0));
                sysDepart.setStatus(CommonConstant.STATUS_1);
                sysDepart.setOrgCategory(DepartCategoryEnum.DEPART_CATEGORY_POST.getValue());
                //获取Rankid
                String positionId = "";
                if(postionMap.containsKey(postName)){
                    positionId = postionMap.get(postName);
                } else {
                    //According to tenantid和Rank名称获取Rankid
                    positionId = this.getSysPosition(tenantId, postName);
                }
                sysDepart.setPositionId(positionId);
                postionMap.put(postName, positionId);
                sysDepartMapper.insert(sysDepart);
                sysDepartMapper.setMainLeaf(depId, CommonConstant.NOT_LEAF);
                postMap.put(mainDepPostName + depId, sysDepart.getId());
                //需要将User table的主post进OK关联
                departId = sysDepart.getId();
            }
            if(oConvertUtils.isNotEmpty(departId)){
                //更新user主post
                SysUser user = new SysUser();
                user.setId(userId);
                user.setMainDepPostId(departId);
                userMapper.updateById(user); 
            }
        }
    }

    /**
     * Get job information
     *
     * @param tenantId
     * @param postName
     * @return
     */
    private String getSysPosition(Integer tenantId, String postName) {
        tenantId = oConvertUtils.getInt(tenantId,0);
        Page<SysPosition> page = new Page<>(1, 1);
        List<String> namePage = sysPositionMapper.getPositionIdByName(postName, tenantId, page);
        if (CollectionUtil.isNotEmpty(namePage)) {
            return namePage.get(0);
        }
        return "";
    }
    //================================================================ end 【user导入】When importing department添加层级不对了======================================================================
    
    private void saveChargeDepart(SysUser sysUser, String departIds, Map<String, SysDepart> departMap) {
        //judge那些department没有，即没有加入到department，则不能成for负责department人员
        if (OkConvertUtils.isEmpty(departIds)) {
            return;
        }
        //多个department用;separated
        String[] split = departIds.split(SymbolConstant.SEMICOLON);
        //负责departmentid
        StringBuilder departIdBulider = new StringBuilder();
        for (String name : split) {
            if (departMap.containsKey(name)) {
                SysDepart sysDepart = departMap.get(name);
                departIdBulider.append(sysDepart.getId()).append(",");
            }
        }
        // Check and remove last comma
        if (departIdBulider.length() > 0 && departIdBulider.charAt(departIdBulider.length() - 1) == ',') {
            departIdBulider.deleteCharAt(departIdBulider.length() - 1);
        }
        SysUser user = new SysUser();
        user.setId(sysUser.getId());
        user.setDepartIds(departIdBulider.toString());
        this.updateById(user);
    }

    /**
     * 保存或编辑Role
     *
     * @param userId
     * @param roleNames
     * @param tenantIdInt
     */
    private void saveOrEditRole(String userId, String roleNames, Integer tenantIdInt) {
        if (oConvertUtils.isEmpty(roleNames)) {
            return;
        }
        String[] roleNameArray = roleNames.split(SymbolConstant.SEMICOLON);
        //删除user下的Role
        LambdaQueryWrapper<SysUserRole> deleteQuery = new LambdaQueryWrapper<>();
        deleteQuery.eq(SysUserRole::getUserId, userId);
        sysUserRoleMapper.delete(deleteQuery);
        //pass名字获取Role
        LambdaQueryWrapper<SysRole> roleQuery = new LambdaQueryWrapper<>();
        roleQuery.orderByDesc(SysRole::getCreateTime);
        if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
            roleQuery.eq(SysRole::getTenantId, tenantIdInt);
        }
        for (String roleName : roleNameArray) {
            roleQuery.eq(SysRole::getRoleName, roleName);
            List<SysRole> sysRoles = sysRoleMapper.selectList(roleQuery);
            String roleId = "";
            if (CollectionUtil.isNotEmpty(sysRoles)) {
                roleId = sysRoles.get(0).getId();
            } else {
                SysRole sysRole = new SysRole();
                sysRole.setRoleName(roleName);
                sysRole.setRoleCode(RandomUtil.randomString(10));
                sysRoleMapper.insert(sysRole);
                roleId = sysRole.getId();
            }
            SysUserRole sysUserRole = new SysUserRole();
            sysUserRole.setUserId(userId);
            sysUserRole.setRoleId(roleId);
            sysUserRoleMapper.insert(sysUserRole);
        }
    }
    //================================================= end 低代码department导入导出 ================================================================

    @Override
    public void updatePasswordNotBindPhone(String oldPassword, String password, String username) {
        LoginUser sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();
        //step1 You can only change your own password
        if(!sysUser.getUsername().equals(username)){
            throw new JeecgBootBizTipException("Only allowed to change own password！");
        }
        //step2 user不存在禁止Change password
        SysUser user = this.getUserByName(username);
        if(null == user){
            throw new JeecgBootBizTipException("user不存在，Unable to change password！");
        }
        //setp3 like果Phone number存在需要用Phone numberChange password的方式
        if(oConvertUtils.isNotEmpty(user.getPhone())){
            throw new JeecgBootBizTipException("Phone number不is empty，请根据Phone number进OKChange password操作！");
        }
        //step4 judge旧密码是否正确
        String passwordEncode = PasswordUtil.encrypt(username, oldPassword, user.getSalt());
        if (!user.getPassword().equals(passwordEncode)) {
            throw new JeecgBootBizTipException("Old password entered incorrectly!");
        }
        if (oConvertUtils.isEmpty(password)) {
            throw new JeecgBootBizTipException("New password is not allowed to be empty!");
        }
        //step5 Change password
        String newPassWord = PasswordUtil.encrypt(username, password, user.getSalt());
        this.userMapper.update(new SysUser().setPassword(newPassWord), new LambdaQueryWrapper<SysUser>().eq(SysUser::getId, user.getId()));
    }

	/**
	 *
	 * @param userName
	 * @return
	 */
	@Override
	public Map<String, String> queryUserAndDeptByName(String userName) {
		// returnuser和department信息（Adjust according to needs）
		Map<String, String> result = new HashMap<>();
		SysUser user = this.getUserByName(userName);
		result.put("userId", user.getId());
		result.put("username", user.getUsername());
		//user的department信息
		String orgCode = user.getOrgCode();
		if (oConvertUtils.isEmpty(orgCode)) {
			return result;
		}

		// Query公司department
		String companyName  = Optional.ofNullable(sysDepartMapper.queryCompByOrgCode(orgCode))
				.map(SysDepart::getDepartName)
				.orElse("");

		// Query userdepartment并匹配
		String userDeptName = sysDepartMapper.queryDepartsByUsername(userName).stream()
				.filter(depart -> orgCode.equals(depart.getOrgCode()))
				.findFirst()
				.map(SysDepart::getDepartName)
				.orElse("");

		// 设置department显示文本
		String compDepart;
		if (StringUtils.isNotEmpty(companyName) && StringUtils.isNotEmpty(userDeptName)) {
			compDepart = companyName.equals(userDeptName)
					? companyName
					: companyName + "-" + userDeptName;
		} else {
			compDepart = StringUtils.isNotEmpty(companyName) ? companyName : userDeptName;
		}
		result.put("compDepart", compDepart);
		return result;
	}

    /**
     * Querydepartment、post下的user 包括子department下的user
     * 
     * @param orgCode
     * @param userParams
     * @param page
     * @return
     */
    @Override
    public IPage<SysUserSysDepPostModel> queryDepartPostUserByOrgCode(String orgCode, SysUser userParams, IPage page) {
        List<SysUserSysDepPostModel> sysDepartModels = baseMapper.queryDepartPostUserByOrgCode(page, orgCode, userParams);
        if(CollectionUtil.isNotEmpty(sysDepartModels)){
            List<String> userIds = sysDepartModels.stream().map(SysUserSysDepPostModel::getId).toList();
            //获取department名称
            Map<String, String> useDepNames = this.getDepNamesByUserIds(userIds);
            sysDepartModels.forEach(item -> {
                List<String> positionList = sysUserPositionMapper.getPositionIdByUserId(item.getId());
                item.setPost(CommonUtils.getSplitText(positionList,SymbolConstant.COMMA));
                item.setOrgCodeTxt(useDepNames.get(item.getId()));
                //Query user的tenantids
                List<Integer> list = userTenantMapper.getTenantIdsByUserId(item.getId());
                if (oConvertUtils.isNotEmpty(list)) {
                    item.setRelTenantIds(StringUtils.join(list.toArray(), SymbolConstant.COMMA));
                } else {
                    item.setRelTenantIds("");
                }
                Integer posTenantId = null;
                if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
                    posTenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);;
                }
            });
        }
        return page.setRecords(sysDepartModels);
    }
}
