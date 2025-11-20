package org.jeecg.modules.system.controller;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.PermissionData;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.PasswordUtil;
import org.jeecg.common.util.TokenUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.modules.system.entity.*;
import org.jeecg.modules.system.service.ISysTenantPackService;
import org.jeecg.modules.system.service.ISysTenantService;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecg.modules.system.service.ISysUserTenantService;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.vo.SysUserTenantVo;
import org.jeecg.modules.system.vo.tenant.TenantDepartAuthInfo;
import org.jeecg.modules.system.vo.tenant.TenantPackModel;
import org.jeecg.modules.system.vo.tenant.TenantPackUser;
import org.jeecg.modules.system.vo.tenant.TenantPackUserCount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Tenant configuration information
 * @author: jeecg-boot
 */
@Slf4j
@RestController
@RequestMapping("/sys/tenant")
public class SysTenantController {

    @Autowired
    private ISysTenantService sysTenantService;

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private ISysUserTenantService relationService;
    
    @Autowired
    private ISysTenantPackService sysTenantPackService;
    
    @Autowired
    private BaseCommonService baseCommonService;

    @Autowired
    private ISysDepartService sysDepartService;

    /**
     * Get list data
     * @param sysTenant
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @RequiresPermissions("system:tenant:list")
    @PermissionData(pageComponent = "system/TenantList")
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public Result<IPage<SysTenant>> queryPageList(SysTenant sysTenant,@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
									  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,HttpServletRequest req) {
		Result<IPage<SysTenant>> result = new Result<IPage<SysTenant>>();
        //---author:zhangyafei---date:20210916-----for: Tenant management adds date range query---
        Date beginDate=null;
        Date endDate=null;
        if(oConvertUtils.isNotEmpty(sysTenant)) {
            beginDate=sysTenant.getBeginDate();
            endDate=sysTenant.getEndDate();
            sysTenant.setBeginDate(null);
            sysTenant.setEndDate(null);
        }
        //---author:zhangyafei---date:20210916-----for: Tenant management adds date range query---
        QueryWrapper<SysTenant> queryWrapper = QueryGenerator.initQueryWrapper(sysTenant, req.getParameterMap());
        //---author:zhangyafei---date:20210916-----for: Tenant management adds date range query---
        if(oConvertUtils.isNotEmpty(sysTenant)){
            queryWrapper.ge(oConvertUtils.isNotEmpty(beginDate),"begin_date",beginDate);
            queryWrapper.le(oConvertUtils.isNotEmpty(endDate),"end_date",endDate);
        }
        //---author:zhangyafei---date:20210916-----for: Tenant management adds date range query---
		Page<SysTenant> page = new Page<SysTenant>(pageNo, pageSize);
		IPage<SysTenant> pageList = sysTenantService.page(page, queryWrapper);
		result.setSuccess(true);
		result.setResult(pageList);
		return result;
	}

    /**
     * Get the list of deleted tenants
     * @param sysTenant
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @GetMapping("/recycleBinPageList")
    @RequiresPermissions("system:tenant:recycleBinPageList")
    public Result<IPage<SysTenant>> recycleBinPageList(SysTenant sysTenant,@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                                   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,HttpServletRequest req){
        Result<IPage<SysTenant>> result = new Result<IPage<SysTenant>>();
        Page<SysTenant> page = new Page<SysTenant>(pageNo, pageSize);
        IPage<SysTenant> pageList = sysTenantService.getRecycleBinPageList(page, sysTenant);
        result.setSuccess(true);
        result.setResult(pageList);
        return result;
    }
    
    /**
     *   Add to
     * @param
     * @return
     */
    @RequiresPermissions("system:tenant:add")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result<SysTenant> add(@RequestBody SysTenant sysTenant) {
        Result<SysTenant> result = new Result();
        if(sysTenant!=null && oConvertUtils.isNotEmpty(sysTenant.getId()) && sysTenantService.getById(sysTenant.getId())!=null){
            return result.error500("This number already exists!");
        }
        try {
            sysTenantService.saveTenant(sysTenant);
            //Add to默认产品包
            sysTenantPackService.addTenantDefaultPack(sysTenant.getId());
            result.success("Add to成功！");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("Operation failed");
        }
        return result;
    }

    /**
     * [QQYUN-11032]【jeecg】Added initialization package button to tenant package management
     * @param tenantId
     * @return
     * @author chenrui
     * @date 2025/2/6 18:24
     */
    @RequiresPermissions("system:tenant:syncDefaultPack")
    @PostMapping(value = "/syncDefaultPack")
    public Result<?> syncDefaultPack(@RequestParam(name="tenantId",required=true) Integer tenantId) {
        //Sync default product packages
        sysTenantPackService.syncDefaultPack(tenantId);
        return Result.OK("Operation successful");
    }

    /**
     *  edit
     * @param
     * @return
     */
    @RequiresPermissions("system:tenant:edit")
    @RequestMapping(value = "/edit", method ={RequestMethod.PUT, RequestMethod.POST})
    public Result<SysTenant> edit(@RequestBody SysTenant tenant) {
        Result<SysTenant> result = new Result();
        SysTenant sysTenant = sysTenantService.getById(tenant.getId());
        if(sysTenant==null) {
           return result.error500("No corresponding entity found");
        }
        if(oConvertUtils.isEmpty(sysTenant.getHouseNumber())){
            tenant.setHouseNumber(RandomUtil.randomStringUpper(6));
        }
        boolean ok = sysTenantService.updateById(tenant);
        if(ok) {
            result.success("Modification successful!");
        }
        return result;
    }

    /**
     *   passiddelete
     * @param id
     * @return
     */
    @RequiresPermissions("system:tenant:delete")
    @RequestMapping(value = "/delete", method ={RequestMethod.DELETE, RequestMethod.POST})
    public Result<?> delete(@RequestParam(name="id",required=true) String id) {
        //------------------------------------------------------------------
        //in the case ofsaasin isolation，Determine the current tenantidIs it under the current tenant?
        if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
            //Get current user
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            SysTenant sysTenant = sysTenantService.getById(id);

            String username = "admin";
            String createdBy = sysUser.getUsername();
            if (!sysTenant.getCreateBy().equals(createdBy) && !username.equals(createdBy)) {
                baseCommonService.addLog("Unauthorized，不能delete非自己创建的tenant，tenantID：" + id + "，operator：" + sysUser.getUsername(), CommonConstant.LOG_TYPE_2, CommonConstant.OPERATE_TYPE_3);
                return Result.error("deletetenant失败,当前operator不是tenant的创建人！");
            }
        }
        //------------------------------------------------------------------
                
        sysTenantService.removeTenantById(id);
        return Result.ok("delete成功");
    }

    /**
     *  批量delete
     * @param ids
     * @return
     */
    @RequiresPermissions("system:tenant:deleteBatch")
    @RequestMapping(value = "/deleteBatch", method = RequestMethod.DELETE)
    public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
        Result<?> result = new Result<>();
        if(oConvertUtils.isEmpty(ids)) {
            result.error500("未选中tenant！");
        }else {
            String[] ls = ids.split(",");
            // 过滤掉已被引用的tenant
            List<Integer> idList = new ArrayList<>();
            for (String id : ls) {
                //------------------------------------------------------------------
                //in the case ofsaasin isolation，Determine the current tenantidIs it under the current tenant?
                if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
                    //Get current user
                    LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
                    SysTenant sysTenant = sysTenantService.getById(id);

                    String username = "admin";
                    String createdBy = sysUser.getUsername();
                    if (!sysTenant.getCreateBy().equals(createdBy) && !username.equals(createdBy)) {
                        baseCommonService.addLog("Unauthorized，不能delete非自己创建的tenant，tenantID：" + id + "，operator：" + sysUser.getUsername(), CommonConstant.LOG_TYPE_2, CommonConstant.OPERATE_TYPE_3);
                        return Result.error("deletetenant失败,当前operator不是tenant的创建人！");
                    }
                }
                //------------------------------------------------------------------
                
                idList.add(Integer.parseInt(id));
            }
            //update-begin---author:wangshuai ---date:20230710  for：【QQYUN-5723】3、tenantdelete直接delete，不delete中间表------------
            sysTenantService.removeByIds(idList);
            result.success("delete成功！");
            //update-end---author:wangshuai ---date:20220523  for：【QQYUN-5723】3、tenantdelete直接delete，不delete中间表------------
        }
        return result;
    }

    /**
     * passidQuery
     * @param id
     * @return
     */
    @RequestMapping(value = "/queryById", method = RequestMethod.GET)
    public Result<SysTenant> queryById(@RequestParam(name="id",required=true) String id) {
        Result<SysTenant> result = new Result<SysTenant>();
        if(oConvertUtils.isEmpty(id)){
            result.error500("Parameter is empty！");
        }
        //------------------------------------------------------------------------------------------------
        //Get logged in user information
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        //是否开启系统管理模块的多tenant数据隔离【SAAS多tenant模式】, admin给特权可以管理所有tenant
        if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL && !"admin".equals(sysUser.getUsername())){
            Integer loginSessionTenant = oConvertUtils.getInt(TenantContext.getTenant());
            if(loginSessionTenant!=null && !loginSessionTenant.equals(Integer.valueOf(id))){
                result.error500("No permission访问他人tenant！");
                return result;
            }
        }
        //------------------------------------------------------------------------------------------------
        SysTenant sysTenant = sysTenantService.getById(id);
        if(sysTenant==null) {
            result.error500("No corresponding entity found");
        }else {
            result.setResult(sysTenant);
            result.setSuccess(true);
        }
        return result;
    }


    /**
     * Query有效的 tenant数据
     * @return
     */
    @RequiresPermissions("system:tenant:queryList")
    @RequestMapping(value = "/queryList", method = RequestMethod.GET)
    public Result<List<SysTenant>> queryList(@RequestParam(name="ids",required=false) String ids) {
        Result<List<SysTenant>> result = new Result<List<SysTenant>>();
        LambdaQueryWrapper<SysTenant> query = new LambdaQueryWrapper<>();
        query.eq(SysTenant::getStatus, 1);
        if(oConvertUtils.isNotEmpty(ids)){
            query.in(SysTenant::getId, ids.split(","));
        }
        //此处Query忽略时间条件
        List<SysTenant> ls = sysTenantService.list(query);
        result.setSuccess(true);
        result.setResult(ls);
        return result;
    }

    /**
     * 产品包Pagination列表Query
     *
     * @param sysTenantPack
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @GetMapping(value = "/packList")
    @RequiresPermissions("system:tenant:packList")
    public Result<IPage<SysTenantPack>> queryPackPageList(SysTenantPack sysTenantPack,
                                                          @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                          @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                          HttpServletRequest req) {
        QueryWrapper<SysTenantPack> queryWrapper = QueryGenerator.initQueryWrapper(sysTenantPack, req.getParameterMap());
        Page<SysTenantPack> page = new Page<SysTenantPack>(pageNo, pageSize);
        IPage<SysTenantPack> pageList = sysTenantPackService.page(page, queryWrapper);
        List<SysTenantPack> records = pageList.getRecords();
        if (null != records && records.size() > 0) {
            pageList.setRecords(sysTenantPackService.setPermissions(records));
        }
        return Result.OK(pageList);
    }

    /**
     * 创建tenant产品包
     *
     * @param sysTenantPack
     * @return
     */
    @PostMapping(value = "/addPackPermission")
    @RequiresPermissions("system:tenant:add:pack")
    public Result<String> addPackPermission(@RequestBody SysTenantPack sysTenantPack) {
        sysTenantPackService.addPackPermission(sysTenantPack);
        return Result.ok("创建tenant产品包成功");
    }

    /**
     * 创建tenant产品包
     *
     * @param sysTenantPack
     * @return
     */
    @PutMapping(value = "/editPackPermission")
    @RequiresPermissions("system:tenant:edit:pack")
    public Result<String> editPackPermission(@RequestBody SysTenantPack sysTenantPack) {
        sysTenantPackService.editPackPermission(sysTenantPack);
        return Result.ok("修改tenant产品包成功");
    }

    /**
     * 批量deleteuser菜单
     *
     * @param ids
     * @return
     */
    @DeleteMapping("/deleteTenantPack")
    @RequiresPermissions("system:tenant:delete:pack")
    public Result<String> deleteTenantPack(@RequestParam(value = "ids") String ids) {
        sysTenantPackService.deleteTenantPack(ids);
        return Result.ok("deletetenant产品包成功");
    }
    


    //===========【low code applications，Front-end dedicated interface —— 加入限制只能维护和查看自己拥有的tenant】==========================================================
    /**
     *  Query当前user的所有有效tenant【low code applications专用接口】
     * @return
     */
    @RequestMapping(value = "/getCurrentUserTenant", method = RequestMethod.GET)
    public Result<Map<String,Object>> getCurrentUserTenant() {
        Result<Map<String,Object>> result = new Result<Map<String,Object>>();
        try {
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            //update-begin---author:wangshuai ---date:20221223  for：[QQYUN-3371]tenant逻辑改造，Change to relational table------------
            List<Integer> tenantIdList = relationService.getTenantIdsByUserId(sysUser.getId());
            Map<String,Object> map = new HashMap(5);
            if (null!=tenantIdList && tenantIdList.size()>0) {
            //update-end---author:wangshuai ---date:20221223  for：[QQYUN-3371]tenant逻辑改造，Change to relational table------------
                // 该方法仅Query有效的tenant，if return0个就说明所有的tenant均无效。
                List<SysTenant> tenantList = sysTenantService.queryEffectiveTenant(tenantIdList);
                map.put("list", tenantList);
            }
            result.setSuccess(true);
            result.setResult(map);
        }catch(Exception e) {
            log.error(e.getMessage(), e);
            result.error500("Query失败！");
        }
        return result;
    }

    /**
     * Invite users【low code applications专用接口】
     * @param ids
     * @param phone
     * @return
     */
    @PutMapping("/invitationUserJoin")
    @RequiresPermissions("system:tenant:invitation:user")
    public Result<String> invitationUserJoin(@RequestParam("ids") String ids,@RequestParam(value = "phone", required = false) String phone, @RequestParam(value = "username", required = false) String username){
        if(oConvertUtils.isEmpty(phone) && oConvertUtils.isEmpty(username)){
            return Result.error("Mobile phone number and user account cannot be empty at the same time！");
        }
        sysTenantService.invitationUserJoin(ids,phone,username);
        return Result.ok("Invite users成功");
    }

    /**
     * Get user list data【low code applications专用接口】
     * @param user
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @RequestMapping(value = "/getTenantUserList", method = RequestMethod.GET)
    @RequiresPermissions("system:tenant:user:list")
    public Result<IPage<SysUser>> getTenantUserList(SysUser user,
                                                    @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                                    @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                                    @RequestParam(name="userTenantId") String userTenantId,
                                                    HttpServletRequest req) {
        Result<IPage<SysUser>> result = new Result<>();
        Page<SysUser> page = new Page<>(pageNo, pageSize);
        Page<SysUser> pageList = relationService.getPageUserList(page,Integer.valueOf(userTenantId),user);
        result.setSuccess(true);
        result.setResult(pageList);
        return result;
    }

    /**
     * 请离usertenant【low code applications专用接口】
     * @param userIds
     * @param tenantId
     * @return
     */
    @PutMapping("/leaveTenant")
    @RequiresPermissions("system:tenant:leave")
    public Result<String> leaveTenant(@RequestParam("userIds") String userIds,
                                      @RequestParam("tenantId") String tenantId){
        Result<String> result = new Result<>();
        //是否开启系统管理模块的多tenant数据隔离【SAAS多tenant模式】
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL && !"admin".equals(sysUser.getUsername())){
            Integer loginSessionTenant = oConvertUtils.getInt(TenantContext.getTenant());
            if(loginSessionTenant!=null && !loginSessionTenant.equals(Integer.valueOf(tenantId))){
                result.error500("No permission访问他人tenant！");
                return result;
            }
        }
        sysTenantService.leaveTenant(userIds,tenantId);
        return Result.ok("Please leave success");
    }

    /**
     *  edit（只允许修改自己拥有的tenant）【low code applications专用接口】
     * @param
     * @return
     */
    @RequestMapping(value = "/editOwnTenant", method ={RequestMethod.PUT, RequestMethod.POST})
    public Result<SysTenant> editOwnTenant(@RequestBody SysTenant tenant,HttpServletRequest req) {
        Result<SysTenant> result = new Result();
        String tenantId = TokenUtils.getTenantIdByRequest(req);
        if(!tenantId.equals(tenant.getId().toString())){
            return result.error500("No rights to modify他人tenant！");
        }

        SysTenant sysTenant = sysTenantService.getById(tenant.getId());
        if(sysTenant==null) {
            return result.error500("No corresponding entity found");
        }
        if(oConvertUtils.isEmpty(sysTenant.getHouseNumber())){
            tenant.setHouseNumber(RandomUtil.randomStringUpper(6));
        }
        boolean ok = sysTenantService.updateById(tenant);
        if(ok) {
            result.success("Modification successful!");
        }
        return result;
    }
    
    /**
     * 创建tenant并且将user保存到中间表【low code applications专用接口】
     * @param sysTenant
     */
    @PostMapping("/saveTenantJoinUser")
    public Result<Integer> saveTenantJoinUser(@RequestBody SysTenant sysTenant){
        Result<Integer> result = new Result<>();
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        Integer tenantId = sysTenantService.saveTenantJoinUser(sysTenant, sysUser.getId());
        result.setSuccess(true);
        result.setMessage("Created successfully");
        result.setResult(tenantId);
        return result;
    }

    /**
     * 加入tenantpass门牌号【low code applications专用接口】
     * @param sysTenant
     */
    @PostMapping("/joinTenantByHouseNumber")
    public Result<Integer> joinTenantByHouseNumber(@RequestBody SysTenant sysTenant){
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        Integer tenantId = sysTenantService.joinTenantByHouseNumber(sysTenant, sysUser.getId());
        Result<Integer> result = new Result<>();
        if(tenantId != 0){
            result.setMessage("Successfully applied to join the organization");
            result.setSuccess(true);
            result.setResult(tenantId);
            return result;
        }else{
            result.setMessage("The house number does not exist");
            result.setSuccess(false);
            return result;
        }
    }
    
    //update-begin---author:wangshuai ---date:20230107  for：[QQYUN-3725]申请加入tenant，Add interface under review status------------
    /**
     * PaginationGettenantuser数据(vue3usertenant页面)【low code applications专用接口】
     *
     * @param pageNo
     * @param pageSize
     * @param userTenantStatus
     * @param type
     * @param req
     * @return
     */
    @GetMapping("/getUserTenantPageList")
    //@RequiresPermissions("system:tenant:tenantPageList")
    public Result<IPage<SysUserTenantVo>> getUserTenantPageList(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                                @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                                @RequestParam(name = "userTenantStatus") String userTenantStatus,
                                                                @RequestParam(name = "type", required = false) String type,
                                                                SysUser user,
                                                                HttpServletRequest req) {
        Page<SysUserTenantVo> page = new Page<SysUserTenantVo>(pageNo, pageSize);
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String tenantId = oConvertUtils.getString(TenantContext.getTenant(), "0");
        IPage<SysUserTenantVo> list = relationService.getUserTenantPageList(page, Arrays.asList(userTenantStatus.split(SymbolConstant.COMMA)), user, Integer.valueOf(tenantId));
        return Result.ok(list);
    }

    /**
     * passuseridGettenant列表【low code applications专用接口】
     *
     * @param userTenantStatus The status of the relationship table
     * @return
     */
    @GetMapping("/getTenantListByUserId")
    //@RequiresPermissions("system:tenant:getTenantListByUserId")
    public Result<List<SysUserTenantVo>> getTenantListByUserId(@RequestParam(name = "userTenantStatus", required = false) String userTenantStatus) {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        List<String> list = null;
        if (oConvertUtils.isNotEmpty(userTenantStatus)) {
            list = Arrays.asList(userTenantStatus.split(SymbolConstant.COMMA));
        }
        //tenant状态，userid,tenantuser关系状态
        List<SysUserTenantVo> sysTenant = relationService.getTenantListByUserId(sysUser.getId(), list);
        return Result.ok(sysTenant);
    }

    /**
     * 更新usertenant关系状态【low code applications专用接口】
     */
    @PutMapping("/updateUserTenantStatus")
    //@RequiresPermissions("system:tenant:updateUserTenantStatus")
    public Result<String> updateUserTenantStatus(@RequestBody SysUserTenant userTenant) {
        String tenantId = TenantContext.getTenant();
        if (oConvertUtils.isEmpty(tenantId)) {
            return Result.error("未找到当前tenant信息"); 
        }
        relationService.updateUserTenantStatus(userTenant.getUserId(), tenantId, userTenant.getStatus());
        return Result.ok("更新usertenant状态成功");
    }

    /**
     * 注销tenant【low code applications专用接口】
     *
     * @param sysTenant
     * @return
     */
    @PutMapping("/cancelTenant")
    //@RequiresPermissions("system:tenant:cancelTenant")
    public Result<String> cancelTenant(@RequestBody SysTenant sysTenant,HttpServletRequest request) {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        SysTenant tenant = sysTenantService.getById(sysTenant.getId());
        if (null == tenant) {
            return Result.error("未找到当前tenant信息");
        }
        if (!sysUser.getUsername().equals(tenant.getCreateBy())) {
            return Result.error("No permission，只能注销自己创建的tenant！");
        }
        SysUser userById = sysUserService.getById(sysUser.getId());
        String loginPassword = request.getParameter("loginPassword");
        String passwordEncode = PasswordUtil.encrypt(sysUser.getUsername(),loginPassword, userById.getSalt());
        if (!passwordEncode.equals(userById.getPassword())) {
            return Result.error("Password is incorrect");
        }
        sysTenantService.removeById(sysTenant.getId());
        return Result.ok("Logout successful");
    }
    //update-end---author:wangshuai ---date:20230107  for：[QQYUN-3725]申请加入tenant，Add interface under review status------------

    /**
     * Gettenantuser不同状态下的数量【low code applications专用接口】
     * @return
     */
    @GetMapping("/getTenantStatusCount")
    public Result<Long> getTenantStatusCount(@RequestParam(value = "status",defaultValue = "1") String status, HttpServletRequest req){
        String tenantId = TokenUtils.getTenantIdByRequest(req);
        if (null == tenantId) {
            return Result.error("未找到当前tenant信息");
        }
        LambdaQueryWrapper<SysUserTenant> query = new LambdaQueryWrapper<>();
        query.eq(SysUserTenant::getTenantId,tenantId);
        query.eq(SysUserTenant::getStatus,status);
        long count = relationService.count(query);
        return Result.ok(count);
    }

    /**
     * user取消tenant申请【low code applications专用接口】
     * @param tenantId
     * @return
     */
    @PutMapping("/cancelApplyTenant")
    public Result<String> cancelApplyTenant(@RequestParam("tenantId") String tenantId){
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        sysTenantService.leaveTenant(sysUser.getId(),tenantId);
        return Result.ok("Cancellation application successful");
    }

    //===========【low code applications，Front-end dedicated interface —— 加入限制只能维护和查看自己拥有的tenant】==========================================================

    /**
     * 彻底deletetenant
     * @param ids
     * @return
     */
    @DeleteMapping("/deleteLogicDeleted")
    @RequiresPermissions("system:tenant:deleteTenantLogic")
    public Result<String> deleteTenantLogic(@RequestParam("ids") String ids){
        sysTenantService.deleteTenantLogic(ids);
        return Result.ok("彻底delete成功");
    }

    /**
     * 还原delete的tenant
     * @param ids
     * @return
     */
    @PutMapping("/revertTenantLogic")
    @RequiresPermissions("system:tenant:revertTenantLogic")
    public Result<String> revertTenantLogic(@RequestParam("ids") String ids){
        sysTenantService.revertTenantLogic(ids);
        return Result.ok("Restore successful");
    }

    /**
     * 退出tenant【low code applications专用接口】
     * @param sysTenant
     * @param request
     * @return
     */
    @DeleteMapping("/exitUserTenant")
    public Result<String> exitUserTenant(@RequestBody SysTenant sysTenant,HttpServletRequest request){
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        //验证user是否已存在
        Integer count = relationService.userTenantIzExist(sysUser.getId(),sysTenant.getId());
        if (count == 0) {
            return Result.error("此tenant下没有当前user");
        }
        //Verify password
        String loginPassword = request.getParameter("loginPassword");
        SysUser userById = sysUserService.getById(sysUser.getId());
        String passwordEncode = PasswordUtil.encrypt(sysUser.getUsername(),loginPassword, userById.getSalt());
        if (!passwordEncode.equals(userById.getPassword())) {
            return Result.error("Password is incorrect");
        }
        //Log out
        sysTenantService.exitUserTenant(sysUser.getId(),sysUser.getUsername(),String.valueOf(sysTenant.getId()));
        return Result.ok("退出tenant成功");
    }

    /**
     * 变更tenant拥有者【low code applications专用接口】
     * @param userId
     * @return
     */
    @PostMapping("/changeOwenUserTenant")
    public Result<String> changeOwenUserTenant(@RequestParam("userId") String userId,
                                               @RequestParam("tenantId") String tenantId){
        sysTenantService.changeOwenUserTenant(userId,tenantId);
        return Result.ok("退出tenant成功");
    }

    /**
     * Invite users到tenant,pass手机号匹配 【low code applications专用接口】
     * @param phone
     * @param departId
     * @return
     */
    @PostMapping("/invitationUser")
    public Result<String> invitationUser(@RequestParam(name="phone") String phone,
                                         @RequestParam(name="departId",defaultValue = "") String departId){
        return sysTenantService.invitationUser(phone,departId);
    }


    /**
     * Get tenant产品包-3defaultadminnumber of personnel
     * @param tenantId
     * @return
     */
    @GetMapping("/loadAdminPackCount")
    public Result<List<TenantPackUserCount>> loadAdminPackCount(@RequestParam("tenantId") Integer tenantId){
        List<TenantPackUserCount> list = sysTenantService.queryTenantPackUserCount(tenantId);
        return Result.ok(list);
    }

    /**
     * Querytenant产品包信息
     * @param packModel
     * @return
     */
    @GetMapping("/getTenantPackInfo")
    public Result<TenantPackModel> getTenantPackInfo(TenantPackModel packModel){
        TenantPackModel tenantPackModel = sysTenantService.queryTenantPack(packModel);
        return Result.ok(tenantPackModel);
    }


    /**
     * Add touser和产品包的关系数据
     * @param sysTenantPackUser
     * @return
     */
    @PostMapping("/addTenantPackUser")
    public Result<?> addTenantPackUser(@RequestBody SysTenantPackUser sysTenantPackUser){
        sysTenantService.addBatchTenantPackUser(sysTenantPackUser);
        return Result.ok("Operation successful！");
    }

    /**
     * 从产品包移除user
     * @param sysTenantPackUser
     * @return
     */
    @PutMapping("/deleteTenantPackUser")
    public Result<?> deleteTenantPackUser(@RequestBody SysTenantPackUser sysTenantPackUser){
        sysTenantService.deleteTenantPackUser(sysTenantPackUser);
        return Result.ok("Operation successful！");
    }


    /**
     * Modify application status
     * @param sysTenant
     * @return
     */
    @PutMapping("/updateApplyStatus")
    public Result<?> updateApplyStatus(@RequestBody SysTenant sysTenant){
        SysTenant entity = this.sysTenantService.getById(sysTenant.getId());
        if(entity==null){
            return Result.error("tenant不存在!");
        }
        entity.setApplyStatus(sysTenant.getApplyStatus());
        sysTenantService.updateById(entity);
        return Result.ok("");
    }


    /**
     * Get产品包人员申请列表
     * @param tenantId
     * @return
     */
    @GetMapping("/getTenantPackApplyUsers")
    public Result<?> getTenantPackApplyUsers(@RequestParam("tenantId") Integer tenantId){
        List<TenantPackUser> list = sysTenantService.getTenantPackApplyUsers(tenantId);
        return Result.ok(list);
    }

    /**
     * personal Apply to become an administrator
     * @param sysTenantPackUser
     * @return
     */
    @PostMapping("/doApplyTenantPackUser")
    public Result<?> doApplyTenantPackUser(@RequestBody SysTenantPackUser sysTenantPackUser){
        sysTenantService.doApplyTenantPackUser(sysTenantPackUser);
        return Result.ok("Application successful！");
    }

    /**
     * 申请pass Become an administrator
     * @param sysTenantPackUser
     * @return
     */
    @PutMapping("/passApply")
    public Result<?> passApply(@RequestBody SysTenantPackUser sysTenantPackUser){
        sysTenantService.passApply(sysTenantPackUser);
        return Result.ok("Operation successful！");
    }

    /**
     *  reject application Become an administrator
     * @param sysTenantPackUser
     * @return
     */
    @PutMapping("/deleteApply")
    public Result<?> deleteApply(@RequestBody SysTenantPackUser sysTenantPackUser){
        sysTenantService.deleteApply(sysTenantPackUser);
        return Result.ok("");
    }

    /**
     * Check whether you have applied for a super administrator
     * @return
     */
    @GetMapping("/getApplySuperAdminCount")
    public Result<Long> getApplySuperAdminCount(){
        Long count = sysTenantService.getApplySuperAdminCount();
        return Result.ok(count);
    }

    /**
     * Enter the application organization page Querytenant信息及当前user是否有 Administrator's permissions--
     * @param id
     * @return
     */
    @RequestMapping(value = "/queryTenantAuthInfo", method = RequestMethod.GET)
    public Result<TenantDepartAuthInfo> queryTenantAuthInfo(@RequestParam(name="id",required=true) String id) {
        TenantDepartAuthInfo info = sysTenantService.getTenantDepartAuthInfo(Integer.parseInt(id));
        return Result.ok(info);
    }

    /**
     * Get产品包下的user列表(Pagination)
     * @param tenantId
     * @param packId
     * @param status
     * @param pageNo
     * @param pageSize
     * @return
     */
    @GetMapping("/queryTenantPackUserList")
    public Result<IPage<TenantPackUser>> queryTenantPackUserList(@RequestParam("tenantId") String tenantId,
                                                                 @RequestParam("packId") String packId,
                                                                 @RequestParam("status") Integer status,
                                                                 @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                                                 @RequestParam(name="pageSize", defaultValue="10") Integer pageSize){
        Page<TenantPackUser> page = new Page<>(pageNo,pageSize);
        IPage<TenantPackUser> pageList = sysTenantService.queryTenantPackUserList(tenantId,packId,status,page);
        return Result.ok(pageList);
    }

    /**
     * Get当前tenant下的部门和成员数量
     */
    @GetMapping("/getTenantCount")
    public Result<Map<String,Long>> getTenantCount(HttpServletRequest request){
        Map<String,Long> map = new HashMap<>();
        //update-begin---author:wangshuai---date:2023-11-24---for:【QQYUN-7177】user数量显示不正确---
        if(oConvertUtils.isEmpty(TokenUtils.getTenantIdByRequest(request))){
            return Result.error("当前tenant为空，Forbidden access！");
        }
        Integer tenantId = oConvertUtils.getInt(TokenUtils.getTenantIdByRequest(request));
        Long userCount = relationService.getUserCount(tenantId,CommonConstant.USER_TENANT_NORMAL);
        //update-end---author:wangshuai---date:2023-11-24---for:【QQYUN-7177】user数量显示不正确---
        map.put("userCount",userCount);
        LambdaQueryWrapper<SysDepart> departQuery = new LambdaQueryWrapper<>();
        departQuery.eq(SysDepart::getDelFlag,String.valueOf(CommonConstant.DEL_FLAG_0));
        departQuery.eq(SysDepart::getTenantId,tenantId);
        //Department status is temporarily unavailable，Comment out first
        //departQuery.eq(SysDepart::getStatus,CommonConstant.STATUS_1);
        long departCount = sysDepartService.count(departQuery);
        map.put("departCount",departCount);
        return Result.ok(map);
    }

    /**
     * passuseridGettenant列表（Pagination）
     *
     * @param sysUserTenantVo
     * @return
     */
    @GetMapping("/getTenantPageListByUserId")
    public Result<IPage<SysTenant>> getTenantPageListByUserId(SysUserTenantVo sysUserTenantVo,
                                                              @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                                              @RequestParam(name="pageSize", defaultValue="10") Integer pageSize) {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        List<String> list = null;
        String userTenantStatus = sysUserTenantVo.getUserTenantStatus();
        if (oConvertUtils.isNotEmpty(userTenantStatus)) {
            list = Arrays.asList(userTenantStatus.split(SymbolConstant.COMMA));
        }
        Page<SysTenant> page = new Page<>(pageNo,pageSize);
        IPage<SysTenant> pageList = relationService.getTenantPageListByUserId(page,sysUser.getId(),list,sysUserTenantVo);
        return Result.ok(pageList);
    }

    /**
     * 同意或拒绝加入tenant
     */
    @PutMapping("/agreeOrRefuseJoinTenant")
    public Result<String> agreeOrRefuseJoinTenant(@RequestParam("tenantId") Integer tenantId, 
                                                  @RequestParam("status") String status){
        //是否开启系统管理模块的多tenant数据隔离【SAAS多tenant模式】
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String userId = sysUser.getId();
        SysTenant tenant = sysTenantService.getById(tenantId);
        if(null == tenant){
            return Result.error("This organization does not exist");
        }
        SysUserTenant sysUserTenant = relationService.getUserTenantByTenantId(userId, tenantId);
        if (null == sysUserTenant) {
            return Result.error("该userThis organization does not exist中，No rights to modify");
        }
        String content = "";
        SysUser user = new SysUser();
        user.setUsername(sysUserTenant.getCreateBy());
        String realname = oConvertUtils.getString(sysUser.getRealname(),sysUser.getUsername());
        //Joined successfully
        if(CommonConstant.USER_TENANT_NORMAL.equals(status)){
            //修改tenant状态
            relationService.agreeJoinTenant(userId,tenantId);
            content = content + realname + "Agree to join you sent " + tenant.getName() + " invitation";
            sysTenantService.sendMsgForAgreeAndRefuseJoin(user, content);
            return Result.OK("您已同意该组织invitation");
        }else if(CommonConstant.USER_TENANT_REFUSE.equals(status)){
            //直接delete关系表即可
            relationService.refuseJoinTenant(userId,tenantId);
            content = content + realname + "Your offer to join has been rejected " + tenant.getName() + " invitation";
            sysTenantService.sendMsgForAgreeAndRefuseJoin(user, content);
            return Result.OK("您已成功拒绝该组织invitation");
        }
        return Result.error("type mismatch，Modification of data is prohibited");
    }
    
    /**
     * 目前只给敲敲云tenant下deleteuser使用
     * 
     * 根据密码deleteuser
     */
    @DeleteMapping("/deleteUserByPassword")
    public Result<String> deleteUserByPassword(@RequestBody SysUser sysUser,HttpServletRequest request){
        Integer tenantId = oConvertUtils.getInteger(TokenUtils.getTenantIdByRequest(request), null);
        sysTenantService.deleteUserByPassword(sysUser, tenantId);
        return Result.ok("deleteuser成功");
    }

    /**
     *  Query当前user的所有有效tenant【Knowledge base dedicated interface】
     * @return
     */
    @RequestMapping(value = "/getCurrentUserTenantForFile", method = RequestMethod.GET)
    public Result<Map<String,Object>> getCurrentUserTenantForFile() {
        Result<Map<String,Object>> result = new Result<Map<String,Object>>();
        try {
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            List<SysTenant> tenantList = sysTenantService.getTenantListByUserId(sysUser.getId());
            Map<String,Object> map = new HashMap<>(5);
            //Openingsaastenant隔离的时候并且tenant数据不为空，则返回tenant信息
            if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL && CollectionUtil.isNotEmpty(tenantList)) {
                map.put("list", tenantList);
            }
            result.setSuccess(true);
            result.setResult(map);
        }catch(Exception e) {
            log.error(e.getMessage(), e);
            result.error500("Query失败！");
        }
        return result;
    }

    /**
     * 目前只给敲敲云人员与部门下的userdelete使用
     *
     * deleteuser
     */
    @DeleteMapping("/deleteUser")
    public Result<String> deleteUser(@RequestBody SysUser sysUser,HttpServletRequest request){
        Integer tenantId = oConvertUtils.getInteger(TokenUtils.getTenantIdByRequest(request), null);
        sysTenantService.deleteUser(sysUser, tenantId);
        return Result.ok("deleteuser成功");
    }

    /**
     * 根据tenantid和useridGetuser的产品包列表和当前user下的产品包id
     *
     * @param tenantId
     * @param request
     * @return
     */
    @GetMapping("/listPackByTenantUserId")
    public Result<Map<String, Object>> listPackByTenantUserId(@RequestParam("tenantId") String tenantId,
                                                              @RequestParam("userId") String userId,
                                                              HttpServletRequest request) {
        if (null == tenantId) {
            return null;
        }
        List<SysTenantPack> list = sysTenantPackService.getPackListByTenantId(tenantId);
        List<String> userPackIdList = sysTenantPackService.getPackIdByUserIdAndTenantId(userId, oConvertUtils.getInt(tenantId));
        Map<String, Object> map = new HashMap<>(5);
        map.put("packList", list);
        map.put("userPackIdList", userPackIdList);
        return Result.ok(map);
    }
}
