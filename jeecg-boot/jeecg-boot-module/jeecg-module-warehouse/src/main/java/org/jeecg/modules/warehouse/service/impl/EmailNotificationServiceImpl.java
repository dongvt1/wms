package org.jeecg.modules.warehouse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.modules.warehouse.entity.OrderNotification;
import org.jeecg.modules.warehouse.mapper.OrderNotificationMapper;
import org.jeecg.modules.warehouse.service.EmailNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.internet.MimeMessage;
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
            String subject = String.format("订单状态更新通知 - %s", orderCode);
            String content = generateOrderStatusChangeContent(orderCode, fromStatus, toStatus, customerName);
            
            return createNotification(orderId, "EMAIL", customerEmail, subject, content);
        } catch (Exception e) {
            log.error("创建订单状态变更通知失败", e);
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String sendOrderConfirmationNotification(String orderId, String orderCode, String customerEmail, 
                                                 String customerName, String orderAmount) {
        try {
            String subject = String.format("订单确认通知 - %s", orderCode);
            String content = generateOrderConfirmationContent(orderCode, customerName, orderAmount);
            
            return createNotification(orderId, "EMAIL", customerEmail, subject, content);
        } catch (Exception e) {
            log.error("创建订单确认通知失败", e);
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String sendOrderCancellationNotification(String orderId, String orderCode, String customerEmail, 
                                                  String customerName, String cancelReason) {
        try {
            String subject = String.format("订单取消通知 - %s", orderCode);
            String content = generateOrderCancellationContent(orderCode, customerName, cancelReason);
            
            return createNotification(orderId, "EMAIL", customerEmail, subject, content);
        } catch (Exception e) {
            log.error("创建订单取消通知失败", e);
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String sendOrderShippingNotification(String orderId, String orderCode, String customerEmail, 
                                              String customerName, String trackingNumber) {
        try {
            String subject = String.format("订单配送通知 - %s", orderCode);
            String content = generateOrderShippingContent(orderCode, customerName, trackingNumber);
            
            return createNotification(orderId, "EMAIL", customerEmail, subject, content);
        } catch (Exception e) {
            log.error("创建订单配送通知失败", e);
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String sendOrderCompletionNotification(String orderId, String orderCode, String customerEmail, 
                                               String customerName) {
        try {
            String subject = String.format("订单完成通知 - %s", orderCode);
            String content = generateOrderCompletionContent(orderCode, customerName);
            
            return createNotification(orderId, "EMAIL", customerEmail, subject, content);
        } catch (Exception e) {
            log.error("创建订单完成通知失败", e);
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String resendNotification(String notificationId) {
        try {
            OrderNotification notification = orderNotificationMapper.selectById(notificationId);
            if (notification == null) {
                return "通知不存在";
            }
            
            // 重置通知状态
            notification.setStatus("PENDING");
            notification.setSentAt(null);
            notification.setErrorMessage(null);
            notification.setRetryCount(0);
            orderNotificationMapper.updateById(notification);
            
            // 处理通知
            processNotification(notification);
            
            return "通知重新发送成功";
        } catch (Exception e) {
            log.error("重新发送通知失败", e);
            return "重新发送通知失败: " + e.getMessage();
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
                    log.error("处理通知失败: {}", notification.getId(), e);
                    failCount++;
                }
            }
            
            return String.format("处理完成，成功: %d，失败: %d", successCount, failCount);
        } catch (Exception e) {
            log.error("处理待发送通知失败", e);
            return "处理待发送通知失败: " + e.getMessage();
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
     * 创建通知记录
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
        
        // 异步处理通知
        try {
            processNotification(notification);
        } catch (Exception e) {
            log.error("异步处理通知失败: {}", notification.getId(), e);
        }
        
        return notification.getId();
    }

    /**
     * 处理通知发送
     */
    private void processNotification(OrderNotification notification) {
        if (mailSender == null) {
            log.warn("邮件发送器未配置，跳过邮件发送");
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_EMAIL);
            message.setTo(notification.getRecipient());
            message.setSubject(notification.getSubject());
            message.setText(notification.getContent());
            
            mailSender.send(message);
            
            // 更新通知状态
            notification.setStatus("SENT");
            notification.setSentAt(new Date());
            orderNotificationMapper.updateById(notification);
            
            log.info("邮件发送成功: {}", notification.getId());
        } catch (Exception e) {
            log.error("邮件发送失败: {}", notification.getId(), e);
            
            // 更新通知状态
            notification.setStatus("FAILED");
            notification.setErrorMessage(e.getMessage());
            notification.setRetryCount(notification.getRetryCount() + 1);
            orderNotificationMapper.updateById(notification);
        }
    }

    /**
     * 生成订单状态变更内容
     */
    private String generateOrderStatusChangeContent(String orderCode, String fromStatus, String toStatus, String customerName) {
        StringBuilder content = new StringBuilder();
        content.append(String.format("尊敬的 %s，您好！\n\n", customerName));
        content.append(String.format("您的订单 %s 状态已更新。\n\n", orderCode));
        content.append(String.format("原状态：%s\n", fromStatus));
        content.append(String.format("新状态：%s\n\n", toStatus));
        content.append("如有任何疑问，请联系我们的客服团队。\n\n");
        content.append("此邮件由系统自动发送，请勿回复。\n");
        content.append(SYSTEM_NAME);
        return content.toString();
    }

    /**
     * 生成订单确认内容
     */
    private String generateOrderConfirmationContent(String orderCode, String customerName, String orderAmount) {
        StringBuilder content = new StringBuilder();
        content.append(String.format("尊敬的 %s，您好！\n\n", customerName));
        content.append(String.format("您的订单 %s 已确认。\n\n", orderCode));
        content.append(String.format("订单金额：%s\n\n", orderAmount));
        content.append("我们将尽快为您处理订单。\n\n");
        content.append("如有任何疑问，请联系我们的客服团队。\n\n");
        content.append("此邮件由系统自动发送，请勿回复。\n");
        content.append(SYSTEM_NAME);
        return content.toString();
    }

    /**
     * 生成订单取消内容
     */
    private String generateOrderCancellationContent(String orderCode, String customerName, String cancelReason) {
        StringBuilder content = new StringBuilder();
        content.append(String.format("尊敬的 %s，您好！\n\n", customerName));
        content.append(String.format("您的订单 %s 已取消。\n\n", orderCode));
        content.append(String.format("取消原因：%s\n\n", cancelReason));
        content.append("如有任何疑问，请联系我们的客服团队。\n\n");
        content.append("此邮件由系统自动发送，请勿回复。\n");
        content.append(SYSTEM_NAME);
        return content.toString();
    }

    /**
     * 生成订单配送内容
     */
    private String generateOrderShippingContent(String orderCode, String customerName, String trackingNumber) {
        StringBuilder content = new StringBuilder();
        content.append(String.format("尊敬的 %s，您好！\n\n", customerName));
        content.append(String.format("您的订单 %s 已发货。\n\n", orderCode));
        content.append(String.format("跟踪号：%s\n\n", trackingNumber));
        content.append("您可以使用跟踪号查询配送状态。\n\n");
        content.append("如有任何疑问，请联系我们的客服团队。\n\n");
        content.append("此邮件由系统自动发送，请勿回复。\n");
        content.append(SYSTEM_NAME);
        return content.toString();
    }

    /**
     * 生成订单完成内容
     */
    private String generateOrderCompletionContent(String orderCode, String customerName) {
        StringBuilder content = new StringBuilder();
        content.append(String.format("尊敬的 %s，您好！\n\n", customerName));
        content.append(String.format("您的订单 %s 已完成。\n\n", orderCode));
        content.append("感谢您的购买，希望您对我们的服务满意。\n\n");
        content.append("如有任何疑问，请联系我们的客服团队。\n\n");
        content.append("此邮件由系统自动发送，请勿回复。\n");
        content.append(SYSTEM_NAME);
        return content.toString();
    }
}