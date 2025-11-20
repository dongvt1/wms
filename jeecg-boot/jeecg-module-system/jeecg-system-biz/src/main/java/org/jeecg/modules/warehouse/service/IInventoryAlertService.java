package org.jeecg.modules.warehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.warehouse.entity.InventoryAlert;

import java.util.List;

/**
 * 库存预警服务接口
 */
public interface IInventoryAlertService extends IService<InventoryAlert> {

    /**
     * 解决预警
     * @param alertId 预警ID
     * @return 操作结果
     */
    String resolveAlert(String alertId);

    /**
     * 忽略预警
     * @param alertId 预警ID
     * @return 操作结果
     */
    String dismissAlert(String alertId);

    /**
     * 批量解决预警
     * @param alertIds 预警ID列表
     * @return 操作结果
     */
    String resolveAlertsBatch(List<String> alertIds);

    /**
     * 批量忽略预警
     * @param alertIds 预警ID列表
     * @return 操作结果
     */
    String dismissAlertsBatch(List<String> alertIds);
}