package org.jeecg.modules.warehouse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.modules.warehouse.mapper.InventoryTransactionMapper;
import org.jeecg.modules.warehouse.service.InventoryTransactionService;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Description: InventoryTransaction Service Implementation
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
@Slf4j
@Service
public class InventoryTransactionServiceImpl extends ServiceImpl<InventoryTransactionMapper, InventoryTransaction> implements InventoryTransactionService {

    @Override
    public List<InventoryTransaction> getByProductId(String productId) {
        return baseMapper.getByProductId(productId);
    }

    @Override
    public List<InventoryTransaction> getByDateRange(Date startDate, Date endDate) {
        return baseMapper.getByDateRange(startDate, endDate);
    }

    @Override
    public List<InventoryTransaction> getByProductIdAndDateRange(String productId, Date startDate, Date endDate) {
        return baseMapper.getByProductIdAndDateRange(productId, startDate, endDate);
    }

    @Override
    public List<InventoryTransaction> getByTransactionType(String transactionType) {
        return baseMapper.getByTransactionType(transactionType);
    }

    @Override
    public List<InventoryTransaction> getTransactionSummary() {
        return baseMapper.getTransactionSummary();
    }

    @Override
    public InventoryTransaction createTransaction(String productId, String transactionType, Integer quantity, 
                                          String referenceId, String reason, String userId) {
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setId(UUIDGenerator.generate());
        transaction.setProductId(productId);
        transaction.setTransactionType(transactionType);
        transaction.setQuantity(quantity);
        transaction.setReferenceId(referenceId);
        transaction.setReason(reason);
        transaction.setUserId(userId);
        transaction.setCreatedAt(new Date());
        
        this.save(transaction);
        return transaction;
    }

    @Override
    public Map<String, Object> getTransactionStatistics(Date startDate, Date endDate) {
        Map<String, Object> statistics = new HashMap<>();
        
        List<InventoryTransaction> transactions = getByDateRange(startDate, endDate);
        
        int totalTransactions = transactions.size();
        int totalInQuantity = transactions.stream()
            .filter(t -> "IN".equals(t.getTransactionType()))
            .mapToInt(InventoryTransaction::getQuantity)
            .sum();
        int totalOutQuantity = transactions.stream()
            .filter(t -> "OUT".equals(t.getTransactionType()))
            .mapToInt(InventoryTransaction::getQuantity)
            .sum();
        int totalAdjustQuantity = transactions.stream()
            .filter(t -> "ADJUST".equals(t.getTransactionType()))
            .mapToInt(InventoryTransaction::getQuantity)
            .sum();
        
        Map<String, Integer> transactionsByType = new HashMap<>();
        transactionsByType.put("IN", totalInQuantity);
        transactionsByType.put("OUT", totalOutQuantity);
        transactionsByType.put("ADJUST", totalAdjustQuantity);
        
        statistics.put("totalTransactions", totalTransactions);
        statistics.put("totalInQuantity", totalInQuantity);
        statistics.put("totalOutQuantity", totalOutQuantity);
        statistics.put("totalAdjustQuantity", totalAdjustQuantity);
        statistics.put("transactionsByType", transactionsByType);
        statistics.put("startDate", startDate);
        statistics.put("endDate", endDate);
        
        return statistics;
    }
}