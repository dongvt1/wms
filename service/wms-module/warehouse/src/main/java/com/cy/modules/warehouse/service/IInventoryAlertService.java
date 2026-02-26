package com.cy.modules.warehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cy.modules.warehouse.entity.InventoryAlert;

import java.util.List;

/**
 * @Description: Inventory Alert Service Interface
 * @Author: jeecg
 * @Date: 2025-11-20
 * @Version: V1.0
 */
public interface IInventoryAlertService extends IService<InventoryAlert> {

    /**
     * 解决单条预警
     * @param alertId 预警ID
     * @return 结果信息
     */
    String resolveAlert(String alertId);

    /**
     * 忽略单条预警
     * @param alertId 预警ID
     * @return 结果信息
     */
    String dismissAlert(String alertId);

    /**
     * 批量解决预警
     * @param alertIds 预警ID列表
     * @return 结果信息
     */
    String resolveAlertsBatch(List<String> alertIds);

    /**
     * 批量忽略预警
     * @param alertIds 预警ID列表
     * @return 结果信息
     */
    String dismissAlertsBatch(List<String> alertIds);
}
