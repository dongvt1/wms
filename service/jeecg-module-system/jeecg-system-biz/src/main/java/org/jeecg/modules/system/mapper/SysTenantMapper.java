package org.jeecg.modules.system.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.system.entity.SysTenant;
import org.jeecg.modules.system.vo.tenant.TenantPackUser;
import org.jeecg.modules.system.vo.tenant.TenantPackUserCount;
import org.jeecg.modules.system.vo.tenant.UserDepart;
import org.jeecg.modules.system.vo.tenant.UserPosition;

import java.util.List;

/**
 * @Description: tenantmapperinterface
 * @author: jeecg-boot
 */
public interface SysTenantMapper extends BaseMapper<SysTenant> {

    /**
     * Get the maximum valueid
     */
    @Select("select MAX(id) id FROM sys_tenant")
    Integer getMaxTenantId();
    
    /**
     * 获取tenant回收站的数据假删除
     * @param page
     * @param sysTenant
     * @return
     */
    List<SysTenant> getRecycleBinPageList(@Param("page") Page<SysTenant> page, @Param("sysTenant") SysTenant sysTenant);
    
    /**
     * 彻底删除tenant
     * @param tenantId
     */
    Integer deleteByTenantId(@Param("tenantIds") List<Integer> tenantId);

    /**
     * tenant还原
     * @param list
     * @return
     */
    Integer revertTenantLogic(@Param("tenantIds")List<Integer> list);

    /**
     * for statistics tenant产品包的人员数量
     * @param tenantId
     * @return
     */
    List<TenantPackUserCount> queryTenantPackUserCount(@Param("tenantId") Integer tenantId);

    /**
     * 查询人员是不是tenant产品包的 super administrator
     * @param tenantId
     * @param userId
     * @return
     */
    Integer querySuperAdminCount(@Param("tenantId") Integer tenantId, @Param("userId") String userId);

    /**
     * Query the product package code of the person
     * @param tenantId
     * @param userId
     * @return
     */
    List<String> queryUserPackCode(@Param("tenantId") Integer tenantId, @Param("userId") String userId);

    /**
     * Query the list of users associated with a product package
     * @param tenantId
     * @param packId
     * @param packUserStatus
     * @return
     */
    List<TenantPackUser> queryPackUserList(@Param("tenantId") Integer tenantId, @Param("packId") String packId, @Param("packUserStatus") Integer packUserStatus);


    /**
     * According to userID Query department
     * @param userIdList
     * @return
     */
    List<UserDepart> queryUserDepartList(@Param("userIdList") List<String> userIdList);

    /**
     * According to userID Search for jobs
     * @param userIdList
     * @return
     */
    List<UserPosition> queryUserPositionList(@Param("userIdList") List<String> userIdList);

    /**
     * Query the list of users associated with a product package
     * @param page
     * @param tenantId
     * @param packId
     * @param status
     * @return
     */
    List<TenantPackUser> queryTenantPackUserList(@Param("page") Page<TenantPackUser> page, @Param("tenantId") String tenantId, @Param("packId") String packId, @Param("status") Integer status);


    /**
     * 根据tenantID 查询tenant
     * @param id
     * @return
     */
    @Select("select * from sys_tenant where id = #{id}")
    SysTenant querySysTenant(@Param("id") Integer id);

    /**
     * 查看是否已经申请过了super administrator
     * @param userId
     * @param tenantId
     * @return
     */
    Long getApplySuperAdminCount(@Param("userId") String userId, @Param("tenantId") Integer tenantId);

    /**
     * tenant是否存在
     * @param tenantId
     * @return
     */
    @Select("select count(1) from sys_tenant where id = #{tenantId} and del_flag = 0")
    Long tenantIzExist(@Param("tenantId") Integer tenantId);

    /**
     * According to userid获取tenant
     * @param userId
     * @return
     */
    List<SysTenant> getTenantListByUserId(@Param("userId") String userId);
}
