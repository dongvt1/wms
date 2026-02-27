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
     * Resolve a single alert
     * @param alertId Alert ID
     * @return Result message
     */
    String resolveAlert(String alertId);

    /**
     * Dismiss a single alert
     * @param alertId Alert ID
     * @return Result message
     */
    String dismissAlert(String alertId);

    /**
     * Batch resolve alerts
     * @param alertIds Alert ID list
     * @return Result message
     */
    String resolveAlertsBatch(List<String> alertIds);

    /**
     * Batch dismiss alerts
     * @param alertIds Alert ID list
     * @return Result message
     */
    String dismissAlertsBatch(List<String> alertIds);
}
