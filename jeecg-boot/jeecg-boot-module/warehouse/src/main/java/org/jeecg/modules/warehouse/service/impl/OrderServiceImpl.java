package org.jeecg.modules.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.warehouse.entity.*;
import org.jeecg.modules.warehouse.mapper.*;
import org.jeecg.modules.warehouse.service.CustomerService;
import org.jeecg.modules.warehouse.service.EmailNotificationService;
import org.jeecg.modules.warehouse.service.InventoryService;
import org.jeecg.modules.warehouse.service.OrderService;
import org.jeecg.modules.warehouse.service.ProductService;
import org.jeecg.modules.warehouse.vo.OrderReportVO;
import org.jeecg.modules.warehouse.vo.OrderStatisticsVO;
import org.jeecgframework.poi.excel.ExcelExportUtil;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 订单服务实现类
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
            // 生成订单编码
            String orderCode = generateOrderCode();
            order.setOrderCode(orderCode);
            order.setOrderDate(new Date());
            order.setStatus("PENDING");
            
            // 计算订单金额
            Map<String, Object> amountResult = calculateOrderAmount(orderItems);
            order.setTotalAmount((BigDecimal) amountResult.get("totalAmount"));
            order.setDiscountAmount((BigDecimal) amountResult.get("discountAmount"));
            order.setTaxAmount((BigDecimal) amountResult.get("taxAmount"));
            order.setFinalAmount((BigDecimal) amountResult.get("finalAmount"));
            
            // 保存订单
            this.save(order);
            
            // 保存订单项
            for (OrderItem item : orderItems) {
                item.setOrderId(order.getId());
                // 计算订单项金额
                BigDecimal totalPrice = item.getUnitPrice().multiply(new BigDecimal(item.getQuantity()));
                item.setTotalPrice(totalPrice);
                item.setFinalAmount(totalPrice.subtract(item.getDiscountAmount() != null ? item.getDiscountAmount() : BigDecimal.ZERO));
                orderItemMapper.insert(item);
            }
            
            // 记录状态变更历史
            addOrderStatusHistory(order.getId(), null, "PENDING", "订单创建", order.getCreatedBy());
            
            // 记录处理日志
            addOrderProcessingLog(order.getId(), "CREATE", "订单创建", "SUCCESS", null, order.getCreatedBy());
            
            // 发送订单创建通知
            sendOrderNotification(order.getId(), "PENDING", order.getCreatedBy());
            
            return "订单创建成功";
        } catch (Exception e) {
            log.error("创建订单失败", e);
            return "创建订单失败: " + e.getMessage();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateOrder(Order order) {
        try {
            // 获取原订单信息
            Order originalOrder = this.getById(order.getId());
            if (originalOrder == null) {
                return "未找到订单信息";
            }
            
            // 只有待处理状态的订单可以修改
            if (!"PENDING".equals(originalOrder.getStatus())) {
                return "只有待处理状态的订单可以修改";
            }
            
            // 更新订单信息
            this.updateById(order);
            
            return "订单更新成功";
        } catch (Exception e) {
            log.error("更新订单失败", e);
            return "更新订单失败: " + e.getMessage();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String cancelOrder(String orderId, String reason) {
        try {
            // 获取订单信息
            Order order = this.getById(orderId);
            if (order == null) {
                return "未找到订单信息";
            }
            
            // 只有待处理或已确认状态的订单可以取消
            if (!"PENDING".equals(order.getStatus()) && !"CONFIRMED".equals(order.getStatus())) {
                return "只有待处理或已确认状态的订单可以取消";
            }
            
            // 更新订单状态
            order.setStatus("CANCELLED");
            this.updateById(order);
            
            // 记录状态变更历史
            addOrderStatusHistory(orderId, order.getStatus(), "CANCELLED", reason, order.getCreatedBy());
            
            // 记录处理日志
            addOrderProcessingLog(orderId, "CANCEL", reason, "SUCCESS", null, order.getCreatedBy());
            
            // 如果订单已确认，需要恢复库存
            if ("CONFIRMED".equals(order.getStatus())) {
                cancelOrderAndRestoreInventory(orderId);
            }
            
            // 发送订单取消通知
            sendOrderNotification(orderId, "CANCELLED", order.getCreatedBy());
            
            return "订单取消成功";
        } catch (Exception e) {
            log.error("取消订单失败", e);
            return "取消订单失败: " + e.getMessage();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateOrderStatus(String orderId, String newStatus, String reason) {
        try {
            // 获取订单信息
            Order order = this.getById(orderId);
            if (order == null) {
                return "未找到订单信息";
            }
            
            String oldStatus = order.getStatus();
            
            // 更新订单状态
            order.setStatus(newStatus);
            this.updateById(order);
            
            // 记录状态变更历史
            addOrderStatusHistory(orderId, oldStatus, newStatus, reason, order.getCreatedBy());
            
            // 记录处理日志
            addOrderProcessingLog(orderId, "STATUS_UPDATE", reason, "SUCCESS", null, order.getCreatedBy());
            
            // 如果状态变更为已确认，需要扣减库存
            if ("CONFIRMED".equals(newStatus) && !"CONFIRMED".equals(oldStatus)) {
                confirmOrderAndDeductInventory(orderId);
            }
            
            // 发送状态变更通知
            sendOrderNotification(orderId, newStatus, order.getCreatedBy());
            
            return "订单状态更新成功";
        } catch (Exception e) {
            log.error("更新订单状态失败", e);
            return "更新订单状态失败: " + e.getMessage();
        }
    }

    @Override
    public OrderReportVO getOrderReport(Integer pageNo, Integer pageSize, String customerId, String status) {
        OrderReportVO report = new OrderReportVO();
        
        // 构建查询条件
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        
        if (oConvertUtils.isNotEmpty(customerId)) {
            queryWrapper.eq("customer_id", customerId);
        }
        
        if (oConvertUtils.isNotEmpty(status)) {
            queryWrapper.eq("status", status);
        }
        
        queryWrapper.orderByDesc("order_date");
        
        // 分页查询
        IPage<Order> page = new Page<>(pageNo, pageSize);
        IPage<Order> orderPage = this.page(page, queryWrapper);
        
        // 转换为OrderItemVO
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
        
        // 计算汇总信息
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
        
        // 获取订单信息
        Order order = this.getById(orderId);
        if (order == null) {
            return result;
        }
        
        result.put("order", order);
        
        // 获取订单项信息
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
        // 构建查询条件
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        
        if (oConvertUtils.isNotEmpty(customerId)) {
            queryWrapper.eq("customer_id", customerId);
        }
        
        if (oConvertUtils.isNotEmpty(status)) {
            queryWrapper.eq("status", status);
        }
        
        // 查询数据
        List<Order> orderList = this.list(queryWrapper);
        
        // 导出Excel
        try {
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("订单报告_" + DateUtils.getDate() + ".xlsx", "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName);
            
            // 构建导出数据
            List<Map<String, Object>> exportList = new ArrayList<>();
            for (Order order : orderList) {
                Map<String, Object> exportMap = new HashMap<>();
                exportMap.put("订单编码", order.getOrderCode());
                exportMap.put("客户名称", order.getCustomerName());
                exportMap.put("订单日期", order.getOrderDate());
                exportMap.put("订单状态", order.getStatus());
                exportMap.put("总金额", order.getTotalAmount());
                exportMap.put("折扣金额", order.getDiscountAmount());
                exportMap.put("税额", order.getTaxAmount());
                exportMap.put("最终金额", order.getFinalAmount());
                exportMap.put("备注", order.getNotes());
                exportMap.put("创建人", order.getCreatedBy());
                exportList.add(exportMap);
            }
            
            // 导出Excel
            ExportParams params = new ExportParams();
            params.setTitle("订单报告");
            params.setSheetName("订单列表");
            
            OutputStream os = response.getOutputStream();
            ExcelExportUtil.exportExcel(params, exportList, Order.class, os);
            os.flush();
            os.close();
            
            log.info("导出订单报告成功，数据量: {}", orderList.size());
            
        } catch (Exception e) {
            log.error("导出订单报告失败", e);
        }
    }

    @Override
    public OrderStatisticsVO getStatistics() {
        OrderStatisticsVO statistics = new OrderStatisticsVO();
        
        // 获取基础统计信息
        Map<String, Object> baseStats = baseMapper.getStatistics();
        statistics.setTotalOrders((Integer) baseStats.get("totalOrders"));
        statistics.setTotalAmount((BigDecimal) baseStats.get("totalAmount"));
        statistics.setPendingCount((Integer) baseStats.get("pendingCount"));
        statistics.setConfirmedCount((Integer) baseStats.get("confirmedCount"));
        statistics.setShippingCount((Integer) baseStats.get("shippingCount"));
        statistics.setCompletedCount((Integer) baseStats.get("completedCount"));
        statistics.setCancelledCount((Integer) baseStats.get("cancelledCount"));
        
        // 获取今日统计
        Map<String, Object> todayStats = baseMapper.getTodayCounts();
        statistics.setTodayCount((Integer) todayStats.get("todayCount"));
        statistics.setTodayAmount((BigDecimal) todayStats.get("todayAmount"));
        
        // 获取本周统计
        Map<String, Object> weekStats = baseMapper.getWeekCounts();
        statistics.setWeekCount((Integer) weekStats.get("weekCount"));
        statistics.setWeekAmount((BigDecimal) weekStats.get("weekAmount"));
        
        // 获取本月统计
        Map<String, Object> monthStats = baseMapper.getMonthCounts();
        statistics.setMonthCount((Integer) monthStats.get("monthCount"));
        statistics.setMonthAmount((BigDecimal) monthStats.get("monthAmount"));
        
        // 计算平均订单金额
        if (statistics.getTotalOrders() != null && statistics.getTotalOrders() > 0) {
            statistics.setAverageOrderAmount(statistics.getTotalAmount().divide(new BigDecimal(statistics.getTotalOrders()), 2, RoundingMode.HALF_UP));
        }
        
        // 计算订单完成率
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
            // 获取订单详情
            Map<String, Object> orderDetail = getOrderDetail(orderId);
            if (orderDetail.isEmpty()) {
                log.error("未找到订单信息，订单ID: {}", orderId);
                return;
            }
            
            Order order = (Order) orderDetail.get("order");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> orderItems = (List<Map<String, Object>>) orderDetail.get("orderItems");
            
            // 生成HTML内容
            String htmlContent = generateOrderHtml(order, orderItems);
            
            // 设置响应头
            response.setContentType("application/pdf");
            response.setCharacterEncoding("UTF-8");
            String fileName = URLEncoder.encode("订单_" + order.getOrderCode() + ".pdf", "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName);
            
            // 使用第三方库转换HTML为PDF（这里简化处理，实际项目中可能需要引入iText或其他PDF库）
            // 这里仅作为示例，实际实现需要根据项目需求选择合适的PDF生成库
            OutputStream os = response.getOutputStream();
            os.write(htmlContent.getBytes("UTF-8"));
            os.flush();
            os.close();
            
            log.info("打印订单成功，订单ID: {}", orderId);
            
        } catch (Exception e) {
            log.error("打印订单失败", e);
        }
    }
    
    /**
     * 生成订单HTML内容
     * @param order 订单信息
     * @param orderItems 订单项列表
     * @return HTML内容
     */
    private String generateOrderHtml(Order order, List<Map<String, Object>> orderItems) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta charset=\"UTF-8\">");
        html.append("<title>订单详情 - ").append(order.getOrderCode()).append("</title>");
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
        
        // 订单头部信息
        html.append("<div class=\"header\">");
        html.append("<h1>订单详情</h1>");
        html.append("<h2>").append(order.getOrderCode()).append("</h2>");
        html.append("</div>");
        
        // 订单基本信息
        html.append("<div class=\"section\">");
        html.append("<div class=\"section-title\">订单信息</div>");
        html.append("<table>");
        html.append("<tr><th>客户名称</th><td>").append(order.getCustomerName() != null ? order.getCustomerName() : "").append("</td></tr>");
        html.append("<tr><th>订单日期</th><td>").append(order.getOrderDate() != null ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(order.getOrderDate()) : "").append("</td></tr>");
        html.append("<tr><th>订单状态</th><td>").append(order.getStatus() != null ? order.getStatus() : "").append("</td></tr>");
        html.append("<tr><th>总金额</th><td>").append(order.getTotalAmount() != null ? order.getTotalAmount().toString() : "0").append("</td></tr>");
        html.append("<tr><th>折扣金额</th><td>").append(order.getDiscountAmount() != null ? order.getDiscountAmount().toString() : "0").append("</td></tr>");
        html.append("<tr><th>税额</th><td>").append(order.getTaxAmount() != null ? order.getTaxAmount().toString() : "0").append("</td></tr>");
        html.append("<tr><th>最终金额</th><td>").append(order.getFinalAmount() != null ? order.getFinalAmount().toString() : "0").append("</td></tr>");
        html.append("<tr><th>备注</th><td>").append(order.getNotes() != null ? order.getNotes() : "").append("</td></tr>");
        html.append("</table>");
        html.append("</div>");
        
        // 订单项信息
        html.append("<div class=\"section\">");
        html.append("<div class=\"section-title\">订单项</div>");
        html.append("<table>");
        html.append("<tr><th>产品编码</th><th>产品名称</th><th>数量</th><th>单价</th><th>总价</th><th>折扣</th><th>最终金额</th></tr>");
        
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
        
        // 查询当天最大的订单编号
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
    public String checkAndReserveInventory(List<OrderItem> orderItems) {
        try {
            for (OrderItem item : orderItems) {
                // 检查库存
                String result = inventoryService.adjustInventory(item.getProductId(), -item.getQuantity(), "订单预留");
                if (!result.contains("成功")) {
                    return "产品库存不足: " + item.getProductId();
                }
            }
            return "库存检查成功";
        } catch (Exception e) {
            log.error("检查库存失败", e);
            return "检查库存失败: " + e.getMessage();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String confirmOrderAndDeductInventory(String orderId) {
        try {
            // 获取订单项
            List<Map<String, Object>> orderItems = orderItemMapper.getOrderItemsByOrderId(orderId);
            
            for (Map<String, Object> item : orderItems) {
                String productId = (String) item.get("product_id");
                Integer quantity = (Integer) item.get("quantity");
                
                // 扣减库存
                String result = inventoryService.adjustInventory(productId, -quantity, "订单出库");
                if (!result.contains("成功")) {
                    throw new RuntimeException("扣减库存失败: " + productId);
                }
            }
            
            return "库存扣减成功";
        } catch (Exception e) {
            log.error("确认订单并扣减库存失败", e);
            return "确认订单并扣减库存失败: " + e.getMessage();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String cancelOrderAndRestoreInventory(String orderId) {
        try {
            // 获取订单项
            List<Map<String, Object>> orderItems = orderItemMapper.getOrderItemsByOrderId(orderId);
            
            for (Map<String, Object> item : orderItems) {
                String productId = (String) item.get("product_id");
                Integer quantity = (Integer) item.get("quantity");
                
                // 恢复库存
                String result = inventoryService.adjustInventory(productId, quantity, "订单取消恢复");
                if (!result.contains("成功")) {
                    throw new RuntimeException("恢复库存失败: " + productId);
                }
            }
            
            return "库存恢复成功";
        } catch (Exception e) {
            log.error("取消订单并恢复库存失败", e);
            return "取消订单并恢复库存失败: " + e.getMessage();
        }
    }
    
    /**
     * 添加订单状态变更历史
     * @param orderId 订单ID
     * @param fromStatus 原状态
     * @param toStatus 新状态
     * @param reason 变更原因
     * @param userId 操作人
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
     * 添加订单处理日志
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
     * 发送订单通知
     */
    private void sendOrderNotification(String orderId, String status, String userId) {
        try {
            // 获取订单信息
            Order order = this.getById(orderId);
            if (order == null) {
                return;
            }
            
            // 获取客户信息
            Map<String, Object> customer = customerService.getById(order.getCustomerId());
            if (customer == null) {
                return;
            }
            
            String customerEmail = (String) customer.get("email");
            String customerName = (String) customer.get("name");
            
            if (customerEmail == null || customerEmail.isEmpty()) {
                log.warn("客户邮箱为空，跳过发送通知: {}", orderId);
                return;
            }
            
            // 根据状态发送不同的通知
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
                    emailNotificationService.sendOrderCancellationNotification(orderId, order.getOrderCode(), customerEmail, customerName, "订单已取消");
                    break;
            }
        } catch (Exception e) {
            log.error("发送订单通知失败: {}", orderId, e);
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
                        processResult = "不支持的操作: " + action;
                        break;
                }
                
                if (processResult.contains("成功")) {
                    successOrders.add(orderId);
                } else {
                    failedOrders.add(orderId + ": " + processResult);
                }
            } catch (Exception e) {
                log.error("批量处理订单失败: {}", orderId, e);
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
        
        // 查询待确认的订单
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "PENDING");
        queryWrapper.orderByAsc("create_time");
        queryWrapper.last("LIMIT 100"); // 限制每次处理100个订单
        
        List<Order> pendingOrders = this.list(queryWrapper);
        
        for (Order order : pendingOrders) {
            try {
                // 检查订单是否满足自动确认条件
                if (canAutoConfirmOrder(order)) {
                    String confirmResult = updateOrderStatus(order.getId(), "CONFIRMED", "系统自动确认");
                    if (confirmResult.contains("成功")) {
                        confirmedOrders.add(order.getId());
                    } else {
                        failedOrders.add(order.getId() + ": " + confirmResult);
                    }
                }
            } catch (Exception e) {
                log.error("自动确认订单失败: {}", order.getId(), e);
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
     * 检查订单是否可以自动确认
     */
    private boolean canAutoConfirmOrder(Order order) {
        // 这里可以添加自动确认的业务规则
        // 例如：订单金额小于某个阈值、客户信用良好、库存充足等
        
        // 简单示例：订单创建超过30分钟且金额小于1000
        long orderAge = System.currentTimeMillis() - order.getCreateTime().getTime();
        boolean isOldEnough = orderAge > 30 * 60 * 1000; // 30分钟
        boolean isSmallAmount = order.getFinalAmount().compareTo(new BigDecimal("1000")) < 0;
        
        return isOldEnough && isSmallAmount;
    }

    @Override
    public void generateStockOutNote(String orderId, HttpServletResponse response) {
        try {
            // 获取订单详情
            Map<String, Object> orderDetail = getOrderDetail(orderId);
            if (orderDetail.isEmpty()) {
                log.error("未找到订单信息，订单ID: {}", orderId);
                return;
            }
            
            Order order = (Order) orderDetail.get("order");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> orderItems = (List<Map<String, Object>>) orderDetail.get("orderItems");
            
            // 生成出库单HTML内容
            String htmlContent = generateStockOutNoteHtml(order, orderItems);
            
            // 设置响应头
            response.setContentType("application/pdf");
            response.setCharacterEncoding("UTF-8");
            String fileName = URLEncoder.encode("出库单_" + order.getOrderCode() + ".pdf", "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName);
            
            // 使用第三方库转换HTML为PDF（这里简化处理，实际项目中可能需要引入iText或其他PDF库）
            OutputStream os = response.getOutputStream();
            os.write(htmlContent.getBytes("UTF-8"));
            os.flush();
            os.close();
            
            log.info("生成出库单成功，订单ID: {}", orderId);
            
        } catch (Exception e) {
            log.error("生成出库单失败", e);
        }
    }

    /**
     * 生成出库单HTML内容
     */
    private String generateStockOutNoteHtml(Order order, List<Map<String, Object>> orderItems) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta charset=\"UTF-8\">");
        html.append("<title>出库单 - ").append(order.getOrderCode()).append("</title>");
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
        
        // 出库单头部信息
        html.append("<div class=\"header\">");
        html.append("<h1>出库单</h1>");
        html.append("<h2>").append(order.getOrderCode()).append("</h2>");
        html.append("</div>");
        
        // 出库单基本信息
        html.append("<div class=\"section\">");
        html.append("<div class=\"section-title\">出库信息</div>");
        html.append("<table>");
        html.append("<tr><th>客户名称</th><td>").append(order.getCustomerName() != null ? order.getCustomerName() : "").append("</td></tr>");
        html.append("<tr><th>出库日期</th><td>").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("</td></tr>");
        html.append("<tr><th>订单状态</th><td>").append(order.getStatus() != null ? order.getStatus() : "").append("</td></tr>");
        html.append("<tr><th>总金额</th><td>").append(order.getTotalAmount() != null ? order.getTotalAmount().toString() : "0").append("</td></tr>");
        html.append("<tr><th>最终金额</th><td>").append(order.getFinalAmount() != null ? order.getFinalAmount().toString() : "0").append("</td></tr>");
        html.append("<tr><th>备注</th><td>").append(order.getNotes() != null ? order.getNotes() : "").append("</td></tr>");
        html.append("</table>");
        html.append("</div>");
        
        // 出库项信息
        html.append("<div class=\"section\">");
        html.append("<div class=\"section-title\">出库商品</div>");
        html.append("<table>");
        html.append("<tr><th>产品编码</th><th>产品名称</th><th>数量</th><th>单价</th><th>总价</th><th>折扣</th><th>最终金额</th></tr>");
        
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