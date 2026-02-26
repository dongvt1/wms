package org.jeecg.modules.system.mapper;

import java.util.List;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.jeecg.modules.system.entity.SysTenant;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.entity.SysUserTenant;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.system.vo.SysUserTenantVo;
import org.jeecg.modules.system.vo.thirdapp.JwUserDepartVo;

/**
 * @Description: sys_user_tenant_relation
 * @Author: jeecg-boot
 * @Date:   2022-12-23
 * @Version: V1.0
 */
public interface SysUserTenantMapper extends BaseMapper<SysUserTenant> {

    /**
     * by tenantidGet data
     * @param page
     * @param userTenantId
     * @return
     */
    List<SysUser> getPageUserList(@Param("page") Page<SysUser> page,@Param("userTenantId") Integer userTenantId,@Param("user") SysUser user);

    /**
     * According to tenantidGet userids
     * @param tenantId
     * @return
     */
    List<String> getUserIdsByTenantId(@Param("tenantId") Integer tenantId);

    /**
     * by useridGet tenantsids
     * @param userId
     * @return
     */
    List<Integer> getTenantIdsByUserId(@Param("userId") String userId);


    
    //==============================================================================================================================
    /**
     * by useridGet tenants列表
     * @param userId
     * @return
     */
    List<SysUserTenantVo> getTenantListByUserId(@Param("userId") String userId, @Param("userTenantStatus") List<String> userTenantStatus);
    
    /**
     * pass status、Username of the currently logged in person，tenantid，Query userid
     * @param tenantId
     * @param statusList
     * @param username
     * @return
     */
    List<String> getUserIdsByCreateBy(@Param("tenantId") Integer tenantId, @Param("userTenantStatus")  List<String> statusList, @Param("username") String username);

    /**
     * 联查用户和tenant审核状态
     * @param page
     * @param status
     * @param tenantId
     * @return
     */
    List<SysUserTenantVo> getUserTenantPageList(@Param("page") Page<SysUserTenantVo> page, @Param("status") List<String> status, @Param("user") SysUser user, @Param("tenantId") Integer tenantId);

    /**
     * According to useridGet tenantsid，no status value(如Get tenants已经存在，It’s just that it was rejected or is in the process of approval.)
     * @param userId
     * @return
     */
    List<Integer> getTenantIdsNoStatus(@Param("userId") String userId);
    //==============================================================================================================================

    /**
     * 统计一个人创建了多少个tenant
     *
     * @param userId
     * @return
     */
    Integer countCreateTenantNum(String userId);

    /**
     * Cancel resignation
     * @param userIds
     * @param tenantId
     */
    void putCancelQuit(@Param("userIds") List<String> userIds, @Param("tenantId") Integer tenantId);

    /**
     * 判断当前用户是否已在该tenant下面
     * @param userId
     * @param tenantId
     */
    Integer userTenantIzExist(@Param("userId") String userId, @Param("tenantId") int tenantId);

    /**
     * 查询未被注销的tenant
     * @param userId
     * @return
     */
    List<SysTenant> getTenantNoCancel(@Param("userId") String userId);

    /**
     * According to userid获取我的tenant
     * @param page
     * @param userId
     * @param userTenantStatus
     * @return
     */
    List<SysTenant> getTenantPageListByUserId(@Param("page") Page<SysTenant> page, @Param("userId") String userId, @Param("userTenantStatus") List<String> userTenantStatus,@Param("sysUserTenantVo") SysUserTenantVo sysUserTenantVo);

    /**
     * 同意加入tenant
     * @param userId
     * @param tenantId
     */
    @Update("update sys_user_tenant set status = '1' where user_id = #{userId} and tenant_id = #{tenantId}")
    void agreeJoinTenant(@Param("userId") String userId, @Param("tenantId") Integer tenantId);

    /**
     * 拒绝加入tenant
     * @param userId
     * @param tenantId
     */
    @Delete("delete from sys_user_tenant where user_id = #{userId} and tenant_id = #{tenantId}")
    void refuseJoinTenant(@Param("userId") String userId, @Param("tenantId") Integer tenantId);

    /**
     * According to userid和tenantidGet usertenant中间表信息
     *
     * @param userId
     * @param tenantId
     * @return
     */
    @Select("select id,user_id,tenant_id,create_by,status from sys_user_tenant where user_id = #{userId} and tenant_id = #{tenantId}")
    SysUserTenant getUserTenantByTenantId(@Param("userId") String userId, @Param("tenantId") Integer tenantId);

    /**
     * 删除tenant下的用户
     *
     * @param tenantIds
     */
    void deleteUserByTenantId(@Param("tenantIds") List<Integer> tenantIds);

    /**
     * Get tenants下的成员数量
     *
     * @param tenantId
     * @param tenantStatus
     * @return
     */
    Long getUserCount(Integer tenantId, String tenantStatus);
    
    /**
     * According to tenantid和名称Get user数据
     * @param tenantId
     * @return
     */
    List<JwUserDepartVo> getUsersByTenantIdAndName(@Param("tenantId") Integer tenantId);

    /**
     * based on multiple usersidGet tenantsid
     *
     * @param userIds
     * @return
     */
    List<Integer> getTenantIdsByUserIds(@Param("userIds") List<String> userIds);
}
