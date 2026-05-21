package com.cy.modules.warehouse.mapper;

import java.util.Date;
import java.util.List;

import com.cy.modules.warehouse.entity.InventoryAdjustment;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: Mapper điều chỉnh tồn kho
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
public interface InventoryAdjustmentMapper extends BaseMapper<InventoryAdjustment> {

    /**
     * Lấy danh sách điều chỉnh theo ID sản phẩm
     * @param productId ID sản phẩm
     * @return Danh sách điều chỉnh
     */
    @Select("SELECT * FROM inventory_adjustments WHERE product_id = #{productId} ORDER BY created_at DESC")
    List<InventoryAdjustment> getByProductId(@Param("productId") String productId);

    /**
     * Lấy danh sách điều chỉnh theo khoảng thời gian
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return Danh sách điều chỉnh
     */
    @Select("SELECT * FROM inventory_adjustments WHERE created_at BETWEEN #{startDate} AND #{endDate} ORDER BY created_at DESC")
    List<InventoryAdjustment> getByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * Lấy danh sách điều chỉnh theo ID sản phẩm và khoảng thời gian
     * @param productId ID sản phẩm
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return Danh sách điều chỉnh
     */
    @Select("SELECT * FROM inventory_adjustments WHERE product_id = #{productId} AND created_at BETWEEN #{startDate} AND #{endDate} ORDER BY created_at DESC")
    List<InventoryAdjustment> getByProductIdAndDateRange(@Param("productId") String productId, 
                                                       @Param("startDate") Date startDate, 
                                                       @Param("endDate") Date endDate);

    /**
     * Lấy báo cáo tổng hợp điều chỉnh
     * @return Danh sách tổng hợp điều chỉnh
     */
    @Select("SELECT ia.*, p.name as product_name, p.code as product_code " +
            "FROM inventory_adjustments ia " +
            "JOIN product p ON ia.product_id = p.id " +
            "ORDER BY ia.created_at DESC")
    List<InventoryAdjustment> getAdjustmentSummary();
}