package org.jeecg.modules.warehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.warehouse.entity.StockTransactionItem;

import java.util.List;

/**
 * @Description: StockTransactionItem Service Interface
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
public interface IStockTransactionItemService extends IService<StockTransactionItem> {

    /**
     * Get items by transaction ID
     * @param transactionId Transaction ID
     * @return List of items
     */
    List<StockTransactionItem> getByTransactionId(String transactionId);

    /**
     * Get items by product ID
     * @param productId Product ID
     * @return List of items
     */
    List<StockTransactionItem> getByProductId(String productId);

    /**
     * Get items with product info
     * @return List of items with product info
     */
    List<StockTransactionItem> getItemsWithProductInfo();
}