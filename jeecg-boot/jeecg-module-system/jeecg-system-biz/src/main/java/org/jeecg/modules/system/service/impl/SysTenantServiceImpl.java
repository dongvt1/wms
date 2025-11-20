package org.jeecg.modules.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.dto.message.BusMessageDTO;
import org.jeecg.common.api.dto.message.MessageDTO;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.exception.JeecgBootBizTipException;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.PasswordUtil;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.constant.enums.SysAnnmentTypeEnum;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.aop.TenantLog;
import org.jeecg.modules.system.entity.*;
import org.jeecg.modules.system.mapper.*;
import org.jeecg.modules.system.service.ISysTenantPackService;
import org.jeecg.modules.system.service.ISysTenantService;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecg.modules.system.vo.tenant.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: Tenant implementation class
 * @author: jeecg-boot
 */
@Service("sysTenantServiceImpl")
@Slf4j
public class SysTenantServiceImpl extends ServiceImpl<SysTenantMapper, SysTenant> implements ISysTenantService {

    @Autowired
    ISysUserService userService;
    @Autowired
    private SysUserTenantMapper userTenantMapper;
    @Autowired
    private SysTenantMapper tenantMapper;

    @Autowired
    private ISysTenantPackService sysTenantPackService;

    @Autowired
    private SysTenantPackUserMapper sysTenantPackUserMapper;

    @Autowired
    private ISysBaseAPI sysBaseApi;
    
    @Autowired
    private SysUserDepartMapper sysUserDepartMapper;

    @Autowired
    private SysTenantPackMapper sysTenantPackMapper;
    
    @Autowired
    private SysPackPermissionMapper sysPackPermissionMapper;

    @Override
    public List<SysTenant> queryEffectiveTenant(Collection<Integer> idList) {
        if(oConvertUtils.listIsEmpty(idList)){
            return null;
        }
        
        LambdaQueryWrapper<SysTenant> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysTenant::getId, idList);
        queryWrapper.eq(SysTenant::getStatus, Integer.valueOf(CommonConstant.STATUS_1));
        //The query here ignores time conditions
        return super.list(queryWrapper);
    }

    @Override
    public Long countUserLinkTenant(String id) {
        LambdaQueryWrapper<SysUserTenant> query = new LambdaQueryWrapper<>();
        query.eq(SysUserTenant::getTenantId,id);
        query.eq(SysUserTenant::getStatus,CommonConstant.STATUS_1);
        // Find the number of connected users
        return userTenantMapper.selectCount(query);
    }

    @Override
    public boolean removeTenantById(String id) {
        // Find the number of connected users
        return super.removeById(Integer.parseInt(id));
    }

    @Override
    @CacheEvict(value={CacheConstant.SYS_USERS_CACHE}, allEntries=true)
    public void invitationUserJoin(String ids, String phone,String username) {
        String[] idArray = ids.split(SymbolConstant.COMMA);
        String userId = null;
        SysUser userByPhone = null;
        //update-begin---author:wangshuai ---date:20230313  for：【QQYUN-4605】Who to invite to join the tenant in the background，There is no way to select users who are not under the tenant.，Invite via mobile number------------
        if(oConvertUtils.isNotEmpty(phone)){
            userByPhone = userService.getUserByPhone(phone);
            //Indicates that the user does not exist
            if(null == userByPhone){
                throw new JeecgBootException("The current user does not exist，Please check mobile phone number");
            }
            userId = userByPhone.getId();
        }else{
            userByPhone = userService.getUserByName(username);
            //Indicates that the user does not exist
            if(null == userByPhone){
                throw new JeecgBootException("The current user does not exist，Please check mobile phone number");
            }
            userId = userByPhone.getId();
        }

        //recurring tenantid
        for (String id:idArray) {
            //update-begin---author:wangshuai ---date:20221223  for：[QQYUN-3371]Tenant logic transformation，Change to relational table------------
            //Get whether the invitee already exists
            SysUserTenant userTenant = userTenantMapper.getUserTenantByTenantId(userId, Integer.valueOf(id));
            if(null == userTenant){
                SysUserTenant relation = new SysUserTenant();
                relation.setUserId(userId);
                relation.setTenantId(Integer.valueOf(id));
                relation.setStatus(CommonConstant.USER_TENANT_NORMAL);
                userTenantMapper.insert(relation);
                //Add all packages under the tenant to the current user
                this.addPackUser(userId,id);
                //update-begin---author:wangshuai---date:2025-09-06---for:【QQYUN-13720】Invite users to join the tenant，No system reminder，remove---
                //Invite users to join the tenant，Send message
                this.sendInvitationTenantMessage(userByPhone,id);
                //update-end---author:wangshuai---date:2025-09-06---for:【QQYUN-13720】Invite users to join the tenant，No system reminder，remove---
            }else{
                //update-begin---author:wangshuai ---date:20230711  for：【QQYUN-5723】2、The user is already in the tenant，Ask again to prompt success，Should prompt the user already exists------------
                //update-begin---author:wangshuai ---date:20230724  for：【QQYUN-5885】Incorrect prompt for inviting users to join------------
                String tenantErrorInfo = getTenantErrorInfo(userTenant.getStatus());
                String errMsg = "Mobile phone number user:" + userByPhone.getPhone() + " Nick name：" + userByPhone.getRealname() + "，" + tenantErrorInfo;
                //update-end---author:wangshuai ---date:20230724  for：【QQYUN-5885】Incorrect prompt for inviting users to join------------
                throw new JeecgBootException(errMsg);
                //update-end---author:wangshuai ---date:20230711  for：【QQYUN-5723】2、The user is already in the tenant，Ask again to prompt success，Should prompt the user already exists------------  
            }
            //update-end---author:wangshuai ---date:20221223  for：[QQYUN-3371]Tenant logic transformation，Change to relational table------------
        //update-end---author:wangshuai ---date:20230313  for：【QQYUN-4605】Who to invite to join the tenant in the background，There is no way to select users who are not under the tenant.，Invite via mobile number------------
        }
    }

    /**
     * Send invitation to join tenant message with low code
     * 
     * @param user
     * @param id
     */
    private void sendInvitationTenantMessage(SysUser user, String id) {
        LoginUser sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();
        // send message
        SysTenant sysTenant = this.baseMapper.querySysTenant((Integer.valueOf(id)));
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setToAll(false);
        messageDTO.setToUser(user.getUsername());
        messageDTO.setFromUser("system");
        String title = sysUser.getRealname() + " You have been invited to join "+sysTenant.getName()+"。";
        messageDTO.setTitle(title);
        Map<String, Object> data = new HashMap<>();
        messageDTO.setData(data);
        messageDTO.setContent(title);
        messageDTO.setType("system");
        messageDTO.setCategory(CommonConstant.MSG_CATEGORY_1);
        sysBaseApi.sendSysAnnouncement(messageDTO);
    }

    @Override
    @CacheEvict(value={CacheConstant.SYS_USERS_CACHE}, allEntries=true)
    public void leaveTenant(String userIds, String tenantId) {
        String[] userIdArray = userIds.split(SymbolConstant.COMMA);
        for (String userId:userIdArray) {
            //update-begin---author:wangshuai ---date:20221223  for：[QQYUN-3371]Tenant logic transformation，Change to relational table------------
            LambdaQueryWrapper<SysUserTenant> query = new LambdaQueryWrapper<>();
            query.eq(SysUserTenant::getTenantId,tenantId);
            query.eq(SysUserTenant::getUserId,userId);
            userTenantMapper.delete(query);
            //update-end---author:wangshuai ---date:20221223  for：[QQYUN-3371]Tenant logic transformation，Change to relational table------------
            //update-begin---author:wangshuai---date:2025-09-06---for:【QQYUN-13720】Move the user out of the current tenant，No system reminder---
            // 给移除人员Send message
            SysTenantPackUser sysTenantPackUser = new SysTenantPackUser();
            sysTenantPackUser.setTenantId(Integer.valueOf(tenantId));
            sysTenantPackUser.setUserId(userId);
            sendMsgForDelete(sysTenantPackUser);
            //update-end---author:wangshuai---date:2025-09-06---for:【QQYUN-13720】Move the user out of the current tenant，No system reminder---
        }
        //Tenant removes user，Directly delete user tenant product packages
        sysTenantPackUserMapper.deletePackUserByTenantId(Integer.valueOf(tenantId),Arrays.asList(userIds.split(SymbolConstant.COMMA)));
    }

    @Override
    @CacheEvict(value={CacheConstant.SYS_USERS_CACHE}, allEntries=true)
    public Integer saveTenantJoinUser(SysTenant sysTenant, String userId) {
        //Add tenant
        this.saveTenant(sysTenant);
        
        // Add tenant产品包
        Integer tenantId = sysTenant.getId();
        sysTenantPackService.addDefaultTenantPack(tenantId);
        
        //Add tenant到关系surface
        return tenantId;
    }

    @Override
    public void saveTenant(SysTenant sysTenant){
        //Get tenantsid
        sysTenant.setId(this.tenantIdGenerate());
        sysTenant.setHouseNumber(RandomUtil.randomStringUpper(6));
        sysTenant.setDelFlag(CommonConstant.DEL_FLAG_0);
        this.save(sysTenant);
        //update-begin---author:wangshuai ---date:20230710  for：【QQYUN-5723】1、Add the current creator to the tenant relationship------------
        //of the currently logged in personid
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        this.saveTenantRelation(sysTenant.getId(),loginUser.getId());
        //update-end---author:wangshuai ---date:20230710  for：【QQYUN-5723】1、Add the current creator to the tenant relationship------------
    }

    @Override
    @CacheEvict(value={CacheConstant.SYS_USERS_CACHE}, allEntries=true)
    public Integer joinTenantByHouseNumber(SysTenant sysTenant, String userId) {
        LambdaQueryWrapper<SysTenant> query = new LambdaQueryWrapper<>();
        query.eq(SysTenant::getHouseNumber,sysTenant.getHouseNumber());
        SysTenant one = this.getOne(query);
        //Need to return tenantid(Used to update the cache in the foreground),return0It means that the current tenant’s house number does not exist.
        if(null == one){
            return 0;
        }else{
            LambdaQueryWrapper<SysUserTenant> relationQuery = new LambdaQueryWrapper<>();
            relationQuery.eq(SysUserTenant::getTenantId,one.getId());
            relationQuery.eq(SysUserTenant::getUserId,userId);
            SysUserTenant relation = userTenantMapper.selectOne(relationQuery);
            if(relation != null){
                String msg = "";
                if(CommonConstant.USER_TENANT_UNDER_REVIEW.equals(relation.getStatus())){
                    msg = ",state:Under review";
                }else if(CommonConstant.USER_TENANT_REFUSE.equals(relation.getStatus())){
                    throw new JeecgBootBizTipException("The administrator has denied you joining the tenant,请联系tenantadministrator");
                }else if(CommonConstant.USER_TENANT_QUIT.equals(relation.getStatus())){
                    msg = ",state:Resigned";
                }
                throw new JeecgBootBizTipException("You are already a member of this tenant"+msg);
            }
            //user加入门牌号Under reviewstate
            SysUserTenant tenant = new SysUserTenant();
            tenant.setTenantId(one.getId());
            tenant.setUserId(userId);
            tenant.setStatus(CommonConstant.USER_TENANT_UNDER_REVIEW);
            userTenantMapper.insert(tenant);

            // QQYUN-4526【application】Organization joining notification
            sendMsgForApplyJoinTenant(userId, one);
            return tenant.getTenantId();
        }
    }

    @Override
    public Integer countCreateTenantNum(String userId) {
        return this.userTenantMapper.countCreateTenantNum(userId);
    }

    @Override
    public IPage<SysTenant> getRecycleBinPageList(Page<SysTenant> page, SysTenant sysTenant) {
        return page.setRecords(tenantMapper.getRecycleBinPageList(page,sysTenant));
    }

    @Override
    @CacheEvict(value={CacheConstant.SYS_USERS_CACHE}, allEntries=true)
    public void deleteTenantLogic(String ids) {
        String[] idArray = ids.split(SymbolConstant.COMMA);
        List<Integer> list = new ArrayList<>();
        //Convert tointtype
        for (String id:idArray) {
            list.add(Integer.valueOf(id));
        }
        //Delete tenant
        tenantMapper.deleteByTenantId(list);
        //update-begin---author:wangshuai ---date:20230710  for：【QQYUN-5723】3、Tenant is completely deleted，The user tenant relationship table also needs to be deleted------------
        //Delete tenant下ofuser
        userTenantMapper.deleteUserByTenantId(list);
        //update-ennd---author:wangshuai ---date:20230710  for：【QQYUN-5723】3、Tenant is completely deleted，The user tenant relationship table also needs to be deleted------------

        //update-begin---author:wangshuai ---date:20230710  for：【QQYUN-5723】3、Tenant is completely deleted，The user tenant relationship table also needs to be deleted------------
        //Delete tenant下of产品包
        this.deleteTenantPackByTenantId(list);
        //update-ennd---author:wangshuai ---date:20230710  for：【QQYUN-5723】3、Tenant is completely deleted，The user tenant relationship table also needs to be deleted------------
    }

    @Override
    public void revertTenantLogic(String ids) {
        String[] idArray = ids.split(SymbolConstant.COMMA);
        List<Integer> list = new ArrayList<>();
        //Convert tointtype
        for (String id:idArray) {
            list.add(Integer.valueOf(id));
        }
        //Restore tenant
        tenantMapper.revertTenantLogic(list);
    }

    /**
     * Add tenant到关系surface
     * @param tenantId
     * @param userId
     */
    @CacheEvict(value={CacheConstant.SYS_USERS_CACHE}, allEntries=true)
    public Integer saveTenantRelation(Integer tenantId,String userId) {
        SysUserTenant relation = new SysUserTenant();
        relation.setTenantId(tenantId);
        relation.setUserId(userId);
        relation.setStatus(CommonConstant.USER_TENANT_NORMAL);
        userTenantMapper.insert(relation);
        return relation.getTenantId();
    }

    /**
     * Get tenantsid
     * @return
     */
   public int tenantIdGenerate(){
       synchronized (this){
           //Get the maximum valueid
           //update-begin---author:wangshuai ---date:20230424  for：When the database does not have a tenant，If it is empty, an error will be reportedsqlreturntype不匹配------------
           int maxTenantId = oConvertUtils.getInt(tenantMapper.getMaxTenantId(),0);
           //update-end---author:wangshuai ---date:20230424  for：When the database does not have a tenant，If it is empty, an error will be reportedsqlreturntype不匹配------------
           if(maxTenantId >= 1000){
               return maxTenantId + 1;
           }else{
               return 1000;
           }
       }
   }


    @Override
    public void exitUserTenant(String userId, String username, String tenantId) {
        int tId = Integer.parseInt(tenantId);
        //Get all tenant information
        List<String> userIdsByTenantId = userTenantMapper.getUserIdsByTenantId(tId);
        //Query whether the current tenant is the owner
        SysTenant sysTenant = tenantMapper.selectById(tId);
        //If it is owned
        if (username.equals(sysTenant.getCreateBy())) {
            //Determine the number of bits of current tenant information
            if (null != userIdsByTenantId && userIdsByTenantId.size() > 1) {
                //Need to assign owner
                throw new JeecgBootException("assignedOwen");
            } else if (null != userIdsByTenantId && userIdsByTenantId.size() == 1) {
                //update-begin---author:wangshuai ---date:20230426  for：【QQYUN-5270】After all the tenants under my name have withdrawn，Log in again，Prompt tenants to freeze all------------
                //When there is only the owner, you need to cancel the tenant
                throw new JeecgBootException("cancelTenant");
                //update-end---author:wangshuai ---date:20230426  for：【QQYUN-5270】After all the tenants under my name have withdrawn，Log in again，Prompt tenants to freeze all------------
            } else {
                throw new JeecgBootException("Failed to exit tenant，Tenant information no longer exists");
            }
        } else {
            //If you are not the owner, delete it directly.
            this.leaveTenant(userId, tenantId);
            this.leveUserProcess(userId, tenantId);
        }
    }

    @Override
    public void changeOwenUserTenant(String userId, String tId) {
        //Query whether the current user exists under the tenant
        //update-begin---author:wangshuai ---date:20230705  for：tenantidIt should have been passed on，不应该是当前tenantof------------
        int tenantId = oConvertUtils.getInt(tId, 0);
        SysTenant sysTenant = tenantMapper.selectById(tenantId);
        if(null == sysTenant){
            throw new JeecgBootException("Failed to exit tenant，不存在此tenant");
        }
        String createBy = sysTenant.getCreateBy();
        //update-end---author:wangshuai ---date:20230705  for：tenantidIt should have been passed on，不应该是当前tenantof------------
        Integer count = userTenantMapper.userTenantIzExist(userId, tenantId);
        if (count == 0) {
            throw new JeecgBootException("Failed to exit tenant，此tenant下没有该user");
        }
        //Get user information
        SysUser user = userService.getById(userId);
        //Change owner
        SysTenant tenant = new SysTenant();
        tenant.setCreateBy(user.getUsername());
        tenant.setId(tenantId);
        tenantMapper.updateById(tenant);
        //删除Currently logged in useroftenant信息
        //update-begin---author:wangshuai ---date:20230705  for：After the old owner quits，需要将就拥有者ofusertenant关系改成Resigned------------
        //Get the user of the original creatorid
        SysUser userByName = userService.getUserByName(createBy);
        LambdaQueryWrapper<SysUserTenant> query = new LambdaQueryWrapper<>();
        query.eq(SysUserTenant::getUserId,userByName.getId());
        query.eq(SysUserTenant::getTenantId,tenantId);
        SysUserTenant userTenant = new SysUserTenant();
        userTenant.setStatus(CommonConstant.USER_TENANT_QUIT);
        userTenantMapper.update(userTenant,query);
        //update-end---author:wangshuai ---date:20230705  for：After the old owner quits，需要将就拥有者ofusertenant关系改成Resigned------------
        //Resignation process
        this.leveUserProcess(userId, String.valueOf(tenantId));
    }

    /**
     * 触发Resignation process
     *
     * @param userId
     * @param tenantId
     * @param tenantId
     */
    private void leveUserProcess(String userId, String tenantId) {
        LoginUser userInfo = new LoginUser();
        SysUser user = userService.getById(userId);
    }

    @Override
    public Result<String> invitationUser(String phone, String departId) {
        Result<String> result = new Result<>();
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();

        //1、Query user information,Determine whether the user exists
        SysUser userByPhone = userService.getUserByPhone(phone);
        if(null == userByPhone){
            result.setSuccess(false);
            result.setMessage("User does not exist");
            return result;
        }
        int tenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);

        //2.判断当前邀请人是否存在tenant列surface中
        Integer userCount = userTenantMapper.userTenantIzExist(sysUser.getId(), tenantId);
        if(userCount == 0){
            result.setSuccess(false);
            result.setMessage("The current administrator does not have invitation permission");
            return result;
        }

        //3.插入到tenant信息，Existing ones are not allowed to be inserted.
        //Get whether the invitee already exists
        SysUserTenant sysUserTenant = userTenantMapper.getUserTenantByTenantId(userByPhone.getId(), tenantId);
        //User already exists
        if(null != sysUserTenant){
            result.setSuccess(false);
            String tenantErrorInfo = getTenantErrorInfo(sysUserTenant.getStatus());
            String msg = "Mobile phone number user:" + userByPhone.getPhone() + " Nick name：" + userByPhone.getRealname() + "，" + tenantErrorInfo;
            result.setMessage(msg);
            return result;
        }

        //4.Requires user manual consent to join
        String status = CommonConstant.USER_TENANT_INVITE;

        //5.The current user does not existtenant中,就需要将user添加到tenant中
        SysUserTenant tenant = new SysUserTenant();
        tenant.setTenantId(tenantId);
        tenant.setUserId(userByPhone.getId());
        tenant.setStatus(status);
        userTenantMapper.insert(tenant);
        result.setSuccess(true);
        result.setMessage("Invite members successfully，Members can join only if they agree");

        //update-begin---author:wangshuai ---date:20230329  for：[QQYUN-4671]Departments and Users，Mobile phone number invitation，Not under the current department，Currently across the organization------------
        //6.Save user department relationships
        if(oConvertUtils.isNotEmpty(departId)){
            //Save user department relationships
            this.saveUserDepart(userByPhone.getId(),departId);
        }
        //update-end---author:wangshuai ---date:20230329  for：[QQYUN-4671]Departments and Users，Mobile phone number invitation，Not under the current department，Currently across the organization------------
        
        //  QQYUN-4527【application】Invite members to join the organization，Send message提醒
        sendMsgForInvitation(userByPhone, tenantId, sysUser.getRealname());
        return result;
    }

    @Override
    public TenantDepartAuthInfo getTenantDepartAuthInfo(Integer tenantId) {
        SysTenant sysTenant = this.getById(tenantId);
        if(sysTenant==null) {
            return null;
        }

        TenantDepartAuthInfo info = new TenantDepartAuthInfo();
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String userId = sysUser.getId();
        boolean superAdmin = false;
        // Querypacksurface
        List<String> packCodeList = baseMapper.queryUserPackCode(tenantId, userId);
        if(packCodeList==null || packCodeList.size()==0){
            //if no data 判断tenant创建人是不是当前user
            if(sysTenant.getCreateBy().equals(sysUser.getUsername())){
                sysTenantPackService.addDefaultTenantPack(tenantId);
                superAdmin = true;
            }else{
                superAdmin = false;
            }
        }
        if(superAdmin == false){
            List<TenantPackUserCount> packCountList = baseMapper.queryTenantPackUserCount(tenantId);
            info.setPackCountList(packCountList);
        }
        info.setSysTenant(sysTenant);
        info.setSuperAdmin(superAdmin);
        info.setPackCodes(packCodeList);
        return info;
    }

    @Override
    public List<TenantPackUserCount> queryTenantPackUserCount(Integer tenantId) {
        return baseMapper.queryTenantPackUserCount(tenantId);
    }

    @Override
    public TenantPackModel queryTenantPack(TenantPackModel model) {
        Integer tenantId = model.getTenantId();
        String packCode = model.getPackCode();

        SysTenantPack sysTenantPack = sysTenantPackService.getSysTenantPack(tenantId, packCode);
        if(sysTenantPack!=null){
            TenantPackModel tenantPackModel = new TenantPackModel();
            tenantPackModel.setPackName(sysTenantPack.getPackName());
            tenantPackModel.setPackId(sysTenantPack.getId());
            // Queryuser
            List<TenantPackUser> userList = getTenantPackUserList(tenantId, sysTenantPack.getId(), 1);
            tenantPackModel.setUserList(userList);
            return tenantPackModel;
        }
        return null;
    }

    @Override
    public void addBatchTenantPackUser(SysTenantPackUser sysTenantPackUser) {
        String userIds = sysTenantPackUser.getUserId();
        if(oConvertUtils.isNotEmpty(userIds)){
            ISysTenantService currentService = SpringContextUtils.getApplicationContext().getBean(ISysTenantService.class);
            String realNames = sysTenantPackUser.getRealname();
            String[] userIdArray = userIds.split(",");
            String[] realNameArray = realNames.split(",");
            for(int i=0;i<userIdArray.length;i++){
                String userId = userIdArray[i];
                String realName = realNameArray[i];
                SysTenantPackUser entity = new SysTenantPackUser(sysTenantPackUser, userId, realName);
                LambdaQueryWrapper<SysTenantPackUser> query = new LambdaQueryWrapper<SysTenantPackUser>()
                        .eq(SysTenantPackUser::getTenantId, entity.getTenantId())
                        .eq(SysTenantPackUser::getPackId, entity.getPackId())
                        .eq(SysTenantPackUser::getUserId, entity.getUserId());
                SysTenantPackUser packUser = sysTenantPackUserMapper.selectOne(query);
                if(packUser==null || packUser.getId()==null){
                    currentService.addTenantPackUser(entity);
                }else{
                    if(packUser.getStatus()==0){
                        packUser.setPackName(entity.getPackName());
                        packUser.setRealname(realName);
                        currentService.addTenantPackUser(packUser);
                    }
                }
            }
        }
    }

    @TenantLog(2)
    @Override
    public void addTenantPackUser(SysTenantPackUser sysTenantPackUser) {
        if(sysTenantPackUser.getId()==null){
            sysTenantPackUserMapper.insert(sysTenantPackUser);
        }else{
            sysTenantPackUser.setStatus(1);
            sysTenantPackUserMapper.updateById(sysTenantPackUser);
        }
    }

    @TenantLog(4)
    @Override
    public void deleteTenantPackUser(SysTenantPackUser sysTenantPackUser) {
        LambdaQueryWrapper<SysTenantPackUser> query = new LambdaQueryWrapper<SysTenantPackUser>()
                .eq(SysTenantPackUser::getUserId, sysTenantPackUser.getUserId())
                .eq(SysTenantPackUser::getPackId, sysTenantPackUser.getPackId());
        sysTenantPackUserMapper.delete(query);

        // QQYUN-4525【Organizational management】administrator 2.administrator权限被移除时，给移除人员Send message
        sendMsgForDelete(sysTenantPackUser);
    }

    @Override
    public List<TenantPackUser> getTenantPackApplyUsers(Integer tenantId) {
        return getTenantPackUserList(tenantId, null, 0);
    }

    /**
     * Get tenants下 Users of a product package
     * or is tenant下产品包of申请user
     * @param tenantId
     * @param packId
     * @param packUserStatus
     * @return
     */
    private List<TenantPackUser> getTenantPackUserList(Integer tenantId, String packId, Integer packUserStatus){
        // Queryuser
        List<TenantPackUser> userList = baseMapper.queryPackUserList(tenantId, packId, packUserStatus);
        if(userList!=null && userList.size()>0){
            List<String> userIdList = userList.stream().map(i->i.getId()).collect(Collectors.toList());
            // department
            List<UserDepart> depList = baseMapper.queryUserDepartList(userIdList);
            // Position TODO
            // Iterate over users Add to users department信息和Position信息
            for(TenantPackUser user: userList){
                for(UserDepart dep: depList){
                    if(user.getId().equals(dep.getUserId())){
                        user.addDepart(dep.getDepartName());
                    }
                }
            }
        }
        return userList;
    }

    @Override
    public void doApplyTenantPackUser(SysTenantPackUser sysTenantPackUser) {
        LambdaQueryWrapper<SysTenantPack> query1 = new LambdaQueryWrapper<SysTenantPack>()
                .eq(SysTenantPack::getTenantId, sysTenantPackUser.getTenantId())
                .eq(SysTenantPack::getPackCode, sysTenantPackUser.getPackCode());
        SysTenantPack pack = sysTenantPackService.getOne(query1);
        if(pack!=null){
            sysTenantPackUser.setStatus(0);
            sysTenantPackUser.setPackId(pack.getId());
            LambdaQueryWrapper<SysTenantPackUser> query = new LambdaQueryWrapper<SysTenantPackUser>()
                    .eq(SysTenantPackUser::getTenantId, sysTenantPackUser.getTenantId())
                    .eq(SysTenantPackUser::getPackId, sysTenantPackUser.getPackId())
                    .eq(SysTenantPackUser::getUserId, sysTenantPackUser.getUserId());
            Long count = sysTenantPackUserMapper.selectCount(query);
            if(count==null || count==0){
                sysTenantPackUserMapper.insert(sysTenantPackUser);
            }
            // QQYUN-4524【organizational affiliation】administrator 1.administrator权限申请-> 给相关administrator Send notification message
            sendMsgForApply(sysTenantPackUser.getUserId(), pack);
        }
    }

    /**
     * 申请administrator权限send message
     * @param userId
     * @param pack
     */
    private void sendMsgForApply(String userId, SysTenantPack pack){
        // send message
        SysUser user = userService.getById(userId);
        Integer tenantId = pack.getTenantId();
        SysTenant sysTenant = this.baseMapper.querySysTenant(tenantId);
        String packCode = pack.getPackCode();

        List<String> packCodeList = Arrays.asList(packCode.split(","));
        List<String> userList = sysTenantPackUserMapper.queryTenantPackUserNameList(tenantId, packCodeList);
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setToAll(false);
        messageDTO.setToUser(String.join(",", userList));
        messageDTO.setFromUser("system");
        String title = user.getRealname()+" Apply to join "+sysTenant.getName()+" of"+pack.getPackName()+"of成员。";
        messageDTO.setTitle(title);
        Map<String, Object> data = new HashMap<>();
        messageDTO.setData(data);
        messageDTO.setContent(title);
        messageDTO.setType("system");
        sysBaseApi.sendTemplateMessage(messageDTO);
    }

    /**
     * 移除administrator权限send message
     * @param sysTenantPackUser
     */
    private void sendMsgForDelete(SysTenantPackUser sysTenantPackUser){
        // send message
        SysUser user = userService.getById(sysTenantPackUser.getUserId());
        SysTenant sysTenant = this.baseMapper.querySysTenant(sysTenantPackUser.getTenantId());
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setToAll(false);
        messageDTO.setToUser(user.getUsername());
        //Remove reminders that users have made under low code，user移出没有套餐包名称of概念
        String title = "";
        if(oConvertUtils.isNotEmpty(sysTenantPackUser.getPackName())){
            title = "You have been "+loginUser.getRealname()+" from "+sysTenant.getName()+"of"+sysTenantPackUser.getPackName()+"removed from。";
        } else {
            title = "You have been "+loginUser.getRealname()+" from "+sysTenant.getName() + "removed from。";
        }
        messageDTO.setTitle(title);
        messageDTO.setFromUser("system");
        Map<String, Object> data = new HashMap<>();
        data.put("realname", loginUser.getRealname());
        data.put("tenantName", sysTenant.getName());
        data.put("packName", sysTenantPackUser.getPackName());
        messageDTO.setData(data);
        messageDTO.setType("system");
        messageDTO.setContent(title);
        sysBaseApi.sendTemplateMessage(messageDTO);
    }

    /**
     * Apply to join the organization send message
     * @param userId
     * @param sysTenant
     */
    private void sendMsgForApplyJoinTenant(String userId, SysTenant sysTenant){
        // send message
        SysUser user = userService.getById(userId);
        // 给超级administrator 和Organizational management员send message
        String codes = "superAdmin,accountAdmin";
        List<String> packCodeList = Arrays.asList(codes.split(","));
        List<String> userList = sysTenantPackUserMapper.queryTenantPackUserNameList(sysTenant.getId(), packCodeList);
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setToAll(false);
        messageDTO.setToUser(String.join(",", userList));
        messageDTO.setFromUser("system");
        String title = user.getRealname()+" Apply to join "+sysTenant.getName()+"。";
        messageDTO.setTitle(title);
        Map<String, Object> data = new HashMap<>();
        messageDTO.setData(data);
        messageDTO.setType("system");
        messageDTO.setContent(title);
        sysBaseApi.sendTemplateMessage(messageDTO);
    }

    /**
     *  Invite members send message
     * @param user
     * @param tenantId
     * @param realname
     */
    private void sendMsgForInvitation(SysUser user, Integer tenantId, String realname){
        // send message
        SysTenant sysTenant = this.baseMapper.querySysTenant(tenantId);
        BusMessageDTO messageDTO = new BusMessageDTO();
        messageDTO.setToAll(false);
        messageDTO.setToUser(user.getUsername());
        messageDTO.setFromUser("system");
        //update-begin---author:wangshuai ---date:20230706  for：【QQYUN-5730】tenant邀请加入提示消息应该显示邀请人of名字------------
        String title = realname + " invite you to join "+sysTenant.getName()+"。";
        //update-end---author:wangshuai ---date:20230706  for：【QQYUN-5730】tenant邀请加入提示消息应该显示邀请人of名字------------
        messageDTO.setTitle(title);
        Map<String, Object> data = new HashMap<>();
        messageDTO.setData(data);
        messageDTO.setContent(title);
        messageDTO.setType("system");
        //update-begin---author:wangshuai---date:2023-11-24---for:【QQYUN-7168】Invite members时，Will report an error，But the invitation has actually been successful---
        messageDTO.setCategory(CommonConstant.MSG_CATEGORY_1);
        //update-end---author:wangshuai---date:2023-11-24---for:【QQYUN-7168】Invite members时，Will report an error，But the invitation has actually been successful---
        //update-begin---author:wangshuai ---date:20230721  for：【QQYUN-5726】邀请加入tenant加个按钮直接跳转过去------------
        messageDTO.setBusType(SysAnnmentTypeEnum.TENANT_INVITE.getType());
        sysBaseApi.sendBusAnnouncement(messageDTO);
        //update-end---author:wangshuai ---date:20230721  for：【QQYUN-5726】邀请加入tenant加个按钮直接跳转过去------------
    }


    @Override
    public void passApply(SysTenantPackUser sysTenantPackUser) {
        LambdaQueryWrapper<SysTenantPackUser> query = new LambdaQueryWrapper<SysTenantPackUser>()
                .eq(SysTenantPackUser::getTenantId, sysTenantPackUser.getTenantId())
                .eq(SysTenantPackUser::getPackId, sysTenantPackUser.getPackId())
                .eq(SysTenantPackUser::getUserId, sysTenantPackUser.getUserId());
        SysTenantPackUser packUser = sysTenantPackUserMapper.selectOne(query);
        if(packUser!=null && packUser.getId()!=null && packUser.getStatus()==0){
            ISysTenantService currentService = SpringContextUtils.getApplicationContext().getBean(ISysTenantService.class);
            packUser.setPackName(sysTenantPackUser.getPackName());
            packUser.setRealname(sysTenantPackUser.getRealname());
            currentService.addTenantPackUser(packUser);
            
            //update-begin---author:wangshuai ---date:20230328  for：[QQYUN-4674]tenantadministrator同意或拒绝成员没有系统通知------------
            //超级administrator成功加入发送系统消息
            SysTenant sysTenant = tenantMapper.selectById(sysTenantPackUser.getTenantId());
            String content = " You have successfully joined"+sysTenant.getName()+"of超级administratorof成员。";
            SysUser sysUser = userService.getById(sysTenantPackUser.getUserId());
            this.sendMsgForAgreeAndRefuseJoin(sysUser,content);
            //update-end---author:wangshuai ---date:20230328  for：[QQYUN-4674]tenantadministrator同意或拒绝成员没有系统通知------------
        }
    }

    @Override
    public void deleteApply(SysTenantPackUser sysTenantPackUser) {
        LambdaQueryWrapper<SysTenantPackUser> query = new LambdaQueryWrapper<SysTenantPackUser>()
                .eq(SysTenantPackUser::getTenantId, sysTenantPackUser.getTenantId())
                .eq(SysTenantPackUser::getPackId, sysTenantPackUser.getPackId())
                .eq(SysTenantPackUser::getUserId, sysTenantPackUser.getUserId());
        SysTenantPackUser packUser = sysTenantPackUserMapper.selectOne(query);
        if(packUser!=null && packUser.getId()!=null && packUser.getStatus()==0){
            sysTenantPackUserMapper.deleteById(packUser.getId());
            //update-begin---author:wangshuai ---date:20230328  for：[QQYUN-4674]tenantadministrator同意或拒绝成员没有系统通知------------
            //超级administrator拒绝加入发送系统消息
            SysTenant sysTenant = tenantMapper.selectById(sysTenantPackUser.getTenantId());
            String content = " administrator已拒绝您加入"+sysTenant.getName()+"of超级administratorof成员请求。";
            SysUser sysUser = userService.getById(sysTenantPackUser.getUserId());
            this.sendMsgForAgreeAndRefuseJoin(sysUser,content);
            //update-end---author:wangshuai ---date:20230328  for：[QQYUN-4674]tenantadministrator同意或拒绝成员没有系统通知------------
        }
    }

    @Override
    public IPage<TenantPackUser> queryTenantPackUserList(String tenantId, String packId,Integer status, Page<TenantPackUser> page) {
        // Queryuser
        List<TenantPackUser> userList = baseMapper.queryTenantPackUserList(page,tenantId, packId,status);
        // Get产品包下userdepartment和Position
        userList = getPackUserPositionAndDepart(userList);
        return page.setRecords(userList);
    }

    /**
     * GetuserPosition和department
     * @param userList
     * @return
     */
    private List<TenantPackUser> getPackUserPositionAndDepart(List<TenantPackUser> userList) {
        if(userList!=null && userList.size()>0){
            List<String> userIdList = userList.stream().map(i->i.getId()).collect(Collectors.toList());
            // department
            List<UserDepart> depList = baseMapper.queryUserDepartList(userIdList);
            // Position
            List<UserPosition> userPositions = baseMapper.queryUserPositionList(userIdList);
            // Iterate over users Add to users department信息和Position信息
            for (TenantPackUser user : userList) {
                //添加department
                for (UserDepart dep : depList) {
                    if (user.getId().equals(dep.getUserId())) {
                        user.addDepart(dep.getDepartName());
                    }
                }
                //添加Position
                for (UserPosition userPosition : userPositions) {
                    if (user.getId().equals(userPosition.getUserId())) {
                        user.addPosition(userPosition.getPositionName());
                    }
                }
            }
        }
        return userList;
    }


    /**
     * Save user department relationships
     * @param userId
     * @param departId
     */
    private void saveUserDepart(String userId, String departId) {
        //According to userid和departmentidGet quantity,用于查看user是否存在userdepartment关系surface中
        Long count = sysUserDepartMapper.getCountByDepartIdAndUserId(userId,departId);
        if(count == 0){
            SysUserDepart sysUserDepart = new SysUserDepart(userId,departId);
            sysUserDepartMapper.insert(sysUserDepart);
        }
    }
    
    @Override
    public Long getApplySuperAdminCount() {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        int tenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);
        return baseMapper.getApplySuperAdminCount(sysUser.getId(),tenantId);
    }

    /**
     * 同意或拒绝加入超级administrator send message
     * @param user
     * @param content
     */
    public void sendMsgForAgreeAndRefuseJoin(SysUser user, String content){
        // send message
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setToAll(false);
        messageDTO.setToUser(user.getUsername());
        messageDTO.setFromUser("system");
        messageDTO.setTitle(content);
        Map<String, Object> data = new HashMap<>();
        messageDTO.setData(data);
        messageDTO.setContent(content);
        messageDTO.setType("system");
        sysBaseApi.sendTemplateMessage(messageDTO);
    }

    /**
     * Get tenants错误提示信息
     *
     * @param status
     * @return
     */
    private String getTenantErrorInfo(String status) {
        String content = "已在tenant中，No invitation required！";
        if (CommonConstant.USER_TENANT_QUIT.equals(status)) {
            content = "Resigned！";
        } else if (CommonConstant.USER_TENANT_UNDER_REVIEW.equals(status)) {
            content = "tenantadministratorUnder review！";
        } else if (CommonConstant.USER_TENANT_REFUSE.equals(status)) {
            content = "tenantadministrator已拒绝！";
        } else if (CommonConstant.USER_TENANT_INVITE.equals(status)) {
            content = "has been invited，Subject to user consent！";
        }
        return content;
    }

    /**
     * Delete tenant下of产品包
     *
     * @param tenantIdList
     */
    private void deleteTenantPackByTenantId(List<Integer> tenantIdList) {
        //1.Delete product package下ofuser
        sysTenantPackUserMapper.deletePackUserByTenantIds(tenantIdList);
        //2.Delete product package对应of菜单权限
        sysPackPermissionMapper.deletePackPermByTenantIds(tenantIdList);
        //3.Delete product package
        sysTenantPackMapper.deletePackByTenantIds(tenantIdList);
    }

    @Override
    public void deleteUserByPassword(SysUser sysUser, Integer tenantId) {
        //被删除人ofuserid
        String userId = sysUser.getId();
        //被删除人of密码
        String password = sysUser.getPassword();
        //Currently logged in user
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        //step1 判断当前user是否为当前tenantofadministrator(只有超级administrator和账号administratorCan be deleted)
        Long isHaveAdmin = sysTenantPackUserMapper.izHaveBuyAuth(user.getId(), tenantId);
        if(null == isHaveAdmin || 0 == isHaveAdmin){
            throw new JeecgBootException("您不是当前组织ofadministrator，Unable to delete user！");
        }
        //step2 离职state下，and without other organizations，Can be deleted
        SysUserTenant sysUserTenant = userTenantMapper.getUserTenantByTenantId(userId, tenantId);
        if(null == sysUserTenant || !CommonConstant.USER_TENANT_QUIT.equals(sysUserTenant.getStatus())){
            throw new JeecgBootException("The user has not resigned，Delete not allowed！"); 
        }
        List<Integer> tenantIdsByUserId = userTenantMapper.getTenantIdsByUserId(userId);
        if(CollectionUtils.isNotEmpty(tenantIdsByUserId) && tenantIdsByUserId.size()>0){
            throw new JeecgBootException("user尚有未退出of组织，cannot be deleted！");
        }
        //step3 当天创建ofuser和创建人Can be deleted
        SysUser sysUserData = userService.getById(userId);
        if(!sysUserData.getCreateBy().equals(user.getUsername())){
            throw new JeecgBootException("您不是该userof创建人，cannot be deleted！");
        }
        
        //update-begin---author:wangshuai---date:2025-04-11---for:【QQYUN-11839】Delete user，需要输入被Delete userof密码，Is this logic correct?？不应该是administratorof密码吗---
        this.verifyCreateTimeAndPassword(sysUserData,password);
        //update-end---author:wangshuai---date:2025-04-11---for:【QQYUN-11839】Delete user，需要输入被Delete userof密码，Is this logic correct?？不应该是administratorof密码吗---

        //step5 逻辑Delete user
        userService.deleteUser(userId);
        //step6 真实Delete user
        userService.removeLogicDeleted(Collections.singletonList(userId));
    }

    /**
     * Verify creation time and password
     * 
     * @param sysUser
     * @param password
     */
    private void verifyCreateTimeAndPassword(SysUser sysUser,String password) {
        if(null == sysUser){
            throw new JeecgBootException("该User does not exist，cannot be deleted！");
        }
        //step1 Verify creation time
        //Currently logged in user
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        Date createTime = sysUser.getCreateTime();
        boolean sameDay = DateUtils.isSameDay(createTime, new Date());
        if(!sameDay){
            throw new JeecgBootException("user不是今天创建of，cannot be deleted！");
        }
        //step2 Verify password
        //Getadminofuser
        SysUser adminUser = userService.getById(user.getId());
        String passwordEncode = PasswordUtil.encrypt(adminUser.getUsername(), password, adminUser.getSalt());
        if(!passwordEncode.equals(adminUser.getPassword())){
            throw new JeecgBootException("您输入of密码不正确，cannot be deleted该user！");
        }
    }

    @Override
    public List<SysTenant> getTenantListByUserId(String userId) {
        return tenantMapper.getTenantListByUserId(userId);
    }

    @Override
    public void deleteUser(SysUser sysUser, Integer tenantId) {
        //被删除人ofuserid
        String userId = sysUser.getId();
        //被删除人of密码
        String password = sysUser.getPassword();
        //Currently logged in user
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        //step1 判断当前user是否为当前tenantof创建者才Can be deleted
        SysTenant sysTenant = this.getById(tenantId);
        if(null == sysTenant || !user.getUsername().equals(sysTenant.getCreateBy())){
            throw new JeecgBootException("您不是当前组织of创建者，Unable to delete user！");
        }
        //step2 Determine whether there are other organizations joined besides the current organization
        LambdaQueryWrapper<SysUserTenant> query = new LambdaQueryWrapper<>();
        query.eq(SysUserTenant::getUserId,userId);
        query.ne(SysUserTenant::getTenantId,tenantId);
        List<SysUserTenant> sysUserTenants = userTenantMapper.selectList(query);
        if(CollectionUtils.isNotEmpty(sysUserTenants)){
            throw new JeecgBootException("This user also exists in other organizations，Unable to delete user！");
        }
        //step3 Verify creation time and password
        SysUser sysUserData = userService.getById(userId);
        this.verifyCreateTimeAndPassword(sysUserData,password);
        //step4 真实Delete user
        userService.deleteUser(userId);
        userService.removeLogicDeleted(Collections.singletonList(userId));
    }

    /**
     * 为userAdd tenant下所有套餐
     *
     * @param userId   userid
     * @param tenantId tenantid
     */
    public void addPackUser(String userId, String tenantId) {
        //根据tenantid和产品包ofcodeGet tenants套餐id
        List<String> packIds = sysTenantPackMapper.getPackIdByPackCodeAndTenantId(oConvertUtils.getInt(tenantId));
        if (CollectionUtil.isNotEmpty(packIds)) {
            for (String packId : packIds) {
                SysTenantPackUser sysTenantPackUser = new SysTenantPackUser();
                sysTenantPackUser.setUserId(userId);
                sysTenantPackUser.setTenantId(oConvertUtils.getInt(tenantId));
                sysTenantPackUser.setPackId(packId);
                sysTenantPackUser.setStatus(CommonConstant.STATUS_1_INT);
                try {
                    this.addTenantPackUser(sysTenantPackUser);
                } catch (Exception e) {
                    log.warn("添加user套餐包失败，reason：" + e.getMessage());
                }
            }
        }
    }
}
