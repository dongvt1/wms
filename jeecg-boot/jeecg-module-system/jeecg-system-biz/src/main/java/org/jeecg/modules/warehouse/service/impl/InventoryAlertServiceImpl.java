package org.jeecg.modules.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.warehouse.entity.InventoryAlert;
import org.jeecg.modules.warehouse.mapper.InventoryAlertMapper;
import org.jeecg.modules.warehouse.service.IInventoryAlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 库存预警服务实现类
 */
@Service
@Slf4j
public class InventoryAlertServiceImpl extends ServiceImpl<InventoryAlertMapper, InventoryAlert> implements IInventoryAlertService {

    @Autowired
    private ISysBaseAPI sysBaseAPI;

    @Override
    public String resolveAlert(String alertId) {
        try {
            if (oConvertUtils.isEmpty(alertId)) {
                return "预警ID不能为空";
            }
            
            InventoryAlert alert = this.getById(alertId);
            if (alert == null) {
                return "未找到对应的预警记录";
            }
            
            alert.setAlertStatus("RESOLVED");
            alert.setResolvedAt(new Date());
            alert.setResolvedBy(sysBaseAPI.getCurrentUser().getUsername());
            
            this.updateById(alert);
            return "预警解决成功";
        } catch (Exception e) {
            log.error("解决预警失败", e);
            return "解决预警失败: " + e.getMessage();
        }
    }

    @Override
    public String dismissAlert(String alertId) {
        try {
            if (oConvertUtils.isEmpty(alertId)) {
                return "预警ID不能为空";
            }
            
            InventoryAlert alert = this.getById(alertId);
            if (alert == null) {
                return "未找到对应的预警记录";
            }
            
            alert.setAlertStatus("DISMISSED");
            alert.setResolvedAt(new Date());
            alert.setResolvedBy(sysBaseAPI.getCurrentUser().getUsername());
            
            this.updateById(alert);
            return "预警忽略成功";
        } catch (Exception e) {
            log.error("忽略预警失败", e);
            return "忽略预警失败: " + e.getMessage();
        }
    }

    @Override
    public String resolveAlertsBatch(List<String> alertIds) {
        try {
            if (alertIds == null || alertIds.isEmpty()) {
                return "预警ID列表不能为空";
            }
            
            String currentUser = sysBaseAPI.getCurrentUser().getUsername();
            int result = baseMapper.resolveAlertsBatch(alertIds, currentUser);
            
            if (result > 0) {
                return "批量解决预警成功，共处理 " + result + " 条记录";
            } else {
                return "没有找到需要处理的预警记录";
            }
        } catch (Exception e) {
            log.error("批量解决预警失败", e);
            return "批量解决预警失败: " + e.getMessage();
        }
    }

    @Override
    public String dismissAlertsBatch(List<String> alertIds) {
        try {
            if (alertIds == null || alertIds.isEmpty()) {
                return "预警ID列表不能为空";
            }
            
            String currentUser = sysBaseAPI.getCurrentUser().getUsername();
            int result = baseMapper.dismissAlertsBatch(alertIds, currentUser);
            
            if (result > 0) {
                return "批量忽略预警成功，共处理 " + result + " 条记录";
            } else {
                return "没有找到需要处理的预警记录";
            }
        } catch (Exception e) {
            log.error("批量忽略预警失败", e);
            return "批量忽略预警失败: " + e.getMessage();
        }
    }
}