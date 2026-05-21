package org.jeecg.modules.system.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.system.entity.SysGatewayRoute;

import java.util.List;

/**
 * @Description: gatewayRoute management
 * @Author: jeecg-boot
 * @Date:   2020-05-26
 * @Version: V1.0
 */
public interface ISysGatewayRouteService extends IService<SysGatewayRoute> {

    /**
     * Add all routing information toredis
     * @param key
     */
     void addRoute2Redis(String key);

    /**
     * Delete route
     * @param id
     */
     void deleteById(String id);

    /**
     * Save routing configuration
     * @param array
     */
    void updateAll(JSONObject array);

    /**
     * Clearredisinrouteinformation
     */
    void clearRedis();

    /**
     * Undo tombstone
     * @param ids
     */
    void revertLogicDeleted(List<String> ids);

    /**
     * Delete completely
     * @param ids
     */
    void deleteLogicDeleted(List<String> ids);

    /**
     * Copy route
     * @param id
     * @return
     */
    SysGatewayRoute copyRoute(String id);

    /**
     * Get delete list
     * @return
     */
    List<SysGatewayRoute> getDeletelist();
}
