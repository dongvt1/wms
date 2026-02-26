package org.jeecg.modules.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.system.entity.SysTenant;
import org.jeecg.modules.system.entity.SysTenantPackUser;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.vo.tenant.TenantDepartAuthInfo;
import org.jeecg.modules.system.vo.tenant.TenantPackModel;
import org.jeecg.modules.system.vo.tenant.TenantPackUser;
import org.jeecg.modules.system.vo.tenant.TenantPackUserCount;

import java.util.Collection;
import java.util.List;

/**
 * @Description: tenantserviceinterface
 * @author: jeecg-boot
 */
public interface ISysTenantService extends IService<SysTenant> {

    /**
     * 查询有效的tenant
     *
     * @param idList
     * @return
     */
    List<SysTenant> queryEffectiveTenant(Collection<Integer> idList);

    /**
     * 返回某个tenant被多少个用户引用了
     *
     * @param id
     * @return
     */
    Long countUserLinkTenant(String id);

    /**
     * according toID删除tenant，Will determine whether it has been referenced
     *
     * @param id
     * @return
     */
    boolean removeTenantById(String id);

    /**
     * 邀请用户加入tenant,Via mobile number
     * @param ids
     * @param phone
     * @param username
     */
    void invitationUserJoin(String ids, String phone,String username);

    /**
     * Please leave the user（tenant）
     * @param userIds
     * @param tenantId
     */
    void leaveTenant(String userIds, String tenantId);

    /**
     * 添加tenant，And add the created user to the relationship table
     * @param sysTenant
     * @param userId
     */
    Integer saveTenantJoinUser(SysTenant sysTenant, String userId);

    /**
     * 保存tenant
     * @param sysTenant
     */
    void saveTenant(SysTenant sysTenant);

    /**
     * 通过门牌号加入tenant
     * @param sysTenant
     * @param userId
     */
    Integer joinTenantByHouseNumber(SysTenant sysTenant, String userId);

    /**
     * 统计一personal创建了多少个tenant
     * 
     * @param userId
     * @return
     */
    Integer countCreateTenantNum(String userId);

    /**
     * Gettenant回收站的数据
     * @param page
     * @param sysTenant
     * @return
     */
    IPage<SysTenant> getRecycleBinPageList(Page<SysTenant> page, SysTenant sysTenant);

    /**
     * 彻底删除tenant
     * @param ids
     */
    void deleteTenantLogic(String ids);

    /**
     * 还原tenant
     * @param ids
     */
    void revertTenantLogic(String ids);

    /**
     * 退出tenant
     * @param userId
     * @param userId
     * @param username
     */
    void exitUserTenant(String userId, String username, String tenantId);

    /**
     * 变更tenant拥有者
     * @param userId
     * @param tenantId
     */
    void changeOwenUserTenant(String userId, String tenantId);

    /**
     * 邀请用户到tenant。Via mobile number匹配
     * @param phone
     * @param departId
     * @return
     */
    Result<String> invitationUser(String phone, String departId);

    /**
     * Enter the application organization page Check if the user has Super administrator privileges
     * @param tenantId
     * @return
     */
    TenantDepartAuthInfo getTenantDepartAuthInfo(Integer tenantId);


    /**
     * Get tenant产品包-3defaultadminnumber of personnel
     * @param tenantId
     * @return
     */
    List<TenantPackUserCount> queryTenantPackUserCount(Integer tenantId);

    /**
     * 查询tenant产品包信息
     * @param model
     * @return
     */
    TenantPackModel queryTenantPack(TenantPackModel model);

    /**
     * Add relationship data for multiple users and product packages
     * @param sysTenantPackUser
     */
    void addBatchTenantPackUser(SysTenantPackUser sysTenantPackUser);

    /**
     * Add relationship data for users and product packages with logging
     * @param sysTenantPackUser
     */
    void addTenantPackUser(SysTenantPackUser sysTenantPackUser);

    /**
     * Remove user and product package relationship data with logging
     * @param sysTenantPackUser
     */
    void deleteTenantPackUser(SysTenantPackUser sysTenantPackUser);


    /**
     * Query the list of users who applied
     */
    List<TenantPackUser> getTenantPackApplyUsers(Integer tenantId);

    /**
     * personal Apply to become an administrator
     * @param sysTenantPackUser
     */
    void doApplyTenantPackUser(SysTenantPackUser sysTenantPackUser);

    /**
     * Application passed Become an administrator
     * @param sysTenantPackUser
     */
    void passApply(SysTenantPackUser sysTenantPackUser);

    /**
     * reject application Become an administrator
     * @param sysTenantPackUser
     */
    void deleteApply(SysTenantPackUser sysTenantPackUser);

    /**
     * Product package user list
     * @param tenantId
     * @param packId
     * @param status
     * @param page
     * @return
     */
    IPage<TenantPackUser> queryTenantPackUserList(String tenantId, String packId, Integer status, Page<TenantPackUser> page);

    /**
     * Check whether you have applied for an administrator
     * @return
     */
    Long getApplySuperAdminCount();

    /**
     * Send consent or rejection message
     * 
     * @param user 
     * @param content 
     */
    void sendMsgForAgreeAndRefuseJoin(SysUser user, String content);

    /**
     * according to密码删除当前用户
     * 
     * @param sysUser
     * @param tenantId
     */
    void deleteUserByPassword(SysUser sysUser, Integer tenantId);

    /**
     * according to用户idGettenant信息
     * @param userId
     * @return
     */
    List<SysTenant> getTenantListByUserId(String userId);

    /**
     * Delete user
     * @param sysUser
     * @param tenantId
     */
    void deleteUser(SysUser sysUser, Integer tenantId);

    /**
     * 为用户添加tenant下所有套餐
     * @param userId
     * @param tenantId
     */
    void addPackUser(String userId, String tenantId);
}
