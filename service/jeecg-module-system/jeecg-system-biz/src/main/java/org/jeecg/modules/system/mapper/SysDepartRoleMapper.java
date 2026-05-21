package org.jeecg.modules.system.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.system.entity.SysDepartRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: Department role
 * @Author: jeecg-boot
 * @Date:   2020-02-12
 * @Version: V1.0
 */
public interface SysDepartRoleMapper extends BaseMapper<SysDepartRole> {
    /**
     * According to userid，departmentid查询可授权所有Department role
     * @param orgCode
     * @param userId
     * @return
     */
    public List<SysDepartRole> queryDeptRoleByDeptAndUser(@Param("orgCode") String orgCode, @Param("userId") String userId);
}
