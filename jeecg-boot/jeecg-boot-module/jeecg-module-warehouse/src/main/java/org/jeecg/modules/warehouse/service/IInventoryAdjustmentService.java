package org.jeecg.modules.warehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.warehouse.entity.InventoryAdjustment;

import java.util.Date;
import java.util.List;

/**
 * @Description: InventoryAdjustment Service Interface
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
public interface IInventoryAdjustmentService extends IService<InventoryAdjustment> {

    /**
     * Get adjustments by product ID
     * @param productId Product ID
     * @return List of adjustments
     */
    List<InventoryAdjustment> getByProductId(String productId);

    /**
     * Get adjustments by date range
     * @param startDate Start date
     * @param endDate End date
     * @return List of adjustments
     */
    List<InventoryAdjustment> getByDateRange(Date startDate, Date endDate);

    /**
     * Get adjustments by product ID and date range
     * @param productId Product ID
     * @param startDate Start date
     * @param endDate End date
     * @return List of adjustments
     */
    List<InventoryAdjustment> getByProductIdAndDateRange(String productId, Date startDate, Date endDate);

    /**
     * Get adjustment summary report
     * @return List of adjustment summary
     */
    List<InventoryAdjustment> getAdjustmentSummary();

    /**
     * Create a new inventory adjustment
     * @param productId Product ID
     * @param oldQuantity Old quantity
     * @param newQuantity New quantity
     * @param adjustmentReason Adjustment reason
     * @param userId User ID
     * @return Created adjustment
     */
    InventoryAdjustment createAdjustment(String productId, Integer oldQuantity, Integer newQuantity, 
                                     String adjustmentReason, String userId);

    /**
     * Get adjustment statistics
     * @param startDate Start date
     * @param endDate End date
     * @return Statistics map
     */
    java.util.Map<String, Object> getAdjustmentStatistics(Date startDate, Date endDate);
}