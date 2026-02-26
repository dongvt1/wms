package org.jeecg.modules.system.service;

import org.jeecg.modules.system.entity.SysDepartPermission;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.system.entity.SysPermissionDataRule;

import java.util.List;

/**
 * @Description: Department authority table
 * @Author: jeecg-boot
 * @Date:   2020-02-11
 * @Version: V1.0
 */
public interface ISysDepartPermissionService extends IService<SysDepartPermission> {
    /**
     * Save authorization Compare the last permissions with this time Difference processing improves efficiency
     * @param departId
     * @param permissionIds
     * @param lastPermissionIds
     */
    public void saveDepartPermission(String departId,String permissionIds,String lastPermissionIds);

    /**
     * According to departmentid，menuidGet data rules
     * @param permissionId menuid
     * @param departId departmentid
     * @return
     */
    List<SysPermissionDataRule> getPermRuleListByDeptIdAndPermId(String departId,String permissionId);
}
