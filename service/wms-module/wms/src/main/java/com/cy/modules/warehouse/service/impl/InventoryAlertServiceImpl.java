package com.cy.modules.warehouse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import com.cy.modules.warehouse.entity.InventoryAlert;
import com.cy.modules.warehouse.mapper.InventoryAlertMapper;
import com.cy.modules.warehouse.service.IInventoryAlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Inventory Alert Service Implementation
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
                return "Alert ID cannot be empty";
            }
            
            InventoryAlert alert = this.getById(alertId);
            if (alert == null) {
                return "Alert record not found";
            }
            
            alert.setAlertStatus("RESOLVED");
            alert.setResolvedAt(new Date());
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            String currentUser = sysUser.getUsername();
            alert.setResolvedBy(currentUser);
            
            this.updateById(alert);
            return "Alert resolved successfully";
        } catch (Exception e) {
            log.error("Failed to resolve alert", e);
            return "Failed to resolve alert: " + e.getMessage();
        }
    }

    @Override
    public String dismissAlert(String alertId) {
        try {
            if (oConvertUtils.isEmpty(alertId)) {
                return "Alert ID cannot be empty";
            }
            
            InventoryAlert alert = this.getById(alertId);
            if (alert == null) {
                return "Alert record not found";
            }
            
            alert.setAlertStatus("DISMISSED");
            alert.setResolvedAt(new Date());
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            String currentUser = sysUser.getUsername();
            alert.setResolvedBy(currentUser);
            
            this.updateById(alert);
            return "Alert dismissed successfully";
        } catch (Exception e) {
            log.error("Failed to dismiss alert", e);
            return "Failed to dismiss alert: " + e.getMessage();
        }
    }

    @Override
    public String resolveAlertsBatch(List<String> alertIds) {
        try {
            if (alertIds == null || alertIds.isEmpty()) {
                return "Alert ID list cannot be empty";
            }

            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            String currentUser = sysUser.getUsername();
            int result = baseMapper.resolveAlertsBatch(alertIds, currentUser);
            
            if (result > 0) {
                return "Batch resolve successful, processed " + result + " records";
            } else {
                return "No alert records found to process";
            }
        } catch (Exception e) {
            log.error("Failed to batch resolve alerts", e);
            return "Failed to batch resolve alerts: " + e.getMessage();
        }
    }

    @Override
    public String dismissAlertsBatch(List<String> alertIds) {
        try {
            if (alertIds == null || alertIds.isEmpty()) {
                return "Alert ID list cannot be empty";
            }
            

            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            String currentUser = sysUser.getUsername();

            int result = baseMapper.dismissAlertsBatch(alertIds, currentUser);
            
            if (result > 0) {
                return "Batch dismiss successful, processed " + result + " records";
            } else {
                return "No alert records found to process";
            }
        } catch (Exception e) {
            log.error("Failed to batch dismiss alerts", e);
            return "Failed to batch dismiss alerts: " + e.getMessage();
        }
    }
}