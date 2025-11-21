package org.jeecg.modules.warehouse.mapper;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: InventoryAdjustment Mapper
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
public interface InventoryAdjustmentMapper extends BaseMapper<InventoryAdjustment> {

    /**
     * Get adjustments by product ID
     * @param productId Product ID
     * @return List of adjustments
     */
    @Select("SELECT * FROM inventory_adjustments WHERE product_id = #{productId} ORDER BY created_at DESC")
    List<InventoryAdjustment> getByProductId(@Param("productId") String productId);

    /**
     * Get adjustments by date range
     * @param startDate Start date
     * @param endDate End date
     * @return List of adjustments
     */
    @Select("SELECT * FROM inventory_adjustments WHERE created_at BETWEEN #{startDate} AND #{endDate} ORDER BY created_at DESC")
    List<InventoryAdjustment> getByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * Get adjustments by product ID and date range
     * @param productId Product ID
     * @param startDate Start date
     * @param endDate End date
     * @return List of adjustments
     */
    @Select("SELECT * FROM inventory_adjustments WHERE product_id = #{productId} AND created_at BETWEEN #{startDate} AND #{endDate} ORDER BY created_at DESC")
    List<InventoryAdjustment> getByProductIdAndDateRange(@Param("productId") String productId, 
                                                       @Param("startDate") Date startDate, 
                                                       @Param("endDate") Date endDate);

    /**
     * Get adjustment summary report
     * @return List of adjustment summary
     */
    @Select("SELECT ia.*, p.name as product_name, p.code as product_code " +
            "FROM inventory_adjustments ia " +
            "JOIN product p ON ia.product_id = p.id " +
            "ORDER BY ia.created_at DESC")
    List<InventoryAdjustment> getAdjustmentSummary();
}