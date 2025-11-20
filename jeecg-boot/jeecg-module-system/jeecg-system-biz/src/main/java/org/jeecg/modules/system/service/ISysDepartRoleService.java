package org.jeecg.modules.system.service;

import org.jeecg.modules.system.entity.SysDepartRole;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description: Department role
 * @Author: jeecg-boot
 * @Date:   2020-02-12
 * @Version: V1.0
 */
public interface ISysDepartRoleService extends IService<SysDepartRole> {

    /**
     * According to userid，departmentid查询可授权所有Department role
     * @param orgCode
     * @param userId
     * @return
     */
    List<SysDepartRole> queryDeptRoleByDeptAndUser(String orgCode, String userId);

    /**
     *  删除Department role和对应关联表信息
     * @param ids
     */
    void deleteDepartRole(List<String> ids);
}
