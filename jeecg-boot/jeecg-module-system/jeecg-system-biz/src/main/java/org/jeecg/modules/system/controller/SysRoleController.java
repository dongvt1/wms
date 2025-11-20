package org.jeecg.modules.system.controller;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.base.BaseMap;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.modules.redis.client.JeecgRedisClient;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.modules.system.entity.*;
import org.jeecg.modules.system.model.TreeModel;
import org.jeecg.modules.system.service.*;
import org.jeecg.modules.system.vo.SysUserRoleCountVo;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.jeecg.common.system.vo.LoginUser;
import org.apache.shiro.SecurityUtils;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * character sheet front controller
 * </p>
 *
 * @Author scott
 * @since 2018-12-19
 */
@RestController
@RequestMapping("/sys/role")
@Slf4j
public class SysRoleController {
	@Autowired
	private ISysRoleService sysRoleService;
	
	@Autowired
	private ISysPermissionDataRuleService sysPermissionDataRuleService;
	
	@Autowired
	private ISysRolePermissionService sysRolePermissionService;
	
	@Autowired
	private ISysPermissionService sysPermissionService;

    @Autowired
    private ISysUserRoleService sysUserRoleService;
	@Autowired
	private BaseCommonService baseCommonService;
	@Autowired
	private JeecgRedisClient jeecgRedisClient;
	
	/**
	  * Paginated list query 【system role，No tenant isolation】
	 * @param role
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@RequiresPermissions("system:role:list")
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public Result<IPage<SysRole>> queryPageList(SysRole role,
									  @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
									  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
									  @RequestParam(name="isMultiTranslate", required = false) Boolean isMultiTranslate,
									  HttpServletRequest req) {
        //update-begin---author:wangshuai---date:2025-03-26---for:【issues/7948】Role resolution based onidThe query response is incorrect---
        if(null != isMultiTranslate && isMultiTranslate){
            pageSize = 100;
        }
        //update-end---author:wangshuai---date:2025-03-26---for:【issues/7948】Role resolution based onidThe query response is incorrect---
		Result<IPage<SysRole>> result = new Result<IPage<SysRole>>();
		//QueryWrapper<SysRole> queryWrapper = QueryGenerator.initQueryWrapper(role, req.getParameterMap());
		//IPage<SysRole> pageList = sysRoleService.page(page, queryWrapper);
		Page<SysRole> page = new Page<SysRole>(pageNo, pageSize);
		//换成No tenant isolation的方法，In fact, there are still flaws（defect：If tenant isolation is enabled，Although you can see the roles under other tenants，The editor will prompt an error）
		IPage<SysRole> pageList = sysRoleService.listAllSysRole(page, role);
		result.setSuccess(true);
		result.setResult(pageList);
		return result;
	}
	
	/**
	 * Paginated list query【Tenant role，Do tenant isolation】
	 * @param role
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "/listByTenant", method = RequestMethod.GET)
	public Result<IPage<SysRole>> listByTenant(SysRole role,
												@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
												@RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
												HttpServletRequest req) {
		Result<IPage<SysRole>> result = new Result<IPage<SysRole>>();
		//This interface must pass the tenant to isolate queries
		if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
			role.setTenantId(oConvertUtils.getInt(!"0".equals(TenantContext.getTenant()) ? TenantContext.getTenant() : "", -1));
		}
		
		QueryWrapper<SysRole> queryWrapper = QueryGenerator.initQueryWrapper(role, req.getParameterMap());
		Page<SysRole> page = new Page<SysRole>(pageNo, pageSize);
		IPage<SysRole> pageList = sysRoleService.page(page, queryWrapper);
		result.setSuccess(true);
		result.setResult(pageList);
		return result;
	}
	
	/**
	  *   Add to
	 * @param role
	 * @return
	 */
	@RequestMapping(value = "/add", method = RequestMethod.POST)
    @RequiresPermissions("system:role:add")
	public Result<SysRole> add(@RequestBody SysRole role) {
		Result<SysRole> result = new Result<SysRole>();
		try {
			//Enable multi-tenant isolation,RoleidAutomatically generated10Bit
			//update-begin---author:wangshuai---date:2024-05-23---for:【TV360X-42】Role新增时设置的编码，Inconsistent after saving---
			if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL && oConvertUtils.isEmpty(role.getRoleCode())){
			//update-end---author:wangshuai---date:2024-05-23---for:【TV360X-42】Role新增时设置的编码，Inconsistent after saving---
				role.setRoleCode(RandomUtil.randomString(10));
			}
			role.setCreateTime(new Date());
			sysRoleService.save(role);
			result.success("Add to成功！");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			result.error500("Operation failed");
		}
		return result;
	}
	
	/**
	  *  edit
	 * @param role
	 * @return
	 */
    @RequiresPermissions("system:role:edit")
	@RequestMapping(value = "/edit",method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<SysRole> edit(@RequestBody SysRole role) {
		Result<SysRole> result = new Result<SysRole>();
		SysRole sysrole = sysRoleService.getById(role.getId());
		if(sysrole==null) {
			result.error500("未找到对应Role！");
		}else {
			role.setUpdateTime(new Date());

			//------------------------------------------------------------------
			//in the case ofsaasin isolation，Determine the current tenantidIs it under the current tenant?
			if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
				//Get current user
				LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
				Integer tenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);
				String username = "admin";
				if (!tenantId.equals(sysrole.getTenantId()) && !username.equals(sysUser.getUsername())) {
					baseCommonService.addLog("Unauthorized，修改非本租户下的RoleID：" + role.getId() + "，operator：" + sysUser.getUsername(), CommonConstant.LOG_TYPE_2, CommonConstant.OPERATE_TYPE_3);
					return Result.error("修改Role失败,当前Role不在此租户中。");
				}
			}
			//------------------------------------------------------------------
			
			boolean ok = sysRoleService.updateById(role);
			if(ok) {
				result.success("Modification successful!");
			}
		}
		return result;
	}
	
	/**
	  *   passiddelete
	 * @param id
	 * @return
	 */
    @RequiresPermissions("system:role:delete")
	@RequestMapping(value = "/delete", method = RequestMethod.DELETE)
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
    	//in the case ofsaasin isolation，Determine the current tenantidIs it under the current tenant?
    	if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
			//Get current user
			LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
			int tenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);
			Long getRoleCount = sysRoleService.getRoleCountByTenantId(id, tenantId);
			String username = "admin";
			if(getRoleCount == 0 && !username.equals(sysUser.getUsername())){
				baseCommonService.addLog("Unauthorized，delete非本租户下的RoleID：" + id + "，operator：" + sysUser.getUsername(), CommonConstant.LOG_TYPE_2, CommonConstant.OPERATE_TYPE_4);
				return Result.error("deleteRole失败,当前Role不在此租户中。");
			}
		}
    	
		//update-begin---author:wangshuai---date:2024-01-16---for:【QQYUN-7974】禁止delete admin Role---
		//existsadminRole
		sysRoleService.checkAdminRoleRejectDel(id);
		//update-end---author:wangshuai---date:2024-01-16---for:【QQYUN-7974】禁止delete admin Role---
    	
		sysRoleService.deleteRole(id);

		return Result.ok("deleteRole成功");
	}
	
	/**
	  *  批量delete
	 * @param ids
	 * @return
	 */
    @RequiresPermissions("system:role:deleteBatch")
	@RequestMapping(value = "/deleteBatch", method = RequestMethod.DELETE)
	public Result<SysRole> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		baseCommonService.addLog("deleteRole操作，Roleids：" + ids, CommonConstant.LOG_TYPE_2, CommonConstant.OPERATE_TYPE_4);
		Result<SysRole> result = new Result<SysRole>();
		if(oConvertUtils.isEmpty(ids)) {
			result.error500("未选中Role！");
		}else {
			//in the case ofsaasin isolation，Determine the current tenantidIs it under the current tenant?
			if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
				int tenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);
				String[] roleIds = ids.split(SymbolConstant.COMMA);
				LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
				String username = "admin";
				for (String id:roleIds) {
					Long getRoleCount = sysRoleService.getRoleCountByTenantId(id, tenantId);
					//如果存在Roleidfor0，That is, does not exist，则deleteRole
					if(getRoleCount == 0 && !username.equals(sysUser.getUsername()) ){
						baseCommonService.addLog("Unauthorized，delete非本租户下的RoleID：" + id + "，operator：" + sysUser.getUsername(), CommonConstant.LOG_TYPE_2, CommonConstant.OPERATE_TYPE_4);
						return Result.error("批量deleteRole失败,存在Role不在此租户中，禁止批量delete");
					}
				}
			}
			//验证是否foradminRole
			sysRoleService.checkAdminRoleRejectDel(ids);
			sysRoleService.deleteBatchRole(ids.split(","));
			result.success("deleteRole成功!");
		}
		return result;
	}
	
	/**
	  * passidQuery
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/queryById", method = RequestMethod.GET)
	public Result<SysRole> queryById(@RequestParam(name="id",required=true) String id) {
		Result<SysRole> result = new Result<SysRole>();
		SysRole sysrole = sysRoleService.getById(id);
		if(sysrole==null) {
			result.error500("No corresponding entity found");
		}else {
			result.setResult(sysrole);
			result.setSuccess(true);
		}
		return result;
	}

	/**
	 * Query全部Role（Participate in tenant isolation）
	 * 
	 * @return
	 */
	@RequestMapping(value = "/queryall", method = RequestMethod.GET)
	public Result<List<SysRole>> queryall() {
		Result<List<SysRole>> result = new Result<>();
		LambdaQueryWrapper<SysRole> query = new LambdaQueryWrapper<SysRole>();
		//------------------------------------------------------------------------------------------------
		//Whether to enable multi-tenant data isolation of the system management module【SAASMulti-tenant model】
		if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
			query.eq(SysRole::getTenantId, oConvertUtils.getInt(TenantContext.getTenant(), 0));
		}
		//------------------------------------------------------------------------------------------------
		List<SysRole> list = sysRoleService.list(query);
		if(list==null||list.size()<=0) {
			result.error500("未找到Role信息");
		}else {
			result.setResult(list);
			result.setSuccess(true);
		}
		return result;
	}

	/**
	 * Query全部system role（No tenant isolation）
	 *
	 * @return
	 */
	@RequiresPermissions("system:role:queryallNoByTenant")
	@RequestMapping(value = "/queryallNoByTenant", method = RequestMethod.GET)
	public Result<List<SysRole>> queryallNoByTenant() {
		Result<List<SysRole>> result = new Result<>();
		LambdaQueryWrapper<SysRole> query = new LambdaQueryWrapper<SysRole>();
		List<SysRole> list = sysRoleService.list(query);
		if(list==null||list.size()<=0) {
			result.error500("未找到Role信息");
		}else {
			result.setResult(list);
			result.setSuccess(true);
		}
		return result;
	}
	
	/**
	  * 校验Role编码唯一
	 */
	@RequestMapping(value = "/checkRoleCode", method = RequestMethod.GET)
	public Result<Boolean> checkUsername(String id,String roleCode) {
		Result<Boolean> result = new Result<>();
        //如果此参数forfalseAn exception occurs in the program
		result.setResult(true);
		log.info("--验证Role编码是否唯一---id:"+id+"--roleCode:"+roleCode);
		try {
			SysRole role = null;
			if(oConvertUtils.isNotEmpty(id)) {
				role = sysRoleService.getById(id);
			}
			//SysRole newRole = sysRoleService.getOne(new QueryWrapper<SysRole>().lambda().eq(SysRole::getRoleCode, roleCode));
			SysRole newRole = sysRoleService.getRoleNoTenant(roleCode);
			if(newRole!=null) {
				//If based on the incomingroleCodeQuery到信息了，Then you need to do verification。
				if(role==null) {
					//rolefor空=>New mode=>if onlyroleCodeReturn if existsfalse
					result.setSuccess(false);
					result.setMessage("Role编码已存在");
					return result;
				}else if(!id.equals(newRole.getId())) {
					//otherwise=>edit模式=>judge bothIDIs it consistent?-
					result.setSuccess(false);
					result.setMessage("Role编码已存在");
					return result;
				}
			}
		} catch (Exception e) {
			result.setSuccess(false);
			result.setResult(false);
			result.setMessage(e.getMessage());
			return result;
		}
		result.setSuccess(true);
		return result;
	}

	/**
	 * Exportexcel
	 * @param request
	 */
	@RequestMapping(value = "/exportXls")
	public ModelAndView exportXls(SysRole sysRole,HttpServletRequest request) {
		//------------------------------------------------------------------------------------------------
		//Whether to enable multi-tenant data isolation of the system management module【SAASMulti-tenant model】
		if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
			sysRole.setTenantId(oConvertUtils.getInt(TenantContext.getTenant(), 0));
		}
		//------------------------------------------------------------------------------------------------
		
		// Step.1 组装Query条件
		QueryWrapper<SysRole> queryWrapper = QueryGenerator.initQueryWrapper(sysRole, request.getParameterMap());
		//Step.2 AutoPoi ExportExcel
		ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
		List<SysRole> pageList = sysRoleService.list(queryWrapper);
		//Export文件名称
		mv.addObject(NormalExcelConstants.FILE_NAME,"Role列表");
		mv.addObject(NormalExcelConstants.CLASS,SysRole.class);
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		mv.addObject(NormalExcelConstants.PARAMS,new ExportParams("Role列表数据","Export人:"+user.getRealname(),"Export信息"));
		mv.addObject(NormalExcelConstants.DATA_LIST,pageList);
		return mv;
	}

	/**
	 * passexcelImport data
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/importExcel", method = RequestMethod.POST)
	public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
		for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            // Get the uploaded file object
			MultipartFile file = entity.getValue();
			ImportParams params = new ImportParams();
			params.setTitleRows(2);
			params.setHeadRows(1);
			params.setNeedSave(true);
			try {
				return sysRoleService.importExcelCheckRoleCode(file, params);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				return Result.error("File import failed:" + e.getMessage());
			} finally {
				try {
					file.getInputStream().close();
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}
			}
		}
		return Result.error("File import failed！");
	}
	
	/**
	 * Query数据规则数据
	 */
	@GetMapping(value = "/datarule/{permissionId}/{roleId}")
	public Result<?> loadDatarule(@PathVariable("permissionId") String permissionId,@PathVariable("roleId") String roleId) {
		List<SysPermissionDataRule> list = sysPermissionDataRuleService.getPermRuleListByPermId(permissionId);
		if(list==null || list.size()==0) {
			return Result.error("Permission configuration information not found");
		}else {
			Map<String,Object> map = new HashMap(5);
			map.put("datarule", list);
			LambdaQueryWrapper<SysRolePermission> query = new LambdaQueryWrapper<SysRolePermission>()
					.eq(SysRolePermission::getPermissionId, permissionId)
					.isNotNull(SysRolePermission::getDataRuleIds)
					.eq(SysRolePermission::getRoleId,roleId);
			SysRolePermission sysRolePermission = sysRolePermissionService.getOne(query);
			if(sysRolePermission==null) {
				//return Result.error("未找到Rolemenu配置信息");
			}else {
				String drChecked = sysRolePermission.getDataRuleIds();
				if(oConvertUtils.isNotEmpty(drChecked)) {
					map.put("drChecked", drChecked.endsWith(",")?drChecked.substring(0, drChecked.length()-1):drChecked);
				}
			}
			return Result.ok(map);
			//TODO 以后按钮权限的Query也走这个请求 nothing more thanmapAdd two morekey
		}
	}
	
	/**
	 * Save data rules至Rolemenu关联表
	 */
	@PostMapping(value = "/datarule")
	public Result<?> saveDatarule(@RequestBody JSONObject jsonObject) {
		try {
			String permissionId = jsonObject.getString("permissionId");
			String roleId = jsonObject.getString("roleId");
			String dataRuleIds = jsonObject.getString("dataRuleIds");
			log.info("Save data rules>>"+"menuID:"+permissionId+"RoleID:"+ roleId+"Data permissionsID:"+dataRuleIds);
			LambdaQueryWrapper<SysRolePermission> query = new LambdaQueryWrapper<SysRolePermission>()
					.eq(SysRolePermission::getPermissionId, permissionId)
					.eq(SysRolePermission::getRoleId,roleId);
			SysRolePermission sysRolePermission = sysRolePermissionService.getOne(query);
			if(sysRolePermission==null) {
				return Result.error("请先保存Rolemenu权限!");
			}else {
				sysRolePermission.setDataRuleIds(dataRuleIds);
				this.sysRolePermissionService.updateById(sysRolePermission);
			}
		} catch (Exception e) {
			log.error("SysRoleController.saveDatarule()Exception occurred：" + e.getMessage(),e);
			return Result.error("Save failed");
		}
		return Result.ok("Saved successfully!");
	}
	
	
	/**
	 * 用户Role授权功能，Querymenu权限树
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/queryTreeList", method = RequestMethod.GET)
	public Result<Map<String,Object>> queryTreeList(HttpServletRequest request) {
		Result<Map<String,Object>> result = new Result<>();
		//All permissionsids
		List<String> ids = new ArrayList<>();
		try {
			LambdaQueryWrapper<SysPermission> query = new LambdaQueryWrapper<SysPermission>();
			query.eq(SysPermission::getDelFlag, CommonConstant.DEL_FLAG_0);
			query.orderByAsc(SysPermission::getSortNo);
			List<SysPermission> list = sysPermissionService.list(query);
			for(SysPermission sysPer : list) {
				ids.add(sysPer.getId());
			}
			List<TreeModel> treeList = new ArrayList<>();
			getTreeModelList(treeList, list, null);
			Map<String,Object> resMap = new HashMap(5);
            //All tree node data
			resMap.put("treeList", treeList);
            //All treesids
			resMap.put("ids", ids);
			result.setResult(resMap);
			result.setSuccess(true);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}
	
	private void getTreeModelList(List<TreeModel> treeList,List<SysPermission> metaList,TreeModel temp) {
		for (SysPermission permission : metaList) {
			String tempPid = permission.getParentId();
			TreeModel tree = new TreeModel(permission.getId(), tempPid, permission.getName(),permission.getRuleFlag(), permission.isLeaf());
			if(temp==null && oConvertUtils.isEmpty(tempPid)) {
				treeList.add(tree);
				if(!tree.getIsLeaf()) {
					getTreeModelList(treeList, metaList, tree);
				}
			}else if(temp!=null && tempPid!=null && tempPid.equals(temp.getKey())){
				temp.getChildren().add(tree);
				if(!tree.getIsLeaf()) {
					getTreeModelList(treeList, metaList, tree);
				}
			}
			
		}
	}

    /**
     * 分页获取全部Role列表（包含每个Role的数量）
     * @return
     */
    @RequestMapping(value = "/queryPageRoleCount", method = RequestMethod.GET)
    public Result<IPage<SysUserRoleCountVo>> queryPageRoleCount(@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                                                @RequestParam(name="pageSize", defaultValue="10") Integer pageSize) {
        Result<IPage<SysUserRoleCountVo>> result = new Result<>();
		LambdaQueryWrapper<SysRole> query = new LambdaQueryWrapper<SysRole>();
		//------------------------------------------------------------------------------------------------
		//Whether to enable multi-tenant data isolation of the system management module【SAASMulti-tenant model】
		if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
			query.eq(SysRole::getTenantId, oConvertUtils.getInt(TenantContext.getTenant(), 0));
		}
		//------------------------------------------------------------------------------------------------
        Page<SysRole> page = new Page<>(pageNo, pageSize);
        IPage<SysRole> pageList = sysRoleService.page(page, query);
        List<SysRole> records = pageList.getRecords();
        IPage<SysUserRoleCountVo> sysRoleCountPage = new PageDTO<>();
        List<SysUserRoleCountVo> sysCountVoList = new ArrayList<>();
        //循环Role数据获取每个Role下面对应的Role数量
        for (SysRole role:records) {
            LambdaQueryWrapper<SysUserRole> countQuery = new LambdaQueryWrapper<>();
			countQuery.eq(SysUserRole::getRoleId,role.getId());
            long count = sysUserRoleService.count(countQuery);
            SysUserRoleCountVo countVo = new SysUserRoleCountVo();
            BeanUtils.copyProperties(role,countVo);
            countVo.setCount(count);
            sysCountVoList.add(countVo);
        }
        sysRoleCountPage.setRecords(sysCountVoList);
        sysRoleCountPage.setTotal(pageList.getTotal());
        sysRoleCountPage.setSize(pageList.getSize());
        result.setSuccess(true);
        result.setResult(sysRoleCountPage);
        return result;
    }

}
