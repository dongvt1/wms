package org.jeecg.modules.system.controller;

import java.util.*;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.modules.system.entity.*;
import org.jeecg.modules.system.service.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.system.base.controller.JeecgController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

 /**
 * @Description: Department role
 * @Author: jeecg-boot
 * @Date:   2020-02-12
 * @Version: V1.0
 */
@Slf4j
@Tag(name="Department role")
@RestController
@RequestMapping("/sys/sysDepartRole")
public class SysDepartRoleController extends JeecgController<SysDepartRole, ISysDepartRoleService> {
	@Autowired
	private ISysDepartRoleService sysDepartRoleService;

	@Autowired
	private ISysDepartRoleUserService departRoleUserService;

	@Autowired
	private ISysDepartPermissionService sysDepartPermissionService;

	 @Autowired
	 private ISysDepartRolePermissionService sysDepartRolePermissionService;

	 @Autowired
	 private ISysDepartService sysDepartService;

	 @Autowired
     private BaseCommonService baseCommonService;
     
	/**
	 * Paginated list query
	 *
	 * @param sysDepartRole
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="Department role-Paginated list query")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(SysDepartRole sysDepartRole,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   @RequestParam(name="deptId",required=false) String deptId,
								   HttpServletRequest req) {
		QueryWrapper<SysDepartRole> queryWrapper = QueryGenerator.initQueryWrapper(sysDepartRole, req.getParameterMap());
		Page<SysDepartRole> page = new Page<SysDepartRole>(pageNo, pageSize);
//		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
//		List<String> deptIds = null;
//		if(oConvertUtils.isEmpty(deptId)){
//			if(oConvertUtils.isNotEmpty(user.getUserIdentity()) && user.getUserIdentity().equals(CommonConstant.USER_IDENTITY_2) ){
//				deptIds = sysDepartService.getMySubDepIdsByDepId(user.getDepartIds());
//			}else{
//				return Result.ok(null);
//			}
//		}else{
//			deptIds = sysDepartService.getSubDepIdsByDepId(deptId);
//		}
//		queryWrapper.in("depart_id",deptIds);

		//my department，If you select a department, you can only view the roles under the current department.
		//update-begin---author:chenrui ---date:20250107  for：[QQYUN-10775]Verification codes can be reused #7674------------
		if(oConvertUtils.isNotEmpty(deptId)){
			queryWrapper.eq("depart_id",deptId);
			IPage<SysDepartRole> pageList = sysDepartRoleService.page(page, queryWrapper);
			return Result.ok(pageList);
		}else{
			return Result.ok(null);
		}
		//update-end---author:chenrui ---date:20250107  for：[QQYUN-10775]Verification codes can be reused #7674------------
	}
	
	/**
	 * Add to
	 *
	 * @param sysDepartRole
	 * @return
	 */
    @RequiresPermissions("system:depart:role:add")
	@Operation(summary="Department role-Add to")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody SysDepartRole sysDepartRole) {
		sysDepartRoleService.save(sysDepartRole);
		return Result.ok("Add to成功！");
	}
	
	/**
	 * edit
	 *
	 * @param sysDepartRole
	 * @return
	 */
	@Operation(summary="Department role-edit")
    @RequiresPermissions("system:depart:role:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<?> edit(@RequestBody SysDepartRole sysDepartRole) {
		sysDepartRoleService.updateById(sysDepartRole);
		return Result.ok("edit成功!");
	}
	
	/**
	 * passiddelete
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "Department role-passiddelete")
	@Operation(summary="Department role-passiddelete")
    @RequiresPermissions("system:depart:role:delete")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		sysDepartRoleService.removeById(id);
		return Result.ok("delete成功!");
	}
	
	/**
	 * 批量delete
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "Department role-批量delete")
	@Operation(summary="Department role-批量delete")
    @RequiresPermissions("system:depart:role:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.sysDepartRoleService.deleteDepartRole(Arrays.asList(ids.split(",")));
		//this.sysDepartRoleService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量delete成功！");
	}
	
	/**
	 * passidQuery
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="Department role-passidQuery")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		SysDepartRole sysDepartRole = sysDepartRoleService.getById(id);
		return Result.ok(sysDepartRole);
	}

	 /**
	  * Get roles under department
	  * @param departId
	  * @return
	  */
	@RequestMapping(value = "/getDeptRoleList", method = RequestMethod.GET)
	public Result<List<SysDepartRole>> getDeptRoleList(@RequestParam(value = "departId") String departId,@RequestParam(value = "userId") String userId){
		Result<List<SysDepartRole>> result = new Result<>();
		//Query选中部门的Role
		List<SysDepartRole> deptRoleList = sysDepartRoleService.list(new LambdaQueryWrapper<SysDepartRole>().eq(SysDepartRole::getDepartId,departId));
		result.setSuccess(true);
		result.setResult(deptRoleList);
		return result;
	}

	 /**
	  * set up
	  * @param json
	  * @return
	  */
     @RequiresPermissions("system:depart:role:userAdd")
	 @RequestMapping(value = "/deptRoleUserAdd", method = RequestMethod.POST)
	 public Result<?> deptRoleAdd(@RequestBody JSONObject json) {
		 String newRoleId = json.getString("newRoleId");
		 String oldRoleId = json.getString("oldRoleId");
		 String userId = json.getString("userId");
		 departRoleUserService.deptRoleUserAdd(userId,newRoleId,oldRoleId);
         //update-begin---author:wangshuai ---date:20220316  for：[VUEN-234]Department role分配Add to敏感日志------------
         LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
         baseCommonService.addLog("To department usersID："+userId+"Assign roles，operator： " +loginUser.getUsername() ,CommonConstant.LOG_TYPE_2, 2);
         //update-end---author:wangshuai ---date:20220316  for：[VUEN-234]Department role分配Add to敏感日志------------
         return Result.ok("Add to成功！");
	 }

	 /**
	  * According to userid获取已set upDepartment role
	  * @param userId
	  * @return
	  */
	 @RequestMapping(value = "/getDeptRoleByUserId", method = RequestMethod.GET)
	 public Result<List<SysDepartRoleUser>> getDeptRoleByUserId(@RequestParam(value = "userId") String userId,@RequestParam(value = "departId") String departId){
		 Result<List<SysDepartRoleUser>> result = new Result<>();
		 //Query部门下Role
		 List<SysDepartRole> roleList = sysDepartRoleService.list(new QueryWrapper<SysDepartRole>().eq("depart_id",departId));
		 List<String> roleIds = roleList.stream().map(SysDepartRole::getId).collect(Collectors.toList());
		 //According to roleid,useridQuery已授权Role
		 List<SysDepartRoleUser> roleUserList = null;
		 if(roleIds!=null && roleIds.size()>0){
			 roleUserList = departRoleUserService.list(new QueryWrapper<SysDepartRoleUser>().eq("user_id",userId).in("drole_id",roleIds));
		 }
		 result.setSuccess(true);
		 result.setResult(roleUserList);
		 return result;
	 }

	 /**
	  * Query数据规则数据
	  */
	 @GetMapping(value = "/datarule/{permissionId}/{departId}/{roleId}")
	 public Result<?> loadDatarule(@PathVariable("permissionId") String permissionId,@PathVariable("departId") String departId,@PathVariable("roleId") String roleId) {
		//Query已授权的部门规则
	 	List<SysPermissionDataRule> list = sysDepartPermissionService.getPermRuleListByDeptIdAndPermId(departId,permissionId);
		 if(list==null || list.size()==0) {
			 return Result.error("Permission configuration information not found");
		 }else {
			 Map<String,Object> map = new HashMap(5);
			 map.put("datarule", list);
			 LambdaQueryWrapper<SysDepartRolePermission> query = new LambdaQueryWrapper<SysDepartRolePermission>()
					 .eq(SysDepartRolePermission::getPermissionId, permissionId)
					 .eq(SysDepartRolePermission::getRoleId,roleId);
			 SysDepartRolePermission sysRolePermission = sysDepartRolePermissionService.getOne(query);
			 if(sysRolePermission==null) {
				 //return Result.error("Character menu configuration information not found");
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
	  * Save data rules to role menu association table
	  */
	 @PostMapping(value = "/datarule")
	 public Result<?> saveDatarule(@RequestBody JSONObject jsonObject) {
		 try {
			 String permissionId = jsonObject.getString("permissionId");
			 String roleId = jsonObject.getString("roleId");
			 String dataRuleIds = jsonObject.getString("dataRuleIds");
			 log.info("Save data rules>>"+"menuID:"+permissionId+"RoleID:"+ roleId+"Data permissionsID:"+dataRuleIds);
			 LambdaQueryWrapper<SysDepartRolePermission> query = new LambdaQueryWrapper<SysDepartRolePermission>()
					 .eq(SysDepartRolePermission::getPermissionId, permissionId)
					 .eq(SysDepartRolePermission::getRoleId,roleId);
			 SysDepartRolePermission sysRolePermission = sysDepartRolePermissionService.getOne(query);
			 if(sysRolePermission==null) {
				 return Result.error("请先保存Rolemenu权限!");
			 }else {
				 sysRolePermission.setDataRuleIds(dataRuleIds);
				 this.sysDepartRolePermissionService.updateById(sysRolePermission);
			 }
		 } catch (Exception e) {
			 log.error("SysRoleController.saveDatarule()Exception occurred：" + e.getMessage(),e);
			 return Result.error("Save failed");
		 }
		 return Result.ok("Saved successfully!");
	 }

  /**
   * Exportexcel
   *
   * @param request
   * @param sysDepartRole
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, SysDepartRole sysDepartRole) {
      return super.exportXls(request, sysDepartRole, SysDepartRole.class, "Department role");
  }

  /**
   * passexcelImport data
   *
   * @param request
   * @param response
   * @return
   */
  @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
  public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
      return super.importExcel(request, response, SysDepartRole.class);
  }

}
