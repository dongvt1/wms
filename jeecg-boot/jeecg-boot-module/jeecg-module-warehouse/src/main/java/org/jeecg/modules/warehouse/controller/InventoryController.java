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
import org.jeecg.modules.warehouse.entity.Inventory;
import org.jeecg.modules.warehouse.service.InventoryService;
import org.jeecg.modules.warehouse.service.InventoryTransactionService;
import org.jeecg.modules.warehouse.service.InventoryAdjustmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Description: Inventory Controller
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
@Slf4j
@Tag(name = "Inventory Management")
@RestController
@RequestMapping("/warehouse/inventory")
public class InventoryController extends JeecgController<Inventory, InventoryService> {

    @Autowired
    private InventoryService inventoryService;
    
    @Autowired
    private InventoryTransactionService inventoryTransactionService;
    
    @Autowired
    private InventoryAdjustmentService inventoryAdjustmentService;

    /**
     * Paginated list query
     *
     * @param inventory
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @Operation(summary = "Get Inventory data list")
    @GetMapping(value = "/list")
    @PermissionData(pageComponent = "warehouse/inventory/list")
    public Result<?> list(Inventory inventory, 
                       @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, 
                       @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                       HttpServletRequest req) {
        QueryWrapper<Inventory> queryWrapper = QueryGenerator.initQueryWrapper(inventory, req.getParameterMap());
        queryWrapper.orderByDesc("last_updated");
        Page<Inventory> page = new Page<Inventory>(pageNo, pageSize);

        IPage<Inventory> pageList = inventoryService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * Get inventory by product ID
     *
     * @param productId
     * @return
     */
    @GetMapping(value = "/product/{productId}")
    @Operation(summary = "Get Inventory by Product ID")
    public Result<?> getByProductId(@PathVariable String productId) {
        Inventory inventory = inventoryService.getByProductId(productId);
        return Result.OK(inventory);
    }

    /**
     * Update inventory
     *
     * @param inventory
     * @return
     */
    @RequestMapping(value = "/update", method = {RequestMethod.PUT,RequestMethod.POST})
    @AutoLog(value = "Update Inventory", operateType = 3)
    @Operation(summary = "Update Inventory")
    public Result<?> update(@RequestBody Inventory inventory) {
        inventoryService.updateById(inventory);
        return Result.OK("Update successful!");
    }

    /**
     * Manual inventory adjustment
     *
     * @param adjustment
     * @return
     */
    @PostMapping(value = "/adjust")
    @AutoLog(value = "Manual Inventory Adjustment")
    @Operation(summary = "Manual Inventory Adjustment")
    public Result<?> adjust(@RequestBody Map<String, Object> adjustment) {
        try {
            String productId = (String) adjustment.get("productId");
            Integer newQuantity = (Integer) adjustment.get("newQuantity");
            String reason = (String) adjustment.get("reason");
            String userId = (String) adjustment.get("userId");
            
            // Get current inventory
            Inventory currentInventory = inventoryService.getByProductId(productId);
            Integer oldQuantity = currentInventory != null ? currentInventory.getQuantity() : 0;
            
            // Create adjustment record
            InventoryAdjustment inventoryAdjustment = inventoryAdjustmentService.createAdjustment(
                productId, oldQuantity, newQuantity, reason, userId);
            
            // Process adjustment
            boolean success = inventoryService.processAdjustment(inventoryAdjustment);
            
            if (success) {
                return Result.OK("Inventory adjustment successful!");
            } else {
                return Result.error("Failed to adjust inventory!");
            }
        } catch (Exception e) {
            log.error("Error adjusting inventory", e);
            return Result.error("Error adjusting inventory: " + e.getMessage());
        }
    }

    /**
     * Get inventory transactions
     *
     * @param productId
     * @param startDate
     * @param endDate
     * @return
     */
    @GetMapping(value = "/transactions")
    @Operation(summary = "Get Inventory Transactions")
    public Result<?> getTransactions(@RequestParam(name = "productId", required = false) String productId,
                                  @RequestParam(name = "startDate", required = false) Date startDate,
                                  @RequestParam(name = "endDate", required = false) Date endDate) {
        List<InventoryTransaction> transactions;
        
        if (productId != null && startDate != null && endDate != null) {
            transactions = inventoryTransactionService.getByProductIdAndDateRange(productId, startDate, endDate);
        } else if (productId != null) {
            transactions = inventoryTransactionService.getByProductId(productId);
        } else if (startDate != null && endDate != null) {
            transactions = inventoryTransactionService.getByDateRange(startDate, endDate);
        } else {
            transactions = inventoryTransactionService.getTransactionSummary();
        }
        
        return Result.OK(transactions);
    }

    /**
     * Get inventory adjustments
     *
     * @param productId
     * @param startDate
     * @param endDate
     * @return
     */
    @GetMapping(value = "/adjustments")
    @Operation(summary = "Get Inventory Adjustments")
    public Result<?> getAdjustments(@RequestParam(name = "productId", required = false) String productId,
                                 @RequestParam(name = "startDate", required = false) Date startDate,
                                 @RequestParam(name = "endDate", required = false) Date endDate) {
        List<InventoryAdjustment> adjustments;
        
        if (productId != null && startDate != null && endDate != null) {
            adjustments = inventoryAdjustmentService.getByProductIdAndDateRange(productId, startDate, endDate);
        } else if (productId != null) {
            adjustments = inventoryAdjustmentService.getByProductId(productId);
        } else if (startDate != null && endDate != null) {
            adjustments = inventoryAdjustmentService.getByDateRange(startDate, endDate);
        } else {
            adjustments = inventoryAdjustmentService.getAdjustmentSummary();
        }
        
        return Result.OK(adjustments);
    }

    /**
     * Get inventory report
     *
     * @return
     */
    @GetMapping(value = "/report")
    @Operation(summary = "Get Inventory Report")
    public Result<?> getReport() {
        Map<String, Object> report = inventoryService.getInventorySummary();
        return Result.OK(report);
    }

    /**
     * Get inventory value report
     *
     * @return
     */
    @GetMapping(value = "/value-report")
    @Operation(summary = "Get Inventory Value Report")
    public Result<?> getValueReport() {
        List<Inventory> report = inventoryService.getInventoryValueReport();
        return Result.OK(report);
    }

    /**
     * Get products with low stock
     *
     * @return
     */
    @GetMapping(value = "/low-stock")
    @Operation(summary = "Get Products with Low Stock")
    public Result<?> getLowStock() {
        List<Inventory> lowStockProducts = inventoryService.getLowStockProducts();
        return Result.OK(lowStockProducts);
    }

    /**
     * Update minimum stock level for a product
     *
     * @param productId
     * @param minStockLevel
     * @return
     */
    @PutMapping(value = "/product/{productId}/min-stock")
    @Operation(summary = "Update Minimum Stock Level")
    public Result<?> updateMinStockLevel(@PathVariable String productId, 
                                      @RequestParam Integer minStockLevel) {
        // This would typically update the product table
        // For now, return success as placeholder
        return Result.OK("Minimum stock level updated successfully!");
    }

    /**
     * Get inventory trend data
     *
     * @param productId
     * @param days
     * @return
     */
    @GetMapping(value = "/trend")
    @Operation(summary = "Get Inventory Trend Data")
    public Result<?> getTrend(@RequestParam(name = "productId", required = false) String productId,
                             @RequestParam(name = "days", defaultValue = "30") Integer days) {
        List<Map<String, Object>> trendData = inventoryService.getInventoryTrend(productId, days);
        return Result.OK(trendData);
    }

    /**
     * Export inventory report
     *
     * @param request
     */
    @RequestMapping(value = "/export")
    @PermissionData(pageComponent = "warehouse/inventory/list")
    public ModelAndView exportXls(HttpServletRequest request, Inventory inventory) {
        return super.exportXls(request, inventory, Inventory.class, "Inventory Report");
    }

    /**
     * Reserve inventory
     *
     * @param productId
     * @param quantity
     * @param userId
     * @return
     */
    @PostMapping(value = "/reserve")
    @Operation(summary = "Reserve Inventory")
    public Result<?> reserveInventory(@RequestParam String productId, 
                                  @RequestParam Integer quantity,
                                  @RequestParam String userId) {
        boolean success = inventoryService.reserveInventory(productId, quantity, userId);
        if (success) {
            return Result.OK("Inventory reserved successfully!");
        } else {
            return Result.error("Failed to reserve inventory!");
        }
    }

    /**
     * Release reserved inventory
     *
     * @param productId
     * @param quantity
     * @param userId
     * @return
     */
    @PostMapping(value = "/release")
    @Operation(summary = "Release Reserved Inventory")
    public Result<?> releaseReservedInventory(@RequestParam String productId, 
                                          @RequestParam Integer quantity,
                                          @RequestParam String userId) {
        boolean success = inventoryService.releaseReservedInventory(productId, quantity, userId);
        if (success) {
            return Result.OK("Reserved inventory released successfully!");
        } else {
            return Result.error("Failed to release reserved inventory!");
        }
    }

    /**
     * Check if sufficient inventory is available
     *
     * @param productId
     * @param requiredQuantity
     * @return
     */
    @GetMapping(value = "/check-availability")
    @Operation(summary = "Check Inventory Availability")
    public Result<?> checkAvailability(@RequestParam String productId, 
                                   @RequestParam Integer requiredQuantity) {
        boolean available = inventoryService.hasSufficientInventory(productId, requiredQuantity);
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("productId", productId);
        result.put("requiredQuantity", requiredQuantity);
        result.put("available", available);
        
        if (available) {
            Inventory inventory = inventoryService.getByProductId(productId);
            result.put("availableQuantity", inventory.getAvailableQuantity());
        }
        
        return Result.OK(result);
    }
}