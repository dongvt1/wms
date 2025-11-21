package org.jeecg.modules.warehouse;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.jeecg.modules.warehouse.entity.Order;
import org.jeecg.modules.warehouse.entity.OrderItem;
import org.jeecg.modules.warehouse.entity.OrderStatusHistory;
import org.jeecg.modules.warehouse.mapper.*;
import org.jeecg.modules.warehouse.service.*;
import org.jeecg.modules.warehouse.vo.OrderStatisticsVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 订单管理测试类
 */
@SpringBootTest
@SpringJUnitConfig
@DisplayName("订单管理测试")
public class OrderManagementTest {

    @Mock
    private OrderMapper orderMapper;
    
    @Mock
    private OrderItemMapper orderItemMapper;
    
    @Mock
    private OrderStatusHistoryMapper orderStatusHistoryMapper;
    
    @Mock
    private InventoryService inventoryService;
    
    @Mock
    private CustomerService customerService;
    
    @Mock
    private ProductService productService;
    
    @InjectMocks
    private OrderServiceImpl orderService;

    private Order testOrder;
    private List<OrderItem> testOrderItems;
    private OrderStatusHistory testStatusHistory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // 初始化测试订单
        testOrder = new Order();
        testOrder.setId("test-order-id");
        testOrder.setOrderCode("TEST001");
        testOrder.setCustomerId("test-customer-id");
        testOrder.setCustomerName("Test Customer");
        testOrder.setStatus("PENDING");
        testOrder.setTotalAmount(new BigDecimal("1000.00"));
        testOrder.setDiscountAmount(new BigDecimal("50.00"));
        testOrder.setTaxAmount(new BigDecimal("95.00"));
        testOrder.setFinalAmount(new BigDecimal("1045.00"));
        testOrder.setNotes("Test order notes");
        testOrder.setCreatedBy("test-user");
        
        // 初始化测试订单项
        testOrderItems = new ArrayList<>();
        OrderItem item1 = new OrderItem();
        item1.setId("test-item-1");
        item1.setOrderId("test-order-id");
        item1.setProductId("test-product-1");
        item1.setProductName("Test Product 1");
        item1.setProductCode("TP001");
        item1.setQuantity(2);
        item1.setUnitPrice(new BigDecimal("500.00"));
        item1.setTotalPrice(new BigDecimal("1000.00"));
        item1.setDiscountAmount(new BigDecimal("25.00"));
        item1.setFinalAmount(new BigDecimal("975.00"));
        
        OrderItem item2 = new OrderItem();
        item2.setId("test-item-2");
        item2.setOrderId("test-order-id");
        item2.setProductId("test-product-2");
        item2.setProductName("Test Product 2");
        item2.setProductCode("TP002");
        item2.setQuantity(1);
        item2.setUnitPrice(new BigDecimal("50.00"));
        item2.setTotalPrice(new BigDecimal("50.00"));
        item2.setDiscountAmount(new BigDecimal("0.00"));
        item2.setFinalAmount(new BigDecimal("50.00"));
        
        testOrderItems.add(item1);
        testOrderItems.add(item2);
        
        // 初始化测试状态历史
        testStatusHistory = new OrderStatusHistory();
        testStatusHistory.setId("test-history-id");
        testStatusHistory.setOrderId("test-order-id");
        testStatusHistory.setFromStatus("PENDING");
        testStatusHistory.setToStatus("CONFIRMED");
        testStatusHistory.setReason("Order confirmed");
        testStatusHistory.setUserId("test-user");
    }

    @Test
    @DisplayName("测试创建订单")
    void testCreateOrder() {
        // 模拟依赖服务返回值
        when(inventoryService.adjustInventory(anyString(), anyInt(), anyString()))
            .thenReturn("库存检查成功");
        when(orderMapper.insert(any(Order.class)))
            .thenReturn(1);
        when(orderItemMapper.insert(any(OrderItem.class)))
            .thenReturn(1);
        
        // 执行测试
        String result = orderService.createOrder(testOrder, testOrderItems);
        
        // 验证结果
        assertEquals("订单创建成功", result);
        verify(orderMapper).insert(testOrder);
        verify(orderItemMapper, times(2)).insert(any(OrderItem.class));
        verify(orderStatusHistoryMapper).insert(any(OrderStatusHistory.class));
    }

    @Test
    @DisplayName("测试创建订单 - 库存不足")
    void testCreateOrderInsufficientInventory() {
        // 模拟库存检查失败
        when(inventoryService.adjustInventory(anyString(), anyInt(), anyString()))
            .thenReturn("产品库存不足: test-product-1");
        
        // 执行测试
        String result = orderService.createOrder(testOrder, testOrderItems);
        
        // 验证结果
        assertTrue(result.contains("库存不足"));
        verify(orderMapper, never()).insert(any(Order.class));
        verify(orderItemMapper, never()).insert(any(OrderItem.class));
    }

    @Test
    @DisplayName("测试更新订单")
    void testUpdateOrder() {
        // 模拟订单存在且为待处理状态
        when(orderMapper.selectById("test-order-id"))
            .thenReturn(testOrder);
        when(orderMapper.updateById(any(Order.class)))
            .thenReturn(1);
        
        // 执行测试
        String result = orderService.updateOrder(testOrder);
        
        // 验证结果
        assertEquals("订单更新成功", result);
        verify(orderMapper).updateById(testOrder);
    }

    @Test
    @DisplayName("测试更新订单 - 状态不允许")
    void testUpdateOrderStatusNotAllowed() {
        // 设置订单为已确认状态
        testOrder.setStatus("CONFIRMED");
        when(orderMapper.selectById("test-order-id"))
            .thenReturn(testOrder);
        
        // 执行测试
        String result = orderService.updateOrder(testOrder);
        
        // 验证结果
        assertEquals("只有待处理状态的订单可以修改", result);
        verify(orderMapper, never()).updateById(any(Order.class));
    }

    @Test
    @DisplayName("测试取消订单")
    void testCancelOrder() {
        // 模拟订单存在且为待处理状态
        when(orderMapper.selectById("test-order-id"))
            .thenReturn(testOrder);
        when(orderMapper.updateById(any(Order.class)))
            .thenReturn(1);
        
        // 执行测试
        String result = orderService.cancelOrder("test-order-id", "Customer request");
        
        // 验证结果
        assertEquals("订单取消成功", result);
        verify(orderMapper).updateById(testOrder);
        verify(orderStatusHistoryMapper).insert(any(OrderStatusHistory.class));
    }

    @Test
    @DisplayName("测试更新订单状态")
    void testUpdateOrderStatus() {
        // 模拟订单存在
        when(orderMapper.selectById("test-order-id"))
            .thenReturn(testOrder);
        when(orderMapper.updateById(any(Order.class)))
            .thenReturn(1);
        
        // 执行测试
        String result = orderService.updateOrderStatus("test-order-id", "CONFIRMED", "Payment received");
        
        // 验证结果
        assertEquals("订单状态更新成功", result);
        verify(orderMapper).updateById(testOrder);
        verify(orderStatusHistoryMapper).insert(any(OrderStatusHistory.class));
    }

    @Test
    @DisplayName("测试更新订单状态为已确认 - 扣减库存")
    void testUpdateOrderStatusToConfirmed() {
        // 模拟订单存在和库存扣减
        when(orderMapper.selectById("test-order-id"))
            .thenReturn(testOrder);
        when(orderMapper.updateById(any(Order.class)))
            .thenReturn(1);
        when(orderItemMapper.getOrderItemsByOrderId("test-order-id"))
            .thenReturn(Arrays.asList(
                createOrderItemMap("test-product-1", 2),
                createOrderItemMap("test-product-2", 1)
            ));
        when(inventoryService.adjustInventory(eq("test-product-1"), eq(-2), anyString()))
            .thenReturn("库存扣减成功");
        when(inventoryService.adjustInventory(eq("test-product-2"), eq(-1), anyString()))
            .thenReturn("库存扣减成功");
        
        // 执行测试
        String result = orderService.updateOrderStatus("test-order-id", "CONFIRMED", "Payment received");
        
        // 验证结果
        assertEquals("订单状态更新成功", result);
        verify(inventoryService).adjustInventory("test-product-1", -2, "订单出库");
        verify(inventoryService).adjustInventory("test-product-2", -1, "订单出库");
    }

    @Test
    @DisplayName("测试生成订单编码")
    void testGenerateOrderCode() {
        // 模拟当天无订单
        when(orderMapper.selectOne(any(QueryWrapper.class)))
            .thenReturn(null);
        
        // 执行测试
        String orderCode = orderService.generateOrderCode();
        
        // 验证结果
        assertNotNull(orderCode);
        assertTrue(orderCode.startsWith("ORD"));
        assertEquals(10, orderCode.length()); // ORD + 8位日期 + 3位序号
    }

    @Test
    @DisplayName("测试计算订单金额")
    void testCalculateOrderAmount() {
        // 执行测试
        Map<String, Object> result = orderService.calculateOrderAmount(testOrderItems);
        
        // 验证结果
        assertEquals(new BigDecimal("1050.00"), result.get("totalAmount"));
        assertEquals(new BigDecimal("25.00"), result.get("discountAmount"));
        assertEquals(new BigDecimal("105.00"), result.get("taxAmount"));
        assertEquals(new BigDecimal("1130.00"), result.get("finalAmount"));
    }

    @Test
    @DisplayName("测试获取订单详情")
    void testGetOrderDetail() {
        // 模拟订单和订单项存在
        when(orderMapper.selectById("test-order-id"))
            .thenReturn(testOrder);
        when(orderItemMapper.getOrderItemsByOrderId("test-order-id"))
            .thenReturn(Arrays.asList(
                createOrderItemMap("test-product-1", 2),
                createOrderItemMap("test-product-2", 1)
            ));
        
        // 执行测试
        Map<String, Object> result = orderService.getOrderDetail("test-order-id");
        
        // 验证结果
        assertNotNull(result);
        assertEquals(testOrder, result.get("order"));
        assertNotNull(result.get("orderItems"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> orderItems = (List<Map<String, Object>>) result.get("orderItems");
        assertEquals(2, orderItems.size());
    }

    @Test
    @DisplayName("测试获取订单统计")
    void testGetStatistics() {
        // 模拟统计数据
        Map<String, Object> baseStats = new HashMap<>();
        baseStats.put("totalOrders", 100);
        baseStats.put("totalAmount", new BigDecimal("100000.00"));
        baseStats.put("pendingCount", 10);
        baseStats.put("confirmedCount", 50);
        baseStats.put("shippingCount", 20);
        baseStats.put("completedCount", 15);
        baseStats.put("cancelledCount", 5);
        
        Map<String, Object> todayStats = new HashMap<>();
        todayStats.put("todayCount", 5);
        todayStats.put("todayAmount", new BigDecimal("5000.00"));
        
        Map<String, Object> weekStats = new HashMap<>();
        weekStats.put("weekCount", 25);
        weekStats.put("weekAmount", new BigDecimal("25000.00"));
        
        Map<String, Object> monthStats = new HashMap<>();
        monthStats.put("monthCount", 80);
        monthStats.put("monthAmount", new BigDecimal("80000.00"));
        
        when(orderMapper.getStatistics()).thenReturn(baseStats);
        when(orderMapper.getTodayCounts()).thenReturn(todayStats);
        when(orderMapper.getWeekCounts()).thenReturn(weekStats);
        when(orderMapper.getMonthCounts()).thenReturn(monthStats);
        
        // 执行测试
        OrderStatisticsVO statistics = orderService.getStatistics();
        
        // 验证结果
        assertEquals(100, statistics.getTotalOrders());
        assertEquals(new BigDecimal("100000.00"), statistics.getTotalAmount());
        assertEquals(10, statistics.getPendingCount());
        assertEquals(50, statistics.getConfirmedCount());
        assertEquals(20, statistics.getShippingCount());
        assertEquals(15, statistics.getCompletedCount());
        assertEquals(5, statistics.getCancelledCount());
        assertEquals(5, statistics.getTodayCount());
        assertEquals(new BigDecimal("5000.00"), statistics.getTodayAmount());
        assertEquals(25, statistics.getWeekCount());
        assertEquals(new BigDecimal("25000.00"), statistics.getWeekAmount());
        assertEquals(80, statistics.getMonthCount());
        assertEquals(new BigDecimal("80000.00"), statistics.getMonthAmount());
        assertEquals(new BigDecimal("1000.00"), statistics.getAverageOrderAmount());
        assertEquals(new BigDecimal("15.00"), statistics.getCompletionRate());
        assertEquals(new BigDecimal("5.00"), statistics.getCancellationRate());
    }

    /**
     * 创建订单项Map对象
     */
    private Map<String, Object> createOrderItemMap(String productId, int quantity) {
        Map<String, Object> itemMap = new HashMap<>();
        itemMap.put("product_id", productId);
        itemMap.put("quantity", quantity);
        return itemMap;
    }
}