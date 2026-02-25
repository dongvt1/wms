package org.jeecg.modules.warehouse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.modules.warehouse.mapper.InventoryMapper;
import org.jeecg.modules.warehouse.service.InventoryService;
import org.jeecg.modules.warehouse.service.InventoryTransactionService;
import org.jeecg.modules.warehouse.service.InventoryAdjustmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @Description: Inventory Service Implementation
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
@Slf4j
@Service
public class InventoryServiceImpl extends ServiceImpl<InventoryMapper, Inventory> implements InventoryService {

    @Autowired
    private InventoryTransactionService inventoryTransactionService;
    
    @Autowired
    private InventoryAdjustmentService inventoryAdjustmentService;

    @Override
    public Inventory getByProductId(String productId) {
        return baseMapper.getByProductId(productId);
    }

    @Override
    @Transactional
    public Inventory createOrUpdateInventory(String productId, Integer quantity, Integer reservedQuantity, String updatedBy) {
        Inventory inventory = getByProductId(productId);
        
        if (inventory == null) {
            // Create new inventory record
            inventory = new Inventory();
            inventory.setId(UUIDGenerator.generate());
            inventory.setProductId(productId);
            inventory.setQuantity(quantity != null ? quantity : 0);
            inventory.setReservedQuantity(reservedQuantity != null ? reservedQuantity : 0);
            inventory.setAvailableQuantity(calculateAvailableQuantity(inventory.getQuantity(), inventory.getReservedQuantity()));
            inventory.setLastUpdated(new Date());
            inventory.setUpdatedBy(updatedBy);
            this.save(inventory);
        } else {
            // Update existing inventory
            inventory.setQuantity(quantity != null ? quantity : inventory.getQuantity());
            inventory.setReservedQuantity(reservedQuantity != null ? reservedQuantity : inventory.getReservedQuantity());
            inventory.setAvailableQuantity(calculateAvailableQuantity(inventory.getQuantity(), inventory.getReservedQuantity()));
            inventory.setLastUpdated(new Date());
            inventory.setUpdatedBy(updatedBy);
            this.updateById(inventory);
        }
        
        return inventory;
    }

    @Override
    @Transactional
    public boolean updateInventoryQuantity(String productId, Integer quantity, Integer reservedQuantity, String updatedBy) {
        return baseMapper.updateQuantity(productId, quantity, reservedQuantity, updatedBy) > 0;
    }

    @Override
    @Transactional
    public boolean processTransaction(InventoryTransaction transaction) {
        try {
            // Save transaction
            inventoryTransactionService.save(transaction);
            
            // Update inventory based on transaction type
            Inventory inventory = getByProductId(transaction.getProductId());
            if (inventory == null) {
                // Create inventory if it doesn't exist
                inventory = initializeInventory(transaction.getProductId(), 0, transaction.getUserId());
            }
            
            Integer oldQuantity = inventory.getQuantity();
            Integer newQuantity = oldQuantity;
            
            switch (transaction.getTransactionType()) {
                case "IN":
                    newQuantity = oldQuantity + transaction.getQuantity();
                    break;
                case "OUT":
                    newQuantity = oldQuantity - transaction.getQuantity();
                    break;
                case "ADJUST":
                    // Adjustments are handled separately
                    break;
                default:
                    log.error("Unknown transaction type: {}", transaction.getTransactionType());
                    return false;
            }
            
            inventory.setQuantity(newQuantity);
            inventory.setAvailableQuantity(calculateAvailableQuantity(newQuantity, inventory.getReservedQuantity()));
            inventory.setLastUpdated(new Date());
            inventory.setUpdatedBy(transaction.getUserId());
            
            return this.updateById(inventory);
        } catch (Exception e) {
            log.error("Error processing inventory transaction", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean processAdjustment(InventoryAdjustment adjustment) {
        try {
            // Save adjustment
            inventoryAdjustmentService.save(adjustment);
            
            // Update inventory
            Inventory inventory = getByProductId(adjustment.getProductId());
            if (inventory == null) {
                // Create inventory if it doesn't exist
                inventory = initializeInventory(adjustment.getProductId(), adjustment.getNewQuantity(), adjustment.getUserId());
            } else {
                inventory.setQuantity(adjustment.getNewQuantity());
                inventory.setAvailableQuantity(calculateAvailableQuantity(adjustment.getNewQuantity(), inventory.getReservedQuantity()));
                inventory.setLastUpdated(new Date());
                inventory.setUpdatedBy(adjustment.getUserId());
                this.updateById(inventory);
            }
            
            return true;
        } catch (Exception e) {
            log.error("Error processing inventory adjustment", e);
            return false;
        }
    }

    @Override
    public List<Inventory> getLowStockProducts() {
        return baseMapper.getLowStockProducts();
    }

    @Override
    public List<Inventory> getInventoryValueReport() {
        return baseMapper.getInventoryValueReport();
    }

    @Override
    public Map<String, Object> getInventorySummary() {
        Map<String, Object> summary = new HashMap<>();
        
        List<Inventory> allInventory = this.list();
        
        int totalProducts = allInventory.size();
        int totalQuantity = allInventory.stream().mapToInt(Inventory::getQuantity).sum();
        int totalReserved = allInventory.stream().mapToInt(Inventory::getReservedQuantity).sum();
        int totalAvailable = allInventory.stream().mapToInt(Inventory::getAvailableQuantity).sum();
        
        List<Inventory> lowStockProducts = getLowStockProducts();
        int lowStockCount = lowStockProducts.size();
        
        summary.put("totalProducts", totalProducts);
        summary.put("totalQuantity", totalQuantity);
        summary.put("totalReserved", totalReserved);
        summary.put("totalAvailable", totalAvailable);
        summary.put("lowStockCount", lowStockCount);
        summary.put("lowStockProducts", lowStockProducts);
        
        return summary;
    }

    @Override
    public List<Map<String, Object>> getInventoryTrend(String productId, Integer days) {
        // This would typically query historical data
        // For now, return empty list as placeholder
        return new ArrayList<>();
    }

    @Override
    @Transactional
    public Inventory initializeInventory(String productId, Integer initialQuantity, String createdBy) {
        Inventory inventory = new Inventory();
        inventory.setId(UUIDGenerator.generate());
        inventory.setProductId(productId);
        inventory.setQuantity(initialQuantity != null ? initialQuantity : 0);
        inventory.setReservedQuantity(0);
        inventory.setAvailableQuantity(initialQuantity != null ? initialQuantity : 0);
        inventory.setLastUpdated(new Date());
        inventory.setUpdatedBy(createdBy);
        this.save(inventory);
        
        return inventory;
    }

    @Override
    @Transactional
    public boolean reserveInventory(String productId, Integer quantity, String updatedBy) {
        Inventory inventory = getByProductId(productId);
        if (inventory == null || inventory.getAvailableQuantity() < quantity) {
            return false;
        }
        
        inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
        inventory.setAvailableQuantity(calculateAvailableQuantity(inventory.getQuantity(), inventory.getReservedQuantity() + quantity));
        inventory.setLastUpdated(new Date());
        inventory.setUpdatedBy(updatedBy);
        
        return this.updateById(inventory);
    }

    @Override
    @Transactional
    public boolean releaseReservedInventory(String productId, Integer quantity, String updatedBy) {
        Inventory inventory = getByProductId(productId);
        if (inventory == null || inventory.getReservedQuantity() < quantity) {
            return false;
        }
        
        inventory.setReservedQuantity(inventory.getReservedQuantity() - quantity);
        inventory.setAvailableQuantity(calculateAvailableQuantity(inventory.getQuantity(), inventory.getReservedQuantity() - quantity));
        inventory.setLastUpdated(new Date());
        inventory.setUpdatedBy(updatedBy);
        
        return this.updateById(inventory);
    }

    @Override
    public boolean hasSufficientInventory(String productId, Integer requiredQuantity) {
        Inventory inventory = getByProductId(productId);
        return inventory != null && inventory.getAvailableQuantity() >= requiredQuantity;
    }
    
    /**
     * Calculate available quantity
     * @param totalQuantity Total quantity
     * @param reservedQuantity Reserved quantity
     * @return Available quantity
     */
    private Integer calculateAvailableQuantity(Integer totalQuantity, Integer reservedQuantity) {
        if (totalQuantity == null) totalQuantity = 0;
        if (reservedQuantity == null) reservedQuantity = 0;
        return Math.max(0, totalQuantity - reservedQuantity);
    }
}