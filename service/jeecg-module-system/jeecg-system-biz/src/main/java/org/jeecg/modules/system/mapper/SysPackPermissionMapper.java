package org.jeecg.modules.system.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.system.entity.SysPackPermission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: Product package menu relationship table
 * @Author: jeecg-boot
 * @Date:   2022-12-31
 * @Version: V1.0
 */
public interface SysPackPermissionMapper extends BaseMapper<SysPackPermission> {

    /**
     * Via product packageidGet menuid
     * @param packId
     * @return
     */
    List<String> getPermissionsByPackId(@Param("packId") String packId);

    /**
     * Delete the menu permission corresponding to the product package
     *
     * @param tenantIdList
     */
    void deletePackPermByTenantIds(@Param("tenantIdList") List<Integer> tenantIdList);
    
}
