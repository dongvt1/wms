package org.jeecg.modules.system.service;

import org.jeecg.modules.system.entity.SysRolePermission;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * Role permission table Service category
 * </p>
 *
 * @Author scott
 * @since 2018-12-21
 */
public interface ISysRolePermissionService extends IService<SysRolePermission> {
	
	/**
	 * Save authorization/Delete first and then add
	 * @param roleId
	 * @param permissionIds
	 */
	public void saveRolePermission(String roleId,String permissionIds);
	
	/**
	 * Save authorization Compare the last permissions with this time Difference processing improves efficiency 
	 * @param roleId
	 * @param permissionIds
	 * @param lastPermissionIds
	 */
	public void saveRolePermission(String roleId,String permissionIds,String lastPermissionIds);

}
