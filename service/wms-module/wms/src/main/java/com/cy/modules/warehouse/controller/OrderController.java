package com.cy.modules.warehouse.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import com.cy.modules.warehouse.entity.Order;
import com.cy.modules.warehouse.entity.OrderItem;
import com.cy.modules.warehouse.service.OrderService;
import com.cy.modules.warehouse.vo.OrderReportVO;
import com.cy.modules.warehouse.vo.OrderStatisticsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Order Management Controller
 */
@RestController
@RequestMapping("/warehouse/orders")
@Slf4j
public class OrderController extends JeecgController<Order, OrderService> {

    @Autowired
    private OrderService orderService;

    /**
     * Paginated list query
     */
    @GetMapping(value = "/list")
    public Result<IPage<Order>> queryPageList(Order order,
                                           @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                           @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                           @RequestParam(name = "sort", required = false) String sort,
                                           HttpServletRequest req) {
        QueryWrapper<Order> queryWrapper = QueryGenerator.initQueryWrapper(order, req.getParameterMap());
        Page<Order> page = new Page<Order>(pageNo, pageSize);
        IPage<Order> pageList = orderService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * Add order
     */
    @AutoLog(value = "Order Management - Add Order")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody Map<String, Object> params) {
        try {
            Order order = new Order();
            order.setCustomerId((String) params.get("customerId"));
            order.setNotes((String) params.get("notes"));
            order.setCreatedBy((String) params.get("createdBy"));
            
            @SuppressWarnings("unchecked")
            List<OrderItem> orderItems = (List<OrderItem>) params.get("orderItems");
            
            String result = orderService.createOrder(order, orderItems);
            if (result.contains("successfully")) {
                return Result.OK(result);
            } else {
                return Result.error(result);
            }
        } catch (Exception e) {
            log.error("Failed to add order", e);
            return Result.error("Failed to add order: " + e.getMessage());
        }
    }

    /**
     * Edit order
     */
    @AutoLog(value = "Order Management - Edit Order")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody Order order) {
        String result = orderService.updateOrder(order);
        if (result.contains("successfully")) {
            return Result.OK(result);
        } else {
            return Result.error(result);
        }
    }

    /**
     * Delete order by ID
     */
    @AutoLog(value = "Order Management - Delete Order by ID")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        // Orders cannot be deleted directly, only cancelled
        return Result.error("Orders cannot be deleted directly, please use the cancel function");
    }

    /**
     * Batch delete orders
     */
    @AutoLog(value = "Order Management - Batch Delete Orders")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        // Orders cannot be deleted directly, only cancelled
        return Result.error("Orders cannot be deleted directly, please use the cancel function");
    }

    /**
     * Query order by ID
     */
    @GetMapping(value = "/queryById")
    public Result<Map<String, Object>> queryById(@RequestParam(name = "id", required = true) String id) {
        Map<String, Object> orderDetail = orderService.getOrderDetail(id);
        if (orderDetail.isEmpty()) {
            return Result.error("Data not found");
        }
        return Result.OK(orderDetail);
    }

    /**
     * Cancel order
     */
    @AutoLog(value = "Order Management - Cancel Order")
    @PutMapping(value = "/cancel")
    public Result<String> cancelOrder(@RequestBody Map<String, String> params) {
        String orderId = params.get("orderId");
        String reason = params.get("reason");
        
        String result = orderService.cancelOrder(orderId, reason);
        if (result.contains("successfully")) {
            return Result.OK(result);
        } else {
            return Result.error(result);
        }
    }

    /**
     * Update order status
     */
    @AutoLog(value = "Order Management - Update Order Status")
    @PutMapping(value = "/status")
    public Result<String> updateOrderStatus(@RequestBody Map<String, String> params) {
        String orderId = params.get("orderId");
        String newStatus = params.get("newStatus");
        String reason = params.get("reason");
        
        String result = orderService.updateOrderStatus(orderId, newStatus, reason);
        if (result.contains("successfully")) {
            return Result.OK(result);
        } else {
            return Result.error(result);
        }
    }

    /**
     * Get order report
     */
    @GetMapping(value = "/report")
    public Result<OrderReportVO> getReport(
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "customerId", required = false) String customerId,
            @RequestParam(name = "status", required = false) String status,
            HttpServletRequest req) {
        
        OrderReportVO report = orderService.getOrderReport(pageNo, pageSize, customerId, status);
        return Result.OK(report);
    }

    /**
     * Search orders by order code
     */
    @GetMapping(value = "/search/code")
    public Result<List<Map<String, Object>>> searchByCode(@RequestParam(name = "orderCode", required = true) String orderCode) {
        List<Map<String, Object>> orders = orderService.searchOrdersByCode(orderCode);
        return Result.OK(orders);
    }

    /**
     * Search orders by customer name
     */
    @GetMapping(value = "/search/customer")
    public Result<List<Map<String, Object>>> searchByCustomerName(@RequestParam(name = "customerName", required = true) String customerName) {
        List<Map<String, Object>> orders = orderService.searchOrdersByCustomerName(customerName);
        return Result.OK(orders);
    }

    /**
     * Get order status history
     */
    @GetMapping(value = "/{orderId}/status-history")
    public Result<List<Map<String, Object>>> getStatusHistory(@PathVariable("orderId") String orderId) {
        List<Map<String, Object>> history = orderService.getOrderStatusHistory(orderId);
        return Result.OK(history);
    }

    /**
     * Export order report
     */
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void exportXls(HttpServletRequest request, HttpServletResponse response) {
        // Parameter validation
        String customerId = request.getParameter("customerId");
        String status = request.getParameter("status");
        
        // Export Excel
        orderService.exportOrderReport(request, response, customerId, status);
    }

    /**
     * Print order
     */
    @GetMapping(value = "/{orderId}/print")
    public void printOrder(@PathVariable("orderId") String orderId,
                         HttpServletResponse response) {
        orderService.printOrder(orderId, response);
    }

    /**
     * Get order statistics
     */
    @GetMapping(value = "/statistics")
    public Result<OrderStatisticsVO> getStatistics() {
        OrderStatisticsVO statistics = orderService.getStatistics();
        return Result.OK(statistics);
    }

    /**
     * Generate order code
     */
    @GetMapping(value = "/generate-code")
    public Result<String> generateOrderCode() {
        String orderCode = orderService.generateOrderCode();
        return Result.OK(orderCode);
    }

    /**
     * Calculate order amount
     */
    @PostMapping(value = "/calculate-amount")
    public Result<Map<String, Object>> calculateAmount(@RequestBody List<OrderItem> orderItems) {
        Map<String, Object> amount = orderService.calculateOrderAmount(orderItems);
        return Result.OK(amount);
    }

    /**
     * Check inventory
     */
    @PostMapping(value = "/check-inventory")
    public Result<String> checkInventory(@RequestBody List<OrderItem> orderItems) {
        String result = orderService.checkAndReserveInventory(orderItems);
        if (result.contains("successfully")) {
            return Result.OK(result);
        } else {
            return Result.error(result);
        }
    }

    /**
     * Confirm order
     */
    @AutoLog(value = "Order Management - Confirm Order")
    @PutMapping(value = "/{orderId}/confirm")
    public Result<String> confirmOrder(@PathVariable("orderId") String orderId) {
        String result = orderService.updateOrderStatus(orderId, "CONFIRMED", "Confirm order");
        if (result.contains("successfully")) {
            return Result.OK(result);
        } else {
            return Result.error(result);
        }
    }

    /**
     * Start shipping
     */
    @AutoLog(value = "Order Management - Start Shipping")
    @PutMapping(value = "/{orderId}/ship")
    public Result<String> shipOrder(@PathVariable("orderId") String orderId) {
        String result = orderService.updateOrderStatus(orderId, "SHIPPING", "Start shipping");
        if (result.contains("successfully")) {
            return Result.OK(result);
        } else {
            return Result.error(result);
        }
    }

    /**
     * Complete order
     */
    @AutoLog(value = "Order Management - Complete Order")
    @PutMapping(value = "/{orderId}/complete")
    public Result<String> completeOrder(@PathVariable("orderId") String orderId) {
        String result = orderService.updateOrderStatus(orderId, "COMPLETED", "Order completed");
        if (result.contains("successfully")) {
            return Result.OK(result);
        } else {
            return Result.error(result);
        }
    }

    /**
     * Search orders
     */
    @GetMapping(value = "/search")
    public Result<IPage<Order>> searchOrders(
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "keyword", required = false) String keyword,
            HttpServletRequest req) {
        
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        
        if (oConvertUtils.isNotEmpty(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                .like("order_code", keyword)
                .or()
                .like("customer_name", keyword));
        }
        
        Page<Order> page = new Page<Order>(pageNo, pageSize);
        IPage<Order> pageList = orderService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * Batch process orders
     */
    @AutoLog(value = "Order Management - Batch Process Orders")
    @PostMapping(value = "/batch-process")
    public Result<Map<String, Object>> batchProcessOrders(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<String> orderIds = (List<String>) params.get("orderIds");
        String action = (String) params.get("action");
        String reason = (String) params.get("reason");
        
        Map<String, Object> result = orderService.batchProcessOrders(orderIds, action, reason);
        return Result.OK(result);
    }

    /**
     * Auto confirm orders
     */
    @AutoLog(value = "Order Management - Auto Confirm Orders")
    @PostMapping(value = "/auto-confirm")
    public Result<Map<String, Object>> autoConfirmOrders() {
        Map<String, Object> result = orderService.autoConfirmOrders();
        return Result.OK(result);
    }

    /**
     * Generate stock-out note
     */
    @GetMapping(value = "/{orderId}/stock-out-note")
    public void generateStockOutNote(@PathVariable("orderId") String orderId,
                                   HttpServletResponse response) {
        orderService.generateStockOutNote(orderId, response);
    }

    /**
     * Get order processing logs
     */
    @GetMapping(value = "/{orderId}/processing-logs")
    public Result<List<Map<String, Object>>> getProcessingLogs(@PathVariable("orderId") String orderId) {
        List<Map<String, Object>> logs = orderService.getOrderProcessingLogs(orderId);
        return Result.OK(logs);
    }

    /**
     * Resend order notification
     */
    @AutoLog(value = "Order Management - Resend Order Notification")
    @PostMapping(value = "/resend-notification")
    public Result<String> resendNotification(@RequestBody Map<String, String> params) {
        String notificationId = params.get("notificationId");
        String result = orderService.resendOrderNotification(notificationId);
        if (result.contains("successfully")) {
            return Result.OK(result);
        } else {
            return Result.error(result);
        }
    }

    /**
     * Process pending notifications
     */
    @AutoLog(value = "Order Management - Process Pending Notifications")
    @PostMapping(value = "/process-notifications")
    public Result<String> processPendingNotifications() {
        String result = orderService.processPendingNotifications();
        return Result.OK(result);
    }

    /**
     * Get order processing statistics
     */
    @GetMapping(value = "/processing-statistics")
    public Result<Map<String, Object>> getProcessingStatistics() {
        Map<String, Object> statistics = orderService.getOrderProcessingStatistics();
        return Result.OK(statistics);
    }
}