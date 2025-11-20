package org.jeecg.modules.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.modules.system.entity.SysTenant;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.entity.SysUserTenant;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.system.vo.SysUserTenantVo;

import java.util.List;

/**
 * @Description: sys_user_tenant_relation
 * @Author: jeecg-boot
 * @Date:   2022-12-23
 * @Version: V1.0
 */
public interface ISysUserTenantService extends IService<SysUserTenant> {

    /**
     * by tenantidGet data
     * @param page
     * @param userTenantId
     * @param user
     * @return
     */
    Page<SysUser> getPageUserList(Page<SysUser> page, Integer userTenantId, SysUser user);

    /**
     * Set up tenantid
     * @param records
     * @return
     */
    List<SysUser> setUserTenantIds(List<SysUser> records);

    /**
     * Get tenantsidGet userids
     * @param tenantId
     * @return
     */
    List<String> getUserIdsByTenantId(Integer tenantId);

    /**
     * by useridGet tenantsids
     * @param userId
     * @return
     */
    List<Integer> getTenantIdsByUserId(String userId);
    
    /**
     * by useridGet tenants列表
     * @param userId
     * @param userTenantStatus
     * @return
     */
    List<SysUserTenantVo> getTenantListByUserId(String userId, List<String>  userTenantStatus);
    
    /**
     * Update user tenant status
     * @param id
     * @param tenantId
     * @param userTenantStatus
     */
    void updateUserTenantStatus(String id, String tenantId, String userTenantStatus);

    /**
     * Jointly check user and tenant review status
     * @param page
     * @param status Tenant user status，Default is1normal
     * @param user
     * @return
     */
    IPage<SysUserTenantVo> getUserTenantPageList(Page<SysUserTenantVo> page, List<String> status, SysUser user, Integer tenantId);

    /**
     * Cancel resignation
     * @param userIds
     * @param tenantId
     */
    void putCancelQuit(List<String> userIds, Integer tenantId);

    /**
     * Verify that user already exists
     * @param userId
     * @param tenantId
     * @return
     */
    Integer userTenantIzExist(String userId, Integer tenantId);

    /**
     * According to useridGet my tenants
     *
     * @param page
     * @param userId
     * @param userTenantStatus
     * @param sysUserTenantVo
     * @return
     */
    IPage<SysTenant> getTenantPageListByUserId(Page<SysTenant> page, String userId, List<String> userTenantStatus,SysUserTenantVo sysUserTenantVo);

    /**
     * Agree to join as tenant
     * @param userId
     * @param tenantId
     */
    void agreeJoinTenant(String userId, Integer tenantId);

    /**
     * Refuse to join tenant
     * @param userId
     * @param tenantId
     */
    void refuseJoinTenant(String userId, Integer tenantId);

    /**
     * According to useridand tenantsidGet user租户中间表信息
     * @param userId
     * @param tenantId
     * @return
     */
    SysUserTenant getUserTenantByTenantId(String userId, Integer tenantId);

    /**
     * Get tenants下的成员数量
     * @param tenantId
     * @param tenantStatus
     * @return
     */
    Long getUserCount(Integer tenantId, String tenantStatus);
}
