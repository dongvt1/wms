package org.jeecg.modules.warehouse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.modules.warehouse.entity.StockTransaction;
import org.jeecg.modules.warehouse.entity.StockTransactionItem;
import org.jeecg.modules.warehouse.mapper.StockTransactionMapper;
import org.jeecg.modules.warehouse.mapper.StockTransactionItemMapper;
import org.jeecg.modules.warehouse.service.IStockTransactionService;
import org.jeecg.modules.warehouse.service.IStockTransactionItemService;
import org.jeecg.modules.warehouse.service.IInventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: StockTransaction Service Implementation
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
@Slf4j
@Service
public class StockTransactionServiceImpl extends ServiceImpl<StockTransactionMapper, StockTransaction> implements IStockTransactionService {

    @Autowired
    private StockTransactionMapper stockTransactionMapper;
    
    @Autowired
    private StockTransactionItemMapper stockTransactionItemMapper;
    
    @Autowired
    private IStockTransactionItemService stockTransactionItemService;
    
    @Autowired
    private IInventoryService inventoryService;

    @Override
    public List<StockTransaction> getByDateRange(Date startDate, Date endDate) {
        return stockTransactionMapper.getByDateRange(startDate, endDate);
    }

    @Override
    public List<StockTransaction> getByStatus(String status) {
        return stockTransactionMapper.getByStatus(status);
    }

    @Override
    public List<StockTransaction> getByTransactionType(String transactionType) {
        return stockTransactionMapper.getByTransactionType(transactionType);
    }

    @Override
    public List<StockTransaction> getTransactionSummary() {
        return stockTransactionMapper.getTransactionSummary();
    }

    @Override
    public StockTransaction getByCode(String transactionCode) {
        return stockTransactionMapper.getByCode(transactionCode);
    }

    @Override
    @Transactional
    public StockTransaction createTransaction(String transactionType, Date transactionDate, String notes, 
                                          List<StockTransactionItem> items, String userId) {
        try {
            // Create transaction
            StockTransaction transaction = new StockTransaction();
            transaction.setId(UUIDGenerator.generate());
            transaction.setTransactionCode(generateTransactionCode(transactionType));
            transaction.setTransactionType(transactionType);
            transaction.setTransactionDate(transactionDate);
            transaction.setStatus("PENDING");
            transaction.setCreatedBy(userId);
            transaction.setNotes(notes);
            
            this.save(transaction);
            
            // Create transaction items
            if (items != null && !items.isEmpty()) {
                for (StockTransactionItem item : items) {
                    item.setId(UUIDGenerator.generate());
                    item.setTransactionId(transaction.getId());
                    stockTransactionItemService.save(item);
                }
            }
            
            return transaction;
        } catch (Exception e) {
            log.error("Error creating stock transaction", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public boolean approveTransaction(String transactionId, String approvedBy) {
        try {
            StockTransaction transaction = this.getById(transactionId);
            if (transaction != null && "PENDING".equals(transaction.getStatus())) {
                transaction.setStatus("APPROVED");
                transaction.setApprovedBy(approvedBy);
                transaction.setApprovedAt(new Date());
                this.updateById(transaction);
                
                // Process inventory updates
                List<StockTransactionItem> items = stockTransactionItemService.getByTransactionId(transactionId);
                for (StockTransactionItem item : items) {
                    processInventoryUpdate(item, transaction.getTransactionType());
                }
                
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Error approving transaction", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean cancelTransaction(String transactionId, String cancelReason) {
        try {
            StockTransaction transaction = this.getById(transactionId);
            if (transaction != null && "PENDING".equals(transaction.getStatus())) {
                transaction.setStatus("CANCELLED");
                transaction.setNotes(transaction.getNotes() + "\nCancelled reason: " + cancelReason);
                this.updateById(transaction);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Error cancelling transaction", e);
            return false;
        }
    }

    @Override
    public List<StockTransactionItem> getTransactionItems(String transactionId) {
        return stockTransactionItemService.getByTransactionId(transactionId);
    }

    @Override
    public Map<String, Object> getTransactionStatistics(Date startDate, Date endDate) {
        Map<String, Object> statistics = new java.util.HashMap<>();
        
        List<StockTransaction> transactions = getByDateRange(startDate, endDate);
        
        int totalTransactions = transactions.size();
        int totalInTransactions = (int) transactions.stream()
            .filter(t -> "IN".equals(t.getTransactionType()))
            .count();
        int totalOutTransactions = (int) transactions.stream()
            .filter(t -> "OUT".equals(t.getTransactionType()))
            .count();
        int totalTransferTransactions = (int) transactions.stream()
            .filter(t -> "TRANSFER".equals(t.getTransactionType()))
            .count();
        
        Map<String, Integer> transactionsByType = new java.util.HashMap<>();
        transactionsByType.put("IN", totalInTransactions);
        transactionsByType.put("OUT", totalOutTransactions);
        transactionsByType.put("TRANSFER", totalTransferTransactions);
        
        statistics.put("totalTransactions", totalTransactions);
        statistics.put("totalInTransactions", totalInTransactions);
        statistics.put("totalOutTransactions", totalOutTransactions);
        statistics.put("totalTransferTransactions", totalTransferTransactions);
        statistics.put("transactionsByType", transactionsByType);
        statistics.put("startDate", startDate);
        statistics.put("endDate", endDate);
        
        return statistics;
    }
    
    @Override
    public String generateTransactionCode(String transactionType) {
        // Generate transaction code based on type and timestamp
        String prefix = "";
        switch (transactionType) {
            case "IN":
                prefix = "IN";
                break;
            case "OUT":
                prefix = "OUT";
                break;
            case "TRANSFER":
                prefix = "TR";
                break;
            default:
                prefix = "TX";
                break;
        }
        
        return prefix + System.currentTimeMillis();
    }
    
    /**
     * Process inventory update based on transaction item
     * @param item Transaction item
     * @param transactionType Transaction type
     */
    private void processInventoryUpdate(StockTransactionItem item, String transactionType) {
        try {
            switch (transactionType) {
                case "IN":
                    // Stock in - increase inventory
                    inventoryService.processTransaction(
                        inventoryService.inventoryTransactionService.createTransaction(
                            item.getProductId(), "IN", item.getQuantity(),
                            item.getTransactionId(), "Stock in transaction", "admin"));
                    break;
                case "OUT":
                    // Stock out - decrease inventory
                    inventoryService.processTransaction(
                        inventoryService.inventoryTransactionService.createTransaction(
                            item.getProductId(), "OUT", item.getQuantity(),
                            item.getTransactionId(), "Stock out transaction", "admin"));
                    break;
                case "TRANSFER":
                    // Transfer - move inventory between locations
                    // For transfers, we don't change the total quantity, just move between locations
                    // Create a record of the transfer
                    inventoryService.inventoryTransactionService.createTransaction(
                        item.getProductId(), "TRANSFER", item.getQuantity(),
                        item.getTransactionId(), "Stock transfer transaction", "admin");
                    
                    // Update location information if needed
                    // This would require additional logic to handle fromLocationId and toLocationId
                    log.info("Transfer transaction processed for product: {} from location: {} to location: {}",
                        item.getProductId(), item.getFromLocationId(), item.getToLocationId());
                    break;
            }
        } catch (Exception e) {
            log.error("Error processing inventory update", e);
            throw new RuntimeException("Failed to process inventory update for transaction item: " + item.getId(), e);
        }
    }
}