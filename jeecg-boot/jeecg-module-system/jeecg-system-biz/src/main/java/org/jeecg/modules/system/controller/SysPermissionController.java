package org.jeecg.modules.system.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.Md5Util;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.JeecgBaseConfig;
import org.jeecg.config.shiro.ShiroRealm;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.modules.system.constant.DefIndexConst;
import org.jeecg.modules.system.entity.*;
import org.jeecg.modules.system.model.SysPermissionTree;
import org.jeecg.modules.system.model.TreeModel;
import org.jeecg.modules.system.service.*;
import org.jeecg.modules.system.util.PermissionDataUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * Menu permission table front controller
 * </p>
 *
 * @Author scott
 * @since 2018-12-21
 */
@Slf4j
@RestController
@RequestMapping("/sys/permission")
public class SysPermissionController {

	@Autowired
	private ISysPermissionService sysPermissionService;

	@Autowired
	private ISysRolePermissionService sysRolePermissionService;

	@Autowired
	private ISysPermissionDataRuleService sysPermissionDataRuleService;

	@Autowired
	private ISysDepartPermissionService sysDepartPermissionService;

	@Autowired
	private ISysUserService sysUserService;

	@Autowired
	private JeecgBaseConfig jeecgBaseConfig;

	@Autowired
    private BaseCommonService baseCommonService;

	@Autowired
	private ISysRoleIndexService sysRoleIndexService;
	
	@Autowired
	private ShiroRealm shiroRealm;

    /**
     * submenu
     */
	private static final String CHILDREN = "children";

	/**
	 * Load data node
	 *
	 * @return
	 */
	//@RequiresPermissions("system:permission:list")
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public Result<List<SysPermissionTree>> list(SysPermission sysPermission, HttpServletRequest req) {
        long start = System.currentTimeMillis();
		Result<List<SysPermissionTree>> result = new Result<>();
		try {
			LambdaQueryWrapper<SysPermission> query = new LambdaQueryWrapper<SysPermission>();
			query.eq(SysPermission::getDelFlag, CommonConstant.DEL_FLAG_0);
			query.orderByAsc(SysPermission::getSortNo);
			
			//Support through menu name，fuzzy query
			if(oConvertUtils.isNotEmpty(sysPermission.getName())){
				query.like(SysPermission::getName, sysPermission.getName());
			}
			List<SysPermission> list = sysPermissionService.list(query);
			List<SysPermissionTree> treeList = new ArrayList<>();

			//If there are menu name query conditions，Then tile the data Don’t be a superior or subordinate
			if(oConvertUtils.isNotEmpty(sysPermission.getName())){
				if(list!=null && list.size()>0){
					treeList = list.stream().map(e -> {
						e.setLeaf(true);
						return new SysPermissionTree(e);
					}).collect(Collectors.toList());
				}
			}else{
				getTreeList(treeList, list, null);
			}
			result.setResult(treeList);
			result.setSuccess(true);
            log.info("======Get all menu data=====time consuming:" + (System.currentTimeMillis() - start) + "millisecond");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}

	/*update_begin author:wuxianquan date:20190908 for:Check the first-level menu first，当用户点击展开菜单时加载submenu */
	/**
	 * System menu list(First level menu)
	 *
	 * @return
	 */
	@RequestMapping(value = "/getSystemMenuList", method = RequestMethod.GET)
	public Result<List<SysPermissionTree>> getSystemMenuList() {
        long start = System.currentTimeMillis();
		Result<List<SysPermissionTree>> result = new Result<>();
		try {
			LambdaQueryWrapper<SysPermission> query = new LambdaQueryWrapper<SysPermission>();
			query.eq(SysPermission::getMenuType,CommonConstant.MENU_TYPE_0);
			query.eq(SysPermission::getDelFlag, CommonConstant.DEL_FLAG_0);
			query.orderByAsc(SysPermission::getSortNo);
			List<SysPermission> list = sysPermissionService.list(query);
			List<SysPermissionTree> sysPermissionTreeList = new ArrayList<SysPermissionTree>();
			for(SysPermission sysPermission : list){
				SysPermissionTree sysPermissionTree = new SysPermissionTree(sysPermission);
				sysPermissionTreeList.add(sysPermissionTree);
			}
			result.setResult(sysPermissionTreeList);
			result.setSuccess(true);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
        log.info("======GetFirst level menu数据=====time consuming:" + (System.currentTimeMillis() - start) + "millisecond");
		return result;
	}

	/**
	 * 查询submenu
	 * @param parentId
	 * @return
	 */
	@RequestMapping(value = "/getSystemSubmenu", method = RequestMethod.GET)
	public Result<List<SysPermissionTree>> getSystemSubmenu(@RequestParam("parentId") String parentId){
		Result<List<SysPermissionTree>> result = new Result<>();
		try{
			LambdaQueryWrapper<SysPermission> query = new LambdaQueryWrapper<SysPermission>();
			query.eq(SysPermission::getParentId,parentId);
			query.eq(SysPermission::getDelFlag, CommonConstant.DEL_FLAG_0);
			query.orderByAsc(SysPermission::getSortNo);
			List<SysPermission> list = sysPermissionService.list(query);
			List<SysPermissionTree> sysPermissionTreeList = new ArrayList<SysPermissionTree>();
			for(SysPermission sysPermission : list){
				SysPermissionTree sysPermissionTree = new SysPermissionTree(sysPermission);
				sysPermissionTreeList.add(sysPermissionTree);
			}
			result.setResult(sysPermissionTreeList);
			result.setSuccess(true);
		}catch (Exception e){
			log.error(e.getMessage(), e);
		}
		return result;
	}
	/*update_end author:wuxianquan date:20190908 for:Check the first-level menu first，当用户点击展开菜单时加载submenu */

	// update_begin author:sunjianlei date:20200108 for: Add batch according to parentIDInterface for querying sub-menu -------------
	/**
	 * 查询submenu
	 *
	 * @param parentIds fatherID（Use half-width commas to separate multiple）
	 * @return return key-value of Map
	 */
	@GetMapping("/getSystemSubmenuBatch")
	public Result getSystemSubmenuBatch(@RequestParam("parentIds") String parentIds) {
		try {
			LambdaQueryWrapper<SysPermission> query = new LambdaQueryWrapper<>();
			List<String> parentIdList = Arrays.asList(parentIds.split(","));
			query.in(SysPermission::getParentId, parentIdList);
			query.eq(SysPermission::getDelFlag, CommonConstant.DEL_FLAG_0);
			query.orderByAsc(SysPermission::getSortNo);
			List<SysPermission> list = sysPermissionService.list(query);
			Map<String, List<SysPermissionTree>> listMap = new HashMap(5);
			for (SysPermission item : list) {
				String pid = item.getParentId();
				if (parentIdList.contains(pid)) {
					List<SysPermissionTree> mapList = listMap.get(pid);
					if (mapList == null) {
						mapList = new ArrayList<>();
					}
					mapList.add(new SysPermissionTree(item));
					listMap.put(pid, mapList);
				}
			}
			return Result.ok(listMap);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Result.error("批量查询submenu失败：" + e.getMessage());
		}
	}
	// update_end author:sunjianlei date:20200108 for: Add batch according to parentIDInterface for querying sub-menu -------------

//	/**
//	 * 查询用户拥有of菜单权限和Button permissions（According to user account）
//	 * 
//	 * @return
//	 */
//	@RequestMapping(value = "/queryByUser", method = RequestMethod.GET)
//	public Result<JSONArray> queryByUser(HttpServletRequest req) {
//		Result<JSONArray> result = new Result<>();
//		try {
//			String username = req.getParameter("username");
//			List<SysPermission> metaList = sysPermissionService.queryByUser(username);
//			JSONArray jsonArray = new JSONArray();
//			this.getPermissionJsonArray(jsonArray, metaList, null);
//			result.setResult(jsonArray);
//			result.success("Query successful");
//		} catch (Exception e) {
//			result.error500("Query failed:" + e.getMessage());
//			log.error(e.getMessage(), e);
//		}
//		return result;
//	}

	/**
	 * 查询用户拥有of菜单权限和Button permissions
	 *
	 * @return
	 */
	@RequestMapping(value = "/getUserPermissionByToken", method = RequestMethod.GET)
	//@DynamicTable(value = DynamicTableConstant.SYS_ROLE_INDEX)
	public Result<?> getUserPermissionByToken(HttpServletRequest request) {
		Result<JSONObject> result = new Result<JSONObject>();
		try {
			//Directly obtaining the current user is not applicable to the front endtoken
			LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
			if (oConvertUtils.isEmpty(loginUser)) {
				return Result.error("Please log in to the system！");
			}
			List<SysPermission> metaList = sysPermissionService.queryByUser(loginUser.getId());
			//Add homepage route
			//update-begin-author:taoyan date:20200211 for: TASK #3368 【route cache】首页of缓存设置有问题，需要根据后台ofrouting配置来实现是否缓存

			//update-begin--Author:zyf Date:20220425  for:Custom homepage address LOWCOD-1578
			String version = request.getHeader(CommonConstant.VERSION);
			SysRoleIndex defIndexCfg = sysUserService.getDynamicIndexByUserRole(loginUser.getUsername(), version);
			if (defIndexCfg == null) {
				defIndexCfg = sysRoleIndexService.initDefaultIndex();
			}
			//update-end--Author:zyf  Date:20220425  for：Custom homepage address LOWCOD-1578

			// If there is no authorized role home page，则自动Add homepage route
			if (!PermissionDataUtil.hasIndexPage(metaList, defIndexCfg)) {
				LambdaQueryWrapper<SysPermission> indexQueryWrapper = new LambdaQueryWrapper<>();
				indexQueryWrapper.eq(SysPermission::getUrl, defIndexCfg.getUrl());
				SysPermission indexMenu = sysPermissionService.getOne(indexQueryWrapper);
				if (indexMenu == null) {
					indexMenu = new SysPermission();
					indexMenu.setUrl(defIndexCfg.getUrl());
					indexMenu.setComponent(defIndexCfg.getComponent());
					indexMenu.setRoute(defIndexCfg.getRoute());
					indexMenu.setName(DefIndexConst.DEF_INDEX_NAME);
					indexMenu.setMenuType(0);
				}
				// 如果没有授权First level menu，则自身变forFirst level menu
				if (indexMenu.getParentId() != null && !PermissionDataUtil.hasMenuById(metaList, indexMenu.getParentId())) {
					indexMenu.setMenuType(0);
					indexMenu.setParentId(null);
				}
				if (oConvertUtils.isEmpty(indexMenu.getIcon())) {
					indexMenu.setIcon("ant-design:home");
				}
				metaList.add(0, indexMenu);
			}
			//update-end-author:taoyan date:20200211 for: TASK #3368 【route cache】首页of缓存设置有问题，需要根据后台ofrouting配置来实现是否缓存

/* TODO Note： 这段代码of主要作用是：把首页菜单of组件替换成角色菜单of组件，由于现在of逻辑如果角色菜单不存在则自动插入一条，So this code is not needed for the time being
			List<SysPermission> menus = metaList.stream().filter(sysPermission -> {
				if (defIndexCfg.getUrl().equals(sysPermission.getUrl())) {
					return true;
				}
				return defIndexCfg.getUrl().equals(sysPermission.getUrl());
			}).collect(Collectors.toList());
			//update-begin---author:liusq ---date:2022-06-29  for：设置Custom homepage address和组件----------
			if (menus.size() == 1) {
				String component = defIndexCfg.getComponent();
				String routeUrl = defIndexCfg.getUrl();
				boolean route = defIndexCfg.isRoute();
				if (oConvertUtils.isNotEmpty(routeUrl)) {
					menus.get(0).setComponent(component);
					menus.get(0).setRoute(route);
					menus.get(0).setUrl(routeUrl);
				} else {
					menus.get(0).setComponent(component);
				}
			}
			//update-end---author:liusq ---date:2022-06-29  for：设置Custom homepage address和组件-----------
*/

			JSONObject json = new JSONObject();
			JSONArray menujsonArray = new JSONArray();
			this.getPermissionJsonArray(menujsonArray, metaList, null);
			//First level menu下ofsubmenu全部是隐藏routing，则First level menu不show
			this.handleFirstLevelMenuHidden(menujsonArray);

			JSONArray authjsonArray = new JSONArray();
			this.getAuthJsonArray(authjsonArray, metaList);
			//查询所有of权限
			LambdaQueryWrapper<SysPermission> query = new LambdaQueryWrapper<SysPermission>().select( SysPermission::getName, SysPermission::getPermsType, SysPermission::getPerms, SysPermission::getStatus);
			query.eq(SysPermission::getDelFlag, CommonConstant.DEL_FLAG_0);
			query.eq(SysPermission::getMenuType, CommonConstant.MENU_TYPE_2);
			//query.eq(SysPermission::getStatus, "1");
			List<SysPermission> allAuthList = sysPermissionService.list(query);
			JSONArray allauthjsonArray = new JSONArray();
			this.getAllAuthJsonArray(allauthjsonArray, allAuthList);
			//routing menu
			json.put("menu", menujsonArray);
			//Button permissions（用户拥有of权限集合）
			json.put("auth", authjsonArray);
			// Button permissions（用户拥有of权限集合）
			List<String> codeList = metaList.stream()
					.filter((permission) -> CommonConstant.MENU_TYPE_2.equals(permission.getMenuType()) && CommonConstant.STATUS_1.equals(permission.getStatus()))
					.collect(ArrayList::new, (list, permission) -> list.add(permission.getPerms()), ArrayList::addAll);
			// 所拥有of权限编码(vue3dedicated)
			json.put("codeList", codeList);
			//All permission configuration sets（Button permissions，Access rights）
			json.put("allAuth", allauthjsonArray);
			//Data source security mode
			json.put("sysSafeMode", jeecgBaseConfig.getFirewall()!=null? jeecgBaseConfig.getFirewall().getDataSourceSafe(): false);
			result.setResult(json);
		} catch (Exception e) {
			result.error500("Query failed:" + e.getMessage());  
			log.error(e.getMessage(), e);
		}
		return result;
	}

	/**
	 * 【vue3dedicated】Get
	 * 1、查询用户拥有ofbutton/表单Access rights
	 * 2、All permissions (Menu permission configuration)
	 * 3、System safe mode (Turn ononline报表of数据源必填)
	 */
	@RequestMapping(value = "/getPermCode", method = RequestMethod.GET)
	public Result<?> getPermCode() {
		try {
			// 直接Get当前用户
			LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
			if (oConvertUtils.isEmpty(loginUser)) {
				return Result.error("Please log in to the system！");
			}
			// Get当前用户of权限集合
			List<SysPermission> metaList = sysPermissionService.queryByUser(loginUser.getId());
            // Button permissions（用户拥有of权限集合）
            List<String> codeList = metaList.stream()
                    .filter((permission) -> CommonConstant.MENU_TYPE_2.equals(permission.getMenuType()) && CommonConstant.STATUS_1.equals(permission.getStatus()))
                    .collect(ArrayList::new, (list, permission) -> list.add(permission.getPerms()), ArrayList::addAll);
            //
			JSONArray authArray = new JSONArray();
			this.getAuthJsonArray(authArray, metaList);
			// 查询所有of权限
			LambdaQueryWrapper<SysPermission> query = new LambdaQueryWrapper<SysPermission>().select( SysPermission::getName, SysPermission::getPermsType, SysPermission::getPerms, SysPermission::getStatus);
			query.eq(SysPermission::getDelFlag, CommonConstant.DEL_FLAG_0);
			query.eq(SysPermission::getMenuType, CommonConstant.MENU_TYPE_2);
			List<SysPermission> allAuthList = sysPermissionService.list(query);
			JSONArray allAuthArray = new JSONArray();
			this.getAllAuthJsonArray(allAuthArray, allAuthList);
			JSONObject result = new JSONObject();
            // 所拥有of权限编码
			result.put("codeList", codeList);
			//Button permissions（用户拥有of权限集合）
			result.put("auth", authArray);
			//All permission configuration sets（Button permissions，Access rights）
			result.put("allAuth", allAuthArray);
            //Data source security mode
			result.put("sysSafeMode", jeecgBaseConfig.getFirewall()!=null? jeecgBaseConfig.getFirewall().getDataSourceSafe(): null);
            return Result.OK(result);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
            return Result.error("Query failed:" + e.getMessage());
		}
	}

	/**
	  * Add menu
	 * @param permission
	 * @return
	 */
    @RequiresPermissions("system:permission:add")
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public Result<SysPermission> add(@RequestBody SysPermission permission) {
		Result<SysPermission> result = new Result<SysPermission>();
		try {
			permission = PermissionDataUtil.intelligentProcessData(permission);
			sysPermissionService.addPermission(permission);
			result.success("Added successfully！");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			result.error500("Operation failed");
		}
		return result;
	}

	/**
	  * Edit menu
	 * @param permission
	 * @return
	 */
    @RequiresPermissions("system:permission:edit")
	@RequestMapping(value = "/edit", method = { RequestMethod.PUT, RequestMethod.POST })
	public Result<SysPermission> edit(@RequestBody SysPermission permission) {
		Result<SysPermission> result = new Result<>();
		try {
			permission = PermissionDataUtil.intelligentProcessData(permission);
			sysPermissionService.editPermission(permission);
			result.success("Modification successful！");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			result.error500("Operation failed");
		}
		return result;
	}

	/**
	 * Check if menu path exists
	 * @param id
	 * @param url
	 * @return
	 */
	@RequestMapping(value = "/checkPermDuplication", method = RequestMethod.GET)
	public Result<String> checkPermDuplication(@RequestParam(name = "id", required = false) String id,@RequestParam(name = "url") String url,@RequestParam(name = "alwaysShow") Boolean alwaysShow) {
		Result<String> result = new Result<>();
		try {
			boolean check=sysPermissionService.checkPermDuplication(id,url,alwaysShow);
			if(check){
				return Result.ok("This value is available！");
			}
			return Result.error("Duplication of access paths is not allowed，Please redefine！");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			result.error500("Operation failed");
		}
		return result;
	}

	/**
	  * delete menu
	 * @param id
	 * @return
	 */
    @RequiresPermissions("system:permission:delete")
	@RequestMapping(value = "/delete", method = RequestMethod.DELETE)
	public Result<SysPermission> delete(@RequestParam(name = "id", required = true) String id) {
		Result<SysPermission> result = new Result<>();
		try {
			sysPermissionService.deletePermission(id);
			result.success("Delete successfully!");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			result.error500(e.getMessage());
		}
		return result;
	}

	/**
	  * 批量delete menu
	 * @param ids
	 * @return
	 */
    @RequiresPermissions("system:permission:deleteBatch")
	@RequestMapping(value = "/deleteBatch", method = RequestMethod.DELETE)
	public Result<SysPermission> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
		Result<SysPermission> result = new Result<>();
		try {
            String[] arr = ids.split(",");
			for (String id : arr) {
				if (oConvertUtils.isNotEmpty(id)) {
					try {
						sysPermissionService.deletePermission(id);
					} catch (JeecgBootException e) {
						if(e.getMessage()!=null && e.getMessage().contains("Menu information not found")){
							log.warn(e.getMessage());
						}else{
							throw e;
						}
					}
				}
			}
			result.success("Delete successfully!");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			result.error500("Delete failed!");
		}
		return result;
	}

	/**
	 * Get全部of权限树
	 *
	 * @return
	 */
	@RequestMapping(value = "/queryTreeList", method = RequestMethod.GET)
	public Result<Map<String, Object>> queryTreeList() {
		Result<Map<String, Object>> result = new Result<>();
		// All permissionsids
		List<String> ids = new ArrayList<>();
		try {
			LambdaQueryWrapper<SysPermission> query = new LambdaQueryWrapper<SysPermission>();
			query.eq(SysPermission::getDelFlag, CommonConstant.DEL_FLAG_0);
			query.orderByAsc(SysPermission::getSortNo);
			List<SysPermission> list = sysPermissionService.list(query);
			for (SysPermission sysPer : list) {
				ids.add(sysPer.getId());
			}
			List<TreeModel> treeList = new ArrayList<>();
			getTreeModelList(treeList, list, null);

			Map<String, Object> resMap = new HashMap<String, Object>(5);
            // All tree node data
			resMap.put("treeList", treeList);
            // All treesids
			resMap.put("ids", ids);
			result.setResult(resMap);
			result.setSuccess(true);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}

	/**
	 * 异步Load data node [接口是废of,Not used]
	 *
	 * @return
	 */
	@RequestMapping(value = "/queryListAsync", method = RequestMethod.GET)
	public Result<List<TreeModel>> queryAsync(@RequestParam(name = "pid", required = false) String parentId) {
		Result<List<TreeModel>> result = new Result<>();
		try {
			List<TreeModel> list = sysPermissionService.queryListByParentId(parentId);
			if (list == null || list.size() <= 0) {
				result.error500("Role information not found");
			} else {
				result.setResult(list);
				result.setSuccess(true);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return result;
	}

	/**
	 * Query role authorization
	 *
	 * @return
	 */
	@RequestMapping(value = "/queryRolePermission", method = RequestMethod.GET)
	public Result<List<String>> queryRolePermission(@RequestParam(name = "roleId", required = true) String roleId) {
		Result<List<String>> result = new Result<>();
		try {
			List<SysRolePermission> list = sysRolePermissionService.list(new QueryWrapper<SysRolePermission>().lambda().eq(SysRolePermission::getRoleId, roleId));
			result.setResult(list.stream().map(sysRolePermission -> String.valueOf(sysRolePermission.getPermissionId())).collect(Collectors.toList()));
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
	@RequestMapping(value = "/saveRolePermission", method = RequestMethod.POST)
    @RequiresPermissions("system:permission:saveRole")
	public Result<String> saveRolePermission(@RequestBody JSONObject json) {
		long start = System.currentTimeMillis();
		Result<String> result = new Result<>();
		try {
			String roleId = json.getString("roleId");
			String permissionIds = json.getString("permissionIds");
			String lastPermissionIds = json.getString("lastpermissionIds");
			this.sysRolePermissionService.saveRolePermission(roleId, permissionIds, lastPermissionIds);
			//update-begin---author:wangshuai ---date:20220316  for：[VUEN-234]User management role authorization to add sensitive logs------------
            LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
			baseCommonService.addLog("Modify roleID: "+roleId+" of权限配置，operator： " +loginUser.getUsername() ,CommonConstant.LOG_TYPE_2, 2);
            //update-end---author:wangshuai ---date:20220316  for：[VUEN-234]User management role authorization to add sensitive logs------------
			result.success("Saved successfully！");
			log.info("======Role authorization successful=====time consuming:" + (System.currentTimeMillis() - start) + "millisecond");

			//update-begin---author:scott ---date:2024-06-18  for：【TV360X-1320】To assign permissions, you must log out and log in again to take effect.，Causes a lot of trouble for users---
			// 清除当前用户of授权缓存信息
			Subject currentUser = SecurityUtils.getSubject();
			if (currentUser.isAuthenticated()) {
				shiroRealm.clearCache(currentUser.getPrincipals());
			}
			//update-end---author:scott ---date::2024-06-18  for：【TV360X-1320】To assign permissions, you must log out and log in again to take effect.，Causes a lot of trouble for users---

		} catch (Exception e) {
			result.error500("Authorization failed！");
			log.error(e.getMessage(), e);
		}
		return result;
	}

	private void getTreeList(List<SysPermissionTree> treeList, List<SysPermission> metaList, SysPermissionTree temp) {
		for (SysPermission permission : metaList) {
			String tempPid = permission.getParentId();
			SysPermissionTree tree = new SysPermissionTree(permission);
			if (temp == null && oConvertUtils.isEmpty(tempPid)) {
				treeList.add(tree);
				if (!tree.getIsLeaf()) {
					getTreeList(treeList, metaList, tree);
				}
			} else if (temp != null && tempPid != null && tempPid.equals(temp.getId())) {
				temp.getChildren().add(tree);
				if (!tree.getIsLeaf()) {
					getTreeList(treeList, metaList, tree);
				}
			}

		}
	}

	private void getTreeModelList(List<TreeModel> treeList, List<SysPermission> metaList, TreeModel temp) {
		for (SysPermission permission : metaList) {
			String tempPid = permission.getParentId();
			TreeModel tree = new TreeModel(permission);
			if (temp == null && oConvertUtils.isEmpty(tempPid)) {
				treeList.add(tree);
				if (!tree.getIsLeaf()) {
					getTreeModelList(treeList, metaList, tree);
				}
			} else if (temp != null && tempPid != null && tempPid.equals(temp.getKey())) {
				temp.getChildren().add(tree);
				if (!tree.getIsLeaf()) {
					getTreeModelList(treeList, metaList, tree);
				}
			}

		}
	}

	/**
	 * First level menuofsubmenu全部是隐藏routing，则First level menu不show
	 * @param jsonArray
	 */
	private void handleFirstLevelMenuHidden(JSONArray jsonArray) {
		jsonArray = jsonArray.stream().map(obj -> {
			JSONObject returnObj = new JSONObject();
			JSONObject jsonObj = (JSONObject)obj;
			if(jsonObj.containsKey(CHILDREN)){
				JSONArray childrens = jsonObj.getJSONArray(CHILDREN);
                childrens = childrens.stream().filter(arrObj -> !"true".equals(((JSONObject) arrObj).getString("hidden"))).collect(Collectors.toCollection(JSONArray::new));
                if(childrens==null || childrens.size()==0){
                    jsonObj.put("hidden",true);

                    //vue3version compatibility code
                    JSONObject meta = new JSONObject();
                    meta.put("hideMenu",true);
                    jsonObj.put("meta", meta);
                }
			}
			return returnObj;
		}).collect(Collectors.toCollection(JSONArray::new));
	}


	/**
	  *  Get权限JSONarray
	 * @param jsonArray
	 * @param allList
	 */
	private void getAllAuthJsonArray(JSONArray jsonArray,List<SysPermission> allList) {
		JSONObject json = null;
		for (SysPermission permission : allList) {
			json = new JSONObject();
			json.put("action", permission.getPerms());
			json.put("status", permission.getStatus());
			//1show2Disable
			json.put("type", permission.getPermsType());
			json.put("describe", permission.getName());
			jsonArray.add(json);
		}
	}

	/**
	  *  Get权限JSONarray
	 * @param jsonArray
	 * @param metaList
	 */
	private void getAuthJsonArray(JSONArray jsonArray,List<SysPermission> metaList) {
		for (SysPermission permission : metaList) {
			if(permission.getMenuType()==null) {
				continue;
			}
			JSONObject json = null;
			if(permission.getMenuType().equals(CommonConstant.MENU_TYPE_2) &&CommonConstant.STATUS_1.equals(permission.getStatus())) {
				json = new JSONObject();
				json.put("action", permission.getPerms());
				json.put("type", permission.getPermsType());
				json.put("describe", permission.getName());
				jsonArray.add(json);
			}
		}
	}
	/**
	  *  Get菜单JSONarray
	 * @param jsonArray
	 * @param metaList
	 * @param parentJson
	 */
	private void getPermissionJsonArray(JSONArray jsonArray, List<SysPermission> metaList, JSONObject parentJson) {
		for (SysPermission permission : metaList) {
			if (permission.getMenuType() == null) {
				continue;
			}
			String tempPid = permission.getParentId();
			JSONObject json = getPermissionJsonObject(permission);
			if(json==null) {
				continue;
			}
			if (parentJson == null && oConvertUtils.isEmpty(tempPid)) {
				jsonArray.add(json);
				if (!permission.isLeaf()) {
					getPermissionJsonArray(jsonArray, metaList, json);
				}
			} else if (parentJson != null && oConvertUtils.isNotEmpty(tempPid) && tempPid.equals(parentJson.getString("id"))) {
				// type( 0：First level menu 1：submenu 2：button )
				if (permission.getMenuType().equals(CommonConstant.MENU_TYPE_2)) {
					JSONObject metaJson = parentJson.getJSONObject("meta");
					if (metaJson.containsKey("permissionList")) {
						metaJson.getJSONArray("permissionList").add(json);
					} else {
						JSONArray permissionList = new JSONArray();
						permissionList.add(json);
						metaJson.put("permissionList", permissionList);
					}
					// type( 0：First level menu 1：submenu 2：button )
				} else if (permission.getMenuType().equals(CommonConstant.MENU_TYPE_1) || permission.getMenuType().equals(CommonConstant.MENU_TYPE_0)) {
					if (parentJson.containsKey("children")) {
						parentJson.getJSONArray("children").add(json);
					} else {
						JSONArray children = new JSONArray();
						children.add(json);
						parentJson.put("children", children);
					}

					if (!permission.isLeaf()) {
						getPermissionJsonArray(jsonArray, metaList, json);
					}
				}
			}

		}
	}

	/**
	 * Generate routes based on menu configurationjson
	 * @param permission
	 * @return
	 */
		private JSONObject getPermissionJsonObject(SysPermission permission) {
		JSONObject json = new JSONObject();
		// type(0：First level menu 1：submenu 2：button)
		if (permission.getMenuType().equals(CommonConstant.MENU_TYPE_2)) {
			//json.put("action", permission.getPerms());
			//json.put("type", permission.getPermsType());
			//json.put("describe", permission.getName());
			return null;
		} else if (permission.getMenuType().equals(CommonConstant.MENU_TYPE_0) || permission.getMenuType().equals(CommonConstant.MENU_TYPE_1)) {
			json.put("id", permission.getId());
			if (permission.isRoute()) {
                //Indicates generating routes
				json.put("route", "1");
			} else {
                //Indicates that routes will not be generated
				json.put("route", "0");
			}

			if (isWwwHttpUrl(permission.getUrl())) {
				json.put("path", Md5Util.md5Encode(permission.getUrl(), "utf-8"));
			} else {
				json.put("path", permission.getUrl());
			}

			// Important rules：routingname (passURL生成routingname,routingnameFor front-end development，Page jump use)
			if (oConvertUtils.isNotEmpty(permission.getComponentName())) {
				json.put("name", permission.getComponentName());
			} else {
				json.put("name", urlToRouteName(permission.getUrl()));
			}

			JSONObject meta = new JSONObject();
			// 是否隐藏routing，默认都是showof
			if (permission.isHidden()) {
				json.put("hidden", true);
                //vue3version compatibility code
                meta.put("hideMenu",true);
			}
			// 聚合routing
			if (permission.isAlwaysShow()) {
				json.put("alwaysShow", true);
			}
			json.put("component", permission.getComponent());
			// Set by the user whether to cache the page Use boolean
			if (permission.isKeepAlive()) {
				meta.put("keepAlive", true);
			} else {
				meta.put("keepAlive", false);
			}

			/*update_begin author:wuxianquan date:20190908 for:Add external link menu opening method to menu information */
			//How to open external link menu
			if (permission.isInternalOrExternal()) {
				meta.put("internalOrExternal", true);
			} else {
				meta.put("internalOrExternal", false);
			}
			/* update_end author:wuxianquan date:20190908 for: Add external link menu opening method to menu information*/

			meta.put("title", permission.getName());

			//update-begin--Author:scott  Date:20201015 for：route cache问题，closedtabThe page will not be refreshed when it is opened again. #842
			String component = permission.getComponent();
			if(oConvertUtils.isNotEmpty(permission.getComponentName()) || oConvertUtils.isNotEmpty(component)){
				meta.put("componentName", oConvertUtils.getString(permission.getComponentName(),component.substring(component.lastIndexOf("/")+1)));
			}
			//update-end--Author:scott  Date:20201015 for：route cache问题，closedtabThe page will not be refreshed when it is opened again. #842

			if (oConvertUtils.isEmpty(permission.getParentId())) {
				// First level menu跳转地址
				json.put("redirect", permission.getRedirect());
				if (oConvertUtils.isNotEmpty(permission.getIcon())) {
					meta.put("icon", permission.getIcon());
				}
			} else {
				if (oConvertUtils.isNotEmpty(permission.getIcon())) {
					meta.put("icon", permission.getIcon());
				}
			}
			if (isWwwHttpUrl(permission.getUrl())) {
				meta.put("url", permission.getUrl());
			}
			// update-begin--Author:sunjianlei  Date:20210918 for：New adaptationvue3项目of隐藏tabFunction
			if (permission.isHideTab()) {
				meta.put("hideTab", true);
			}
			// update-end--Author:sunjianlei  Date:20210918 for：New adaptationvue3项目of隐藏tabFunction
			json.put("meta", meta);
		}

		return json;
	}

	/**
	 * Determine whether it is an external networkURL For example： http://localhost:8080/jeecg-boot/swagger-ui.html#/ Support special formats： {{
	 * window._CONFIG['domianURL'] }}/druid/ {{ JScode snippet }}，Foreground parsing will be performed automaticallyJScode snippet
	 *
	 * @return
	 */
	private boolean isWwwHttpUrl(String url) {
        boolean flag = url != null && (url.startsWith(CommonConstant.HTTP_PROTOCOL) || url.startsWith(CommonConstant.HTTPS_PROTOCOL) || url.startsWith(SymbolConstant.DOUBLE_LEFT_CURLY_BRACKET));
        if (flag) {
			return true;
		}
		return false;
	}

	/**
	 * passURL生成routingname（removeURLprefix slash，替换内容中of斜杠‘/’for-） Example： URL = /isystem/role RouteName =
	 * isystem-role
	 *
	 * @return
	 */
	private String urlToRouteName(String url) {
		if (oConvertUtils.isNotEmpty(url)) {
			if (url.startsWith(SymbolConstant.SINGLE_SLASH)) {
				url = url.substring(1);
			}
			url = url.replace("/", "-");

			// special mark
			url = url.replace(":", "@");
			return url;
		} else {
			return null;
		}
	}

	/**
	 * According to the menuid来Get其对应of权限数据
	 *
	 * @param sysPermissionDataRule
	 * @return
	 */
	@RequestMapping(value = "/getPermRuleListByPermId", method = RequestMethod.GET)
	public Result<List<SysPermissionDataRule>> getPermRuleListByPermId(SysPermissionDataRule sysPermissionDataRule) {
		List<SysPermissionDataRule> permRuleList = sysPermissionDataRuleService.getPermRuleListByPermId(sysPermissionDataRule.getPermissionId());
		Result<List<SysPermissionDataRule>> result = new Result<>();
		result.setSuccess(true);
		result.setResult(permRuleList);
		return result;
	}

	/**
	 * Add menu权限数据
	 *
	 * @param sysPermissionDataRule
	 * @return
	 */
    @RequiresPermissions("system:permission:addRule")
	@RequestMapping(value = "/addPermissionRule", method = RequestMethod.POST)
	public Result<SysPermissionDataRule> addPermissionRule(@RequestBody SysPermissionDataRule sysPermissionDataRule) {
		Result<SysPermissionDataRule> result = new Result<SysPermissionDataRule>();
		try {
			sysPermissionDataRule.setCreateTime(new Date());
			sysPermissionDataRuleService.savePermissionDataRule(sysPermissionDataRule);
			result.success("Added successfully！");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			result.error500("Operation failed");
		}
		return result;
	}

    @RequiresPermissions("system:permission:editRule")
	@RequestMapping(value = "/editPermissionRule", method = { RequestMethod.PUT, RequestMethod.POST })
	public Result<SysPermissionDataRule> editPermissionRule(@RequestBody SysPermissionDataRule sysPermissionDataRule) {
		Result<SysPermissionDataRule> result = new Result<SysPermissionDataRule>();
		try {
			sysPermissionDataRuleService.saveOrUpdate(sysPermissionDataRule);
			result.success("Update successful！");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			result.error500("Operation failed");
		}
		return result;
	}

	/**
	 * delete menu权限数据
	 *
	 * @param id
	 * @return
	 */
    @RequiresPermissions("system:permission:deleteRule")
	@RequestMapping(value = "/deletePermissionRule", method = RequestMethod.DELETE)
	public Result<SysPermissionDataRule> deletePermissionRule(@RequestParam(name = "id", required = true) String id) {
		Result<SysPermissionDataRule> result = new Result<SysPermissionDataRule>();
		try {
			sysPermissionDataRuleService.deletePermissionDataRule(id);
			result.success("Delete successfully！");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			result.error500("Operation failed");
		}
		return result;
	}

	/**
	 * Query menu permission data
	 *
	 * @param sysPermissionDataRule
	 * @return
	 */
	@RequestMapping(value = "/queryPermissionRule", method = RequestMethod.GET)
	public Result<List<SysPermissionDataRule>> queryPermissionRule(SysPermissionDataRule sysPermissionDataRule) {
		Result<List<SysPermissionDataRule>> result = new Result<>();
		try {
			List<SysPermissionDataRule> permRuleList = sysPermissionDataRuleService.queryPermissionRule(sysPermissionDataRule);
			result.setResult(permRuleList);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			result.error500("Operation failed");
		}
		return result;
	}

	/**
	 * Department authority table
	 * @param departId
	 * @return
	 */
	@RequestMapping(value = "/queryDepartPermission", method = RequestMethod.GET)
	public Result<List<String>> queryDepartPermission(@RequestParam(name = "departId", required = true) String departId) {
		Result<List<String>> result = new Result<>();
		try {
			List<SysDepartPermission> list = sysDepartPermissionService.list(new QueryWrapper<SysDepartPermission>().lambda().eq(SysDepartPermission::getDepartId, departId));
			result.setResult(list.stream().map(sysDepartPermission -> String.valueOf(sysDepartPermission.getPermissionId())).collect(Collectors.toList()));
			result.setSuccess(true);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}

	/**
	 * Save department authorization
	 *
	 * @return
	 */
	@RequestMapping(value = "/saveDepartPermission", method = RequestMethod.POST)
    @RequiresPermissions("system:permission:saveDepart")
	public Result<String> saveDepartPermission(@RequestBody JSONObject json) {
		long start = System.currentTimeMillis();
		Result<String> result = new Result<>();
		try {
			String departId = json.getString("departId");
			String permissionIds = json.getString("permissionIds");
			String lastPermissionIds = json.getString("lastpermissionIds");
			this.sysDepartPermissionService.saveDepartPermission(departId, permissionIds, lastPermissionIds);
			result.success("Saved successfully！");
			log.info("======Department authorization successful=====time consuming:" + (System.currentTimeMillis() - start) + "millisecond");
		} catch (Exception e) {
			result.error500("Authorization failed！");
			log.error(e.getMessage(), e);
		}
		return result;
	}

}
