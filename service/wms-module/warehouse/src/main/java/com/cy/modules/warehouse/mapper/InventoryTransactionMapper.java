package com.cy.modules.warehouse.mapper;

import java.util.Date;
import java.util.List;

import com.cy.modules.warehouse.entity.InventoryTransaction;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: Mapper giao dịch tồn kho
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
public interface InventoryTransactionMapper extends BaseMapper<InventoryTransaction> {

    /**
     * Lấy danh sách giao dịch theo ID sản phẩm
     * @param productId ID sản phẩm
     * @return Danh sách giao dịch
     */
    @Select("SELECT * FROM inventory_transactions WHERE product_id = #{productId} ORDER BY created_at DESC")
    List<InventoryTransaction> getByProductId(@Param("productId") String productId);

    /**
     * Lấy danh sách giao dịch theo khoảng thời gian
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return Danh sách giao dịch
     */
    @Select("SELECT * FROM inventory_transactions WHERE created_at BETWEEN #{startDate} AND #{endDate} ORDER BY created_at DESC")
    List<InventoryTransaction> getByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * Lấy danh sách giao dịch theo ID sản phẩm và khoảng thời gian
     * @param productId ID sản phẩm
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return Danh sách giao dịch
     */
    @Select("SELECT * FROM inventory_transactions WHERE product_id = #{productId} AND created_at BETWEEN #{startDate} AND #{endDate} ORDER BY created_at DESC")
    List<InventoryTransaction> getByProductIdAndDateRange(@Param("productId") String productId, 
                                                       @Param("startDate") Date startDate, 
                                                       @Param("endDate") Date endDate);

    /**
     * Lấy danh sách giao dịch theo loại
     * @param transactionType Loại giao dịch
     * @return Danh sách giao dịch
     */
    @Select("SELECT * FROM inventory_transactions WHERE transaction_type = #{transactionType} ORDER BY created_at DESC")
    List<InventoryTransaction> getByTransactionType(@Param("transactionType") String transactionType);

    /**
     * Lấy báo cáo tổng hợp giao dịch
     * @return Danh sách tổng hợp giao dịch
     */
    @Select("SELECT it.*, p.name as product_name, p.code as product_code " +
            "FROM inventory_transactions it " +
            "JOIN product p ON it.product_id = p.id " +
            "ORDER BY it.created_at DESC")
    List<InventoryTransaction> getTransactionSummary();
}