package org.jeecg.modules.warehouse.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.aspect.annotation.PermissionData;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.warehouse.entity.StockTransaction;
import org.jeecg.modules.warehouse.entity.StockTransactionItem;
import org.jeecg.modules.warehouse.service.StockTransactionService;
import org.jeecg.modules.warehouse.service.StockTransactionItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Description: StockTransaction Controller
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
@Slf4j
@Tag(name = "Stock Transaction Management")
@RestController
@RequestMapping("/warehouse/stock")
public class StockTransactionController extends JeecgController<StockTransaction, StockTransactionService> {

    @Autowired
    private StockTransactionService stockTransactionService;
    
    @Autowired
    private StockTransactionItemService stockTransactionItemService;

    /**
     * Paginated list query
     *
     * @param stockTransaction
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @Operation(summary = "Get Stock Transaction data list")
    @GetMapping(value = "/transactions")
    @PermissionData(pageComponent = "warehouse/stock/transactions")
    public Result<?> list(StockTransaction stockTransaction, 
                       @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, 
                       @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                       HttpServletRequest req) {
        QueryWrapper<StockTransaction> queryWrapper = QueryGenerator.initQueryWrapper(stockTransaction, req.getParameterMap());
        queryWrapper.orderByDesc("transaction_date");
        Page<StockTransaction> page = new Page<StockTransaction>(pageNo, pageSize);

        IPage<StockTransaction> pageList = stockTransactionService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * Create stock in transaction
     *
     * @param transactionData Transaction data
     * @return
     */
    @PostMapping(value = "/stock-in")
    @AutoLog(value = "Create Stock In Transaction")
    @Operation(summary = "Create Stock In Transaction")
    public Result<?> createStockIn(@RequestBody Map<String, Object> transactionData) {
        try {
            String transactionType = "IN";
            Date transactionDate = new Date();
            String notes = (String) transactionData.get("notes");
            String userId = (String) transactionData.get("userId");
            
            // Extract items from transaction data
            List<Map<String, Object>> items = (List<Map<String, Object>>) transactionData.get("items");
            
            StockTransaction transaction = stockTransactionService.createTransaction(
                transactionType, transactionDate, notes, null, userId);
            
            // Create transaction items
            if (items != null && !items.isEmpty()) {
                for (Map<String, Object> itemData : items) {
                    StockTransactionItem item = new StockTransactionItem();
                    item.setTransactionId(transaction.getId());
                    item.setProductId((String) itemData.get("productId"));
                    item.setQuantity((Integer) itemData.get("quantity"));
                    item.setUnitPrice((java.math.BigDecimal) itemData.get("unitPrice"));
                    item.setTotalPrice((java.math.BigDecimal) itemData.get("totalPrice"));
                    item.setBatchNumber((String) itemData.get("batchNumber"));
                    item.setExpiryDate((Date) itemData.get("expiryDate"));
                    
                    stockTransactionItemService.save(item);
                }
            }
            
            return Result.OK("Stock in transaction created successfully!");
        } catch (Exception e) {
            log.error("Error creating stock in transaction", e);
            return Result.error("Failed to create stock in transaction: " + e.getMessage());
        }
    }

    /**
     * Create stock out transaction
     *
     * @param transactionData Transaction data
     * @return
     */
    @PostMapping(value = "/stock-out")
    @AutoLog(value = "Create Stock Out Transaction")
    @Operation(summary = "Create Stock Out Transaction")
    public Result<?> createStockOut(@RequestBody Map<String, Object> transactionData) {
        try {
            String transactionType = "OUT";
            Date transactionDate = new Date();
            String notes = (String) transactionData.get("notes");
            String userId = (String) transactionData.get("userId");
            
            // Extract items from transaction data
            List<Map<String, Object>> items = (List<Map<String, Object>>) transactionData.get("items");
            
            StockTransaction transaction = stockTransactionService.createTransaction(
                transactionType, transactionDate, notes, null, userId);
            
            // Create transaction items
            if (items != null && !items.isEmpty()) {
                for (Map<String, Object> itemData : items) {
                    StockTransactionItem item = new StockTransactionItem();
                    item.setTransactionId(transaction.getId());
                    item.setProductId((String) itemData.get("productId"));
                    item.setQuantity((Integer) itemData.get("quantity"));
                    item.setUnitPrice((java.math.BigDecimal) itemData.get("unitPrice"));
                    item.setTotalPrice((java.math.BigDecimal) itemData.get("totalPrice"));
                    item.setBatchNumber((String) itemData.get("batchNumber"));
                    item.setExpiryDate((Date) itemData.get("expiryDate"));
                    
                    stockTransactionItemService.save(item);
                }
            }
            
            return Result.OK("Stock out transaction created successfully!");
        } catch (Exception e) {
            log.error("Error creating stock out transaction", e);
            return Result.error("Failed to create stock out transaction: " + e.getMessage());
        }
    }

    /**
     * Create transfer transaction
     *
     * @param transactionData Transaction data
     * @return
     */
    @PostMapping(value = "/transfer")
    @AutoLog(value = "Create Transfer Transaction")
    @Operation(summary = "Create Transfer Transaction")
    public Result<?> createTransfer(@RequestBody Map<String, Object> transactionData) {
        try {
            String transactionType = "TRANSFER";
            Date transactionDate = new Date();
            String notes = (String) transactionData.get("notes");
            String userId = (String) transactionData.get("userId");
            
            // Extract items from transaction data
            List<Map<String, Object>> items = (List<Map<String, Object>>) transactionData.get("items");
            
            StockTransaction transaction = stockTransactionService.createTransaction(
                transactionType, transactionDate, notes, null, userId);
            
            // Create transaction items
            if (items != null && !items.isEmpty()) {
                for (Map<String, Object> itemData : items) {
                    StockTransactionItem item = new StockTransactionItem();
                    item.setTransactionId(transaction.getId());
                    item.setProductId((String) itemData.get("productId"));
                    item.setQuantity((Integer) itemData.get("quantity"));
                    item.setUnitPrice((java.math.BigDecimal) itemData.get("unitPrice"));
                    item.setTotalPrice((java.math.BigDecimal) itemData.get("totalPrice"));
                    item.setFromLocationId((String) itemData.get("fromLocationId"));
                    item.setToLocationId((String) itemData.get("toLocationId"));
                    item.setBatchNumber((String) itemData.get("batchNumber"));
                    item.setExpiryDate((Date) itemData.get("expiryDate"));
                    
                    stockTransactionItemService.save(item);
                }
            }
            
            return Result.OK("Transfer transaction created successfully!");
        } catch (Exception e) {
            log.error("Error creating transfer transaction", e);
            return Result.error("Failed to create transfer transaction: " + e.getMessage());
        }
    }

    /**
     * Get transaction details
     *
     * @param id Transaction ID
     * @return
     */
    @GetMapping(value = "/transactions/{id}")
    @Operation(summary = "Get Transaction Details")
    public Result<?> getTransactionDetails(@PathVariable String id) {
        StockTransaction transaction = stockTransactionService.getById(id);
        List<StockTransactionItem> items = stockTransactionItemService.getByTransactionId(id);
        
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("transaction", transaction);
        result.put("items", items);
        
        return Result.OK(result);
    }

    /**
     * Approve transaction
     *
     * @param id Transaction ID
     * @param approvedBy Approved by
     * @return
     */
    @PutMapping(value = "/transactions/{id}/approve")
    @AutoLog(value = "Approve Transaction")
    @Operation(summary = "Approve Transaction")
    public Result<?> approveTransaction(@PathVariable String id, @RequestParam String approvedBy) {
        try {
            boolean success = stockTransactionService.approveTransaction(id, approvedBy);
            if (success) {
                return Result.OK("Transaction approved successfully!");
            } else {
                return Result.error("Failed to approve transaction!");
            }
        } catch (Exception e) {
            log.error("Error approving transaction", e);
            return Result.error("Error approving transaction: " + e.getMessage());
        }
    }

    /**
     * Cancel transaction
     *
     * @param id Transaction ID
     * @param cancelReason Cancel reason
     * @return
     */
    @PutMapping(value = "/transactions/{id}/cancel")
    @AutoLog(value = "Cancel Transaction")
    @Operation(summary = "Cancel Transaction")
    public Result<?> cancelTransaction(@PathVariable String id, @RequestParam String cancelReason) {
        try {
            boolean success = stockTransactionService.cancelTransaction(id, cancelReason);
            if (success) {
                return Result.OK("Transaction cancelled successfully!");
            } else {
                return Result.error("Failed to cancel transaction!");
            }
        } catch (Exception e) {
            log.error("Error cancelling transaction", e);
            return Result.error("Error cancelling transaction: " + e.getMessage());
        }
    }

    /**
     * Get transaction statistics
     *
     * @param startDate Start date
     * @param endDate End date
     * @return
     */
    @GetMapping(value = "/transactions/statistics")
    @Operation(summary = "Get Transaction Statistics")
    public Result<?> getTransactionStatistics(@RequestParam Date startDate, @RequestParam Date endDate) {
        try {
            Map<String, Object> statistics = stockTransactionService.getTransactionStatistics(startDate, endDate);
            return Result.OK(statistics);
        } catch (Exception e) {
            log.error("Error getting transaction statistics", e);
            return Result.error("Error getting transaction statistics: " + e.getMessage());
        }
    }

    /**
     * Print transaction
     *
     * @param id Transaction ID
     * @return
     */
    @GetMapping(value = "/transactions/{id}/print")
    @Operation(summary = "Print Transaction")
    public Result<?> printTransaction(@PathVariable String id, HttpServletResponse response) {
        try {
            StockTransaction transaction = stockTransactionService.getById(id);
            List<StockTransactionItem> items = stockTransactionService.getTransactionItems(id);
            
            // Generate PDF
            byte[] pdfData = generateTransactionPdf(transaction, items);
            
            // Set response headers
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=transaction_" + transaction.getTransactionCode() + ".pdf");
            response.setContentLength(pdfData.length);
            
            // Write PDF to response
            response.getOutputStream().write(pdfData);
            response.getOutputStream().flush();
            
            return null; // Return null for binary response
        } catch (Exception e) {
            log.error("Error printing transaction", e);
            return Result.error("Error printing transaction: " + e.getMessage());
        }
    }
    
    /**
     * Generate PDF for transaction
     */
    private byte[] generateTransactionPdf(StockTransaction transaction, List<StockTransactionItem> items) throws Exception {
        // This would use a PDF library like iText or JasperReports
        // For now, return a simple implementation
        String content = "Transaction Code: " + transaction.getTransactionCode() + "\n";
        content += "Transaction Type: " + transaction.getTransactionType() + "\n";
        content += "Status: " + transaction.getStatus() + "\n";
        content += "Transaction Date: " + transaction.getTransactionDate() + "\n";
        content += "Notes: " + transaction.getNotes() + "\n\n";
        
        content += "Transaction Items:\n";
        for (StockTransactionItem item : items) {
            content += "- Product ID: " + item.getProductId() + "\n";
            content += "  Quantity: " + item.getQuantity() + "\n";
            content += "  Unit Price: " + item.getUnitPrice() + "\n";
            content += "  Total Price: " + item.getTotalPrice() + "\n";
        }
        
        return content.getBytes();
    }
}