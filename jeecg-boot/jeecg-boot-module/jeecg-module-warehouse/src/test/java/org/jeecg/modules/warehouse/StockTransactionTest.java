package org.jeecg.modules.warehouse;

import org.jeecg.modules.warehouse.entity.StockTransaction;
import org.jeecg.modules.warehouse.entity.StockTransactionItem;
import org.jeecg.modules.warehouse.service.StockTransactionService;
import org.jeecg.modules.warehouse.service.StockTransactionItemService;
import org.jeecg.modules.warehouse.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Stock Transaction Test
 * 
 * @author BMad
 * @date 2025-11-20
 */
@SpringBootTest
@Transactional
public class StockTransactionTest {

    @Autowired
    private StockTransactionService stockTransactionService;
    
    @Autowired
    private StockTransactionItemService stockTransactionItemService;
    
    @Autowired
    private InventoryService inventoryService;

    @Test
    public void testCreateStockInTransaction() {
        // Create transaction items
        List<StockTransactionItem> items = new ArrayList<>();
        StockTransactionItem item = new StockTransactionItem();
        item.setProductId("1"); // Assuming product with ID 1 exists
        item.setQuantity(10);
        item.setUnitPrice(new java.math.BigDecimal("100.00"));
        item.setTotalPrice(new java.math.BigDecimal("1000.00"));
        item.setBatchNumber("BATCH001");
        items.add(item);
        
        // Create stock in transaction
        StockTransaction transaction = stockTransactionService.createTransaction(
            "IN", 
            new Date(), 
            "Test stock in transaction", 
            items, 
            "admin"
        );
        
        assertNotNull(transaction);
        assertNotNull(transaction.getId());
        assertEquals("IN", transaction.getTransactionType());
        assertEquals("PENDING", transaction.getStatus());
        
        // Verify transaction items were created
        List<StockTransactionItem> savedItems = stockTransactionItemService.getByTransactionId(transaction.getId());
        assertEquals(1, savedItems.size());
    }

    @Test
    public void testCreateStockOutTransaction() {
        // Create transaction items
        List<StockTransactionItem> items = new ArrayList<>();
        StockTransactionItem item = new StockTransactionItem();
        item.setProductId("1"); // Assuming product with ID 1 exists
        item.setQuantity(5);
        item.setUnitPrice(new java.math.BigDecimal("100.00"));
        item.setTotalPrice(new java.math.BigDecimal("500.00"));
        items.add(item);
        
        // Create stock out transaction
        StockTransaction transaction = stockTransactionService.createTransaction(
            "OUT", 
            new Date(), 
            "Test stock out transaction", 
            items, 
            "admin"
        );
        
        assertNotNull(transaction);
        assertNotNull(transaction.getId());
        assertEquals("OUT", transaction.getTransactionType());
        assertEquals("PENDING", transaction.getStatus());
    }

    @Test
    public void testCreateTransferTransaction() {
        // Create transaction items
        List<StockTransactionItem> items = new ArrayList<>();
        StockTransactionItem item = new StockTransactionItem();
        item.setProductId("1"); // Assuming product with ID 1 exists
        item.setQuantity(3);
        item.setUnitPrice(new java.math.BigDecimal("100.00"));
        item.setTotalPrice(new java.math.BigDecimal("300.00"));
        item.setFromLocationId("1"); // Assuming location with ID 1 exists
        item.setToLocationId("2"); // Assuming location with ID 2 exists
        items.add(item);
        
        // Create transfer transaction
        StockTransaction transaction = stockTransactionService.createTransaction(
            "TRANSFER", 
            new Date(), 
            "Test transfer transaction", 
            items, 
            "admin"
        );
        
        assertNotNull(transaction);
        assertNotNull(transaction.getId());
        assertEquals("TRANSFER", transaction.getTransactionType());
        assertEquals("PENDING", transaction.getStatus());
    }

    @Test
    public void testApproveTransaction() {
        // Create a transaction first
        List<StockTransactionItem> items = new ArrayList<>();
        StockTransactionItem item = new StockTransactionItem();
        item.setProductId("1");
        item.setQuantity(10);
        item.setUnitPrice(new java.math.BigDecimal("100.00"));
        item.setTotalPrice(new java.math.BigDecimal("1000.00"));
        items.add(item);
        
        StockTransaction transaction = stockTransactionService.createTransaction(
            "IN", 
            new Date(), 
            "Test transaction for approval", 
            items, 
            "admin"
        );
        
        // Approve the transaction
        boolean approved = stockTransactionService.approveTransaction(transaction.getId(), "admin");
        
        assertTrue(approved);
        
        // Verify transaction status changed
        StockTransaction approvedTransaction = stockTransactionService.getById(transaction.getId());
        assertEquals("APPROVED", approvedTransaction.getStatus());
        assertNotNull(approvedTransaction.getApprovedBy());
        assertNotNull(approvedTransaction.getApprovedAt());
    }

    @Test
    public void testCancelTransaction() {
        // Create a transaction first
        List<StockTransactionItem> items = new ArrayList<>();
        StockTransactionItem item = new StockTransactionItem();
        item.setProductId("1");
        item.setQuantity(10);
        item.setUnitPrice(new java.math.BigDecimal("100.00"));
        item.setTotalPrice(new java.math.BigDecimal("1000.00"));
        items.add(item);
        
        StockTransaction transaction = stockTransactionService.createTransaction(
            "IN", 
            new Date(), 
            "Test transaction for cancellation", 
            items, 
            "admin"
        );
        
        // Cancel the transaction
        boolean cancelled = stockTransactionService.cancelTransaction(transaction.getId(), "Test cancellation");
        
        assertTrue(cancelled);
        
        // Verify transaction status changed
        StockTransaction cancelledTransaction = stockTransactionService.getById(transaction.getId());
        assertEquals("CANCELLED", cancelledTransaction.getStatus());
        assertTrue(cancelledTransaction.getNotes().contains("Test cancellation"));
    }

    @Test
    public void testGetTransactionStatistics() {
        // Create some test transactions first
        // ... (This would typically involve creating multiple transactions)
        
        // Get statistics
        Date endDate = new Date();
        Date startDate = new Date();
        startDate.setTime(startDate.getTime() - (7L * 24 * 60 * 60 * 1000)); // 7 days ago
        
        var statistics = stockTransactionService.getTransactionStatistics(startDate, endDate);
        
        assertNotNull(statistics);
        assertTrue(statistics.containsKey("totalTransactions"));
        assertTrue(statistics.containsKey("totalInTransactions"));
        assertTrue(statistics.containsKey("totalOutTransactions"));
        assertTrue(statistics.containsKey("totalTransferTransactions"));
        assertTrue(statistics.containsKey("transactionsByType"));
    }

    @Test
    public void testGenerateTransactionCode() {
        String inCode = stockTransactionService.generateTransactionCode("IN");
        assertTrue(inCode.startsWith("IN"));
        
        String outCode = stockTransactionService.generateTransactionCode("OUT");
        assertTrue(outCode.startsWith("OUT"));
        
        String transferCode = stockTransactionService.generateTransactionCode("TRANSFER");
        assertTrue(transferCode.startsWith("TR"));
    }
}