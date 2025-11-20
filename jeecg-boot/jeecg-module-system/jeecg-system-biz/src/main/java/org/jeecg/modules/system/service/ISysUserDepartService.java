package org.jeecg.modules.system.service;


import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.entity.SysUserDepart;
import org.jeecg.modules.system.model.DepartIdModel;


import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * SysUserDpeartUser organizationservice
 * </p>
 * @Author ZhiLin
 *
 */
public interface ISysUserDepartService extends IService<SysUserDepart> {
	

	/**
	 * According to the specified useridQuery department information
	 * @param userId
	 * @return
	 */
	List<DepartIdModel> queryDepartIdsOfUser(String userId);
	

	/**
	 * According to departmentidQuery user information
	 * @param depId
	 * @return
	 */
	List<SysUser> queryUserByDepId(String depId);
  	/**
	 * According to departmentcode，Query user information of the current department and subordinate departments
     * @param depCode departmentcode
     * @param realname real name
     * @return List<SysUser>
	 */
	List<SysUser> queryUserByDepCode(String depCode,String realname);

	/**
	 * User component data query
	 * @param departId
	 * @param username
	 * @param pageSize
	 * @param pageNo
     * @param realname
     * @param id
     * @param isMultiTranslate Whether to translate multiple fields
	 * @return
	 */
	IPage<SysUser> queryDepartUserPageList(String departId, String username, String realname, int pageSize, int pageNo,String id,String isMultiTranslate);

    /**
     * Get user information
	 * @param tenantId
     * @param departId
     * @param keyword
     * @param pageSize
     * @param pageNo
     * @return
     */
    IPage<SysUser> getUserInformation(Integer tenantId, String departId, String keyword, Integer pageSize, Integer pageNo);

	/**
	 * Get user information
	 * @param tenantId
	 * @param departId
	 * @param roleId
	 * @param keyword
	 * @param pageSize
	 * @param pageNo
	 * @return
	 */
	IPage<SysUser> getUserInformation(Integer tenantId,String departId,String roleId, String keyword, Integer pageSize, Integer pageNo, String excludeUserIdList);

	/**
	 * 通过departmentidand tenantsidGet multiple users
	 * @param departId
	 * @param tenantId
	 * @return
	 */
	List<SysUser> getUsersByDepartTenantId(String departId,Integer tenantId);

    /**
     * 查询department岗位下的用户
     * 
     * @param departId
     * @param username
     * @param realname
     * @param pageSize
     * @param pageNo
     * @param id
     * @param isMultiTranslate
     * @return
     */
    IPage<SysUser> queryDepartPostUserPageList(String departId, String username, String realname, Integer pageSize, Integer pageNo, String id, String isMultiTranslate);
}
