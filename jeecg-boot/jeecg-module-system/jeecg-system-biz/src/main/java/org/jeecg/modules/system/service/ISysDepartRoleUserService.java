package org.jeecg.modules.system.service;

import org.jeecg.modules.system.entity.SysDepartRoleUser;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description: Department role personnel information
 * @Author: jeecg-boot
 * @Date:   2020-02-13
 * @Version: V1.0
 */
public interface ISysDepartRoleUserService extends IService<SysDepartRoleUser> {

    /**
     * Add user and department association
     * @param userId userid
     * @param newRoleId new rolesid
     * @param oldRoleId old roleid
     */
    void deptRoleUserAdd(String userId,String newRoleId,String oldRoleId);

    /**
     * 取消user与部门关联，Delete relationship
     * @param userIds
     * @param depId
     */
    void removeDeptRoleUser(List<String> userIds,String depId);
}
