package org.jeecg.modules.system.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.system.entity.SysTenantPack;
import org.jeecg.modules.system.entity.SysTenantPackUser;

import java.util.List;

/**
 * @Description: Tenant Product Package User Relationship
 * @Author: jeecg-boot
 * @Date:   2023-02-16
 * @Version: V1.0
 */
public interface SysTenantPackUserMapper extends BaseMapper<SysTenantPackUser> {


    /**
     * Search for tenants List of people in a specific role
     * @param tenantId
     * @param packCodeList
     * @return
     */
    @InterceptorIgnore(tenantLine = "true")
    List<String> queryTenantPackUserNameList(@Param("tenantId") Integer tenantId, @Param("packCodeList") List<String> packCodeList); 

    /**
     * Determine whether the current user has administrator rights under the tenant
     * @param userId
     * @param tenantId
     * @return
     */
    Long izHaveBuyAuth(@Param("userId") String userId, @Param("tenantId") Integer tenantId);

    /**
     * According to tenantid Delete the tenant product package user
     * @param tenantId
     */
    void deletePackUserByTenantId(@Param("tenantId") Integer tenantId, @Param("userIds") List<String> userIds);

    /**
     * based on multiple tenantsid Delete the tenant product package user
     * @param
     */
    void deletePackUserByTenantIds(@Param("tenantIds") List<Integer> tenantIds);

    /**
     * 根据useridand tenantsid获取当前租户user下的产品包id
     *
     * @param tenantId
     * @param userId
     * @return
     */
    @Select("select pack_id from sys_tenant_pack_user where tenant_id = #{tenantId} and user_id = #{userId}")
    List<String> getPackIdByTenantIdAndUserId(@Param("tenantId") Integer tenantId, @Param("userId") String userId);
    
    /**
     * According to tenantid获取user的产品包列表
     * 
     * @param tenantId
     * @return
     */
    @Select("select id,pack_name,pack_code,pack_type from sys_tenant_pack where tenant_id = #{tenantId}")
    List<SysTenantPack> getPackListByTenantId(@Param("tenantId") Integer tenantId);
}
