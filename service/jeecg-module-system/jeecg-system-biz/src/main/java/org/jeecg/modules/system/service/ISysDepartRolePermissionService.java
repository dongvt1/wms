package org.jeecg.modules.system.service;

import org.jeecg.modules.system.entity.SysDepartRolePermission;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: Department role permissions
 * @Author: jeecg-boot
 * @Date:   2020-02-12
 * @Version: V1.0
 */
public interface ISysDepartRolePermissionService extends IService<SysDepartRolePermission> {
    /**
     * Save authorization Compare the last permissions with this time Difference processing improves efficiency
     * @param roleId
     * @param permissionIds
     * @param lastPermissionIds
     */
    public void saveDeptRolePermission(String roleId,String permissionIds,String lastPermissionIds);
}
