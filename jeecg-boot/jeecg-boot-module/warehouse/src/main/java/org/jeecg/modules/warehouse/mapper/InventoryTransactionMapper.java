package org.jeecg.modules.warehouse.mapper;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: InventoryTransaction Mapper
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
public interface InventoryTransactionMapper extends BaseMapper<InventoryTransaction> {

    /**
     * Get transactions by product ID
     * @param productId Product ID
     * @return List of transactions
     */
    @Select("SELECT * FROM inventory_transactions WHERE product_id = #{productId} ORDER BY created_at DESC")
    List<InventoryTransaction> getByProductId(@Param("productId") String productId);

    /**
     * Get transactions by date range
     * @param startDate Start date
     * @param endDate End date
     * @return List of transactions
     */
    @Select("SELECT * FROM inventory_transactions WHERE created_at BETWEEN #{startDate} AND #{endDate} ORDER BY created_at DESC")
    List<InventoryTransaction> getByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * Get transactions by product ID and date range
     * @param productId Product ID
     * @param startDate Start date
     * @param endDate End date
     * @return List of transactions
     */
    @Select("SELECT * FROM inventory_transactions WHERE product_id = #{productId} AND created_at BETWEEN #{startDate} AND #{endDate} ORDER BY created_at DESC")
    List<InventoryTransaction> getByProductIdAndDateRange(@Param("productId") String productId, 
                                                       @Param("startDate") Date startDate, 
                                                       @Param("endDate") Date endDate);

    /**
     * Get transactions by type
     * @param transactionType Transaction type
     * @return List of transactions
     */
    @Select("SELECT * FROM inventory_transactions WHERE transaction_type = #{transactionType} ORDER BY created_at DESC")
    List<InventoryTransaction> getByTransactionType(@Param("transactionType") String transactionType);

    /**
     * Get transaction summary report
     * @return List of transaction summary
     */
    @Select("SELECT it.*, p.name as product_name, p.code as product_code " +
            "FROM inventory_transactions it " +
            "JOIN product p ON it.product_id = p.id " +
            "ORDER BY it.created_at DESC")
    List<InventoryTransaction> getTransactionSummary();
}