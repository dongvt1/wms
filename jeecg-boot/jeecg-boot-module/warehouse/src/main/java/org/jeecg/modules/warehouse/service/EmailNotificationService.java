package org.jeecg.modules.warehouse.service;

import org.jeecg.modules.warehouse.entity.OrderNotification;

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
     * 发送订单状态变更通知
     * @param orderId 订单ID
     * @param orderCode 订单编码
     * @param fromStatus 原状态
     * @param toStatus 新状态
     * @param customerEmail 客户邮箱
     * @param customerName 客户姓名
     * @return 通知ID
     */
    String sendOrderStatusChangeNotification(String orderId, String orderCode, String fromStatus, 
                                           String toStatus, String customerEmail, String customerName);

    /**
     * 发送订单确认通知
     * @param orderId 订单ID
     * @param orderCode 订单编码
     * @param customerEmail 客户邮箱
     * @param customerName 客户姓名
     * @param orderAmount 订单金额
     * @return 通知ID
     */
    String sendOrderConfirmationNotification(String orderId, String orderCode, String customerEmail, 
                                          String customerName, String orderAmount);

    /**
     * 发送订单取消通知
     * @param orderId 订单ID
     * @param orderCode 订单编码
     * @param customerEmail 客户邮箱
     * @param customerName 客户姓名
     * @param cancelReason 取消原因
     * @return 通知ID
     */
    String sendOrderCancellationNotification(String orderId, String orderCode, String customerEmail, 
                                          String customerName, String cancelReason);

    /**
     * 发送订单配送通知
     * @param orderId 订单ID
     * @param orderCode 订单编码
     * @param customerEmail 客户邮箱
     * @param customerName 客户姓名
     * @param trackingNumber 跟踪号
     * @return 通知ID
     */
    String sendOrderShippingNotification(String orderId, String orderCode, String customerEmail, 
                                       String customerName, String trackingNumber);

    /**
     * 发送订单完成通知
     * @param orderId 订单ID
     * @param orderCode 订单编码
     * @param customerEmail 客户邮箱
     * @param customerName 客户姓名
     * @return 通知ID
     */
    String sendOrderCompletionNotification(String orderId, String orderCode, String customerEmail, 
                                         String customerName);

    /**
     * 重新发送通知
     * @param notificationId 通知ID
     * @return 操作结果
     */
    String resendNotification(String notificationId);

    /**
     * 处理待发送的通知
     * @return 处理结果
     */
    String processPendingNotifications();

    /**
     * 获取订单通知列表
     * @param orderId 订单ID
     * @return 通知列表
     */
    List<Map<String, Object>> getOrderNotifications(String orderId);

    /**
     * 获取通知详情
     * @param notificationId 通知ID
     * @return 通知详情
     */
    OrderNotification getNotificationById(String notificationId);
}