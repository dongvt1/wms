package org.jeecg.modules.warehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.warehouse.entity.Inventory;
import org.jeecg.modules.warehouse.entity.InventoryAdjustment;
import org.jeecg.modules.warehouse.entity.InventoryTransaction;

import java.util.List;
import java.util.Map;

/**
 * @Description: Inventory Service Interface
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
public interface InventoryService extends IService<Inventory> {

    /**
     * Get inventory by product ID
     * @param productId Product ID
     * @return Inventory
     */
    Inventory getByProductId(String productId);

    /**
     * Create or update inventory for a product
     * @param productId Product ID
     * @param quantity Quantity
     * @param reservedQuantity Reserved quantity
     * @param updatedBy Updated by
     * @return Inventory
     */
    Inventory createOrUpdateInventory(String productId, Integer quantity, Integer reservedQuantity, String updatedBy);

    /**
     * Update inventory quantity
     * @param productId Product ID
     * @param quantity New quantity
     * @param reservedQuantity Reserved quantity
     * @param updatedBy Updated by
     * @return Success status
     */
    boolean updateInventoryQuantity(String productId, Integer quantity, Integer reservedQuantity, String updatedBy);

    /**
     * Process inventory transaction
     * @param transaction Inventory transaction
     * @return Success status
     */
    boolean processTransaction(InventoryTransaction transaction);

    /**
     * Process inventory adjustment
     * @param adjustment Inventory adjustment
     * @return Success status
     */
    boolean processAdjustment(InventoryAdjustment adjustment);

    /**
     * Get products with low stock
     * @return List of inventory records with low stock
     */
    List<Inventory> getLowStockProducts();

    /**
     * Get inventory value report
     * @return List of inventory with value
     */
    List<Inventory> getInventoryValueReport();

    /**
     * Get inventory summary report
     * @return Map with summary statistics
     */
    Map<String, Object> getInventorySummary();

    /**
     * Get inventory trend data for charts
     * @param productId Product ID
     * @param days Number of days to look back
     * @return List of trend data
     */
    List<Map<String, Object>> getInventoryTrend(String productId, Integer days);

    /**
     * Initialize inventory for a new product
     * @param productId Product ID
     * @param initialQuantity Initial quantity
     * @param createdBy Created by
     * @return Inventory
     */
    Inventory initializeInventory(String productId, Integer initialQuantity, String createdBy);

    /**
     * Reserve inventory quantity
     * @param productId Product ID
     * @param quantity Quantity to reserve
     * @param updatedBy Updated by
     * @return Success status
     */
    boolean reserveInventory(String productId, Integer quantity, String updatedBy);

    /**
     * Release reserved inventory
     * @param productId Product ID
     * @param quantity Quantity to release
     * @param updatedBy Updated by
     * @return Success status
     */
    boolean releaseReservedInventory(String productId, Integer quantity, String updatedBy);

    /**
     * Check if product has sufficient available inventory
     * @param productId Product ID
     * @param requiredQuantity Required quantity
     * @return True if sufficient, false otherwise
     */
    boolean hasSufficientInventory(String productId, Integer requiredQuantity);
}