package org.jeecg.modules.warehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.warehouse.entity.StockTransaction;
import org.jeecg.modules.warehouse.entity.StockTransactionItem;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Description: StockTransaction Service Interface
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
public interface IStockTransactionService extends IService<StockTransaction> {

    /**
     * Get transactions by date range
     * @param startDate Start date
     * @param endDate End date
     * @return List of transactions
     */
    List<StockTransaction> getByDateRange(Date startDate, Date endDate);

    /**
     * Get transactions by status
     * @param status Status
     * @return List of transactions
     */
    List<StockTransaction> getByStatus(String status);

    /**
     * Get transactions by type
     * @param transactionType Transaction type
     * @return List of transactions
     */
    List<StockTransaction> getByTransactionType(String transactionType);

    /**
     * Get transaction summary report
     * @return List of transaction summary
     */
    List<StockTransaction> getTransactionSummary();

    /**
     * Get transaction by code
     * @param transactionCode Transaction code
     * @return Transaction
     */
    StockTransaction getByCode(String transactionCode);

    /**
     * Create a new stock transaction
     * @param transactionType Transaction type
     * @param transactionDate Transaction date
     * @param notes Notes
     * @param items Transaction items
     * @param userId User ID
     * @return Created transaction
     */
    StockTransaction createTransaction(String transactionType, Date transactionDate, String notes, 
                                   List<StockTransactionItem> items, String userId);

    /**
     * Approve a transaction
     * @param transactionId Transaction ID
     * @param approvedBy Approved by
     * @return Success status
     */
    boolean approveTransaction(String transactionId, String approvedBy);

    /**
     * Cancel a transaction
     * @param transactionId Transaction ID
     * @param cancelReason Cancel reason
     * @return Success status
     */
    boolean cancelTransaction(String transactionId, String cancelReason);

    /**
     * Get transaction statistics
     * @param startDate Start date
     * @param endDate End date
     * @return Statistics map
     */
    Map<String, Object> getTransactionStatistics(Date startDate, Date endDate);

    /**
     * Get transaction items by transaction ID
     * @param transactionId Transaction ID
     * @return List of transaction items
     */
    List<StockTransactionItem> getTransactionItems(String transactionId);

    /**
     * Generate transaction code
     * @param transactionType Transaction type
     * @return Generated transaction code
     */
    String generateTransactionCode(String transactionType);
}