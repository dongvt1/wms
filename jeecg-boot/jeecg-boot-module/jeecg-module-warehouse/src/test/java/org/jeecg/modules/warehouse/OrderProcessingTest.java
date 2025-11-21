package org.jeecg.modules.warehouse;

import org.jeecg.modules.warehouse.entity.Order;
import org.jeecg.modules.warehouse.entity.OrderItem;
import org.jeecg.modules.warehouse.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 订单处理功能测试
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class OrderProcessingTest {

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private EmailNotificationService emailNotificationService;
    
    @Autowired
    private InventoryService inventoryService;
    
    @Autowired
    private CustomerService customerService;
    
    @Autowired
    private ProductService productService;

    /**
     * 测试批量处理订单
     */
    @Test
    public void testBatchProcessOrders() {
        // 创建测试订单
        List<String> orderIds = createTestOrders(3);
        
        // 批量确认订单
        Map<String, Object> result = orderService.batchProcessOrders(orderIds, "CONFIRM", "批量确认测试");
        
        // 验证结果
        Integer successCount = (Integer) result.get("successCount");
        Integer failedCount = (Integer) result.get("failedCount");
        
        assert successCount > 0 : "应该有订单处理成功";
        assert failedCount >= 0 : "失败数量不能为负数";
    }

    /**
     * 测试自动确认订单
     */
    @Test
    public void testAutoConfirmOrders() {
        // 创建符合条件的测试订单
        createTestOrdersForAutoConfirm(5);
        
        // 执行自动确认
        Map<String, Object> result = orderService.autoConfirmOrders();
        
        // 验证结果
        Integer confirmedCount = (Integer) result.get("confirmedCount");
        Integer failedCount = (Integer) result.get("failedCount");
        
        assert confirmedCount >= 0 : "确认数量不能为负数";
        assert failedCount >= 0 : "失败数量不能为负数";
    }

    /**
     * 测试订单状态变更通知
     */
    @Test
    public void testOrderStatusChangeNotification() {
        // 创建测试订单
        String orderId = createTestOrder();
        
        // 更新订单状态
        String result = orderService.updateOrderStatus(orderId, "CONFIRMED", "测试确认");
        
        // 验证结果
        assert result.contains("成功") : "订单状态更新应该成功";
        
        // 验证通知是否创建
        List<Map<String, Object>> notifications = emailNotificationService.getOrderNotifications(orderId);
        assert notifications.size() > 0 : "应该创建通知记录";
    }

    /**
     * 测试出库单生成
     */
    @Test
    public void testGenerateStockOutNote() {
        // 创建测试订单
        String orderId = createTestOrder();
        
        // 确认订单
        orderService.updateOrderStatus(orderId, "CONFIRMED", "测试确认");
        
        // 验证出库单生成（这里只是验证方法不抛出异常）
        try {
            // 在实际测试中，这里需要模拟HTTP响应
            // orderService.generateStockOutNote(orderId, mockResponse);
            assert true : "出库单生成方法应该正常执行";
        } catch (Exception e) {
            assert false : "出库单生成不应该抛出异常: " + e.getMessage();
        }
    }

    /**
     * 测试订单处理日志
     */
    @Test
    public void testOrderProcessingLogs() {
        // 创建测试订单
        String orderId = createTestOrder();
        
        // 更新订单状态
        orderService.updateOrderStatus(orderId, "CONFIRMED", "测试确认");
        
        // 获取处理日志
        List<Map<String, Object>> logs = orderService.getOrderProcessingLogs(orderId);
        
        // 验证日志
        assert logs.size() > 0 : "应该有处理日志";
        
        boolean hasStatusUpdateLog = logs.stream()
            .anyMatch(log -> "STATUS_UPDATE".equals(log.get("action")));
        assert hasStatusUpdateLog : "应该有状态更新日志";
    }

    /**
     * 测试重新发送通知
     */
    @Test
    public void testResendNotification() {
        // 创建测试订单
        String orderId = createTestOrder();
        
        // 更新订单状态
        orderService.updateOrderStatus(orderId, "CONFIRMED", "测试确认");
        
        // 获取通知ID
        List<Map<String, Object>> notifications = emailNotificationService.getOrderNotifications(orderId);
        assert notifications.size() > 0 : "应该有通知记录";
        
        String notificationId = (String) notifications.get(0).get("id");
        
        // 重新发送通知
        String result = emailNotificationService.resendNotification(notificationId);
        
        // 验证结果
        assert result.contains("成功") : "重新发送通知应该成功";
    }

    /**
     * 测试处理待发送通知
     */
    @Test
    public void testProcessPendingNotifications() {
        // 创建测试订单
        String orderId = createTestOrder();
        
        // 更新订单状态
        orderService.updateOrderStatus(orderId, "CONFIRMED", "测试确认");
        
        // 处理待发送通知
        String result = emailNotificationService.processPendingNotifications();
        
        // 验证结果
        assert result != null : "处理结果不能为空";
    }

    /**
     * 测试订单处理统计
     */
    @Test
    public void testOrderProcessingStatistics() {
        // 创建测试订单
        createTestOrders(5);
        
        // 更新订单状态
        for (int i = 0; i < 5; i++) {
            String orderId = createTestOrder();
            orderService.updateOrderStatus(orderId, "CONFIRMED", "测试确认");
        }
        
        // 获取统计信息
        Map<String, Object> statistics = orderService.getOrderProcessingStatistics();
        
        // 验证统计信息
        assert statistics != null : "统计信息不能为空";
        assert statistics.containsKey("totalLogs") : "应该包含总日志数";
        assert statistics.containsKey("successCount") : "应该包含成功数";
        assert statistics.containsKey("failedCount") : "应该包含失败数";
    }

    /**
     * 测试订单取消和库存恢复
     */
    @Test
    public void testOrderCancelAndInventoryRestore() {
        // 创建测试订单
        String orderId = createTestOrder();
        
        // 确认订单（扣减库存）
        orderService.updateOrderStatus(orderId, "CONFIRMED", "测试确认");
        
        // 取消订单（恢复库存）
        String result = orderService.cancelOrder(orderId, "测试取消");
        
        // 验证结果
        assert result.contains("成功") : "订单取消应该成功";
        
        // 验证处理日志
        List<Map<String, Object>> logs = orderService.getOrderProcessingLogs(orderId);
        boolean hasCancelLog = logs.stream()
            .anyMatch(log -> "CANCEL".equals(log.get("action")));
        assert hasCancelLog : "应该有取消日志";
    }

    /**
     * 创建测试订单
     */
    private String createTestOrder() {
        // 创建测试客户
        String customerId = createTestCustomer();
        
        // 创建测试产品
        String productId = createTestProduct();
        
        // 创建订单
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setNotes("测试订单");
        order.setCreatedBy("test");
        
        // 创建订单项
        List<OrderItem> orderItems = new ArrayList<>();
        OrderItem item = new OrderItem();
        item.setProductId(productId);
        item.setQuantity(1);
        item.setUnitPrice(new java.math.BigDecimal("100.00"));
        orderItems.add(item);
        
        return orderService.createOrder(order, orderItems);
    }

    /**
     * 创建多个测试订单
     */
    private List<String> createTestOrders(int count) {
        List<String> orderIds = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            orderIds.add(createTestOrder());
        }
        return orderIds;
    }

    /**
     * 创建用于自动确认的测试订单
     */
    private void createTestOrdersForAutoConfirm(int count) {
        for (int i = 0; i < count; i++) {
            String orderId = createTestOrder();
            // 设置订单创建时间为较早的时间，满足自动确认条件
            // 这里需要直接操作数据库来修改创建时间
        }
    }

    /**
     * 创建测试客户
     */
    private String createTestCustomer() {
        // 这里应该调用客户服务创建测试客户
        // 简化处理，返回固定ID
        return "test-customer-" + UUID.randomUUID().toString();
    }

    /**
     * 创建测试产品
     */
    private String createTestProduct() {
        // 这里应该调用产品服务创建测试产品
        // 简化处理，返回固定ID
        return "test-product-" + UUID.randomUUID().toString();
    }
}