package org.jeecg.modules.system.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.system.entity.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.system.model.SysUserSysDepPostModel;
import org.jeecg.modules.system.model.SysUserSysDepartModel;
import org.jeecg.modules.system.vo.SysUserDepVo;

import java.util.List;

/**
 * <p>
 * User table Mapper interface
 * </p>
 *
 * @Author scott
 * @since 2018-12-20
 */
public interface SysUserMapper extends BaseMapper<SysUser> {
	/**
	  * Query user information through user account
	 * @param username
	 * @return
	 */
	public SysUser getUserByName(@Param("username") String username);
	
	/**
	  * Query users by user accountId
	 * @param username
	 * @return
	 */
	public String getUserIdByName(@Param("username") String username);

	/**
	 *  According to departmentIdQuery user information
	 * @param page
	 * @param departId
     * @param username User login account
	 * @return
	 */
	IPage<SysUser> getUserByDepId(Page page, @Param("departId") String departId, @Param("username") String username);

	/**
	 * According to department和子部门下of所有user账号
	 *
	 * @param orgCode Department code
	 * @return
	 */
	List<String> getUserAccountsByDepCode(@Param("orgCode") String orgCode);

	/**
	 *  According to userIds,Query the name information of the department to which the user belongs
	 * @param userIds
	 * @return
	 */
	List<SysUserDepVo> getDepNamesByUserIds(@Param("userIds")List<String> userIds);

	/**
	 *  According to departmentIds,Query user information under a department
	 * @param page
	 * @param departIds
     * @param username User login account
	 * @return
	 */
	IPage<SysUser> getUserByDepIds(Page page, @Param("departIds") List<String> departIds, @Param("username") String username);

	/**
	 * According to roleIdQuery user information
	 * @param page
	 * @param roleId Roleid
     * @param username User login account
     * @param realname User name
	 * @return
	 */
	IPage<SysUser> getUserByRoleId(Page page, @Param("roleId") String roleId, @Param("username") String username, @Param("realname") String realname);
	
	/**
	 * According to user名设置部门ID
	 * @param username
	 * @param orgCode
	 */
	void updateUserDepart(@Param("username") String username,@Param("orgCode") String orgCode, @Param("loginTenantId") Integer loginTenantId);
	
	/**
	 * according to手机号Query user information
	 * @param phone
	 * @return
	 */
	public SysUser getUserByPhone(@Param("phone") String phone);
	
	
	/**
	 * according to邮箱Query user information
	 * @param email
	 * @return
	 */
	public SysUser getUserByEmail(@Param("email")String email);

	/**
	 * according to orgCode Query user，Include users under sub-departments
	 *
	 * @param page Pagination object, xmlYou can get the value from it,Pass parameters Page i.e. automatic paging,must be put first(you can inheritPage实现自己ofPagination object)
	 * @param orgCode
	 * @param userParams User query conditions，Can be null
	 * @return
	 */
	List<SysUserSysDepartModel> getUserByOrgCode(IPage page, @Param("orgCode") String orgCode, @Param("userParams") SysUser userParams);


    /**
     * Query getUserByOrgCode ofTotal
     *
     * @param orgCode
     * @param userParams User query conditions，Can be null
     * @return
     */
    Integer getUserByOrgCodeTotal(@Param("orgCode") String orgCode, @Param("userParams") SysUser userParams);

    /**
     * 批量删除Role与user关系
     * @Author scott
     * @Date 2019/12/13 16:10
     * @param roleIdArray
     */
	void deleteBathRoleUserRelation(@Param("roleIdArray") String[] roleIdArray);

    /**
     * 批量删除Role与权限关系
     * @Author scott
     * @Date 2019/12/13 16:10
     * @param roleIdArray
     */
	void deleteBathRolePermissionRelation(@Param("roleIdArray") String[] roleIdArray);

	/**
	 * Query被逻辑删除ofuser
     * @param wrapper
     * @return List<SysUser>
	 */
	List<SysUser> selectLogicDeleted(@Param(Constants.WRAPPER) Wrapper<SysUser> wrapper);

	/**
	 * 还原被逻辑删除ofuser
     * @param userIds userid
     * @param entity
     * @return int
	 */
	int revertLogicDeleted(@Param("userIds") List<String> userIds, @Param("entity") SysUser entity);

	/**
	 * 彻底删除被逻辑删除ofuser
     * @param userIds 多个userid
     * @return int
	 */
	int deleteLogicDeleted(@Param("userIds") List<String> userIds);

    /**
     * Update the empty string tonull【This writing method hassqlInject risk，No casual use is allowed】
     * @param fieldName
     * @return int
     */
    @Deprecated
    int updateNullByEmptyString(@Param("fieldName") String fieldName);
    
	/**
	 *  According to departmentIds,Query user information under a department
	 * @param departIds
     * @param username user账户名称
	 * @return
	 */
	List<SysUser> queryByDepIds(@Param("departIds")List<String> departIds,@Param("username") String username);

	/**
	 * 获取user信息
	 * @param page
	 * @param roleId
	 * @param keyword
	 * @param userIdList
	 * @return
	 */
	IPage<SysUser> selectUserListByRoleId(Page<SysUser> page,  @Param("roleId") String roleId,  @Param("keyword") String keyword,  @Param("tenantId") Integer tenantId, @Param("excludeUserIdList") List<String> excludeUserIdList);

    /**
     * Update deletion status and resignation status
     * @param userIds  存放useridgather
     * @param sysUser
     * @return boolean
     */
    void updateStatusAndFlag(@Param("userIds") List<String> userIds, @Param("sysUser") SysUser sysUser);

	/**
	 * 获取租户下of离职列表信息
	 * @param tenantId
	 * @return
	 */
	List<SysUser> getTenantQuitList(@Param("tenantId") Integer tenantId);
	
	/**
	 * 获取租户下of有效userids
	 * @param tenantId
	 * @return
	 */
	List<String> getTenantUserIdList(@Param("tenantId") Integer tenantId);
	
	/**
	 * According to departmentidand tenantsid获取user数据 
	 * @param departIds
	 * @param tenantId
	 * @return
	 */
	List<SysUser> getUserByDepartsTenantId(@Param("departIds") List<String> departIds,@Param("tenantId") Integer tenantId);

	/**
	 * According to user名和手机号获取user
	 * @param phone
	 * @param username
	 * @return
	 */
	@Select("select id,phone from sys_user where phone = #{phone} and username = #{username}")
    SysUser getUserByNameAndPhone(@Param("phone") String phone, @Param("username") String username);

    /**
     * Query部门、岗位下ofuser Include users under sub-departments
     * 
     * @param page
     * @param orgCode
     * @param userParams
     * @return
     */
    List<SysUserSysDepPostModel> queryDepartPostUserByOrgCode(@Param("page") IPage page, @Param("orgCode") String orgCode, @Param("userParams") SysUser userParams);

    /**
     * According to departmentid和user名获取部门岗位user分页列表
     * 
     * @param page
     * @param userIdList
     * @return
     */
    IPage<SysUser> getDepPostListByIdUserName(@Param("page") Page<SysUser> page, @Param("userIdList") List<String> userIdList, @Param("userId") String userId, @Param("userName") String userName, @Param("userNameList") List<String> userNameList);

    /**
     * According to departmentid、user名和真实姓名获取部门岗位user分页列表
     *
     * @param page
     * @param username
     * @param realname
     * @param orgCode
     * @return
     */
    IPage<SysUser> getDepartPostListByIdUserRealName(@Param("page") Page<SysUser> page, @Param("username") String username, @Param("realname") String realname, @Param("orgCode") String orgCode);
}
