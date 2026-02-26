package org.jeecg.modules.system.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.system.entity.SysRole;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.vo.SysUserPositionVo;

import java.util.List;

/**
 * <p>
 * character sheet Mapper interface
 * </p>
 *
 * @Author scott
 * @since 2018-12-19
 */
public interface SysRoleMapper extends BaseMapper<SysRole> {
    /**
     * Query all roles（No tenant isolation）
     * @param page
     * @param role
     * @return
     */
    @InterceptorIgnore(tenantLine = "true")
    List<SysRole> listAllSysRole(@Param("page") Page<SysRole> page, @Param("role") SysRole role);

    /**
     * 查询角色是否存在No tenant isolation
     *
     * @param roleCode
     * @return
     */
    @InterceptorIgnore(tenantLine = "true")
    SysRole getRoleNoTenant(@Param("roleCode") String roleCode);

    /**
     * According to useridQuery the roles owned by the userCode
     *
     * @param userId
     * @param tenantId
     * @return
     */
    List<SysRole> getRoleCodeListByUserId(@Param("userId") String userId, @Param("tenantId") Integer tenantId);

    /**
     * Delete role and user relationship
     * @Author scott
     * @Date 2019/12/13 16:12
     * @param roleId
     */
    @Delete("delete from sys_user_role where role_id = #{roleId}")
    void deleteRoleUserRelation(@Param("roleId") String roleId);


    /**
     * Delete role and permission relationships
     * @Author scott
     * @param roleId
     * @Date 2019/12/13 16:12
     */
    @Delete("delete from sys_role_permission where role_id = #{roleId}")
    void deleteRolePermissionRelation(@Param("roleId") String roleId);

    /**
     * According to roleidand the current tenant to determine whether the current role exists in this tenant.
     * @param id
     * @return
     */
    @Select("select count(*) from sys_role where id=#{id} and tenant_id=#{tenantId}")
    Long getRoleCountByTenantId(@Param("id") String id, @Param("tenantId") Integer tenantId);

    /**
     * According to useridGet role information
     * 
     * @param userList
     * @return
     */
    List<SysUserPositionVo> getUserRoleByUserId(@Param("userList") List<SysUser> userList);
}
