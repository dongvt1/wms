package com.cy.modules.warehouse.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cy.modules.warehouse.entity.StockTransactionItem;

/**
 * @Description: Mapper chi tiết phiếu xuất nhập kho
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
public interface StockTransactionItemMapper extends BaseMapper<StockTransactionItem> {

    /**
     * Lấy danh sách chi tiết theo ID phiếu
     * @param transactionId ID phiếu
     * @return Danh sách chi tiết phiếu
     */
    @Select("SELECT * FROM stock_transaction_items WHERE transaction_id = #{transactionId}")
    List<StockTransactionItem> getByTransactionId(@Param("transactionId") String transactionId);

    /**
     * Lấy danh sách chi tiết theo ID sản phẩm
     * @param productId ID sản phẩm
     * @return Danh sách chi tiết phiếu
     */
    @Select("SELECT * FROM stock_transaction_items WHERE product_id = #{productId} ORDER BY create_time DESC")
    List<StockTransactionItem> getByProductId(@Param("productId") String productId);

    /**
     * Lấy danh sách chi tiết kèm thông tin sản phẩm
     * @return Danh sách chi tiết kèm thông tin sản phẩm
     */
    @Select("SELECT sti.*, p.name as product_name, p.code as product_code " +
            "FROM stock_transaction_items sti " +
            "JOIN product p ON sti.product_id = p.id " +
            "ORDER BY sti.create_time DESC")
    List<StockTransactionItem> getItemsWithProductInfo();
}