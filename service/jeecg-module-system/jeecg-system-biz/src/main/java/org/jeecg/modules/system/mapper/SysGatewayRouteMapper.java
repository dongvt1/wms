package org.jeecg.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.system.entity.SysGatewayRoute;

import java.util.List;

/**
 * @Description: gatewayRoute management
 * @Author: jeecg-boot
 * @Date:   2020-05-26
 * @Version: V1.0
 */
public interface SysGatewayRouteMapper extends BaseMapper<SysGatewayRoute> {
    /**
     * Undo tombstone
     * @param ids
     */
    int revertLogicDeleted(@Param("ids") List<String> ids);

    /**
     *Delete completely
     * @param ids
     */
    int deleteLogicDeleted(@Param("ids") List<String> ids);

    /**
     * Query deleted list
     * @return
     */
    @Select("select * from sys_gateway_route where del_flag = 1")
    List<SysGatewayRoute> queryDeleteList();
}
