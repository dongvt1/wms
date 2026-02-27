package com.cy.modules.warehouse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.UUIDGenerator;
import com.cy.modules.warehouse.entity.OrderNotification;
import com.cy.modules.warehouse.mapper.OrderNotificationMapper;
import com.cy.modules.warehouse.service.EmailNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Description: Email notification service implementation
 * @Author: jeecg
 * @Date: 2025-11-21
 * @Version: V1.0
 */
@Service
@Slf4j
public class EmailNotificationServiceImpl extends ServiceImpl<OrderNotificationMapper, OrderNotification> implements EmailNotificationService {

    @Autowired
    private OrderNotificationMapper orderNotificationMapper;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    private static final String FROM_EMAIL = "noreply@wms.com";
    private static final String SYSTEM_NAME = "Warehouse Management System";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String sendOrderStatusChangeNotification(String orderId, String orderCode, String fromStatus, 
                                                 String toStatus, String customerEmail, String customerName) {
        try {
            String subject = String.format("Order Status Update Notification - %s", orderCode);
            String content = generateOrderStatusChangeContent(orderCode, fromStatus, toStatus, customerName);
            
            return createNotification(orderId, "EMAIL", customerEmail, subject, content);
        } catch (Exception e) {
            log.error("Failed to create order status change notification", e);
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String sendOrderConfirmationNotification(String orderId, String orderCode, String customerEmail, 
                                                 String customerName, String orderAmount) {
        try {
            String subject = String.format("Order Confirmation Notification - %s", orderCode);
            String content = generateOrderConfirmationContent(orderCode, customerName, orderAmount);
            
            return createNotification(orderId, "EMAIL", customerEmail, subject, content);
        } catch (Exception e) {
            log.error("Failed to create order confirmation notification", e);
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String sendOrderCancellationNotification(String orderId, String orderCode, String customerEmail, 
                                                  String customerName, String cancelReason) {
        try {
            String subject = String.format("Order Cancellation Notification - %s", orderCode);
            String content = generateOrderCancellationContent(orderCode, customerName, cancelReason);
            
            return createNotification(orderId, "EMAIL", customerEmail, subject, content);
        } catch (Exception e) {
            log.error("Failed to create order cancellation notification", e);
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String sendOrderShippingNotification(String orderId, String orderCode, String customerEmail, 
                                              String customerName, String trackingNumber) {
        try {
            String subject = String.format("Order Shipping Notification - %s", orderCode);
            String content = generateOrderShippingContent(orderCode, customerName, trackingNumber);
            
            return createNotification(orderId, "EMAIL", customerEmail, subject, content);
        } catch (Exception e) {
            log.error("Failed to create order shipping notification", e);
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String sendOrderCompletionNotification(String orderId, String orderCode, String customerEmail, 
                                               String customerName) {
        try {
            String subject = String.format("Order Completion Notification - %s", orderCode);
            String content = generateOrderCompletionContent(orderCode, customerName);
            
            return createNotification(orderId, "EMAIL", customerEmail, subject, content);
        } catch (Exception e) {
            log.error("Failed to create order completion notification", e);
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String resendNotification(String notificationId) {
        try {
            OrderNotification notification = orderNotificationMapper.selectById(notificationId);
            if (notification == null) {
                return "Notification not found";
            }
            
            // Reset notification status
            notification.setStatus("PENDING");
            notification.setSentAt(null);
            notification.setErrorMessage(null);
            notification.setRetryCount(0);
            orderNotificationMapper.updateById(notification);
            
            // Process notification
            processNotification(notification);
            
            return "Notification resent successfully";
        } catch (Exception e) {
            log.error("Failed to resend notification", e);
            return "Failed to resend notification: " + e.getMessage();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String processPendingNotifications() {
        try {
            List<OrderNotification> pendingNotifications = orderNotificationMapper.getPendingNotifications();
            int successCount = 0;
            int failCount = 0;
            
            for (OrderNotification notification : pendingNotifications) {
                try {
                    processNotification(notification);
                    successCount++;
                } catch (Exception e) {
                    log.error("Failed to process notification: {}", notification.getId(), e);
                    failCount++;
                }
            }
            
            return String.format("Processing complete, success: %d, failed: %d", successCount, failCount);
        } catch (Exception e) {
            log.error("Failed to process pending notifications", e);
            return "Failed to process pending notifications: " + e.getMessage();
        }
    }

    @Override
    public List<Map<String, Object>> getOrderNotifications(String orderId) {
        return orderNotificationMapper.getNotificationsByOrderId(orderId);
    }

    @Override
    public OrderNotification getNotificationById(String notificationId) {
        return orderNotificationMapper.selectById(notificationId);
    }

    /**
     * Create notification record
     */
    private String createNotification(String orderId, String type, String recipient, String subject, String content) {
        OrderNotification notification = new OrderNotification();
        notification.setId(UUIDGenerator.generate());
        notification.setOrderId(orderId);
        notification.setType(type);
        notification.setRecipient(recipient);
        notification.setSubject(subject);
        notification.setContent(content);
        notification.setStatus("PENDING");
        notification.setRetryCount(0);
        notification.setCreateTime(new Date());
        
        orderNotificationMapper.insert(notification);
        
        // Process notification asynchronously
        try {
            processNotification(notification);
        } catch (Exception e) {
            log.error("Failed to process notification asynchronously: {}", notification.getId(), e);
        }
        
        return notification.getId();
    }

    /**
     * Process notification sending
     */
    private void processNotification(OrderNotification notification) {
        if (mailSender == null) {
            log.warn("Mail sender not configured, skipping email sending");
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_EMAIL);
            message.setTo(notification.getRecipient());
            message.setSubject(notification.getSubject());
            message.setText(notification.getContent());
            
            mailSender.send(message);
            
            // Update notification status
            notification.setStatus("SENT");
            notification.setSentAt(new Date());
            orderNotificationMapper.updateById(notification);
            
            log.info("Email sent successfully: {}", notification.getId());
        } catch (Exception e) {
            log.error("Failed to send email: {}", notification.getId(), e);
            
            // Update notification status
            notification.setStatus("FAILED");
            notification.setErrorMessage(e.getMessage());
            notification.setRetryCount(notification.getRetryCount() + 1);
            orderNotificationMapper.updateById(notification);
        }
    }

    /**
     * Generate order status change content
     */
    private String generateOrderStatusChangeContent(String orderCode, String fromStatus, String toStatus, String customerName) {
        StringBuilder content = new StringBuilder();
        content.append(String.format("Dear %s,\n\n", customerName));
        content.append(String.format("Your order %s status has been updated.\n\n", orderCode));
        content.append(String.format("Previous status: %s\n", fromStatus));
        content.append(String.format("New status: %s\n\n", toStatus));
        content.append("If you have any questions, please contact our customer support team.\n\n");
        content.append("This email was sent automatically. Please do not reply.\n");
        content.append(SYSTEM_NAME);
        return content.toString();
    }

    /**
     * Generate order confirmation content
     */
    private String generateOrderConfirmationContent(String orderCode, String customerName, String orderAmount) {
        StringBuilder content = new StringBuilder();
        content.append(String.format("Dear %s,\n\n", customerName));
        content.append(String.format("Your order %s has been confirmed.\n\n", orderCode));
        content.append(String.format("Order amount: %s\n\n", orderAmount));
        content.append("We will process your order as soon as possible.\n\n");
        content.append("If you have any questions, please contact our customer support team.\n\n");
        content.append("This email was sent automatically. Please do not reply.\n");
        content.append(SYSTEM_NAME);
        return content.toString();
    }

    /**
     * Generate order cancellation content
     */
    private String generateOrderCancellationContent(String orderCode, String customerName, String cancelReason) {
        StringBuilder content = new StringBuilder();
        content.append(String.format("Dear %s,\n\n", customerName));
        content.append(String.format("Your order %s has been cancelled.\n\n", orderCode));
        content.append(String.format("Cancellation reason: %s\n\n", cancelReason));
        content.append("If you have any questions, please contact our customer support team.\n\n");
        content.append("This email was sent automatically. Please do not reply.\n");
        content.append(SYSTEM_NAME);
        return content.toString();
    }

    /**
     * Generate order shipping content
     */
    private String generateOrderShippingContent(String orderCode, String customerName, String trackingNumber) {
        StringBuilder content = new StringBuilder();
        content.append(String.format("Dear %s,\n\n", customerName));
        content.append(String.format("Your order %s has been shipped.\n\n", orderCode));
        content.append(String.format("Tracking number: %s\n\n", trackingNumber));
        content.append("You can use the tracking number to check the delivery status.\n\n");
        content.append("If you have any questions, please contact our customer support team.\n\n");
        content.append("This email was sent automatically. Please do not reply.\n");
        content.append(SYSTEM_NAME);
        return content.toString();
    }

    /**
     * Generate order completion content
     */
    private String generateOrderCompletionContent(String orderCode, String customerName) {
        StringBuilder content = new StringBuilder();
        content.append(String.format("Dear %s,\n\n", customerName));
        content.append(String.format("Your order %s has been completed.\n\n", orderCode));
        content.append("Thank you for your purchase. We hope you are satisfied with our service.\n\n");
        content.append("If you have any questions, please contact our customer support team.\n\n");
        content.append("This email was sent automatically. Please do not reply.\n");
        content.append(SYSTEM_NAME);
        return content.toString();
    }
}