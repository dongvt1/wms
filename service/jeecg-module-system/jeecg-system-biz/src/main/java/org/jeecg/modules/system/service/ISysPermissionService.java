package org.jeecg.modules.system.service;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.system.entity.SysPermission;
import org.jeecg.modules.system.model.TreeModel;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * Menu permission table Service category
 * </p>
 *
 * @Author scott
 * @since 2018-12-21
 */
public interface ISysPermissionService extends IService<SysPermission> {
    /**
     * switchvue3menu
     */
	public void switchVue3Menu();
	
    /**
     * by parentid查询menu
     * @param parentId fatherid
     * @return
     */
	public List<TreeModel> queryListByParentId(String parentId);
	
	/**
     * true deletion
     * @param id menuid
     * @throws JeecgBootException
     */
	public void deletePermission(String id) throws JeecgBootException;
	/**
     * tombstone
     * @param id menuid
     * @throws JeecgBootException
     */
	public void deletePermissionLogical(String id) throws JeecgBootException;

    /**
     * 添加menu
     * @param sysPermission SysPermissionobject
     * @throws JeecgBootException
     */
	public void addPermission(SysPermission sysPermission) throws JeecgBootException;

    /**
     * 编辑menu
     * @param sysPermission SysPermissionobject
     * @throws JeecgBootException
     */
	public void editPermission(SysPermission sysPermission) throws JeecgBootException;

    /**
     * Get the permissions owned by the logged in user
     * @param username username
     * @return
     */
	public List<SysPermission> queryByUser(String username);
	
	/**
	 * according topermissionIdDelete its associatedSysPermissionDataRuledata in table
	 * 
	 * @param id
	 * @return
	 */
	public void deletePermRuleByPermId(String id);
	
	/**
	  * 查询出带有特殊符号的menu地址的集合
	 * @return
	 */
	public List<String> queryPermissionUrlWithStar();

	/**
	 * Determine whether the user has permissions
	 * @param username
	 * @param sysPermission
	 * @return
	 */
	public boolean hasPermission(String username, SysPermission sysPermission);

	/**
	 * according to用户和请求地址判断是否有此权限
	 * @param username
	 * @param url
	 * @return
	 */
	public boolean hasPermission(String username, String url);

	/**
	 * Query department authority data
	 * @param departId
	 * @return
	 */
	List<SysPermission> queryDepartPermissionList(String departId);

	/**
	 * Check if the address exists(聚合路由的情况下允许使用子menu路径作为fathermenu的路由地址)
	 * @param id
	 * @param url
	 * @param alwaysShow Is it an aggregate route?
	 * @return
	 */
	 boolean checkPermDuplication(String id, String url,Boolean alwaysShow);
}
