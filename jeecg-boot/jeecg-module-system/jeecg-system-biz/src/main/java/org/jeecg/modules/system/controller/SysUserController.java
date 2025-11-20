package org.jeecg.modules.system.controller;


import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.PermissionData;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.modules.redis.client.JeecgRedisClient;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.*;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.modules.system.entity.*;
import org.jeecg.modules.system.excelstyle.ExcelExportSysUserStyle;
import org.jeecg.modules.system.model.DepartIdModel;
import org.jeecg.modules.system.model.SysUserSysDepPostModel;
import org.jeecg.modules.system.model.SysUserSysDepartModel;
import org.jeecg.modules.system.service.*;
import org.jeecg.modules.system.util.ImportSysUserCache;
import org.jeecg.modules.system.vo.SysDepartUsersVO;
import org.jeecg.modules.system.vo.SysUserExportVo;
import org.jeecg.modules.system.vo.SysUserRoleVO;
import org.jeecg.modules.system.vo.lowapp.DepartAndUserInfo;
import org.jeecg.modules.system.vo.lowapp.UpdateDepartInfo;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * User table front controller
 * </p>
 *
 * @Author scott
 * @since 2018-12-20
 */
@Slf4j
@RestController
@RequestMapping("/sys/user")
public class SysUserController {

	@Autowired
	private ISysUserService sysUserService;

    @Autowired
    private ISysDepartService sysDepartService;

	@Autowired
	private ISysUserRoleService sysUserRoleService;

	@Autowired
	private ISysUserDepartService sysUserDepartService;

    @Autowired
    private ISysDepartRoleUserService departRoleUserService;

    @Autowired
    private ISysDepartRoleService departRoleService;

	@Autowired
	private RedisUtil redisUtil;

    @Value("${jeecg.path.upload}")
    private String upLoadPath;

    @Autowired
    private BaseCommonService baseCommonService;

    @Autowired
    private ISysPositionService sysPositionService;

    @Autowired
    private ISysUserTenantService userTenantService;

    @Autowired
    private JeecgRedisClient jeecgRedisClient;
    
    /**
     * Get user data under the tenant（Support tenant isolation）
     * @param user
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @PermissionData(pageComponent = "system/UserList")
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public Result<IPage<SysUser>> queryPageList(SysUser user,@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
									  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,HttpServletRequest req) {
		QueryWrapper<SysUser> queryWrapper = QueryGenerator.initQueryWrapper(user, req.getParameterMap());
        //------------------------------------------------------------------------------------------------
        //Whether to enable multi-tenant data isolation of the system management module【SAASMulti-tenant model】
        if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
            String tenantId = oConvertUtils.getString(TenantContext.getTenant(), "-1");
            List<String> userIds = userTenantService.getUserIdsByTenantId(Integer.valueOf(tenantId));
            if (oConvertUtils.listIsNotEmpty(userIds)) {
                queryWrapper.in("id", userIds);
            }else{
                queryWrapper.eq("id", "No users can be queried through the tenant");
            }
        }
        //------------------------------------------------------------------------------------------------
        return sysUserService.queryPageList(req, queryWrapper, pageSize, pageNo);
	}

    /**
     * Get system user data（Query all users，No tenant isolation）
     *
     * @param user
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @RequiresPermissions("system:user:listAll")
    @RequestMapping(value = "/listAll", method = RequestMethod.GET)
    public Result<IPage<SysUser>> queryAllPageList(SysUser user, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
        QueryWrapper<SysUser> queryWrapper = QueryGenerator.initQueryWrapper(user, req.getParameterMap());
        return sysUserService.queryPageList(req, queryWrapper, pageSize, pageNo);
    }

    @RequiresPermissions("system:user:add")
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public Result<SysUser> add(@RequestBody JSONObject jsonObject) {
		Result<SysUser> result = new Result<SysUser>();
		String selectedRoles = jsonObject.getString("selectedroles");
		String selectedDeparts = jsonObject.getString("selecteddeparts");
		try {
			SysUser user = JSON.parseObject(jsonObject.toJSONString(), SysUser.class);
			user.setCreateTime(new Date());//Set creation time
			String salt = oConvertUtils.randomGen(8);
			user.setSalt(salt);
			String passwordEncode = PasswordUtil.encrypt(user.getUsername(), user.getPassword(), salt);
			user.setPassword(passwordEncode);
			user.setStatus(1);
			user.setDelFlag(CommonConstant.DEL_FLAG_0);
			//User table字段org_codeHis value cannot be set here
            user.setOrgCode(null);
			// Save user go oneservice Guaranteed transaction
            //Get tenantsids
            String relTenantIds = jsonObject.getString("relTenantIds");
            sysUserService.saveUser(user, selectedRoles, selectedDeparts, relTenantIds, false);
            baseCommonService.addLog("Add user，username： " +user.getUsername() ,CommonConstant.LOG_TYPE_2, 2);
			result.success("Added successfully！");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			result.error500("Operation failed");
		}
		return result;
	}

    @RequiresPermissions("system:user:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<SysUser> edit(@RequestBody JSONObject jsonObject) {
		Result<SysUser> result = new Result<SysUser>();
		try {
			SysUser sysUser = sysUserService.getById(jsonObject.getString("id"));
			baseCommonService.addLog("Edit user，username： " +sysUser.getUsername() ,CommonConstant.LOG_TYPE_2, 2);
			if(sysUser==null) {
				result.error500("No corresponding entity found");
			}else {
				SysUser user = JSON.parseObject(jsonObject.toJSONString(), SysUser.class);
				user.setUpdateTime(new Date());
				//String passwordEncode = PasswordUtil.encrypt(user.getUsername(), user.getPassword(), sysUser.getSalt());
				user.setPassword(sysUser.getPassword());
				String roles = jsonObject.getString("selectedroles");
                String departs = jsonObject.getString("selecteddeparts");
                if(oConvertUtils.isEmpty(departs)){
                    //vue3.0The front end only passeddepartIds
                    departs=user.getDepartIds();
                }
                //User table字段org_codeHis value cannot be set here
                user.setOrgCode(null);
                // Modify the user to go oneservice Guaranteed transaction
                //Get tenantsids
                String relTenantIds = jsonObject.getString("relTenantIds");
                String updateFromPage = jsonObject.getString("updateFromPage");
				sysUserService.editUser(user, roles, departs, relTenantIds, updateFromPage);
				result.success("Modification successful!");
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			result.error500("Operation failed");
		}
		return result;
	}

	/**
	 * Delete user
	 */
    @RequiresPermissions("system:user:delete")
	@RequestMapping(value = "/delete", method = RequestMethod.DELETE)
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		baseCommonService.addLog("Delete user，id： " +id ,CommonConstant.LOG_TYPE_2, 3);
        List<String> userNameList = sysUserService.userIdToUsername(Arrays.asList(id));
		this.sysUserService.deleteUser(id);

        if (!userNameList.isEmpty()) {
            String joinedString = String.join(",", userNameList);
        }
		return Result.ok("Delete user成功");
	}

	/**
	 * 批量Delete user
	 */
    @RequiresPermissions("system:user:deleteBatch")
	@RequestMapping(value = "/deleteBatch", method = RequestMethod.DELETE)
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		baseCommonService.addLog("批量Delete user， ids： " +ids ,CommonConstant.LOG_TYPE_2, 3);
        List<String> userNameList = sysUserService.userIdToUsername(Arrays.asList(ids.split(",")));
		this.sysUserService.deleteBatchUsers(ids);
		
        // User change，Trigger sync workflow
        if (!userNameList.isEmpty()) {
            String joinedString = String.join(",", userNameList);
        }
		return Result.ok("批量Delete user成功");
	}

	/**
	  * freeze&Unfreeze user
	 * @param jsonObject
	 * @return
	 */
    @RequiresPermissions("system:user:frozenBatch")
	@RequestMapping(value = "/frozenBatch", method = RequestMethod.PUT)
	public Result<SysUser> frozenBatch(@RequestBody JSONObject jsonObject) {
		Result<SysUser> result = new Result<SysUser>();
		try {
			String ids = jsonObject.getString("ids");
			sysUserService.checkUserAdminRejectDel(ids);
			String status = jsonObject.getString("status");
			String[] arr = ids.split(",");
            for (String id : arr) {
				if(oConvertUtils.isNotEmpty(id)) {
                    //update-begin---author:liusq ---date:20230620  for：[QQYUN-5577]User list-freeze用户，After thawing again，User still cannot log in，Have caching issues #5066------------
                    sysUserService.updateStatus(id,status);
                    //update-end---author:liusq ---date:20230620  for：[QQYUN-5577]User list-freeze用户，After thawing again，User still cannot log in，Have caching issues #5066------------
                }
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			result.error500("Operation failed"+e.getMessage());
		}
		result.success("Operation successful!");
		return result;

    }

    @RequiresPermissions("system:user:queryById")
    @RequestMapping(value = "/queryById", method = RequestMethod.GET)
    public Result<SysUser> queryById(@RequestParam(name = "id", required = true) String id) {
        Result<SysUser> result = new Result<SysUser>();
        SysUser sysUser = sysUserService.getById(id);
        if (sysUser == null) {
            result.error500("No corresponding entity found");
        } else {
            result.setResult(sysUser);
            result.setSuccess(true);
        }
        return result;
    }

    @RequiresPermissions("system:user:queryUserRole")
    @RequestMapping(value = "/queryUserRole", method = RequestMethod.GET)
    public Result<List<String>> queryUserRole(@RequestParam(name = "userid", required = true) String userid) {
        Result<List<String>> result = new Result<>();
        List<String> list = new ArrayList<String>();
        List<SysUserRole> userRole = sysUserRoleService.list(new QueryWrapper<SysUserRole>().lambda().eq(SysUserRole::getUserId, userid));
        if (userRole == null || userRole.size() <= 0) {
            result.error500("No user related role information found");
        } else {
            for (SysUserRole sysUserRole : userRole) {
                list.add(sysUserRole.getRoleId());
            }
            result.setSuccess(true);
            result.setResult(list);
        }
        return result;
    }


    /**
	  *  Verify whether the user account is unique<br>
	  *  Can check other Pass whatever needs to be checked。。。
     *
     * @param sysUser
     * @return
     */
    @RequestMapping(value = "/checkOnlyUser", method = RequestMethod.GET)
    public Result<Boolean> checkOnlyUser(SysUser sysUser) {
        Result<Boolean> result = new Result<>();
        //If this parameter isfalseAn exception occurs in the program
        result.setResult(true);
        try {
            //Query new user information through incoming information
            sysUser.setPassword(null);
            SysUser user = sysUserService.getOne(new QueryWrapper<SysUser>(sysUser));
            if (user != null) {
                result.setSuccess(false);
                result.setMessage("User account already exists");
                return result;
            }

        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage(e.getMessage());
            return result;
        }
        result.setSuccess(true);
        return result;
    }

    /**
     * Change password
     */
    @RequiresPermissions("system:user:changepwd")
    @RequestMapping(value = "/changePassword", method = RequestMethod.PUT)
    public Result<?> changePassword(@RequestBody SysUser sysUser) {
        SysUser u = this.sysUserService.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, sysUser.getUsername()));
        if (u == null) {
            return Result.error("User does not exist！");
        }
        sysUser.setId(u.getId());
        //update-begin---author:wangshuai ---date:20220316  for：[VUEN-234]Change password添加敏感日志------------
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        baseCommonService.addLog("Modify user "+sysUser.getUsername()+" password，operator： " +loginUser.getUsername() ,CommonConstant.LOG_TYPE_2, 2);
        //update-end---author:wangshuai ---date:20220316  for：[VUEN-234]Change password添加敏感日志------------
        return sysUserService.changePassword(sysUser);
    }

    /**
     * Query the data associated with the specified user and department
     *
     * @param userId
     * @return
     */
    @RequestMapping(value = "/userDepartList", method = RequestMethod.GET)
    public Result<List<DepartIdModel>> getUserDepartsList(@RequestParam(name = "userId", required = true) String userId) {
        Result<List<DepartIdModel>> result = new Result<>();
        try {
            List<DepartIdModel> depIdModelList = this.sysUserDepartService.queryDepartIdsOfUser(userId);
            if (depIdModelList != null && depIdModelList.size() > 0) {
                result.setSuccess(true);
                result.setMessage("Search successful");
                result.setResult(depIdModelList);
            } else {
                result.setSuccess(false);
                result.setMessage("Search failed");
            }
            return result;
        } catch (Exception e) {
        	log.error(e.getMessage(), e);
            result.setSuccess(false);
            result.setMessage("An exception occurred during the search: " + e.getMessage());
            return result;
        }

    }

    /**
     * 生成在Add user情况下没有主键的问题,Return to front end,According to theidBind department data
     *
     * @return
     */
    @RequestMapping(value = "/generateUserId", method = RequestMethod.GET)
    public Result<String> generateUserId() {
        Result<String> result = new Result<>();
        System.out.println("I executed,Generate userID==============================");
        String userId = UUID.randomUUID().toString().replace("-", "");
        result.setSuccess(true);
        result.setResult(userId);
        return result;
    }

    /**
     * According to departmentidQuery user information
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/queryUserByDepId", method = RequestMethod.GET)
    public Result<List<SysUser>> queryUserByDepId(@RequestParam(name = "id", required = true) String id,@RequestParam(name="realname",required=false) String realname) {
        Result<List<SysUser>> result = new Result<>();
        //List<SysUser> userList = sysUserDepartService.queryUserByDepId(id);
        SysDepart sysDepart = sysDepartService.getById(id);
        List<SysUser> userList = sysUserDepartService.queryUserByDepCode(sysDepart.getOrgCode(),realname);

        //Query the departments to which users belong in batches
        //step.1 Get them all first useids
        //step.2 pass useids，One-time query of the user’s department name
        List<String> userIds = userList.stream().map(SysUser::getId).collect(Collectors.toList());
        if(userIds!=null && userIds.size()>0){
            Map<String,String>  useDepNames = sysUserService.getDepNamesByUserIds(userIds);
            userList.forEach(item->{
                //TODO Temporarily borrow this field for page display
                item.setOrgCodeTxt(useDepNames.get(item.getId()));
            });
        }

        try {
            result.setSuccess(true);
            result.setResult(userList);
            return result;
        } catch (Exception e) {
        	log.error(e.getMessage(), e);
            result.setSuccess(false);
            return result;
        }
    }

    /**
     * user selects component dedicated  Query by page or department
     * @param departId
     * @param username
     * @return
     */
    @RequestMapping(value = "/queryUserComponentData", method = RequestMethod.GET)
    public Result<IPage<SysUser>> queryUserComponentData(
            @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
            @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
            @RequestParam(name = "departId", required = false) String departId,
            @RequestParam(name="realname",required=false) String realname,
            @RequestParam(name="username",required=false) String username,
            @RequestParam(name="isMultiTranslate",required=false) String isMultiTranslate,
            @RequestParam(name="id",required = false) String id) {
        //update-begin-author:taoyan date:2022-7-14 for: VUEN-1702【Forbidden questions】sqlinjection vulnerability
        String[] arr = new String[]{departId, realname, username, id};
        SqlInjectionUtil.filterContent(arr, SymbolConstant.SINGLE_QUOTATION_MARK);
        //update-end-author:taoyan date:2022-7-14 for: VUEN-1702【Forbidden questions】sqlinjection vulnerability
        IPage<SysUser> pageList = sysUserDepartService.queryDepartUserPageList(departId, username, realname, pageSize, pageNo,id,isMultiTranslate);
        return Result.OK(pageList);
    }

    /**
     * Exportexcel
     *
     * @param request
     * @param sysUser
     */
    @RequiresPermissions("system:user:export")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(SysUser sysUser,HttpServletRequest request) {
        // Step.1 Assemble query conditions
        QueryWrapper<SysUser> queryWrapper = QueryGenerator.initQueryWrapper(sysUser, request.getParameterMap());
        //Step.2 AutoPoi ExportExcel
        ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
        //update-begin--Author:kangxiaolin  Date:20180825 for：[03]用户Export，like果选择数据则只Export相关数据--------------------
        String selections = request.getParameter("selections");
       if(!oConvertUtils.isEmpty(selections)){
           queryWrapper.in("id",selections.split(","));
       }
        //update-end--Author:kangxiaolin  Date:20180825 for：[03]用户Export，like果选择数据则只Export相关数据----------------------
        List<SysUser> pageList = sysUserService.list(queryWrapper);
        List<SysUserExportVo> list  = sysUserService.getDepartAndRoleExportMsg(pageList);

        //Export文件名称
        mv.addObject(NormalExcelConstants.FILE_NAME, "User list");
        mv.addObject(NormalExcelConstants.CLASS, SysUserExportVo.class);
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        ExportParams exportParams = new ExportParams("Import rules：\n" +
                "1. Username is required，Only supports new data import；\n" +
                "2. multiple departments、Please use an English semicolon for roles or responsible departments. ; separate，like：Finance Department;R&D Department；\n" +
                "3. Please use English slashes for department levels / separate，like：Beijing company/Finance Department/Finance Department；\n" +
                "4. The department type must be consistent with the department level，Also used / separate，like：company/department/department or 1/3/3，For multiple types ; separate。Institution type code：company(1)，子company(4)，department(3)；\n" +
                "5. departmentaccording to用户名匹配，若存在Multiple则关联最新创建的department，Automatically add if it does not exist；\n" +
                "6. 负责department与所属departmentImport rules一致，若所属department不包含负责department，则不关联负责department；\n" +
                "7. 用户主岗位导入时会在department下自动创建新岗位，When the rank is empty, it will not be associated with the position by default.。", "Export人：" + user.getRealname(), "Export信息");
        exportParams.setTitleHeight((short)70);
        exportParams.setStyle(ExcelExportSysUserStyle.class);
        exportParams.setImageBasePath(upLoadPath);
        mv.addObject(NormalExcelConstants.PARAMS, exportParams);
        mv.addObject(NormalExcelConstants.DATA_LIST, list);
        return mv;
    }

    /**
     * passexcelImport data
     *
     * @param request
     * @param response
     * @return
     */
    @RequiresPermissions("system:user:import")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response)throws IOException {
        //return ImportOldUserUtil.importOldSysUser(request);
        return sysUserService.importSysUser(request);
    }

    /**
	 * @Function：according toid Batch query
	 * @param userIds
	 * @return
	 */
	@RequestMapping(value = "/queryByIds", method = RequestMethod.GET)
	public Result<Collection<SysUser>> queryByIds(@RequestParam(name = "userIds") String userIds) {
		Result<Collection<SysUser>> result = new Result<>();
		String[] userId = userIds.split(",");
		Collection<String> idList = Arrays.asList(userId);
		Collection<SysUser> userRole = sysUserService.listByIds(idList);
		result.setSuccess(true);
		result.setResult(userRole);
		return result;
	}


    /**
     * @Function：according toid Batch query
     * @param userNames
     * @return
     */
    @RequestMapping(value = "/queryByNames", method = RequestMethod.GET)
    public Result<Collection<SysUser>> queryByNames(@RequestParam(name = "userNames") String userNames) {
        Result<Collection<SysUser>> result = new Result<>();
        String[] names = userNames.split(",");
        QueryWrapper<SysUser> queryWrapper=new QueryWrapper();
        queryWrapper.lambda().in(true,SysUser::getUsername,names);
        Collection<SysUser> userRole = sysUserService.list(queryWrapper);
        result.setSuccess(true);
        result.setResult(userRole);
        return result;
    }

    /**
     * @Function：according touserNameQuery用户以及department信息
     * @param userName
     * @return
     */
    @RequestMapping(value = "/queryUserAndDeptByName", method = RequestMethod.GET)
    public Result<Map<String,String>> queryUserAndDeptByName(@RequestParam(name = "userName") String userName) {
        Map<String,String> userInfo= sysUserService.queryUserAndDeptByName(userName);
        return Result.ok(userInfo);
    }

	/**
	 * Home page user reset password
	 */
    @RequiresPermissions("system:user:updatepwd")
    @RequestMapping(value = "/updatePassword", method = RequestMethod.PUT)
	public Result<?> updatePassword(@RequestBody JSONObject json) {
		String username = json.getString("username");
		String oldpassword = json.getString("oldpassword");
		String password = json.getString("password");
		String confirmpassword = json.getString("confirmpassword");
        LoginUser sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();
        if(!sysUser.getUsername().equals(username)){
            return Result.error("只允许Revise自己password！");
        }
		SysUser user = this.sysUserService.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
		if(user==null) {
			return Result.error("User does not exist！");
		}
        //update-begin---author:wangshuai ---date:20220316  for：[VUEN-234]Change password添加敏感日志------------
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        baseCommonService.addLog("Change password，username： " +loginUser.getUsername() ,CommonConstant.LOG_TYPE_2, 2);
        //update-end---author:wangshuai ---date:20220316  for：[VUEN-234]Change password添加敏感日志------------
		return sysUserService.resetPassword(username,oldpassword,password,confirmpassword);
	}

    @RequestMapping(value = "/userRoleList", method = RequestMethod.GET)
    public Result<IPage<SysUser>> userRoleList(@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                               @RequestParam(name="pageSize", defaultValue="10") Integer pageSize, HttpServletRequest req) {
        Result<IPage<SysUser>> result = new Result<IPage<SysUser>>();
        Page<SysUser> page = new Page<SysUser>(pageNo, pageSize);
        String roleId = req.getParameter("roleId");
        String username = req.getParameter("username");
        String realname = req.getParameter("realname");
        IPage<SysUser> pageList = sysUserService.getUserByRoleId(page,roleId,username,realname);
        result.setSuccess(true);
        result.setResult(pageList);
        return result;
    }

    /**
     * 给指定RoleAdd user
     *
     * @param
     * @return
     */
    @RequiresPermissions("system:user:addUserRole")
    @RequestMapping(value = "/addSysUserRole", method = RequestMethod.POST)
    public Result<String> addSysUserRole(@RequestBody SysUserRoleVO sysUserRoleVO) {
        Result<String> result = new Result<String>();
        //TODO Determine whether the role of the current operation is under the currently logged in tenant.
        try {
            String sysRoleId = sysUserRoleVO.getRoleId();
            for(String sysUserId:sysUserRoleVO.getUserIdList()) {
                SysUserRole sysUserRole = new SysUserRole(sysUserId,sysRoleId);
                QueryWrapper<SysUserRole> queryWrapper = new QueryWrapper<SysUserRole>();
                queryWrapper.eq("role_id", sysRoleId).eq("user_id",sysUserId);
                SysUserRole one = sysUserRoleService.getOne(queryWrapper);
                if(one==null){
                    sysUserRoleService.save(sysUserRole);
                }

            }
            result.setMessage("Added successfully!");
            result.setSuccess(true);
            return result;
        }catch(Exception e) {
            log.error(e.getMessage(), e);
            result.setSuccess(false);
            result.setMessage("something went wrong: " + e.getMessage());
            return result;
        }
    }
    /**
     *   Delete the user relationship of the specified role
     * @param
     * @return
     */
    @RequiresPermissions("system:user:deleteRole")
    @RequestMapping(value = "/deleteUserRole", method = RequestMethod.DELETE)
    public Result<SysUserRole> deleteUserRole(@RequestParam(name="roleId") String roleId,
                                                    @RequestParam(name="userId",required=true) String userId
    ) {
        Result<SysUserRole> result = new Result<SysUserRole>();
        try {
            QueryWrapper<SysUserRole> queryWrapper = new QueryWrapper<SysUserRole>();
            queryWrapper.eq("role_id", roleId).eq("user_id",userId);
            sysUserRoleService.remove(queryWrapper);
            result.success("Delete successfully!");
        }catch(Exception e) {
            log.error(e.getMessage(), e);
            result.error500("Delete failed！");
        }
        return result;
    }

    /**
     * 批量Delete the user relationship of the specified role
     *
     * @param
     * @return
     */
    @RequiresPermissions("system:user:deleteRoleBatch")
    @RequestMapping(value = "/deleteUserRoleBatch", method = RequestMethod.DELETE)
    public Result<SysUserRole> deleteUserRoleBatch(
            @RequestParam(name="roleId") String roleId,
            @RequestParam(name="userIds",required=true) String userIds) {
        Result<SysUserRole> result = new Result<SysUserRole>();
        try {
            QueryWrapper<SysUserRole> queryWrapper = new QueryWrapper<SysUserRole>();
            queryWrapper.eq("role_id", roleId).in("user_id",Arrays.asList(userIds.split(",")));
            sysUserRoleService.remove(queryWrapper);
            result.success("Delete successfully!");
        }catch(Exception e) {
            log.error(e.getMessage(), e);
            result.error500("Delete failed！");
        }
        return result;
    }

    /**
     * departmentUser list
     */
    @RequestMapping(value = "/departUserList", method = RequestMethod.GET)
    public Result<IPage<SysUser>> departUserList(@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                                 @RequestParam(name="pageSize", defaultValue="10") Integer pageSize, HttpServletRequest req) {
        Result<IPage<SysUser>> result = new Result<IPage<SysUser>>();
        Page<SysUser> page = new Page<SysUser>(pageNo, pageSize);
        String depId = req.getParameter("depId");
        String username = req.getParameter("username");
        //According to departmentIDQuery,当前和下级所有的departmentIDS
        List<String> subDepids = new ArrayList<>();
        //departmentidWhen empty，Query我的department下所有用户
        if(oConvertUtils.isEmpty(depId)){
            LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            int userIdentity = user.getUserIdentity() != null?user.getUserIdentity():CommonConstant.USER_IDENTITY_1;
            //update-begin---author:chenrui ---date:20250107  for：[QQYUN-10775]Verification codes can be reused #7674------------
            if(oConvertUtils.isNotEmpty(userIdentity) && userIdentity == CommonConstant.USER_IDENTITY_2
                    && oConvertUtils.isNotEmpty(user.getDepartIds())) {
            //update-end---author:chenrui ---date:20250107  for：[QQYUN-10775]Verification codes can be reused #7674------------
                subDepids = sysDepartService.getMySubDepIdsByDepId(user.getDepartIds());
            }
        }else{
            subDepids = sysDepartService.getSubDepIdsByDepId(depId);
        }
        if(subDepids != null && subDepids.size()>0){
            IPage<SysUser> pageList = sysUserService.getUserByDepIds(page,subDepids,username);
            //Query the departments to which users belong in batches
            //step.1 Get them all first useids
            //step.2 pass useids，One-time query of the user’s department name
            List<String> userIds = pageList.getRecords().stream().map(SysUser::getId).collect(Collectors.toList());
            if(userIds!=null && userIds.size()>0){
                Map<String, String> useDepNames = sysUserService.getDepNamesByUserIds(userIds);
                pageList.getRecords().forEach(item -> {
                    //Query the departments to which users belong in batches
                    item.setOrgCode(useDepNames.get(item.getId()));
                });
            }
            //update-begin---author:wangshuai ---date:20221223  for：[QQYUN-3371]Tenant logic transformation，Change to relational table------------
            //Set up tenantid
            page.setRecords(userTenantService.setUserTenantIds(page.getRecords()));
            //update-end---author:wangshuai ---date:20221223  for：[QQYUN-3371]Tenant logic transformation，Change to relational table------------
            result.setSuccess(true);
            result.setResult(pageList);
        }else{
            result.setSuccess(true);
            result.setResult(null);
        }
        return result;
    }


    /**
     * according to orgCode Query用户，包括子department下的用户
     * 若某个用户包含multiple departments，Multiple records will be displayed，Can be processed into a single record by itself
     */
    @GetMapping("/queryByOrgCode")
    public Result<?> queryByDepartId(
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "orgCode") String orgCode,
            SysUser userParams
    ) {
        IPage<SysUserSysDepartModel> pageList = sysUserService.queryUserByOrgCode(orgCode, userParams, new Page(pageNo, pageSize));
        return Result.ok(pageList);
    }

    /**
     * according to orgCode Query用户，包括子department下的用户
     * Interface for address book module，将multiple departments的用户合并成一条记录，And convert it into a front-end friendly format
     */
    @GetMapping("/queryByOrgCodeForAddressList")
    public Result<?> queryByOrgCodeForAddressList(
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "orgCode",required = false) String orgCode,
            SysUser userParams
    ) {
        IPage page = new Page(pageNo, pageSize);
        IPage<SysUserSysDepPostModel> pageList = sysUserService.queryDepartPostUserByOrgCode(orgCode, userParams, page);
        return Result.ok(pageList);
    }

    /**
     * 给指定department添加对应的用户
     */
    @RequiresPermissions("system:user:editDepartWithUser")
    @RequestMapping(value = "/editSysDepartWithUser", method = RequestMethod.POST)
    public Result<String> editSysDepartWithUser(@RequestBody SysDepartUsersVO sysDepartUsersVO) {
        Result<String> result = new Result<String>();
        try {
            String sysDepId = sysDepartUsersVO.getDepId();
            for(String sysUserId:sysDepartUsersVO.getUserIdList()) {
                SysUserDepart sysUserDepart = new SysUserDepart(null,sysUserId,sysDepId);
                QueryWrapper<SysUserDepart> queryWrapper = new QueryWrapper<SysUserDepart>();
                queryWrapper.eq("dep_id", sysDepId).eq("user_id",sysUserId);
                SysUserDepart one = sysUserDepartService.getOne(queryWrapper);
                if(one==null){
                    sysUserDepartService.save(sysUserDepart);
                }
            }
            result.setMessage("Added successfully!");
            result.setSuccess(true);
            return result;
        }catch(Exception e) {
            log.error(e.getMessage(), e);
            result.setSuccess(false);
            result.setMessage("something went wrong: " + e.getMessage());
            return result;
        }
    }

    /**
     *   Delete the user relationship of the specified organization
     */
    @RequiresPermissions("system:user:deleteUserInDepart")
    @RequestMapping(value = "/deleteUserInDepart", method = RequestMethod.DELETE)
    public Result<SysUserDepart> deleteUserInDepart(@RequestParam(name="depId") String depId,
                                                    @RequestParam(name="userId",required=true) String userId
    ) {
        Result<SysUserDepart> result = new Result<SysUserDepart>();
        try {
            QueryWrapper<SysUserDepart> queryWrapper = new QueryWrapper<SysUserDepart>();
            queryWrapper.eq("dep_id", depId).eq("user_id",userId);
            boolean b = sysUserDepartService.remove(queryWrapper);
            if(b){
                List<SysDepartRole> sysDepartRoleList = departRoleService.list(new QueryWrapper<SysDepartRole>().eq("depart_id",depId));
                List<String> roleIds = sysDepartRoleList.stream().map(SysDepartRole::getId).collect(Collectors.toList());
                if(roleIds != null && roleIds.size()>0){
                    QueryWrapper<SysDepartRoleUser> query = new QueryWrapper<>();
                    query.eq("user_id",userId).in("drole_id",roleIds);
                    departRoleUserService.remove(query);
                }
                result.success("Delete successfully!");
            }else{
                result.error500("当前选中department与用户无关联关系!");
            }
        }catch(Exception e) {
            log.error(e.getMessage(), e);
            result.error500("Delete failed！");
        }
        return result;
    }

    /**
     * 批量Delete the user relationship of the specified organization
     */
    @RequiresPermissions("system:user:deleteUserInDepartBatch")
    @RequestMapping(value = "/deleteUserInDepartBatch", method = RequestMethod.DELETE)
    public Result<SysUserDepart> deleteUserInDepartBatch(
            @RequestParam(name="depId") String depId,
            @RequestParam(name="userIds",required=true) String userIds) {
        Result<SysUserDepart> result = new Result<SysUserDepart>();
        try {
            QueryWrapper<SysUserDepart> queryWrapper = new QueryWrapper<SysUserDepart>();
            queryWrapper.eq("dep_id", depId).in("user_id",Arrays.asList(userIds.split(",")));
            boolean b = sysUserDepartService.remove(queryWrapper);
            if(b){
                departRoleUserService.removeDeptRoleUser(Arrays.asList(userIds.split(",")),depId);
            }else{
                result.error500("Delete failed，目标用户不在当前department！");
                return result;
            }
            result.success("Delete successfully!");
        }catch(Exception e) {
            log.error(e.getMessage(), e);
            result.error500("Delete failed！");
        }
        return result;
    }
    
    /**
         *  Querycurrent user的所有department/当前department编码
     * @return
     */
    @RequestMapping(value = "/getCurrentUserDeparts", method = RequestMethod.GET)
    public Result<Map<String,Object>> getCurrentUserDeparts() {
        Result<Map<String,Object>> result = new Result<Map<String,Object>>();
        try {
        	LoginUser sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();
            List<SysDepart> list = this.sysDepartService.queryUserDeparts(sysUser.getId());
            Map<String,Object> map = new HashMap(5);
            map.put("list", list);
            map.put("orgCode", sysUser.getOrgCode());
            result.setSuccess(true);
            result.setResult(map);
        }catch(Exception e) {
            log.error(e.getMessage(), e);
            result.error500("Query失败！");
        }
        return result;
    }

    


	/**
	 * User registration interface
	 * 
	 * @param jsonObject
	 * @param user
	 * @return
	 */
	@PostMapping("/register")
	public Result<JSONObject> userRegister(@RequestBody JSONObject jsonObject, SysUser user) {
		Result<JSONObject> result = new Result<JSONObject>();
		String phone = jsonObject.getString("phone");
		String smscode = jsonObject.getString("smscode");

        //update-begin-author:taoyan date:2022-9-13 for: VUEN-2245 【loopholes】发现新loopholes待处理20220906
		String redisKey = CommonConstant.PHONE_REDIS_KEY_PRE+phone;
		Object code = redisUtil.get(redisKey);
        //update-end-author:taoyan date:2022-9-13 for: VUEN-2245 【loopholes】发现新loopholes待处理20220906

		String username = jsonObject.getString("username");
		//Username not set，Then use the mobile phone number as the username
		if(oConvertUtils.isEmpty(username)){
            username = phone;
        }
        //No password set，then randomly generate a password
		String password = jsonObject.getString("password");
		if(oConvertUtils.isEmpty(password)){
            password = RandomUtil.randomString(8);
        }
		String email = jsonObject.getString("email");
		SysUser sysUser1 = sysUserService.getUserByName(username);
		if (sysUser1 != null) {
			result.setMessage("Username has been registered");
			result.setSuccess(false);
			return result;
		}
		SysUser sysUser2 = sysUserService.getUserByPhone(phone);
		if (sysUser2 != null) {
			result.setMessage("This mobile number has been registered");
			result.setSuccess(false);
			return result;
		}

		if(oConvertUtils.isNotEmpty(email)){
            SysUser sysUser3 = sysUserService.getUserByEmail(email);
            if (sysUser3 != null) {
                result.setMessage("Email has been registered");
                result.setSuccess(false);
                return result;
            }
        }
        if(null == code){
            result.setMessage("Mobile phone verification code invalid，Please get it again");
            result.setSuccess(false);
            return result;
        }
		if (!smscode.equals(code.toString())) {
			result.setMessage("Mobile phone verification code error");
			result.setSuccess(false);
			return result;
		}

        String realname = jsonObject.getString("realname");
        if(oConvertUtils.isEmpty(realname)){
            realname = username;
        }
        
		try {
			user.setCreateTime(new Date());// Set creation time
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
			sysUserService.addUserWithRole(user,"");//Default temporary role test
			result.success("Registration successful");
		} catch (Exception e) {
			result.error500("Registration failed");
		}
		return result;
	}

//	/**
//	 * according to用户名or手机号Query user information
//	 * @param
//	 * @return
//	 */
//	@GetMapping("/querySysUser")
//	public Result<Map<String, Object>> querySysUser(SysUser sysUser) {
//		String phone = sysUser.getPhone();
//		String username = sysUser.getUsername();
//		Result<Map<String, Object>> result = new Result<Map<String, Object>>();
//		Map<String, Object> map = new HashMap<String, Object>();
//		if (oConvertUtils.isNotEmpty(phone)) {
//			SysUser user = sysUserService.getUserByPhone(phone);
//			if(user!=null) {
//				map.put("username",user.getUsername());
//				map.put("phone",user.getPhone());
//				result.setSuccess(true);
//				result.setResult(map);
//				return result;
//			}
//		}
//		if (oConvertUtils.isNotEmpty(username)) {
//			SysUser user = sysUserService.getUserByName(username);
//			if(user!=null) {
//				map.put("username",user.getUsername());
//				map.put("phone",user.getPhone());
//				result.setSuccess(true);
//				result.setResult(map);
//				return result;
//			}
//		}
//		result.setSuccess(false);
//		result.setMessage("Authentication failed");
//		return result;
//	}

	/**
	 * User mobile phone number verification
	 */
	@PostMapping("/phoneVerification")
	public Result<Map<String,String>> phoneVerification(@RequestBody JSONObject jsonObject) {
		Result<Map<String,String>> result = new Result<Map<String,String>>();
		String phone = jsonObject.getString("phone");
		String smscode = jsonObject.getString("smscode");
        //update-begin-author:taoyan date:2022-9-13 for: VUEN-2245 【loopholes】发现新loopholes待处理20220906
        String redisKey = CommonConstant.PHONE_REDIS_KEY_PRE+phone;
		Object code = redisUtil.get(redisKey);
        //update-begin---author:wangshuai---date:2025-07-15---for:【issues/8567】serious：Change password存在水平越权问题。---
        if (null == code) {
            result.setMessage("SMS verification code invalid！");
            result.setSuccess(false);
            return result;
        }
        String smsCode = "";
        if (code.toString().contains("code")) {
            smsCode = JSONObject.parseObject(code.toString()).getString("code");
        } else {
            smsCode = code.toString();
        }
		if (!smscode.equals(smsCode)) {
        //update-end---author:wangshuai---date:2025-07-15---for:【issues/8567】serious：Change password存在水平越权问题。---
			result.setMessage("Mobile phone verification code error");
			result.setSuccess(false);
			return result;
		}
		//Set valid time
		redisUtil.set(redisKey, code,600);
        //update-end-author:taoyan date:2022-9-13 for: VUEN-2245 【loopholes】发现新loopholes待处理20220906

		//新增Query用户名
		LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<>();
        query.eq(SysUser::getPhone,phone);
        SysUser user = sysUserService.getOne(query);
        Map<String,String> map = new HashMap(5);
        map.put("smscode",smscode);
        if(null == user){
            //前端according to文字做judge用户是否存在judge，cannot be modified
            result.setMessage("User information does not exist");
            result.setSuccess(false);
            return result;
        }
        map.put("username",user.getUsername());
        result.setResult(map);
		result.setSuccess(true);
		return result;
	}
	
	/**
	 * User changes password
	 */
	@GetMapping("/passwordChange")
	public Result<SysUser> passwordChange(@RequestParam(name="username")String username,
										  @RequestParam(name="password")String password,
			                              @RequestParam(name="smscode")String smscode,
			                              @RequestParam(name="phone") String phone) {
        Result<SysUser> result = new Result<SysUser>();
        if(oConvertUtils.isEmpty(username) || oConvertUtils.isEmpty(password) || oConvertUtils.isEmpty(smscode)  || oConvertUtils.isEmpty(phone) ) {
            result.setMessage("Password reset failed！");
            result.setSuccess(false);
            return result;
        }

        SysUser sysUser=new SysUser();
        //update-begin-author:taoyan date:2022-9-13 for: VUEN-2245 【loopholes】发现新loopholes待处理20220906
        String redisKey = CommonConstant.PHONE_REDIS_KEY_PRE+phone;
        Object object= redisUtil.get(redisKey);
        //update-end-author:taoyan date:2022-9-13 for: VUEN-2245 【loopholes】发现新loopholes待处理20220906
        if(null==object) {
        	result.setMessage("SMS verification code invalid！");
            result.setSuccess(false);
            return result;
        }

        //update-begin---author:wangshuai---date:2025-07-14---for:【issues/8567】serious：Change password存在水平越权问题。---
        String redisUsername = "";
        if(object.toString().contains("code")){
            JSONObject jsonObject = JSONObject.parseObject(object.toString());
            object = jsonObject.getString("code");
            redisUsername = jsonObject.getString("username");
        }
        //Verify that it belongs to the current user
        if(oConvertUtils.isNotEmpty(redisUsername) && !username.equals(redisUsername)){
            result.setMessage("This verification code does not belong to the current user！");
            result.setSuccess(false);
            return result;
        }
        //update-end---author:wangshuai---date:2025-07-14---for:【issues/8567】serious：Change password存在水平越权问题。---
        
        if(!smscode.equals(object.toString())) {
        	result.setMessage("SMS verification code does not match！");
            result.setSuccess(false);
            return result;
        }
        sysUser = this.sysUserService.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername,username).eq(SysUser::getPhone,phone));
        if (sysUser == null) {
            result.setMessage("The current user and the bound mobile phone number do not match，无法Change password！");
            result.setSuccess(false);
            return result;
        } else {
            String salt = oConvertUtils.randomGen(8);
            sysUser.setSalt(salt);
            String passwordEncode = PasswordUtil.encrypt(sysUser.getUsername(), password, salt);
            sysUser.setPassword(passwordEncode);
            this.sysUserService.updateById(sysUser);
            //update-begin---author:wangshuai ---date:20220316  for：[VUEN-234]Password reset adds sensitive logs------------
            baseCommonService.addLog("reset "+username+" password，operator： " +sysUser.getUsername() ,CommonConstant.LOG_TYPE_2, 2);
            //update-end---author:wangshuai ---date:20220316  for：[VUEN-234]Password reset adds sensitive logs------------
            result.setSuccess(true);
            result.setMessage("密码reset完成！");
            //Clear the password after changing itredis
            redisUtil.removeAll(redisKey);
            return result;
        }
    }
	

	/**
	 * according toTOKENGet some user information（The data returned is data that can be used by the form designer）
	 * 
	 * @return
	 */
	@GetMapping("/getUserSectionInfoByToken")
	public Result<?> getUserSectionInfoByToken(HttpServletRequest request, @RequestParam(name = "token", required = false) String token) {
		try {
			String username = null;
			// like果没有传递token，Just fromheaderGet intokenand get user information
			if (oConvertUtils.isEmpty(token)) {
				 username = JwtUtil.getUserNameByToken(request);
			} else {
				 username = JwtUtil.getUsername(token);				
			}

			log.debug(" ------ pass令牌获取部分用户信息，current user： " + username);

			// according to用户名Query user information
			SysUser sysUser = sysUserService.getUserByName(username);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("sysUserId", sysUser.getId());
			map.put("sysUserCode", sysUser.getUsername()); // Current logged in user login account
			map.put("sysUserName", sysUser.getRealname()); // Real name of currently logged in user
			map.put("sysOrgCode", sysUser.getOrgCode()); // 当前登录用户department编号

            // 【QQYUN-12930】设置department名称
            if (oConvertUtils.isNotEmpty(sysUser.getOrgCode())) {
                SysDepart sysDepart = sysDepartService.lambdaQuery().select(SysDepart::getDepartName).eq(SysDepart::getOrgCode, sysUser.getOrgCode()).one();
                if (sysDepart != null) {
                    map.put("sysOrgName", sysDepart.getDepartName()); // 当前登录用户department名称
                }
            }

			log.debug(" ------ pass令牌获取部分用户信息，Obtained user information： " + map);

			return Result.ok(map);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Result.error(500, "Query失败:" + e.getMessage());
		}
	}
	
	/**
	 * 【APPend interface】获取User list  according to用户名和真实名 fuzzy matching
	 * @param keyword
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	@GetMapping("/appUserList")
	public Result<?> appUserList(@RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "username", required = false) String username,
			@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
			@RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
            @RequestParam(name = "syncFlow", required = false) String syncFlow) {
		try {
			//TODO 从Query效率上将不要用mpencapsulatedpage分页Query It is recommended to write paging statements yourself
			LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<SysUser>();
			if(oConvertUtils.isNotEmpty(syncFlow)){
                query.eq(SysUser::getActivitiSync, CommonConstant.ACT_SYNC_1);
            }
			query.eq(SysUser::getDelFlag,CommonConstant.DEL_FLAG_0);
			if(oConvertUtils.isNotEmpty(username)){
			    if(username.contains(",")){
                    query.in(SysUser::getUsername,username.split(","));
                }else{
                    query.eq(SysUser::getUsername,username);
                }
            }else{
                query.and(i -> i.like(SysUser::getUsername, keyword).or().like(SysUser::getRealname, keyword));
            }
			Page<SysUser> page = new Page<>(pageNo, pageSize);
			IPage<SysUser> res = this.sysUserService.page(page, query);
			return Result.ok(res);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Result.error(500, "Query失败:" + e.getMessage());
		}
		
	}

    /**
     * 获取被逻辑删除的User list，No pagination
     *
     * @return logicDeletedUserList
     */
    @GetMapping("/recycleBin")
    public Result getRecycleBin() {
        List<SysUser> logicDeletedUserList = sysUserService.queryLogicDeleted();
        if (logicDeletedUserList.size() > 0) {
            // Query the departments to which users belong in batches
            // step.1 Get them all first userIds
            List<String> userIds = logicDeletedUserList.stream().map(SysUser::getId).collect(Collectors.toList());
            // step.2 pass userIds，One-time query of the user’s department name
            Map<String, String> useDepNames = sysUserService.getDepNamesByUserIds(userIds);
            logicDeletedUserList.forEach(item -> item.setOrgCode(useDepNames.get(item.getId())));
        }
        return Result.ok(logicDeletedUserList);
    }

    /**
     * Restore tombstoned users
     *
     * @param jsonObject
     * @return
     */
    @RequestMapping(value = "/putRecycleBin", method = RequestMethod.PUT)
    public Result putRecycleBin(@RequestBody JSONObject jsonObject, HttpServletRequest request) {
        String userIds = jsonObject.getString("userIds");
        if (StringUtils.isNotBlank(userIds)) {
            SysUser updateUser = new SysUser();
            updateUser.setUpdateBy(JwtUtil.getUserNameByToken(request));
            updateUser.setUpdateTime(new Date());
            sysUserService.revertLogicDeleted(Arrays.asList(userIds.split(",")), updateUser);
        }
        return Result.ok("Restore successful");
    }

    /**
     * 彻底Delete user
     *
     * @param userIds Deleted userID，MultipleidSeparate with half-width commas
     * @return
     */
    @RequiresPermissions("system:user:deleteRecycleBin")
    @RequestMapping(value = "/deleteRecycleBin", method = RequestMethod.DELETE)
    public Result deleteRecycleBin(@RequestParam("userIds") String userIds) {
        if (StringUtils.isNotBlank(userIds)) {
            sysUserService.removeLogicDeleted(Arrays.asList(userIds.split(",")));
        }
        return Result.ok("Delete successfully");
    }


    /**
     * 移动端Modify user信息
     * @param jsonObject
     * @return
     */
    @RequiresPermissions("system:user:app:edit")
    @RequestMapping(value = "/appEdit", method = {RequestMethod.PUT,RequestMethod.POST})
    public Result<SysUser> appEdit(HttpServletRequest request,@RequestBody JSONObject jsonObject) {
        Result<SysUser> result = new Result<SysUser>();
        try {
            String username = JwtUtil.getUserNameByToken(request);
            SysUser sysUser = sysUserService.getUserByName(username);
            baseCommonService.addLog("移动端Edit user，id： " +jsonObject.getString("id") ,CommonConstant.LOG_TYPE_2, 2);
            String realname=jsonObject.getString("realname");
            String avatar=jsonObject.getString("avatar");
            String sex=jsonObject.getString("sex");
            String phone=jsonObject.getString("phone");
            String email=jsonObject.getString("email");
            Date birthday=jsonObject.getDate("birthday");
            SysUser userPhone = sysUserService.getUserByPhone(phone);
            if(sysUser==null) {
                result.error500("No corresponding user found!");
            }else {
                if(userPhone!=null){
                    String userPhonename = userPhone.getUsername();
                    if(!userPhonename.equals(username)){
                        result.error500("Mobile phone number already exists!");
                        return result;
                    }
                }
                if(StringUtils.isNotBlank(realname)){
                    sysUser.setRealname(realname);
                }
                if(StringUtils.isNotBlank(avatar)){
                    sysUser.setAvatar(avatar);
                }
                if(StringUtils.isNotBlank(sex)){
                    sysUser.setSex(Integer.parseInt(sex));
                }
                if(StringUtils.isNotBlank(phone)){
                    sysUser.setPhone(phone);
                }
                if(StringUtils.isNotBlank(email)){
                    //update-begin---author:wangshuai ---date:20220708  for：[VUEN-1528]Duplicate email address on the official website of Building Blocks，It should prompt accurately------------
                    LambdaQueryWrapper<SysUser> emailQuery = new LambdaQueryWrapper<>();
                    emailQuery.eq(SysUser::getEmail,email);
                    long count = sysUserService.count(emailQuery);
                    if (!email.equals(sysUser.getEmail()) && count!=0) {
                        result.error500("Save failed，Email already exists!");
                        return result;
                    }
                    //update-end---author:wangshuai ---date:20220708  for：[VUEN-1528]Duplicate email address on the official website of Building Blocks，It should prompt accurately--------------
                    sysUser.setEmail(email);
                }
                if(null != birthday){
                    sysUser.setBirthday(birthday);
                }
                sysUser.setUpdateTime(new Date());
                sysUserService.updateById(sysUser);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("Save failed!");
        }
        return result;
    }
    /**
     * Save device information on mobile terminal
     * @param clientId
     * @return
     */
    @RequestMapping(value = "/saveClientId", method = RequestMethod.GET)
    public Result<SysUser> saveClientId(HttpServletRequest request,@RequestParam("clientId")String clientId) {
        Result<SysUser> result = new Result<SysUser>();
        try {
            String username = JwtUtil.getUserNameByToken(request);
            SysUser sysUser = sysUserService.getUserByName(username);
            if(sysUser==null) {
                result.error500("No corresponding user found!");
            }else {
                sysUser.setClientId(clientId);
                sysUserService.updateById(sysUser);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("Operation failed!");
        }
        return result;
    }
    /**
     * according touseridGet user information和department员工信息
     *
     * @return Result
     */
    @GetMapping("/queryChildrenByUsername")
    public Result queryChildrenByUsername(@RequestParam("userId") String userId) {
        //Get user information
        Map<String,Object> map=new HashMap(5);
        SysUser sysUser = sysUserService.getById(userId);
        String username = sysUser.getUsername();
        Integer identity = sysUser.getUserIdentity();
        map.put("sysUser",sysUser);
        if(identity!=null && identity==2){
            //获取department用户信息
            String departIds = sysUser.getDepartIds();
            if(StringUtils.isNotBlank(departIds)){
                List<String> departIdList = Arrays.asList(departIds.split(","));
                List<SysUser> childrenUser = sysUserService.queryByDepIds(departIdList,username);
                map.put("children",childrenUser);
            }
        }
        return Result.ok(map);
    }
    /**
     * 移动端Querydepartment用户信息
     * @param departId
     * @return
     */
    @GetMapping("/appQueryByDepartId")
    public Result<List<SysUser>> appQueryByDepartId(@RequestParam(name="departId", required = false) String departId) {
        Result<List<SysUser>> result = new Result<List<SysUser>>();
        List<String> list=new ArrayList<String> ();
        list.add(departId);
        List<SysUser> childrenUser = sysUserService.queryByDepIds(list,null);
        result.setResult(childrenUser);
        return result;
    }
    /**
     * 移动端Query user information(pass用户名模糊Query)
     * @param keyword
     * @return
     */
    @GetMapping("/appQueryUser")
    public Result<List<SysUser>> appQueryUser(@RequestParam(name = "keyword", required = false) String keyword,
                                              @RequestParam(name = "username", required = false) String username,
                                              @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                              @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,HttpServletRequest request) {
        Result<List<SysUser>> result = new Result<List<SysUser>>();
        LambdaQueryWrapper<SysUser> queryWrapper =new LambdaQueryWrapper<SysUser>();
        //TODO External simulated login temporary account，List is not displayed
        queryWrapper.ne(SysUser::getUsername,"_reserve_user_external");
        //Increase usernamePassing on parameters
        if(oConvertUtils.isNotEmpty(username)){
            if(username.contains(",")){
                queryWrapper.in(SysUser::getUsername,username.split(","));
            }else{
                queryWrapper.eq(SysUser::getUsername,username);
            }
        }else if(StringUtils.isNotBlank(keyword)){
            queryWrapper.and(i -> i.like(SysUser::getUsername, keyword).or().like(SysUser::getRealname, keyword));
        }
        //------------------------------------------------------------------------------------------------
        //Whether to enable multi-tenant data isolation of the system management module【SAASMulti-tenant model】
        if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
            String tenantId = oConvertUtils.getString(TokenUtils.getTenantIdByRequest(request),"-1");
            //update-begin---author:wangshuai ---date:20221223  for：[QQYUN-3371]Tenant logic transformation，Change to relational table------------
            List<String> userIds = userTenantService.getUserIdsByTenantId(Integer.valueOf(tenantId));
            if (oConvertUtils.listIsNotEmpty(userIds)) {
                queryWrapper.in(SysUser::getId, userIds);
            }
            //update-end---author:wangshuai ---date:20221223  for：[QQYUN-3371]Tenant logic transformation，Change to relational table------------
        }
        //------------------------------------------------------------------------------------------------
        Page<SysUser> page = new Page<>(pageNo, pageSize);
        IPage<SysUser> pageList = this.sysUserService.page(page, queryWrapper);
        //Query the departments to which users belong in batches
        //step.1 Get them all first useids
        //step.2 pass useids，One-time query of the user’s department name
        List<String> userIds = pageList.getRecords().stream().map(SysUser::getId).collect(Collectors.toList());
        if(userIds!=null && userIds.size()>0){
            Map<String,String>  useDepNames = sysUserService.getDepNamesByUserIds(userIds);
            pageList.getRecords().forEach(item->{
                item.setOrgCodeTxt(useDepNames.get(item.getId()));
            });
        }
        result.setResult(pageList.getRecords());
        return result;
    }

    /**
     * according to用户名Revise手机号[This method is not used]
     * @param json
     * @return
     */
    @RequestMapping(value = "/updateMobile", method = RequestMethod.PUT)
    public Result<?> changMobile(@RequestBody JSONObject json,HttpServletRequest request) {
        String smscode = json.getString("smscode");
        String phone = json.getString("phone");
        Result<SysUser> result = new Result<SysUser>();
        //Get login username
        String username = JwtUtil.getUserNameByToken(request);
        if(oConvertUtils.isEmpty(username) || oConvertUtils.isEmpty(smscode) || oConvertUtils.isEmpty(phone)) {
            result.setMessage("Failed to modify mobile phone number！");
            result.setSuccess(false);
            return result;
        }
        //update-begin-author:taoyan date:2022-9-13 for: VUEN-2245 【loopholes】发现新loopholes待处理20220906
        String redisKey = CommonConstant.PHONE_REDIS_KEY_PRE+phone;
        Object object= redisUtil.get(redisKey);
        //update-end-author:taoyan date:2022-9-13 for: VUEN-2245 【loopholes】发现新loopholes待处理20220906
        if(null==object) {
            result.setMessage("SMS verification code invalid！");
            result.setSuccess(false);
            return result;
        }
        if(!smscode.equals(object.toString())) {
            result.setMessage("SMS verification code does not match！");
            result.setSuccess(false);
            return result;
        }
        SysUser user = sysUserService.getUserByName(username);
        if(user==null) {
            return Result.error("User does not exist！");
        }
        user.setPhone(phone);
        sysUserService.updateById(user);
        return Result.ok("Mobile phone number set successfully!");
    }


    /**
     * according to对象里面的属性值作inQuery Properties may change User components are used
     * @param sysUser
     * @return
     */
    @GetMapping("/getMultiUser")
    public List<SysUser> getMultiUser(SysUser sysUser){
        QueryWrapper<SysUser> queryWrapper = QueryGenerator.initQueryWrapper(sysUser, null);
        //update-begin---author:wangshuai ---date:20220104  for：[JTC-297]已freeze用户仍可设置为代理人------------
        queryWrapper.eq("status",Integer.parseInt(CommonConstant.STATUS_1));
        //update-end---author:wangshuai ---date:20220104  for：[JTC-297]已freeze用户仍可设置为代理人------------
        List<SysUser> ls = this.sysUserService.list(queryWrapper);
        for(SysUser user: ls){
            user.setPassword(null);
            user.setSalt(null);
        }
        return ls;
    }
    
    /**
     * chat 创建chat组件dedicated  according to用户账号、User name、departmentid分页Query
     * @param departId departmentid
     * @param keyword search value
     * @return
     */
    @GetMapping(value = "/getUserInformation")
    public Result<IPage<SysUser>> getUserInformation(
            @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
            @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
            @RequestParam(name = "departId", required = false) String departId,
            @RequestParam(name="keyword",required=false) String keyword) {
        //------------------------------------------------------------------------------------------------
        Integer tenantId = null;
        //Whether to enable multi-tenant data isolation of the system management module【SAASMulti-tenant model】
        if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
            tenantId = oConvertUtils.getInt(TenantContext.getTenant(),0);
        }
        //------------------------------------------------------------------------------------------------
        IPage<SysUser> pageList = sysUserDepartService.getUserInformation(tenantId,departId, keyword, pageSize, pageNo);
        return Result.OK(pageList);
    }

    /**
     * 简版流程user selects component
     * @param departId departmentid
     * @param roleId Roleid
     * @param keyword search value
     * @return
     */
    @GetMapping(value = "/selectUserList")
    public Result<IPage<SysUser>> selectUserList(
            @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
            @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
            @RequestParam(name = "departId", required = false) String departId,
            @RequestParam(name = "roleId", required = false) String roleId,
            @RequestParam(name="keyword",required=false) String keyword,
            @RequestParam(name="excludeUserIdList",required = false) String excludeUserIdList,
            HttpServletRequest req) {
        //------------------------------------------------------------------------------------------------
        Integer tenantId = null;
        //Whether to enable multi-tenant data isolation of the system management module【SAASMulti-tenant model】
        if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
            String tenantStr = TenantContext.getTenant();
            tenantId = oConvertUtils.getInteger(tenantStr, oConvertUtils.getInt(TokenUtils.getTenantIdByRequest(req), -1));
            log.info("---------Select user interface in simplified flow，passtenant筛选，tenantID={}", tenantId);
        }
        //------------------------------------------------------------------------------------------------
        IPage<SysUser> pageList = sysUserDepartService.getUserInformation(tenantId, departId,roleId, keyword, pageSize, pageNo,excludeUserIdList);
        return Result.OK(pageList);
    }
    

    /**
     * 获取被逻辑删除的User list，No pagination【低代码applicationdedicated接口】
     *
     * @return List<SysUser>
     */
    @GetMapping("/getQuitList")
    public Result<List<SysUser>> getQuitList(HttpServletRequest req) {
        Integer tenantId = oConvertUtils.getInt(TokenUtils.getTenantIdByRequest(req),0);
        List<SysUser> quitList = sysUserService.getQuitList(tenantId);
        if (null != quitList && quitList.size() > 0) {
            // Query the departments to which users belong in batches
            // step.1 Get them all first userIds
            List<String> userIds = quitList.stream().map(SysUser::getId).collect(Collectors.toList());
            // step.2 pass userIds，One-time query of the user’s department name
            Map<String, String> useDepNames = sysUserService.getDepNamesByUserIds(userIds);
            quitList.forEach(item -> item.setOrgCode(useDepNames.get(item.getId())));
        }
        return Result.ok(quitList);
    }

    /**
     * Get user information(vue3用户设置dedicated)【低代码applicationdedicated接口】
     * @return
     */
    @GetMapping("/login/setting/getUserData")
    public Result<SysUser> getUserData(HttpServletRequest request) {
        String username = JwtUtil.getUserNameByToken(request);
        SysUser user = sysUserService.getUserByName(username);
        if(user==null) {
            return Result.error("The user data was not found");
        }

        //update-begin---author:wangshuai ---date:20230220  for：[QQYUN-3980]Under organization management 职位Function 职位表加tenantid Add position-User association table------------
        //Get useridpass职位数据
        List<SysPosition> sysPositionList = sysPositionService.getPositionList(user.getId());
        if(null != sysPositionList && sysPositionList.size()>0){
        //update-end---author:wangshuai ---date:20230220  for：[QQYUN-3980]Under organization management 职位Function 职位表加tenantid Add position-User association table------------
            StringBuilder nameBuilder = new StringBuilder();
            StringBuilder idBuilder = new StringBuilder();
            String verticalBar = " | ";
            for (SysPosition sysPosition:sysPositionList){
                nameBuilder.append(sysPosition.getName()).append(verticalBar);
                idBuilder.append(sysPosition.getId()).append(SymbolConstant.COMMA);
            }
            String names = nameBuilder.toString();
            if(oConvertUtils.isNotEmpty(names)){
                names = names.substring(0,names.lastIndexOf(verticalBar));
                user.setPostText(names);
            }
            //Splicing positionsid
            String ids = idBuilder.toString();
            if(oConvertUtils.isNotEmpty(ids)){
                ids = ids.substring(0,ids.lastIndexOf(SymbolConstant.COMMA));
                user.setPost(ids);
            }
        }
        return Result.ok(user);
    }

    /**
     * User edit(vue3用户设置dedicated)【低代码applicationdedicated接口】
     * @param sysUser
     * @return
     */
    @PostMapping("/login/setting/userEdit")
    @RequiresPermissions("system:user:setting:edit")
    public Result<String> userEdit(@RequestBody SysUser sysUser, HttpServletRequest request) {
        String username = JwtUtil.getUserNameByToken(request);
        SysUser user = sysUserService.getById(sysUser.getId());
        if(user==null) {
           return Result.error("The user data was not found");
        }
        if(!username.equals(user.getUsername())){
            return Result.error("You can only modify your own data");
        }
        sysUserService.updateById(sysUser);
        return Result.ok("Updated personal information successfully");
    }

    /**
     * Batch edit 【low-app】
     * @param jsonObject
     * @return
     */
    @PutMapping("/batchEditUsers")
    public Result<SysUser> batchEditUsers(@RequestBody JSONObject jsonObject) {
        Result<SysUser> result = new Result<SysUser>();
        try {
            sysUserService.batchEditUsers(jsonObject);
            result.setSuccess(true);
            result.setMessage("Operation successful！");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("Operation failed");
        }
        return result;
    }

    /**
     * according to关键词搜索department和用户【low-app】
     * @param keyword
     * @return
     */
    @GetMapping("/searchByKeyword")
    public Result<DepartAndUserInfo> searchByKeyword(@RequestParam(name="keyword",required=false) String keyword) {
        DepartAndUserInfo info = sysUserService.searchByKeyword(keyword);
        return Result.ok(info);
    }

    /**
     * 编辑department前获取department相关信息 【low-app】
     * @param id
     * @return
     */
    @GetMapping("/getUpdateDepartInfo")
    public Result<UpdateDepartInfo> getUpdateDepartInfo(@RequestParam(name="id",required=false) String id) {
        UpdateDepartInfo info = sysUserService.getUpdateDepartInfo(id);
        return Result.ok(info);
    }

    /**
     * 编辑department 【low-app】
     * @param updateDepartInfo
     * @return
     */
    @PutMapping("/doUpdateDepartInfo")
    public Result<?> doUpdateDepartInfo(@RequestBody UpdateDepartInfo updateDepartInfo) {
        sysUserService.doUpdateDepartInfo(updateDepartInfo);
        return Result.ok();
    }

    /**
     * Set up a person in charge Cancel the person in charge
     * @param json
     * @return
     */
    @PutMapping("/changeDepartChargePerson")
    public Result<?> changeDepartChargePerson(@RequestBody JSONObject json) {
        sysUserService.changeDepartChargePerson(json);
        return Result.ok();
    }

    /**
     * Add user【后台tenant模式dedicated，Don’t use this on KnockKowun】
     *
     * @param jsonObject
     * @return
     */
    @RequiresPermissions("system:user:addTenantUser")
    @RequestMapping(value = "/addTenantUser", method = RequestMethod.POST)
    public Result<SysUser> addTenantUser(@RequestBody JSONObject jsonObject) {
        Result<SysUser> result = new Result<SysUser>();
        String selectedRoles = jsonObject.getString("selectedroles");
        String selectedDeparts = jsonObject.getString("selecteddeparts");
        try {
            SysUser user = JSON.parseObject(jsonObject.toJSONString(), SysUser.class);
            user.setCreateTime(new Date());//Set creation time
            String salt = oConvertUtils.randomGen(8);
            user.setSalt(salt);
            String passwordEncode = PasswordUtil.encrypt(user.getUsername(), user.getPassword(), salt);
            user.setPassword(passwordEncode);
            user.setStatus(1);
            user.setDelFlag(CommonConstant.DEL_FLAG_0);
            //User table字段org_codeHis value cannot be set here
            user.setOrgCode(null);
            // Save user go oneservice Guaranteed transaction
            //Get tenantsids
            String relTenantIds = jsonObject.getString("relTenantIds");
            sysUserService.saveUser(user, selectedRoles, selectedDeparts, relTenantIds, true);
            baseCommonService.addLog("Add user，username： " + user.getUsername(), CommonConstant.LOG_TYPE_2, 2);
            result.success("Added successfully！");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("Operation failed");
        }
        return result;
    }
    
    /**
     * Revisetenant下的用户【低代码applicationdedicated接口】
     * @param sysUser
     * @param req
     * @return
     */
    @RequestMapping(value = "/editTenantUser", method = {RequestMethod.PUT,RequestMethod.POST})
    public Result<String> editTenantUser(@RequestBody SysUser sysUser,HttpServletRequest req){
        Result<String> result = new Result<>();
        String tenantId = TokenUtils.getTenantIdByRequest(req);
        if(oConvertUtils.isEmpty(tenantId)){
            return result.error500("No right to modify other people’s information！");
        }
        LambdaQueryWrapper<SysUserTenant> query = new LambdaQueryWrapper<>();
        query.eq(SysUserTenant::getTenantId,Integer.valueOf(tenantId));
        query.eq(SysUserTenant::getUserId,sysUser.getId());
        SysUserTenant one = userTenantService.getOne(query);
        if(null == one){
            return result.error500("非当前tenant下的用户，Modification not allowed！");
        }
        String departs = req.getParameter("selecteddeparts");
        sysUserService.editTenantUser(sysUser,tenantId,departs,null);
        return Result.ok("Modification successful");
    }

    /**
     * 切换tenant时 Need to modify loginTenantId
     * QQYUN-4491 【application】some minor issues  1、上次选中登录的tenant，Not remembered for next login
     * @param sysUser
     * @return
     */
    @PutMapping("/changeLoginTenantId")
    public Result<?> changeLoginTenantId(@RequestBody SysUser sysUser){
        Result<String> result = new Result<>();
        Integer tenantId = sysUser.getLoginTenantId();
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String userId = loginUser.getId();
        
        // judge 指定的tenantID是不是当前登录用户的tenant
        LambdaQueryWrapper<SysUserTenant> query = new LambdaQueryWrapper<>();
        query.eq(SysUserTenant::getTenantId, tenantId);
        query.eq(SysUserTenant::getUserId, userId);
        SysUserTenant one = userTenantService.getOne(query);
        if(null == one){
            return result.error500("非tenant下的用户，Modification not allowed！");
        }
        
        // Revise loginTenantId
        LambdaQueryWrapper<SysUser> update = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getId, userId);
        SysUser updateUser = new SysUser();
        updateUser.setLoginTenantId(tenantId);
        sysUserService.update(updateUser, update);
        return Result.ok();
    } 

    /**
     * application用户Export
     * @param request
     * @return
     */
    @RequestMapping(value = "/exportAppUser")
    public ModelAndView exportAppUser(HttpServletRequest request) {
        return sysUserService.exportAppUser(request);
    }
    
   /**
     * application用户导入
     * @param request
     * @return
     */
    @RequestMapping(value = "/importAppUser", method = RequestMethod.POST)
    public Result<?> importAppUser(HttpServletRequest request, HttpServletResponse response)throws IOException {
        return sysUserService.importAppUser(request);
    }

    /**
     * Change mobile number（敲敲云个人设置dedicated）
     *
     * @param json
     * @param request
     */
    @PutMapping("/changePhone")
    public Result<String> changePhone(@RequestBody JSONObject json, HttpServletRequest request){
        //Get login username
        String username = JwtUtil.getUserNameByToken(request);
        sysUserService.changePhone(json,username);
        return Result.ok("Revise手机号成功！");
    }

    /**
     * Send SMS verification code interface(Revise手机号)
     *
     * @param jsonObject
     * @return
     */
    @PostMapping(value = "/sendChangePhoneSms")
    public Result<String> sendChangePhoneSms(@RequestBody JSONObject jsonObject, HttpServletRequest request) {
        //Get login username
        String username = JwtUtil.getUserNameByToken(request);
        String ipAddress = IpUtils.getIpAddr(request);
        sysUserService.sendChangePhoneSms(jsonObject, username, ipAddress);
        return Result.ok("Verification code sent successfully！");
    }

    /**
     * 发送注销User mobile phone number verification密码[敲敲云dedicated]
     *
     * @param jsonObject
     * @return
     */
    @PostMapping(value = "/sendLogOffPhoneSms")
    public Result<String> sendLogOffPhoneSms(@RequestBody JSONObject jsonObject, HttpServletRequest request) {
        Result<String> result = new Result<>();
        //Get login username
        String username = JwtUtil.getUserNameByToken(request);
        String name = jsonObject.getString("username");
        if (oConvertUtils.isEmpty(name) || !name.equals(username)) {
            result.setSuccess(false);
            result.setMessage("Failed to send verification code，User does not match！");
            return result;
        }
        String ipAddress = IpUtils.getIpAddr(request);
        sysUserService.sendLogOffPhoneSms(jsonObject, username, ipAddress);
        result.setSuccess(true);
        result.setMessage("Verification code sent successfully！");
        return result;
    }

    /**
     * No mobile phone number bound 直接Change password
     * @param oldPassword
     * @param password
     * @return
     */
    @PutMapping("/updatePasswordNotBindPhone")
    public Result<String> updatePasswordNotBindPhone(@RequestParam(value="oldPassword") String oldPassword,
                                                     @RequestParam(value="password") String password,
                                                     @RequestParam(value="username") String username){
        sysUserService.updatePasswordNotBindPhone(oldPassword, password, username);
        return Result.OK("Change password成功！");
    }

    /**
     * According to department岗位选择用户【department岗位选择用户dedicated】
     * @return
     */
    @GetMapping("/queryDepartPostUserPageList")
    public Result<IPage<SysUser>> queryDepartPostUserPageList( @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                                        @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                                        @RequestParam(name = "departId", required = false) String departId,
                                                        @RequestParam(name="realname",required=false) String realname,
                                                        @RequestParam(name="username",required=false) String username,
                                                        @RequestParam(name="isMultiTranslate",required=false) String isMultiTranslate,
                                                        @RequestParam(name="id",required = false) String id){
        String[] arr = new String[]{departId, realname, username, id};
        SqlInjectionUtil.filterContent(arr, SymbolConstant.SINGLE_QUOTATION_MARK);
        IPage<SysUser> pageList = sysUserDepartService.queryDepartPostUserPageList(departId, username, realname, pageSize, pageNo,id,isMultiTranslate);
        return Result.OK(pageList);
    }

    /**
     * Get the progress of uploading files
     * 
     * @param fileKey
     * @param type
     * @return
     */
    @GetMapping("/getUploadFileProgress")
    public Result<Double> getUploadFileProgress(@RequestParam(name = "fileKey") String fileKey,
                                                @RequestParam("type") String type){
        Double progress = ImportSysUserCache.getImportSysUserMap(fileKey, type);
        if(progress == 100){
            ImportSysUserCache.removeImportLowAppMap(fileKey);
        }
        return Result.ok(progress);
    }
}
