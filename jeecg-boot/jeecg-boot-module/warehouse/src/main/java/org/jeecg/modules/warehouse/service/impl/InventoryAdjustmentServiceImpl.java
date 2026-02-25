package org.jeecg.modules.warehouse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.modules.warehouse.mapper.InventoryAdjustmentMapper;
import org.jeecg.modules.warehouse.service.InventoryAdjustmentService;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Description: InventoryAdjustment Service Implementation
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
@Slf4j
@Service
public class InventoryAdjustmentServiceImpl extends ServiceImpl<InventoryAdjustmentMapper, InventoryAdjustment> implements InventoryAdjustmentService {

    @Override
    public List<InventoryAdjustment> getByProductId(String productId) {
        return baseMapper.getByProductId(productId);
    }

    @Override
    public List<InventoryAdjustment> getByDateRange(Date startDate, Date endDate) {
        return baseMapper.getByDateRange(startDate, endDate);
    }

    @Override
    public List<InventoryAdjustment> getByProductIdAndDateRange(String productId, Date startDate, Date endDate) {
        return baseMapper.getByProductIdAndDateRange(productId, startDate, endDate);
    }

    @Override
    public List<InventoryAdjustment> getAdjustmentSummary() {
        return baseMapper.getAdjustmentSummary();
    }

    @Override
    public InventoryAdjustment createAdjustment(String productId, Integer oldQuantity, Integer newQuantity, 
                                           String adjustmentReason, String userId) {
        InventoryAdjustment adjustment = new InventoryAdjustment();
        adjustment.setId(UUIDGenerator.generate());
        adjustment.setProductId(productId);
        adjustment.setOldQuantity(oldQuantity != null ? oldQuantity : 0);
        adjustment.setNewQuantity(newQuantity != null ? newQuantity : 0);
        adjustment.setAdjustmentReason(adjustmentReason);
        adjustment.setUserId(userId);
        adjustment.setCreatedAt(new Date());
        
        this.save(adjustment);
        return adjustment;
    }

    @Override
    public Map<String, Object> getAdjustmentStatistics(Date startDate, Date endDate) {
        Map<String, Object> statistics = new HashMap<>();
        
        List<InventoryAdjustment> adjustments = getByDateRange(startDate, endDate);
        
        int totalAdjustments = adjustments.size();
        int totalIncrease = adjustments.stream()
            .filter(a -> a.getNewQuantity() > a.getOldQuantity())
            .mapToInt(a -> a.getNewQuantity() - a.getOldQuantity())
            .sum();
        int totalDecrease = adjustments.stream()
            .filter(a -> a.getNewQuantity() < a.getOldQuantity())
            .mapToInt(a -> a.getOldQuantity() - a.getNewQuantity())
            .sum();
        
        Map<String, Integer> adjustmentsByType = new HashMap<>();
        adjustmentsByType.put("increase", totalIncrease);
        adjustmentsByType.put("decrease", totalDecrease);
        
        statistics.put("totalAdjustments", totalAdjustments);
        statistics.put("totalIncrease", totalIncrease);
        statistics.put("totalDecrease", totalDecrease);
        statistics.put("adjustmentsByType", adjustmentsByType);
        statistics.put("startDate", startDate);
        statistics.put("endDate", endDate);
        
        return statistics;
    }
}