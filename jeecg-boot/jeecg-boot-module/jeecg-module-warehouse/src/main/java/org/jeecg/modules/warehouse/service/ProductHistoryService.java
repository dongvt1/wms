package org.jeecg.modules.warehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.warehouse.entity.ProductHistory;

import java.util.List;

/**
 * @Description: Product History Service
 * @Author: BMad
 * @Date:   2025-11-20
 * @Version: V1.0
 */
public interface ProductHistoryService extends IService<ProductHistory> {

    /**
     * Get product history by product ID
     * @param productId Product ID
     * @return List of product history
     */
    List<ProductHistory> getByProductId(String productId);

    /**
     * Get product history by user ID
     * @param userId User ID
     * @return List of product history
     */
    List<ProductHistory> getByUserId(String userId);

    /**
     * Get product history by action
     * @param action Action type (CREATE, UPDATE, DELETE)
     * @return List of product history
     */
    List<ProductHistory> getByAction(String action);

    /**
     * Record product history
     * @param productId Product ID
     * @param action Action type
     * @param oldData Old data (JSON format)
     * @param newData New data (JSON format)
     * @param userId User ID who performed action
     * @return Created history record
     */
    ProductHistory recordHistory(String productId, String action, String oldData, String newData, String userId);
}