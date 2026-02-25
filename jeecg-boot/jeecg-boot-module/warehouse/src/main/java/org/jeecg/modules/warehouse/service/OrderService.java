package org.jeecg.modules.warehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.modules.warehouse.entity.Order;
import org.jeecg.modules.warehouse.entity.OrderItem;
import org.jeecg.modules.warehouse.vo.OrderReportVO;
import org.jeecg.modules.warehouse.vo.OrderStatisticsVO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @Description: Order Service Interface
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
public interface OrderService extends IService<Order> {

    /**
     * Create order with order items
     * @param order Order information
     * @param orderItems List of order items
     * @return Result message
     */
    String createOrder(Order order, List<OrderItem> orderItems);

    /**
     * Update order information
     * @param order Order information
     * @return Result message
     */
    String updateOrder(Order order);

    /**
     * Cancel order
     * @param orderId Order ID
     * @param reason Cancellation reason
     * @return Result message
     */
    String cancelOrder(String orderId, String reason);

    /**
     * Update order status
     * @param orderId Order ID
     * @param newStatus New status
     * @param reason Update reason
     * @return Result message
     */
    String updateOrderStatus(String orderId, String newStatus, String reason);

    /**
     * Get order report with pagination
     * @param pageNo Page number
     * @param pageSize Page size
     * @param customerId Customer ID filter
     * @param status Status filter
     * @return Order report
     */
    OrderReportVO getOrderReport(Integer pageNo, Integer pageSize, String customerId, String status);

    /**
     * Search orders by code
     * @param orderCode Order code
     * @return List of matching orders
     */
    List<Map<String, Object>> searchOrdersByCode(String orderCode);

    /**
     * Search orders by customer name
     * @param customerName Customer name
     * @return List of matching orders
     */
    List<Map<String, Object>> searchOrdersByCustomerName(String customerName);

    /**
     * Get order detail with items
     * @param orderId Order ID
     * @return Order detail
     */
    Map<String, Object> getOrderDetail(String orderId);

    /**
     * Get order status history
     * @param orderId Order ID
     * @return Status history
     */
    List<Map<String, Object>> getOrderStatusHistory(String orderId);

    /**
     * Export order report to Excel
     * @param request HTTP request
     * @param response HTTP response
     * @param customerId Customer ID filter
     * @param status Status filter
     */
    void exportOrderReport(HttpServletRequest request, HttpServletResponse response, String customerId, String status);

    /**
     * Get order statistics
     * @return Order statistics
     */
    OrderStatisticsVO getStatistics();

    /**
     * Print order to PDF
     * @param orderId Order ID
     * @param response HTTP response
     */
    void printOrder(String orderId, HttpServletResponse response);

    /**
     * Generate order code
     * @return Generated order code
     */
    String generateOrderCode();

    /**
     * Calculate order amount
     * @param orderItems List of order items
     * @return Amount calculation result
     */
    Map<String, Object> calculateOrderAmount(List<OrderItem> orderItems);

    /**
     * Check and reserve inventory for order items
     * @param orderItems List of order items
     * @return Result message
     */
    String checkAndReserveInventory(List<OrderItem> orderItems);

    /**
     * Confirm order and deduct inventory
     * @param orderId Order ID
     * @return Result message
     */
    String confirmOrderAndDeductInventory(String orderId);

    /**
     * Cancel order and restore inventory
     * @param orderId Order ID
     * @return Result message
     */
    String cancelOrderAndRestoreInventory(String orderId);

    /**
     * Batch process orders
     * @param orderIds List of order IDs
     * @param action Action to perform
     * @param reason Reason for action
     * @return Batch processing result
     */
    Map<String, Object> batchProcessOrders(List<String> orderIds, String action, String reason);

    /**
     * Auto confirm pending orders
     * @return Auto confirmation result
     */
    Map<String, Object> autoConfirmOrders();

    /**
     * Generate stock out note
     * @param orderId Order ID
     * @param response HTTP response
     */
    void generateStockOutNote(String orderId, HttpServletResponse response);

    /**
     * Get order processing logs
     * @param orderId Order ID
     * @return Processing logs
     */
    List<Map<String, Object>> getOrderProcessingLogs(String orderId);

    /**
     * Resend order notification
     * @param notificationId Notification ID
     * @return Result message
     */
    String resendOrderNotification(String notificationId);

    /**
     * Process pending notifications
     * @return Result message
     */
    String processPendingNotifications();

    /**
     * Get order processing statistics
     * @return Processing statistics
     */
    Map<String, Object> getOrderProcessingStatistics();
}