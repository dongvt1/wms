package org.jeecg.modules.system.controller;

import java.util.*;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.modules.system.entity.SysDepartPermission;
import org.jeecg.modules.system.entity.SysDepartRolePermission;
import org.jeecg.modules.system.entity.SysPermission;
import org.jeecg.modules.system.entity.SysPermissionDataRule;
import org.jeecg.modules.system.model.TreeModel;
import org.jeecg.modules.system.service.ISysDepartPermissionService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.modules.system.service.ISysDepartRolePermissionService;
import org.jeecg.modules.system.service.ISysPermissionDataRuleService;
import org.jeecg.modules.system.service.ISysPermissionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

 /**
 * @Description: Department authority table
 * @Author: jeecg-boot
 * @Date:   2020-02-11
 * @Version: V1.0
 */
@Slf4j
@Tag(name="Department authority table")
@RestController
@RequestMapping("/sys/sysDepartPermission")
public class SysDepartPermissionController extends JeecgController<SysDepartPermission, ISysDepartPermissionService> {
	@Autowired
	private ISysDepartPermissionService sysDepartPermissionService;

	 @Autowired
	 private ISysPermissionDataRuleService sysPermissionDataRuleService;

	 @Autowired
	 private ISysPermissionService sysPermissionService;

	 @Autowired
	 private ISysDepartRolePermissionService sysDepartRolePermissionService;

	 @Autowired
     private BaseCommonService baseCommonService;
	 
	/**
	 * Paginated list query
	 *
	 * @param sysDepartPermission
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="Department authority table-Paginated list query")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(SysDepartPermission sysDepartPermission,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<SysDepartPermission> queryWrapper = QueryGenerator.initQueryWrapper(sysDepartPermission, req.getParameterMap());
		Page<SysDepartPermission> page = new Page<SysDepartPermission>(pageNo, pageSize);
		IPage<SysDepartPermission> pageList = sysDepartPermissionService.page(page, queryWrapper);
		return Result.ok(pageList);
	}
	
	/**
	 * Add to
	 *
	 * @param sysDepartPermission
	 * @return
	 */
	@Operation(summary="Department authority table-Add to")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody SysDepartPermission sysDepartPermission) {
		sysDepartPermissionService.save(sysDepartPermission);
		return Result.ok("Add to成功！");
	}
	
	/**
	 * edit
	 *
	 * @param sysDepartPermission
	 * @return
	 */
	@Operation(summary="Department authority table-edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<?> edit(@RequestBody SysDepartPermission sysDepartPermission) {
		sysDepartPermissionService.updateById(sysDepartPermission);
		return Result.ok("edit成功!");
	}
	
	/**
	 * passiddelete
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="Department authority table-passiddelete")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		sysDepartPermissionService.removeById(id);
		return Result.ok("delete成功!");
	}
	
	/**
	 * 批量delete
	 *
	 * @param ids
	 * @return
	 */
	@Operation(summary="Department authority table-批量delete")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.sysDepartPermissionService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("批量delete成功！");
	}
	
	/**
	 * passidQuery
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="Department authority table-passidQuery")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		SysDepartPermission sysDepartPermission = sysDepartPermissionService.getById(id);
		return Result.ok(sysDepartPermission);
	}

	/**
	* Exportexcel
	*
	* @param request
	* @param sysDepartPermission
	*/
	@RequestMapping(value = "/exportXls")
	public ModelAndView exportXls(HttpServletRequest request, SysDepartPermission sysDepartPermission) {
	  return super.exportXls(request, sysDepartPermission, SysDepartPermission.class, "Department authority table");
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
	  return super.importExcel(request, response, SysDepartPermission.class);
	}

	/**
	* department管理授权Query数据规则数据
	*/
	@GetMapping(value = "/datarule/{permissionId}/{departId}")
	public Result<?> loadDatarule(@PathVariable("permissionId") String permissionId,@PathVariable("departId") String departId) {
		List<SysPermissionDataRule> list = sysPermissionDataRuleService.getPermRuleListByPermId(permissionId);
		if(list==null || list.size()==0) {
			return Result.error("Permission configuration information not found");
		}else {
			Map<String,Object> map = new HashMap(5);
			map.put("datarule", list);
			LambdaQueryWrapper<SysDepartPermission> query = new LambdaQueryWrapper<SysDepartPermission>()
				 .eq(SysDepartPermission::getPermissionId, permissionId)
				 .eq(SysDepartPermission::getDepartId,departId);
			SysDepartPermission sysDepartPermission = sysDepartPermissionService.getOne(query);
			if(sysDepartPermission==null) {
			 //return Result.error("Character menu configuration information not found");
			}else {
				String drChecked = sysDepartPermission.getDataRuleIds();
				if(oConvertUtils.isNotEmpty(drChecked)) {
					map.put("drChecked", drChecked.endsWith(",")?drChecked.substring(0, drChecked.length()-1):drChecked);
				}
			}
			return Result.ok(map);
			//TODO 以后按钮权限的Query也走这个请求 nothing more thanmapAdd two morekey
		}
	}

	/**
	* Save data rules to department menu association table
	*/
	@PostMapping(value = "/datarule")
	public Result<?> saveDatarule(@RequestBody JSONObject jsonObject) {
		try {
			String permissionId = jsonObject.getString("permissionId");
			String departId = jsonObject.getString("departId");
			String dataRuleIds = jsonObject.getString("dataRuleIds");
			log.info("Save data rules>>"+"menuID:"+permissionId+"departmentID:"+ departId+"Data permissionsID:"+dataRuleIds);
			LambdaQueryWrapper<SysDepartPermission> query = new LambdaQueryWrapper<SysDepartPermission>()
				 .eq(SysDepartPermission::getPermissionId, permissionId)
				 .eq(SysDepartPermission::getDepartId,departId);
			SysDepartPermission sysDepartPermission = sysDepartPermissionService.getOne(query);
			if(sysDepartPermission==null) {
				return Result.error("请先保存departmentmenu权限!");
			}else {
				sysDepartPermission.setDataRuleIds(dataRuleIds);
			 	this.sysDepartPermissionService.updateById(sysDepartPermission);
			}
		} catch (Exception e) {
		 	log.error("SysDepartPermissionController.saveDatarule()Exception occurred：" + e.getMessage(),e);
		 	return Result.error("Save failed");
		}
		return Result.ok("Saved successfully!");
	}

	 /**
	  * Query角色授权
	  *
	  * @return
	  */
	 @RequestMapping(value = "/queryDeptRolePermission", method = RequestMethod.GET)
	 public Result<List<String>> queryDeptRolePermission(@RequestParam(name = "roleId", required = true) String roleId) {
		 Result<List<String>> result = new Result<>();
		 try {
			 List<SysDepartRolePermission> list = sysDepartRolePermissionService.list(new QueryWrapper<SysDepartRolePermission>().lambda().eq(SysDepartRolePermission::getRoleId, roleId));
			 result.setResult(list.stream().map(sysDepartRolePermission -> String.valueOf(sysDepartRolePermission.getPermissionId())).collect(Collectors.toList()));
			 result.setSuccess(true);
		 } catch (Exception e) {
			 log.error(e.getMessage(), e);
		 }
		 return result;
	 }

	 /**
	  * Save role authorization
	  *
	  * @return
	  */
	 @RequestMapping(value = "/saveDeptRolePermission", method = RequestMethod.POST)
	 public Result<String> saveDeptRolePermission(@RequestBody JSONObject json) {
		 long start = System.currentTimeMillis();
		 Result<String> result = new Result<>();
		 try {
			 String roleId = json.getString("roleId");
			 String permissionIds = json.getString("permissionIds");
			 String lastPermissionIds = json.getString("lastpermissionIds");
			 this.sysDepartRolePermissionService.saveDeptRolePermission(roleId, permissionIds, lastPermissionIds);
			 result.success("Saved successfully！");
             //update-begin---author:wangshuai ---date:20220316  for：[VUEN-234]department角色授权Add to敏感日志------------
             LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
             baseCommonService.addLog("修改department角色ID:"+roleId+"Permission configuration，operator： " +loginUser.getUsername() ,CommonConstant.LOG_TYPE_2, 2);
             //update-end---author:wangshuai ---date:20220316  for：[VUEN-234]department角色授权Add to敏感日志------------
             log.info("======department角色授权成功=====time consuming:" + (System.currentTimeMillis() - start) + "millisecond");
		 } catch (Exception e) {
			 result.error500("Authorization failed！");
			 log.error(e.getMessage(), e);
		 }
		 return result;
	 }

	 /**
	  * User role authorization function，Querymenu权限树
	  * @param request
	  * @return
	  */
	 @RequestMapping(value = "/queryTreeListForDeptRole", method = RequestMethod.GET)
	 public Result<Map<String,Object>> queryTreeListForDeptRole(@RequestParam(name="departId",required=true) String departId,HttpServletRequest request) {
		 Result<Map<String,Object>> result = new Result<>();
		 //All permissionsids
		 List<String> ids = new ArrayList<>();
		 try {
			 List<SysPermission> list = sysPermissionService.queryDepartPermissionList(departId);
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

	 private void getTreeModelList(List<TreeModel> treeList, List<SysPermission> metaList, TreeModel temp) {
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

}
