package org.jeecg.modules.system.mapper;

import java.util.List;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.entity.SysUserDepart;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: User departmentmapperinterface
 * @author: jeecg-boot
 */
public interface SysUserDepartMapper extends BaseMapper<SysUserDepart>{

    /**
     * by useridQuery department users
     * @param userId userid
     * @return List<SysUserDepart>
     */
	List<SysUserDepart> getUserDepartByUid(@Param("userId") String userId);

	/**
	 *  查询指定部门下的user 并且支持user真实姓名模糊查询
	 * @param orgCode
	 * @param realname
	 * @return
	 */
	List<SysUser> queryDepartUserList(@Param("orgCode") String orgCode, @Param("realname") String realname);

	/**
	 * 根据部门Query department users
	 * @param page
	 * @param orgCode
	 * @param username
	 * @param realname
	 * @return
	 */
	IPage<SysUser> queryDepartUserPageList(Page<SysUser> page, @Param("orgCode") String orgCode, @Param("username") String username, @Param("realname") String realname);

    /**
     * 获取user信息
     * @param page
     * @param orgCode
     * @param keyword
     * @return
     */
    IPage<SysUser> getUserInformation(Page<SysUser> page,  @Param("orgCode") String orgCode,  @Param("keyword") String keyword,@Param("userId") String userId);


	/**
	 * 获取user信息
	 * @param page
	 * @param orgCode
	 * @param keyword
	 * @return
	 */
	IPage<SysUser> getProcessUserList(Page<SysUser> page,  @Param("orgCode") String orgCode,  @Param("keyword") String keyword,  @Param("tenantId") Integer tenantId, @Param("excludeUserIdList") List<String> excludeUserIdList);

	/**
	 * Get the departments under the tenant through the departments passed from the front deskid
	 * @param departIds
	 * @param tenantId
	 * @return
	 */
    List<String> getTenantDepart(@Param("departIds") List<String> departIds, @Param("tenantId") String tenantId);

	/**
	 * 根据当前租户和userid查询User department数据
	 * @param userId
	 * @param tenantId
	 * @return
	 */
	List<SysUserDepart> getTenantUserDepart(@Param("userId") String userId, @Param("tenantId") String tenantId);

	/**
	 * 根据useridand tenantsid,删除User department数据
	 * @param userId
	 * @param tenantId
	 */
	void deleteUserDepart(@Param("userId") String userId, @Param("tenantId") String tenantId);

	/**
	 * by departmentidand tenantsid获取user
	 * @param departId
	 * @param tenantId
	 * @return
	 */
    List<SysUser> getUsersByDepartTenantId(@Param("departId") String departId, @Param("tenantId") Integer tenantId);

	/**
	 * 根据useridand departmentsidGet quantity,用于查看user是否存在User department关系表中
	 * @param userId
	 * @param departId
	 * @return
	 */
	@Select("SELECT COUNT(*) FROM sys_user_depart WHERE user_id = #{userId} AND dep_id = #{departId}")
    Long getCountByDepartIdAndUserId(String userId, String departId);
}
