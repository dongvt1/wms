package com.cy.modules.warehouse.service;

import com.cy.modules.warehouse.entity.OrderNotification;

import java.util.List;
import java.util.Map;

/**
 * @Description: Email notification service interface
 * @Author: jeecg
 * @Date: 2025-11-21
 * @Version: V1.0
 */
public interface EmailNotificationService {

    /**
     * Send order status change notification
     * @param orderId Order ID
     * @param orderCode Order code
     * @param fromStatus Previous status
     * @param toStatus New status
     * @param customerEmail Customer email
     * @param customerName Customer name
     * @return Notification ID
     */
    String sendOrderStatusChangeNotification(String orderId, String orderCode, String fromStatus, 
                                           String toStatus, String customerEmail, String customerName);

    /**
     * Send order confirmation notification
     * @param orderId Order ID
     * @param orderCode Order code
     * @param customerEmail Customer email
     * @param customerName Customer name
     * @param orderAmount Order amount
     * @return Notification ID
     */
    String sendOrderConfirmationNotification(String orderId, String orderCode, String customerEmail, 
                                          String customerName, String orderAmount);

    /**
     * Send order cancellation notification
     * @param orderId Order ID
     * @param orderCode Order code
     * @param customerEmail Customer email
     * @param customerName Customer name
     * @param cancelReason Cancellation reason
     * @return Notification ID
     */
    String sendOrderCancellationNotification(String orderId, String orderCode, String customerEmail, 
                                          String customerName, String cancelReason);

    /**
     * Send order shipping notification
     * @param orderId Order ID
     * @param orderCode Order code
     * @param customerEmail Customer email
     * @param customerName Customer name
     * @param trackingNumber Tracking number
     * @return Notification ID
     */
    String sendOrderShippingNotification(String orderId, String orderCode, String customerEmail, 
                                       String customerName, String trackingNumber);

    /**
     * Send order completion notification
     * @param orderId Order ID
     * @param orderCode Order code
     * @param customerEmail Customer email
     * @param customerName Customer name
     * @return Notification ID
     */
    String sendOrderCompletionNotification(String orderId, String orderCode, String customerEmail, 
                                         String customerName);

    /**
     * Resend notification
     * @param notificationId Notification ID
     * @return Operation result
     */
    String resendNotification(String notificationId);

    /**
     * Process pending notifications
     * @return Processing result
     */
    String processPendingNotifications();

    /**
     * Get order notification list
     * @param orderId Order ID
     * @return Notification list
     */
    List<Map<String, Object>> getOrderNotifications(String orderId);

    /**
     * Get notification details
     * @param notificationId Notification ID
     * @return Notification details
     */
    OrderNotification getNotificationById(String notificationId);
}