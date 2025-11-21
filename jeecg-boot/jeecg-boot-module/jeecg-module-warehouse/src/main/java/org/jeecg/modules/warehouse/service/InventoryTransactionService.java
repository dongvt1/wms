package org.jeecg.modules.warehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.warehouse.entity.InventoryTransaction;

import java.util.Date;
import java.util.List;

/**
 * @Description: InventoryTransaction Service Interface
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
public interface InventoryTransactionService extends IService<InventoryTransaction> {

    /**
     * Get transactions by product ID
     * @param productId Product ID
     * @return List of transactions
     */
    List<InventoryTransaction> getByProductId(String productId);

    /**
     * Get transactions by date range
     * @param startDate Start date
     * @param endDate End date
     * @return List of transactions
     */
    List<InventoryTransaction> getByDateRange(Date startDate, Date endDate);

    /**
     * Get transactions by product ID and date range
     * @param productId Product ID
     * @param startDate Start date
     * @param endDate End date
     * @return List of transactions
     */
    List<InventoryTransaction> getByProductIdAndDateRange(String productId, Date startDate, Date endDate);

    /**
     * Get transactions by type
     * @param transactionType Transaction type
     * @return List of transactions
     */
    List<InventoryTransaction> getByTransactionType(String transactionType);

    /**
     * Get transaction summary report
     * @return List of transaction summary
     */
    List<InventoryTransaction> getTransactionSummary();

    /**
     * Create a new inventory transaction
     * @param productId Product ID
     * @param transactionType Transaction type
     * @param quantity Quantity
     * @param referenceId Reference ID
     * @param reason Reason
     * @param userId User ID
     * @return Created transaction
     */
    InventoryTransaction createTransaction(String productId, String transactionType, Integer quantity, 
                                     String referenceId, String reason, String userId);

    /**
     * Get transaction statistics
     * @param startDate Start date
     * @param endDate End date
     * @return Statistics map
     */
    java.util.Map<String, Object> getTransactionStatistics(Date startDate, Date endDate);
}