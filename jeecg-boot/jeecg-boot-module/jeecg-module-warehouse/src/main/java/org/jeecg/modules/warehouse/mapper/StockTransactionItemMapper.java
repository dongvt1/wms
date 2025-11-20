package org.jeecg.modules.warehouse.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.warehouse.entity.StockTransactionItem;

/**
 * @Description: StockTransactionItem Mapper
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
public interface StockTransactionItemMapper extends BaseMapper<StockTransactionItem> {

    /**
     * Get items by transaction ID
     * @param transactionId Transaction ID
     * @return List of items
     */
    @Select("SELECT * FROM stock_transaction_items WHERE transaction_id = #{transactionId}")
    List<StockTransactionItem> getByTransactionId(@Param("transactionId") String transactionId);

    /**
     * Get items by product ID
     * @param productId Product ID
     * @return List of items
     */
    @Select("SELECT * FROM stock_transaction_items WHERE product_id = #{productId} ORDER BY create_time DESC")
    List<StockTransactionItem> getByProductId(@Param("productId") String productId);

    /**
     * Get items with product info
     * @return List of items with product info
     */
    @Select("SELECT sti.*, p.name as product_name, p.code as product_code " +
            "FROM stock_transaction_items sti " +
            "JOIN product p ON sti.product_id = p.id " +
            "ORDER BY sti.create_time DESC")
    List<StockTransactionItem> getItemsWithProductInfo();
}