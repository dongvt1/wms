package com.cy.modules.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cy.modules.warehouse.entity.Order;
import com.cy.modules.warehouse.entity.Customer;
import com.cy.modules.warehouse.entity.OrderItem;
import com.cy.modules.warehouse.entity.OrderProcessingLog;
import com.cy.modules.warehouse.entity.OrderStatusHistory;
import com.cy.modules.warehouse.mapper.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.common.util.oConvertUtils;
import com.cy.modules.warehouse.service.CustomerService;
import com.cy.modules.warehouse.service.EmailNotificationService;
import com.cy.modules.warehouse.service.InventoryService;
import com.cy.modules.warehouse.service.OrderService;
import com.cy.modules.common.service.ProductService;
import com.cy.modules.warehouse.vo.OrderReportVO;
import com.cy.modules.warehouse.vo.OrderStatisticsVO;
import org.jeecgframework.poi.excel.ExcelExportUtil;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Order Service Implementation
 */
@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private OrderItemMapper orderItemMapper;
    
    @Autowired
    private OrderStatusHistoryMapper orderStatusHistoryMapper;
    
    @Autowired
    private InventoryService inventoryService;
    
    @Autowired
    private CustomerService customerService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private OrderNotificationMapper orderNotificationMapper;
    
    @Autowired
    private OrderProcessingLogMapper orderProcessingLogMapper;
    
    @Autowired
    private EmailNotificationService emailNotificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createOrder(Order order, List<OrderItem> orderItems) {
        try {
            // Generate order code
            String orderCode = generateOrderCode();
            order.setOrderCode(orderCode);
            order.setOrderDate(new Date());
            order.setStatus("PENDING");
            
            // Calculate order amount
            Map<String, Object> amountResult = calculateOrderAmount(orderItems);
            order.setTotalAmount((BigDecimal) amountResult.get("totalAmount"));
            order.setDiscountAmount((BigDecimal) amountResult.get("discountAmount"));
            order.setTaxAmount((BigDecimal) amountResult.get("taxAmount"));
            order.setFinalAmount((BigDecimal) amountResult.get("finalAmount"));
            
            // Save order
            this.save(order);
            
            // Save order items
            for (OrderItem item : orderItems) {
                item.setOrderId(order.getId());
                // Calculate order item amount
                BigDecimal totalPrice = item.getUnitPrice().multiply(new BigDecimal(item.getQuantity()));
                item.setTotalPrice(totalPrice);
                item.setFinalAmount(totalPrice.subtract(item.getDiscountAmount() != null ? item.getDiscountAmount() : BigDecimal.ZERO));
                orderItemMapper.insert(item);
            }
            
            // Record status change history
            addOrderStatusHistory(order.getId(), null, "PENDING", "Order created", order.getCreatedBy());
            
            // Record processing log
            addOrderProcessingLog(order.getId(), "CREATE", "Order created", "SUCCESS", null, order.getCreatedBy());
            
            // Send order creation notification
            sendOrderNotification(order.getId(), "PENDING", order.getCreatedBy());
            
            return "Order created successfully";
        } catch (Exception e) {
            log.error("Failed to create order", e);
            return "Failed to create order: " + e.getMessage();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateOrder(Order order) {
        try {
            // Get original order info
            Order originalOrder = this.getById(order.getId());
            if (originalOrder == null) {
                return "Order not found";
            }
            
            // Only orders in PENDING status can be edited
            if (!"PENDING".equals(originalOrder.getStatus())) {
                return "Only orders in PENDING status can be edited";
            }
            
            // Update order info
            this.updateById(order);
            
            return "Order updated successfully";
        } catch (Exception e) {
            log.error("Failed to update order", e);
            return "Failed to update order: " + e.getMessage();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String cancelOrder(String orderId, String reason) {
        try {
            // Get order info
            Order order = this.getById(orderId);
            if (order == null) {
                return "Order not found";
            }
            
            // Only PENDING or CONFIRMED orders can be cancelled
            if (!"PENDING".equals(order.getStatus()) && !"CONFIRMED".equals(order.getStatus())) {
                return "Only PENDING or CONFIRMED orders can be cancelled";
            }
            
            // Update order status
            order.setStatus("CANCELLED");
            this.updateById(order);
            
            // Record status change history
            addOrderStatusHistory(orderId, order.getStatus(), "CANCELLED", reason, order.getCreatedBy());
            
            // Record processing log
            addOrderProcessingLog(orderId, "CANCEL", reason, "SUCCESS", null, order.getCreatedBy());
            
            // If order was CONFIRMED, restore inventory
            if ("CONFIRMED".equals(order.getStatus())) {
                cancelOrderAndRestoreInventory(orderId);
            }
            
            // Send order cancellation notification
            sendOrderNotification(orderId, "CANCELLED", order.getCreatedBy());
            
            return "Order cancelled successfully";
        } catch (Exception e) {
            log.error("Failed to cancel order", e);
            return "Failed to cancel order: " + e.getMessage();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateOrderStatus(String orderId, String newStatus, String reason) {
        try {
            // Get order info
            Order order = this.getById(orderId);
            if (order == null) {
                return "Order not found";
            }
            
            String oldStatus = order.getStatus();
            
            // Update order status
            order.setStatus(newStatus);
            this.updateById(order);
            
            // Record status change history
            addOrderStatusHistory(orderId, oldStatus, newStatus, reason, order.getCreatedBy());
            
            // Record processing log
            addOrderProcessingLog(orderId, "STATUS_UPDATE", reason, "SUCCESS", null, order.getCreatedBy());
            
            // If status changes to CONFIRMED, deduct inventory
            if ("CONFIRMED".equals(newStatus) && !"CONFIRMED".equals(oldStatus)) {
                confirmOrderAndDeductInventory(orderId);
            }
            
            // Send status change notification
            sendOrderNotification(orderId, newStatus, order.getCreatedBy());
            
            return "Order status updated successfully";
        } catch (Exception e) {
            log.error("Failed to update order status", e);
            return "Failed to update order status: " + e.getMessage();
        }
    }

    @Override
    public OrderReportVO getOrderReport(Integer pageNo, Integer pageSize, String customerId, String status) {
        OrderReportVO report = new OrderReportVO();
        
        // Build query conditions
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        
        if (oConvertUtils.isNotEmpty(customerId)) {
            queryWrapper.eq("customer_id", customerId);
        }
        
        if (oConvertUtils.isNotEmpty(status)) {
            queryWrapper.eq("status", status);
        }
        
        queryWrapper.orderByDesc("order_date");
        
        // Paginated query
        IPage<Order> page = new Page<>(pageNo, pageSize);
        IPage<Order> orderPage = this.page(page, queryWrapper);
        
        // Convert to OrderItemVO
        List<OrderReportVO.OrderItemVO> items = new ArrayList<>();
        for (Order order : orderPage.getRecords()) {
            OrderReportVO.OrderItemVO itemVO = new OrderReportVO.OrderItemVO();
            itemVO.setOrderId(order.getId());
            itemVO.setOrderCode(order.getOrderCode());
            itemVO.setCustomerId(order.getCustomerId());
            itemVO.setCustomerName(order.getCustomerName());
            itemVO.setOrderDate(order.getOrderDate());
            itemVO.setStatus(order.getStatus());
            itemVO.setTotalAmount(order.getTotalAmount());
            itemVO.setDiscountAmount(order.getDiscountAmount());
            itemVO.setTaxAmount(order.getTaxAmount());
            itemVO.setFinalAmount(order.getFinalAmount());
            itemVO.setNotes(order.getNotes());
            itemVO.setCreatedBy(order.getCreatedBy());
            items.add(itemVO);
        }
        
        report.setRecords(items);
        report.setTotal(orderPage.getTotal());
        report.setSize(orderPage.getSize());
        report.setCurrent(orderPage.getCurrent());
        report.setPages(orderPage.getPages());
        
        // Calculate summary information
        Map<String, Object> statistics = baseMapper.getStatistics();
        OrderReportVO.OrderSummaryVO summary = new OrderReportVO.OrderSummaryVO();
        summary.setTotalOrders((Integer) statistics.get("totalOrders"));
        summary.setTotalAmount((BigDecimal) statistics.get("totalAmount"));
        summary.setPendingCount((Integer) statistics.get("pendingCount"));
        summary.setConfirmedCount((Integer) statistics.get("confirmedCount"));
        summary.setShippingCount((Integer) statistics.get("shippingCount"));
        summary.setCompletedCount((Integer) statistics.get("completedCount"));
        summary.setCancelledCount((Integer) statistics.get("cancelledCount"));
        
        report.setSummary(summary);
        
        return report;
    }

    @Override
    public List<Map<String, Object>> searchOrdersByCode(String orderCode) {
        return baseMapper.searchOrdersByCode(orderCode);
    }

    @Override
    public List<Map<String, Object>> searchOrdersByCustomerName(String customerName) {
        return baseMapper.searchOrdersByCustomerName(customerName);
    }

    @Override
    public Map<String, Object> getOrderDetail(String orderId) {
        Map<String, Object> result = new HashMap<>();
        
        // Get order info
        Order order = this.getById(orderId);
        if (order == null) {
            return result;
        }
        
        result.put("order", order);
        
        // Get order items
        List<Map<String, Object>> orderItems = orderItemMapper.getOrderItemsByOrderId(orderId);
        result.put("orderItems", orderItems);
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getOrderStatusHistory(String orderId) {
        return orderStatusHistoryMapper.getStatusHistoryByOrderId(orderId);
    }

    @Override
    public void exportOrderReport(HttpServletRequest request, HttpServletResponse response,
                                  String customerId, String status) {
        // Build query conditions
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        
        if (oConvertUtils.isNotEmpty(customerId)) {
            queryWrapper.eq("customer_id", customerId);
        }
        
        if (oConvertUtils.isNotEmpty(status)) {
            queryWrapper.eq("status", status);
        }
        
        // Query data
        List<Order> orderList = this.list(queryWrapper);
        
        // Export Excel
        try {
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("OrderReport_" + DateUtils.getDate() + ".xlsx", "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName);
            
            // Export Excel
            ExportParams params = new ExportParams();
            params.setTitle("Order Report");
            params.setSheetName("Order List");
            
            OutputStream os = response.getOutputStream();
            ExcelExportUtil.exportExcel(params, Order.class, orderList);
            os.flush();
            os.close();
            
            log.info("Order report exported successfully, count: {}", orderList.size());
            
        } catch (Exception e) {
            log.error("Failed to export order report", e);
        }
    }

    @Override
    public OrderStatisticsVO getStatistics() {
        OrderStatisticsVO statistics = new OrderStatisticsVO();
        
        // Get base statistics
        Map<String, Object> baseStats = baseMapper.getStatistics();
        statistics.setTotalOrders((Integer) baseStats.get("totalOrders"));
        statistics.setTotalAmount((BigDecimal) baseStats.get("totalAmount"));
        statistics.setPendingCount((Integer) baseStats.get("pendingCount"));
        statistics.setConfirmedCount((Integer) baseStats.get("confirmedCount"));
        statistics.setShippingCount((Integer) baseStats.get("shippingCount"));
        statistics.setCompletedCount((Integer) baseStats.get("completedCount"));
        statistics.setCancelledCount((Integer) baseStats.get("cancelledCount"));
        
        // Get today's statistics
        Map<String, Object> todayStats = baseMapper.getTodayCounts();
        statistics.setTodayCount((Integer) todayStats.get("todayCount"));
        statistics.setTodayAmount((BigDecimal) todayStats.get("todayAmount"));
        
        // Get this week's statistics
        Map<String, Object> weekStats = baseMapper.getWeekCounts();
        statistics.setWeekCount((Integer) weekStats.get("weekCount"));
        statistics.setWeekAmount((BigDecimal) weekStats.get("weekAmount"));
        
        // Get this month's statistics
        Map<String, Object> monthStats = baseMapper.getMonthCounts();
        statistics.setMonthCount((Integer) monthStats.get("monthCount"));
        statistics.setMonthAmount((BigDecimal) monthStats.get("monthAmount"));
        
        // Calculate average order amount
        if (statistics.getTotalOrders() != null && statistics.getTotalOrders() > 0) {
            statistics.setAverageOrderAmount(statistics.getTotalAmount().divide(new BigDecimal(statistics.getTotalOrders()), 2, RoundingMode.HALF_UP));
        }
        
        // Calculate order completion rate
        if (statistics.getTotalOrders() != null && statistics.getTotalOrders() > 0) {
            BigDecimal completionRate = new BigDecimal(statistics.getCompletedCount())
                    .divide(new BigDecimal(statistics.getTotalOrders()), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
            statistics.setCompletionRate(completionRate);
            
            BigDecimal cancellationRate = new BigDecimal(statistics.getCancelledCount())
                    .divide(new BigDecimal(statistics.getTotalOrders()), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
            statistics.setCancellationRate(cancellationRate);
        }
        
        return statistics;
    }

    @Override
    public void printOrder(String orderId, HttpServletResponse response) {
        try {
            // Get order details
            Map<String, Object> orderDetail = getOrderDetail(orderId);
            if (orderDetail.isEmpty()) {
                log.error("Order not found, order ID: {}", orderId);
                return;
            }
            
            Order order = (Order) orderDetail.get("order");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> orderItems = (List<Map<String, Object>>) orderDetail.get("orderItems");
            
            // Generate HTML content
            String htmlContent = generateOrderHtml(order, orderItems);
            
            // Set response headers
            response.setContentType("application/pdf");
            response.setCharacterEncoding("UTF-8");
            String fileName = URLEncoder.encode("Order_" + order.getOrderCode() + ".pdf", "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName);
            
            // Use third-party library to convert HTML to PDF (simplified here; in real projects, consider using iText or another PDF library)
            // This is an example only; the actual implementation should choose an appropriate PDF generation library based on project requirements
            OutputStream os = response.getOutputStream();
            os.write(htmlContent.getBytes("UTF-8"));
            os.flush();
            os.close();
            
            log.info("Order printed successfully, order ID: {}", orderId);
            
        } catch (Exception e) {
            log.error("Failed to print order", e);
        }
    }
    
    /**
     * Generate order HTML content
     * @param order Order info
     * @param orderItems Order item list
     * @return HTML content
     */
    private String generateOrderHtml(Order order, List<Map<String, Object>> orderItems) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta charset=\"UTF-8\">");
        html.append("<title>Order Details - ").append(order.getOrderCode()).append("</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; }");
        html.append("table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }");
        html.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
        html.append("th { background-color: #f2f2f2; }");
        html.append(".header { text-align: center; margin-bottom: 30px; }");
        html.append(".section { margin-bottom: 30px; }");
        html.append(".section-title { font-size: 18px; font-weight: bold; margin-bottom: 10px; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        
        // Order header
        html.append("<div class=\"header\">");
        html.append("<h1>Order Details</h1>");
        html.append("<h2>").append(order.getOrderCode()).append("</h2>");
        html.append("</div>");
        
        // Order basic info
        html.append("<div class=\"section\">");
        html.append("<div class=\"section-title\">Order Information</div>");
        html.append("<table>");
        html.append("<tr><th>Customer Name</th><td>").append(order.getCustomerName() != null ? order.getCustomerName() : "").append("</td></tr>");
        html.append("<tr><th>Order Date</th><td>").append(order.getOrderDate() != null ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(order.getOrderDate()) : "").append("</td></tr>");
        html.append("<tr><th>Order Status</th><td>").append(order.getStatus() != null ? order.getStatus() : "").append("</td></tr>");
        html.append("<tr><th>Total Amount</th><td>").append(order.getTotalAmount() != null ? order.getTotalAmount().toString() : "0").append("</td></tr>");
        html.append("<tr><th>Discount Amount</th><td>").append(order.getDiscountAmount() != null ? order.getDiscountAmount().toString() : "0").append("</td></tr>");
        html.append("<tr><th>Tax Amount</th><td>").append(order.getTaxAmount() != null ? order.getTaxAmount().toString() : "0").append("</td></tr>");
        html.append("<tr><th>Final Amount</th><td>").append(order.getFinalAmount() != null ? order.getFinalAmount().toString() : "0").append("</td></tr>");
        html.append("<tr><th>Notes</th><td>").append(order.getNotes() != null ? order.getNotes() : "").append("</td></tr>");
        html.append("</table>");
        html.append("</div>");
        
        // Order items
        html.append("<div class=\"section\">");
        html.append("<div class=\"section-title\">Order Items</div>");
        html.append("<table>");
        html.append("<tr><th>Product Code</th><th>Product Name</th><th>Quantity</th><th>Unit Price</th><th>Total Price</th><th>Discount</th><th>Final Amount</th></tr>");
        
        for (Map<String, Object> item : orderItems) {
            html.append("<tr>");
            html.append("<td>").append(item.get("productCode") != null ? item.get("productCode").toString() : "").append("</td>");
            html.append("<td>").append(item.get("productName") != null ? item.get("productName").toString() : "").append("</td>");
            html.append("<td>").append(item.get("quantity") != null ? item.get("quantity").toString() : "0").append("</td>");
            html.append("<td>").append(item.get("unitPrice") != null ? item.get("unitPrice").toString() : "0").append("</td>");
            html.append("<td>").append(item.get("totalPrice") != null ? item.get("totalPrice").toString() : "0").append("</td>");
            html.append("<td>").append(item.get("discountAmount") != null ? item.get("discountAmount").toString() : "0").append("</td>");
            html.append("<td>").append(item.get("finalAmount") != null ? item.get("finalAmount").toString() : "0").append("</td>");
            html.append("</tr>");
        }
        
        html.append("</table>");
        html.append("</div>");
        
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
    }

    @Override
    public String generateOrderCode() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String dateStr = sdf.format(new Date());
        
        // Query the highest order code for today
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.likeRight("order_code", "ORD" + dateStr);
        queryWrapper.orderByDesc("order_code");
        queryWrapper.last("LIMIT 1");
        
        Order lastOrder = this.getOne(queryWrapper);
        
        int sequence = 1;
        if (lastOrder != null) {
            String lastCode = lastOrder.getOrderCode();
            String sequenceStr = lastCode.substring(lastCode.length() - 3);
            sequence = Integer.parseInt(sequenceStr) + 1;
        }
        
        return "ORD" + dateStr + String.format("%03d", sequence);
    }

    @Override
    public Map<String, Object> calculateOrderAmount(List<OrderItem> orderItems) {
        Map<String, Object> result = new HashMap<>();
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        
        for (OrderItem item : orderItems) {
            BigDecimal itemTotal = item.getUnitPrice().multiply(new BigDecimal(item.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
            
            if (item.getDiscountAmount() != null) {
                discountAmount = discountAmount.add(item.getDiscountAmount());
            }
        }
        
        BigDecimal taxAmount = totalAmount.multiply(new BigDecimal("0.1")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal finalAmount = totalAmount.subtract(discountAmount).add(taxAmount);
        
        result.put("totalAmount", totalAmount);
        result.put("discountAmount", discountAmount);
        result.put("taxAmount", taxAmount);
        result.put("finalAmount", finalAmount);
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String checkAndReserveInventory(List<OrderItem> orderItems) {
        try {
            // Bước 1: Kiểm tra toàn bộ trước khi reserve
            for (OrderItem item : orderItems) {
                boolean sufficient = inventoryService.hasSufficientInventory(
                        item.getProductId(), item.getQuantity());
                if (!sufficient) {
                    return "Sản phẩm không đủ tồn kho: " + item.getProductId();
                }
            }
            // Bước 2: Reserve sau khi đã xác nhận đủ hàng
            for (OrderItem item : orderItems) {
                boolean reserved = inventoryService.reserveInventory(
                        item.getProductId(), item.getQuantity(), "system");
                if (!reserved) {
                    throw new RuntimeException("Đặt trước tồn kho thất bại: " + item.getProductId());
                }
            }
            return "Kiểm tra và đặt trước tồn kho thành công";
        } catch (Exception e) {
            log.error("Lỗi kiểm tra và đặt trước tồn kho", e);
            return "Lỗi kiểm tra tồn kho: " + e.getMessage();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String confirmOrderAndDeductInventory(String orderId) {
        try {
            // Get order items
            List<Map<String, Object>> orderItems = orderItemMapper.getOrderItemsByOrderId(orderId);
            
            for (Map<String, Object> item : orderItems) {
                String productId = (String) item.get("product_id");
                Integer quantity = (Integer) item.get("quantity");
                
                // Deduct inventory
                String result = inventoryService.adjustInventory(productId, -quantity, "Order outbound");
                if (!result.contains("successfully")) {
                    throw new RuntimeException("Failed to deduct inventory: " + productId);
                }
            }
            
            return "Inventory deducted successfully";
        } catch (Exception e) {
            log.error("Failed to confirm order and deduct inventory", e);
            return "Failed to confirm order and deduct inventory: " + e.getMessage();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String cancelOrderAndRestoreInventory(String orderId) {
        try {
            // Get order items
            List<Map<String, Object>> orderItems = orderItemMapper.getOrderItemsByOrderId(orderId);
            
            for (Map<String, Object> item : orderItems) {
                String productId = (String) item.get("product_id");
                Integer quantity = (Integer) item.get("quantity");
                
                // Restore inventory
                String result = inventoryService.adjustInventory(productId, quantity, "Order cancellation restore");
                if (!result.contains("successfully")) {
                    throw new RuntimeException("Failed to restore inventory: " + productId);
                }
            }
            
            return "Inventory restored successfully";
        } catch (Exception e) {
            log.error("Failed to cancel order and restore inventory", e);
            return "Failed to cancel order and restore inventory: " + e.getMessage();
        }
    }
    
    /**
     * Add order status change history
     * @param orderId Order ID
     * @param fromStatus Previous status
     * @param toStatus New status
     * @param reason Reason for change
     * @param userId Operator
     */
    private void addOrderStatusHistory(String orderId, String fromStatus, String toStatus, String reason, String userId) {
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrderId(orderId);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setReason(reason);
        history.setUserId(userId);
        history.setCreatedAt(new Date());
        orderStatusHistoryMapper.insert(history);
    }
    
    /**
     * Add order processing log
     */
    private void addOrderProcessingLog(String orderId, String action, String details, String status, String errorMessage, String userId) {
        OrderProcessingLog log = new OrderProcessingLog();
        log.setId(UUIDGenerator.generate());
        log.setOrderId(orderId);
        log.setAction(action);
        log.setDetails(details);
        log.setStatus(status);
        log.setErrorMessage(errorMessage);
        log.setUserId(userId);
        log.setCreateTime(new Date());
        orderProcessingLogMapper.insert(log);
    }
    
    /**
     * Send order notification
     */
    private void sendOrderNotification(String orderId, String status, String userId) {
        try {
            // Get order info
            Order order = this.getById(orderId);
            if (order == null) {
                return;
            }
            
            // Get customer info
            Customer customer = customerService.getById(order.getCustomerId());
            if (customer == null) {
                return;
            }
            
            String customerEmail = customer.getEmail();
            String customerName = customer.getCustomerName();

            
            if (customerEmail == null || customerEmail.isEmpty()) {
                log.warn("Customer email is empty, skipping notification: {}", orderId);
                return;
            }
            
            // Send different notifications based on status
            switch (status) {
                case "PENDING":
                    emailNotificationService.sendOrderConfirmationNotification(orderId, order.getOrderCode(), customerEmail, customerName, order.getFinalAmount().toString());
                    break;
                case "CONFIRMED":
                    emailNotificationService.sendOrderStatusChangeNotification(orderId, order.getOrderCode(), "PENDING", "CONFIRMED", customerEmail, customerName);
                    break;
                case "SHIPPING":
                    emailNotificationService.sendOrderShippingNotification(orderId, order.getOrderCode(), customerEmail, customerName, "TRACK" + order.getOrderCode());
                    break;
                case "COMPLETED":
                    emailNotificationService.sendOrderCompletionNotification(orderId, order.getOrderCode(), customerEmail, customerName);
                    break;
                case "CANCELLED":
                    emailNotificationService.sendOrderCancellationNotification(orderId, order.getOrderCode(), customerEmail, customerName, "Order has been cancelled");
                    break;
            }
        } catch (Exception e) {
            log.error("Failed to send order notification: {}", orderId, e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchProcessOrders(List<String> orderIds, String action, String reason) {
        Map<String, Object> result = new HashMap<>();
        List<String> successOrders = new ArrayList<>();
        List<String> failedOrders = new ArrayList<>();
        
        for (String orderId : orderIds) {
            try {
                String processResult;
                switch (action) {
                    case "CONFIRM":
                        processResult = updateOrderStatus(orderId, "CONFIRMED", reason);
                        break;
                    case "CANCEL":
                        processResult = cancelOrder(orderId, reason);
                        break;
                    case "SHIP":
                        processResult = updateOrderStatus(orderId, "SHIPPING", reason);
                        break;
                    case "COMPLETE":
                        processResult = updateOrderStatus(orderId, "COMPLETED", reason);
                        break;
                    default:
                        processResult = "Unsupported action: " + action;
                        break;
                }
                
                if (processResult.contains("successfully")) {
                    successOrders.add(orderId);
                } else {
                    failedOrders.add(orderId + ": " + processResult);
                }
            } catch (Exception e) {
                log.error("Failed to batch process order: {}", orderId, e);
                failedOrders.add(orderId + ": " + e.getMessage());
            }
        }
        
        result.put("successCount", successOrders.size());
        result.put("failedCount", failedOrders.size());
        result.put("successOrders", successOrders);
        result.put("failedOrders", failedOrders);
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> autoConfirmOrders() {
        Map<String, Object> result = new HashMap<>();
        List<String> confirmedOrders = new ArrayList<>();
        List<String> failedOrders = new ArrayList<>();
        
        // Query pending orders
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "PENDING");
        queryWrapper.orderByAsc("create_time");
        queryWrapper.last("LIMIT 100"); // Limit to 100 orders per run
        
        List<Order> pendingOrders = this.list(queryWrapper);
        
        for (Order order : pendingOrders) {
            try {
                // Check if the order meets the auto-confirm criteria
                if (canAutoConfirmOrder(order)) {
                    String confirmResult = updateOrderStatus(order.getId(), "CONFIRMED", "Auto-confirmed by system");
                    if (confirmResult.contains("successfully")) {
                        confirmedOrders.add(order.getId());
                    } else {
                        failedOrders.add(order.getId() + ": " + confirmResult);
                    }
                }
            } catch (Exception e) {
                log.error("Failed to auto-confirm order: {}", order.getId(), e);
                failedOrders.add(order.getId() + ": " + e.getMessage());
            }
        }
        
        result.put("confirmedCount", confirmedOrders.size());
        result.put("failedCount", failedOrders.size());
        result.put("confirmedOrders", confirmedOrders);
        result.put("failedOrders", failedOrders);
        
        return result;
    }

    /**
     * Check if the order can be auto-confirmed
     */
    private boolean canAutoConfirmOrder(Order order) {
        // Add auto-confirm business rules here
        // e.g.: order amount below threshold, customer has good credit, sufficient inventory, etc.
        
        // Simple example: order created more than 30 minutes ago and amount < 1000
        long orderAge = System.currentTimeMillis() - order.getCreateTime().getTime();
        boolean isOldEnough = orderAge > 30 * 60 * 1000; // 30 minutes
        boolean isSmallAmount = order.getFinalAmount().compareTo(new BigDecimal("1000")) < 0;
        
        return isOldEnough && isSmallAmount;
    }

    @Override
    public void generateStockOutNote(String orderId, HttpServletResponse response) {
        try {
            // Get order details
            Map<String, Object> orderDetail = getOrderDetail(orderId);
            if (orderDetail.isEmpty()) {
                log.error("Order not found, order ID: {}", orderId);
                return;
            }
            
            Order order = (Order) orderDetail.get("order");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> orderItems = (List<Map<String, Object>>) orderDetail.get("orderItems");
            
            // Generate stock-out note HTML content
            String htmlContent = generateStockOutNoteHtml(order, orderItems);
            
            // Set response headers
            response.setContentType("application/pdf");
            response.setCharacterEncoding("UTF-8");
            String fileName = URLEncoder.encode("StockOutNote_" + order.getOrderCode() + ".pdf", "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName);
            
            // Use third-party library to convert HTML to PDF (simplified; consider iText or similar in production)
            OutputStream os = response.getOutputStream();
            os.write(htmlContent.getBytes("UTF-8"));
            os.flush();
            os.close();
            
            log.info("Stock-out note generated successfully, order ID: {}", orderId);
            
        } catch (Exception e) {
            log.error("Failed to generate stock-out note", e);
        }
    }

    /**
     * Generate stock-out note HTML content
     */
    private String generateStockOutNoteHtml(Order order, List<Map<String, Object>> orderItems) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta charset=\"UTF-8\">");
        html.append("<title>Stock-Out Note - ").append(order.getOrderCode()).append("</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; }");
        html.append("table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }");
        html.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
        html.append("th { background-color: #f2f2f2; }");
        html.append(".header { text-align: center; margin-bottom: 30px; }");
        html.append(".section { margin-bottom: 30px; }");
        html.append(".section-title { font-size: 18px; font-weight: bold; margin-bottom: 10px; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        
        // Stock-out note header
        html.append("<div class=\"header\">");
        html.append("<h1>Stock-Out Note</h1>");
        html.append("<h2>").append(order.getOrderCode()).append("</h2>");
        html.append("</div>");
        
        // Stock-out note basic info
        html.append("<div class=\"section\">");
        html.append("<div class=\"section-title\">Outbound Information</div>");
        html.append("<table>");
        html.append("<tr><th>Customer Name</th><td>").append(order.getCustomerName() != null ? order.getCustomerName() : "").append("</td></tr>");
        html.append("<tr><th>Outbound Date</th><td>").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("</td></tr>");
        html.append("<tr><th>Order Status</th><td>").append(order.getStatus() != null ? order.getStatus() : "").append("</td></tr>");
        html.append("<tr><th>Total Amount</th><td>").append(order.getTotalAmount() != null ? order.getTotalAmount().toString() : "0").append("</td></tr>");
        html.append("<tr><th>Final Amount</th><td>").append(order.getFinalAmount() != null ? order.getFinalAmount().toString() : "0").append("</td></tr>");
        html.append("<tr><th>Notes</th><td>").append(order.getNotes() != null ? order.getNotes() : "").append("</td></tr>");
        html.append("</table>");
        html.append("</div>");
        
        // Outbound items
        html.append("<div class=\"section\">");
        html.append("<div class=\"section-title\">Outbound Products</div>");
        html.append("<table>");
        html.append("<tr><th>Product Code</th><th>Product Name</th><th>Quantity</th><th>Unit Price</th><th>Total Price</th><th>Discount</th><th>Final Amount</th></tr>");
        
        for (Map<String, Object> item : orderItems) {
            html.append("<tr>");
            html.append("<td>").append(item.get("productCode") != null ? item.get("productCode").toString() : "").append("</td>");
            html.append("<td>").append(item.get("productName") != null ? item.get("productName").toString() : "").append("</td>");
            html.append("<td>").append(item.get("quantity") != null ? item.get("quantity").toString() : "0").append("</td>");
            html.append("<td>").append(item.get("unitPrice") != null ? item.get("unitPrice").toString() : "0").append("</td>");
            html.append("<td>").append(item.get("totalPrice") != null ? item.get("totalPrice").toString() : "0").append("</td>");
            html.append("<td>").append(item.get("discountAmount") != null ? item.get("discountAmount").toString() : "0").append("</td>");
            html.append("<td>").append(item.get("finalAmount") != null ? item.get("finalAmount").toString() : "0").append("</td>");
            html.append("</tr>");
        }
        
        html.append("</table>");
        html.append("</div>");
        
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
    }

    @Override
    public List<Map<String, Object>> getOrderProcessingLogs(String orderId) {
        return orderProcessingLogMapper.getProcessingLogsByOrderId(orderId);
    }

    @Override
    public String resendOrderNotification(String notificationId) {
        return emailNotificationService.resendNotification(notificationId);
    }

    @Override
    public String processPendingNotifications() {
        return emailNotificationService.processPendingNotifications();
    }

    @Override
    public Map<String, Object> getOrderProcessingStatistics() {
        return orderProcessingLogMapper.getProcessingStatistics();
    }
}