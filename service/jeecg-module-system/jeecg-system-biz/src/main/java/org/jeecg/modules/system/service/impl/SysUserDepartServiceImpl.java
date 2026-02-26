package org.jeecg.modules.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.constant.enums.DepartCategoryEnum;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.entity.SysUserDepart;
import org.jeecg.modules.system.mapper.SysUserDepartMapper;
import org.jeecg.modules.system.mapper.SysUserMapper;
import org.jeecg.modules.system.mapper.SysUserTenantMapper;
import org.jeecg.modules.system.model.DepartIdModel;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.service.ISysUserDepartService;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecg.modules.system.vo.SysUserDepVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <P>
 * User department table implementation class
 * <p/>
 * @Author ZhiLin
 *@since 2019-02-22
 */
@Service
public class SysUserDepartServiceImpl extends ServiceImpl<SysUserDepartMapper, SysUserDepart> implements ISysUserDepartService {
	@Autowired
	private ISysDepartService sysDepartService;
	@Lazy
	@Autowired
	private ISysUserService sysUserService;
	@Autowired
	private SysUserMapper sysUserMapper;
	@Autowired
    private SysUserTenantMapper userTenantMapper;
	

	/**
	 * According to useridQuery department information
	 */
	@Override
	public List<DepartIdModel> queryDepartIdsOfUser(String userId) {
		LambdaQueryWrapper<SysUserDepart> queryUserDep = new LambdaQueryWrapper<SysUserDepart>();
		LambdaQueryWrapper<SysDepart> queryDep = new LambdaQueryWrapper<SysDepart>();
		try {
            queryUserDep.eq(SysUserDepart::getUserId, userId);
			List<String> depIdList = new ArrayList<>();
			List<DepartIdModel> depIdModelList = new ArrayList<>();
			List<SysUserDepart> userDepList = this.list(queryUserDep);
			if(userDepList != null && userDepList.size() > 0) {
			for(SysUserDepart userDepart : userDepList) {
					depIdList.add(userDepart.getDepId());
				}

			//update-begin---author:wangshuai ---date:20230112  for：Determine whether to open the tenantsaasmodel，Enable query based on current tenant------------
			if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
				Integer tenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);
				queryDep.eq(SysDepart::getTenantId,tenantId);
			}
			//update-end---author:wangshuai ---date:20230112  for：Determine whether to open the tenantsaasmodel，Enable query based on current tenant------------
			
			queryDep.in(SysDepart::getId, depIdList);
			List<SysDepart> depList = sysDepartService.list(queryDep);
			//jeecg-boot/issues/3906
			if(depList != null && depList.size() > 0) {
				for(SysDepart depart : depList) {
					depIdModelList.add(new DepartIdModel().convertByUserDepart(depart));
				}
			}
			return depIdModelList;
			}
		}catch(Exception e) {
			e.fillInStackTrace();
		}
		return null;
		
		
	}


	/**
	 * According to departmentidQuery user information
	 */
	@Override
	public List<SysUser> queryUserByDepId(String depId) {
		LambdaQueryWrapper<SysUserDepart> queryUserDep = new LambdaQueryWrapper<SysUserDepart>();
		queryUserDep.eq(SysUserDepart::getDepId, depId);
		List<String> userIdList = new ArrayList<>();
		List<SysUserDepart> uDepList = this.list(queryUserDep);
		if(uDepList != null && uDepList.size() > 0) {
			for(SysUserDepart uDep : uDepList) {
				userIdList.add(uDep.getUserId());
			}
			List<SysUser> userList = (List<SysUser>) sysUserMapper.selectBatchIds(userIdList);
			//update-begin-author:taoyan date:201905047 for:The result returned by the interface call query cannot return password-related information.
			for (SysUser sysUser : userList) {
				sysUser.setSalt("");
				sysUser.setPassword("");
			}
			//update-end-author:taoyan date:201905047 for:The result returned by the interface call query cannot return password-related information.
			return userList;
		}
		return new ArrayList<SysUser>();
	}

	/**
	 * According to departmentcode，Query the current department and subordinate departments User information
	 */
	@Override
	public List<SysUser> queryUserByDepCode(String depCode,String realname) {
		//update-begin-author:taoyan date:20210422 for: According to department选择用户接口代码优化
		if(oConvertUtils.isNotEmpty(realname)){
			realname = realname.trim();
		}
		List<SysUser> userList = this.baseMapper.queryDepartUserList(depCode, realname);
		Map<String, SysUser> map = new HashMap(5);
		for (SysUser sysUser : userList) {
			// The returned user data removes the password information
			sysUser.setSalt("");
			sysUser.setPassword("");
			map.put(sysUser.getId(), sysUser);
		}
		return new ArrayList<SysUser>(map.values());
		//update-end-author:taoyan date:20210422 for: According to department选择用户接口代码优化

	}

	/**
	 *
	 * @param departId
	 * @param username
	 * @param realname
	 * @param pageSize
	 * @param pageNo
	 * @param id
	 * @param isMultiTranslate Whether to translate multiple fields
	 * @return
	 */
	@Override
	public IPage<SysUser> queryDepartUserPageList(String departId, String username, String realname, int pageSize, int pageNo,String id,String isMultiTranslate) {
		IPage<SysUser> pageList = null;
		// departmentIDdoes not exist Just query the user table directly
		Page<SysUser> page = new Page<SysUser>(pageNo, pageSize);
		if(oConvertUtils.isEmpty(departId)){
			LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<>();
            //update-begin---author:wangshuai ---date:20220104  for：[JTC-297]Frozen users can still be set as delegates------------
            query.eq(SysUser::getStatus,Integer.parseInt(CommonConstant.STATUS_1));
            //update-end---author:wangshuai ---date:20220104  for：[JTC-297]Frozen users can still be set as delegates------------
			//update-begin---author:liusq ---date:20231215  for：Comma separated multiple user translation problem------------
			if(oConvertUtils.isNotEmpty(username)){
				String COMMA = ",";
				if(oConvertUtils.isNotEmpty(isMultiTranslate) && username.contains(COMMA)){
					String[] usernameArr = username.split(COMMA);
					query.in(SysUser::getUsername,usernameArr);
				}else {
					query.like(SysUser::getUsername, username);
				}
			}
			//update-end---author:liusq ---date:20231215  for：Comma separated multiple user translation problem------------

			//update-begin---author:liusq ---date:20250908  for：JHHB-304 process transfer When selecting personnel，Add name search------------
			if(oConvertUtils.isNotEmpty(realname)){
				String COMMA = ",";
				if(oConvertUtils.isNotEmpty(isMultiTranslate) && realname.contains(COMMA)){
					String[] realnameArr = realname.split(COMMA);
					query.in(SysUser::getRealname,realnameArr);
				}else {
					query.like(SysUser::getRealname, realname);
				}
			}
			//update-end---author:liusq ---date:20250908  for：JHHB-304 process transfer When selecting personnel，Add name search------------

            //update-begin---author:wangshuai ---date:20220608  for：[VUEN-1238]When replying by email，Sent to displayed as userid------------
            if(oConvertUtils.isNotEmpty(id)){
				//update-begin---author:wangshuai ---date:2024-06-25  for：【TV360X-1482】write a letter，The first echo after selecting a user is not translated.------------
				String COMMA = ",";
				if(oConvertUtils.isNotEmpty(isMultiTranslate) && id.contains(COMMA)){
					String[] idArr = id.split(COMMA);
					query.in(SysUser::getId, Arrays.asList(idArr));
				}else {
					query.eq(SysUser::getId, id);
				}
				//update-end---author:wangshuai ---date:2024-06-25  for：【TV360X-1482】write a letter，The first echo after selecting a user is not translated.------------
            }
            //update-end---author:wangshuai ---date:20220608  for：[VUEN-1238]When replying by email，Sent to displayed as userid------------
            //update-begin---author:wangshuai ---date:20220902  for：[VUEN-2121]Temporary users cannot display directly------------
            query.ne(SysUser::getUsername,"_reserve_user_external");
            //update-end---author:wangshuai ---date:20220902  for：[VUEN-2121]Temporary users cannot display directly------------

			//------------------------------------------------------------------------------------------------
			//Whether to enable multi-tenant data isolation of the system management module【SAAS多租户model】
			if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
				String tenantId = oConvertUtils.getString(TenantContext.getTenant(), "0");
                //update-begin---author:wangshuai ---date:20221223  for：[QQYUN-3371]Tenant logic transformation，Change to relational table------------
				List<String> userIdList = userTenantMapper.getUserIdsByTenantId(Integer.valueOf(tenantId));
				if(null!=userIdList && userIdList.size()>0){
                    query.in(SysUser::getId,userIdList);
                }
                //update-end---author:wangshuai ---date:20221223  for：[QQYUN-3371]Tenant logic transformation，Change to relational table------------
			}
			//------------------------------------------------------------------------------------------------
			pageList = sysUserMapper.selectPage(page, query);
		}else{
			// 有departmentID Need to customizesql
			SysDepart sysDepart = sysDepartService.getById(departId);
			pageList = this.baseMapper.queryDepartUserPageList(page, sysDepart.getOrgCode(), username, realname);
		}
		List<SysUser> userList = pageList.getRecords();
		if(userList!=null && userList.size()>0){
			List<String> userIds = userList.stream().map(SysUser::getId).collect(Collectors.toList());
			Map<String, SysUser> map = new HashMap(5);
			if(userIds!=null && userIds.size()>0){
				// 查department名称
				Map<String,String>  useDepNames = this.getDepNamesByUserIds(userIds);
				userList.forEach(item->{
					//TODO Temporarily borrow this field for page display
					item.setOrgCodeTxt(useDepNames.get(item.getId()));
					item.setSalt("");
					item.setPassword("");
					// Remove duplicates
					map.put(item.getId(), item);
				});
			}
			pageList.setRecords(new ArrayList<SysUser>(map.values()));
		}
		return pageList;
	}

    @Override
    public IPage<SysUser> getUserInformation(Integer tenantId, String departId, String keyword, Integer pageSize, Integer pageNo) {
        IPage<SysUser> pageList = null;
        // departmentIDdoes not exist Just query the user table directly
        Page<SysUser> page = new Page<>(pageNo, pageSize);
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if(oConvertUtils.isEmpty(departId)){
            LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<>();
            query.eq(SysUser::getStatus,Integer.parseInt(CommonConstant.STATUS_1));
            query.ne(SysUser::getUsername,"_reserve_user_external");

			// Support tenant isolation
			if (tenantId != null) {
				List<String> userIds = userTenantMapper.getUserIdsByTenantId(tenantId);
				if(oConvertUtils.listIsNotEmpty(userIds)){
					query.in(SysUser::getId, userIds);
				}else{
					query.eq(SysUser::getId,"by tenantIDUser not found");
				}
			}
			
            //exclude yourself
            query.ne(SysUser::getId,sysUser.getId());
			if(StringUtils.isNotEmpty(keyword)){
				//This syntax can beorSurround it with parentheses，Avoid missing data
				query.and((wrapper) -> wrapper.like(SysUser::getUsername, keyword).or().like(SysUser::getRealname,keyword));
			}
            pageList = sysUserMapper.selectPage(page, query);
        }else{
            // 有departmentID Need to customizesql
            SysDepart sysDepart = sysDepartService.getById(departId);
            //update-begin---author:wangshuai ---date:20220908  for：departmentexclude yourself------------
            pageList = this.baseMapper.getUserInformation(page, sysDepart.getOrgCode(), keyword,sysUser.getId());
            //update-end---author:wangshuai ---date:20220908  for：departmentexclude yourself--------------
        }
        return pageList;
    }

	@Override
	public IPage<SysUser> getUserInformation(Integer tenantId, String departId,String roleId, String keyword, Integer pageSize, Integer pageNo, String excludeUserIdList) {
		IPage<SysUser> pageList = null;
		// departmentIDdoes not exist Just query the user table directly
		Page<SysUser> page = new Page<>(pageNo, pageSize);
		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		
		List<String> userIdList = new ArrayList<>();
		if(oConvertUtils.isNotEmpty(excludeUserIdList)){
			userIdList = Arrays.asList(excludeUserIdList.split(SymbolConstant.COMMA));
		}
		if(oConvertUtils.isNotEmpty(departId)){
			// 有departmentID Need to customizesql
			SysDepart sysDepart = sysDepartService.getById(departId);
			//update-begin-author:taoyan date:2023-1-3 for: user selects component Load user needs according to tenantIDfilter
			//update-begin---author:wangshuai---date:2024-02-02---for:【QQYUN-8239】user role，Add user return2page data，Only one page is actually displayed---
			//update-begin---author:wangshuai---date:2024-02-02---for:【QQYUN-8239】user role，Add user return2page data，Only one page is actually displayed---
			pageList = this.baseMapper.getProcessUserList(page, sysDepart.getOrgCode(), keyword, tenantId, userIdList);
			//update-end---author:wangshuai---date:2024-02-02---for:【QQYUN-8239】user role，Add user return2page data，Only one page is actually displayed---
		} else if (oConvertUtils.isNotEmpty(roleId)) {
			//update-begin---author:wangshuai---date:2024-02-02---for:【QQYUN-8239】user role，Add user return2page data，Only one page is actually displayed---
			pageList = this.sysUserMapper.selectUserListByRoleId(page, roleId, keyword, tenantId,userIdList);
			//update-end---author:wangshuai---date:2024-02-02---for:【QQYUN-8239】user role，Add user return2page data，Only one page is actually displayed---
			//update-end-author:taoyan date:2023-1-3 for: user selects component Load user needs according to tenantIDfilter
		} else{
			LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<>();
			query.eq(SysUser::getStatus,Integer.parseInt(CommonConstant.STATUS_1));
			query.ne(SysUser::getUsername,"_reserve_user_external");
			//update-begin---author:wangshuai---date:2024-02-02---for:【QQYUN-8239】user role，Add user return2page data，Only one page is actually displayed---
			if(oConvertUtils.isNotEmpty(excludeUserIdList)){
				query.notIn(SysUser::getId,Arrays.asList(excludeUserIdList.split(SymbolConstant.COMMA)));
			}
			//update-end---author:wangshuai---date:2024-02-02---for:【QQYUN-8239】user role，Add user return2page data，Only one page is actually displayed---
			// Support tenant isolation
			if (tenantId != null) {
				List<String> userIds = userTenantMapper.getUserIdsByTenantId(tenantId);
				if(oConvertUtils.listIsNotEmpty(userIds)){
					query.in(SysUser::getId, userIds);
				}else{
					query.eq(SysUser::getId,"by tenantIDUser not found");
				}
			}
			
			if(StringUtils.isNotEmpty(keyword)){
				//This syntax can beorSurround it with parentheses，Avoid missing data
				query.and((wrapper) -> wrapper.like(SysUser::getUsername, keyword).or().like(SysUser::getRealname,keyword));
			}
			pageList = sysUserMapper.selectPage(page, query);
		}
		// 批量查询用户的所属department
		// step.1 Get them all first useids
		// step.2 pass useids，一次性查询用户的所属department名字
		List<String> userIds = pageList.getRecords().stream().map(SysUser::getId).collect(Collectors.toList());
		if (userIds.size() > 0) {
			Map<String, String> useDepNames = sysUserService.getDepNamesByUserIds(userIds);
			pageList.getRecords().forEach(item -> item.setOrgCodeTxt(useDepNames.get(item.getId())));
		}
		return pageList;
	}

	@Override
	public List<SysUser> getUsersByDepartTenantId(String departId, Integer tenantId) {
		return baseMapper.getUsersByDepartTenantId(departId,tenantId);
	}

	/**
	 * upgradeSpringBoot2.6.6,Circular dependencies are not allowed
	 * @param userIds
	 * @return
	 */
	private Map<String, String> getDepNamesByUserIds(List<String> userIds) {
		List<SysUserDepVo> list = sysUserMapper.getDepNamesByUserIds(userIds);

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


    /**
     * 查询department岗位下的用户
     * @param departId
     * @param username
     * @param realname
     * @param pageSize
     * @param pageNo
     * @param id
     * @param isMultiTranslate
     * @return
     */
    @Override
    public IPage<SysUser> queryDepartPostUserPageList(String departId, String username, String realname, Integer pageSize, Integer pageNo, String id, String isMultiTranslate) {
        Page<SysUser> page = new Page<SysUser>(pageNo, pageSize);
        if (oConvertUtils.isEmpty(departId)) {
            // departmentIDdoes not exist Just query the user table directly
            return getDepPostListByIdUserName(username,id,isMultiTranslate,page);
        } else {
            // 有departmentID 需要走department岗位用户查询
            return getDepartPostListByIdUserRealName(departId,username,realname,page);
        }
    }

    /**
     * According to departmentid和用户名获取department岗位用户分页列表
     *
     * @param id
     * @param username
     * @param isMultiTranslate
     * @param page
     * @return
     */
    private IPage<SysUser> getDepPostListByIdUserName(String username, String id, String isMultiTranslate, Page<SysUser> page) {
        //需要查询department下的用户，Therefore, it will be written as customsql，NoLambdaExpression usage
        List<String> userIdList = new ArrayList<>();
        List<String> userNameList = new ArrayList<>();
        String userId = "";
        String userName = "";
        if (oConvertUtils.isNotEmpty(username)) {
            String COMMA = ",";
            if (oConvertUtils.isNotEmpty(isMultiTranslate) && username.contains(COMMA)) {
                String[] usernameArr = username.split(COMMA);
                userNameList.addAll(Arrays.asList(usernameArr));
            } else {
                userName = username;
            }
        }
        if (oConvertUtils.isNotEmpty(id)) {
            String COMMA = ",";
            if (oConvertUtils.isNotEmpty(isMultiTranslate) && id.contains(COMMA)) {
                String[] idArr = id.split(COMMA);
                userIdList.addAll(Arrays.asList(idArr));
            } else {
                userId = "";
            }
        }
        //------------------------------------------------------------------------------------------------
        //Whether to enable multi-tenant data isolation of the system management module【SAAS多租户model】
        if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
            String tenantId = oConvertUtils.getString(TenantContext.getTenant(), "0");
            List<String> userIdsList = userTenantMapper.getUserIdsByTenantId(Integer.valueOf(tenantId));
            if (null != userIdsList && !userIdsList.isEmpty()) {
                userIdList.addAll(userIdsList);
            }
        }
        //------------------------------------------------------------------------------------------------
        return sysUserMapper.getDepPostListByIdUserName(page,userIdList,userId,userName,userNameList);
    }

    /**
     * According to departmentid、用户名和真实姓名获取department岗位用户分页列表
     *
     * @param departId
     * @param username
     * @param realname
     * @param page
     * @return
     */
    private IPage<SysUser> getDepartPostListByIdUserRealName(String departId, String username, String realname, Page<SysUser> page) {
        SysDepart sysDepart = sysDepartService.getById(departId);
        return sysUserMapper.getDepartPostListByIdUserRealName(page, username, realname, sysDepart.getOrgCode());
    }
}
