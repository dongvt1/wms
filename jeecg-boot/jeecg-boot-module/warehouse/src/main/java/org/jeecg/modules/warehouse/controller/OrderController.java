package org.jeecg.modules.warehouse.controller;

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
import org.jeecg.modules.warehouse.entity.Order;
import org.jeecg.modules.warehouse.entity.OrderItem;
import org.jeecg.modules.warehouse.service.OrderService;
import org.jeecg.modules.warehouse.vo.OrderReportVO;
import org.jeecg.modules.warehouse.vo.OrderStatisticsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 订单管理控制器
 */
@RestController
@RequestMapping("/warehouse/orders")
@Slf4j
public class OrderController extends JeecgController<Order, OrderService> {

    @Autowired
    private OrderService orderService;

    /**
     * 分页列表查询
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
     * 添加订单
     */
    @AutoLog(value = "订单管理-添加订单")
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
            if (result.contains("成功")) {
                return Result.OK(result);
            } else {
                return Result.error(result);
            }
        } catch (Exception e) {
            log.error("添加订单失败", e);
            return Result.error("添加订单失败: " + e.getMessage());
        }
    }

    /**
     * 编辑订单
     */
    @AutoLog(value = "订单管理-编辑订单")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody Order order) {
        String result = orderService.updateOrder(order);
        if (result.contains("成功")) {
            return Result.OK(result);
        } else {
            return Result.error(result);
        }
    }

    /**
     * 通过id删除订单
     */
    @AutoLog(value = "订单管理-通过id删除订单")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        // 订单不能直接删除，只能取消
        return Result.error("订单不能直接删除，请使用取消功能");
    }

    /**
     * 批量删除订单
     */
    @AutoLog(value = "订单管理-批量删除订单")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        // 订单不能直接删除，只能取消
        return Result.error("订单不能直接删除，请使用取消功能");
    }

    /**
     * 通过id查询订单
     */
    @GetMapping(value = "/queryById")
    public Result<Map<String, Object>> queryById(@RequestParam(name = "id", required = true) String id) {
        Map<String, Object> orderDetail = orderService.getOrderDetail(id);
        if (orderDetail.isEmpty()) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(orderDetail);
    }

    /**
     * 取消订单
     */
    @AutoLog(value = "订单管理-取消订单")
    @PutMapping(value = "/cancel")
    public Result<String> cancelOrder(@RequestBody Map<String, String> params) {
        String orderId = params.get("orderId");
        String reason = params.get("reason");
        
        String result = orderService.cancelOrder(orderId, reason);
        if (result.contains("成功")) {
            return Result.OK(result);
        } else {
            return Result.error(result);
        }
    }

    /**
     * 更新订单状态
     */
    @AutoLog(value = "订单管理-更新订单状态")
    @PutMapping(value = "/status")
    public Result<String> updateOrderStatus(@RequestBody Map<String, String> params) {
        String orderId = params.get("orderId");
        String newStatus = params.get("newStatus");
        String reason = params.get("reason");
        
        String result = orderService.updateOrderStatus(orderId, newStatus, reason);
        if (result.contains("成功")) {
            return Result.OK(result);
        } else {
            return Result.error(result);
        }
    }

    /**
     * 获取订单报告
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
     * 根据订单编码搜索订单
     */
    @GetMapping(value = "/search/code")
    public Result<List<Map<String, Object>>> searchByCode(@RequestParam(name = "orderCode", required = true) String orderCode) {
        List<Map<String, Object>> orders = orderService.searchOrdersByCode(orderCode);
        return Result.OK(orders);
    }

    /**
     * 根据客户名称搜索订单
     */
    @GetMapping(value = "/search/customer")
    public Result<List<Map<String, Object>>> searchByCustomerName(@RequestParam(name = "customerName", required = true) String customerName) {
        List<Map<String, Object>> orders = orderService.searchOrdersByCustomerName(customerName);
        return Result.OK(orders);
    }

    /**
     * 获取订单状态历史
     */
    @GetMapping(value = "/{orderId}/status-history")
    public Result<List<Map<String, Object>>> getStatusHistory(@PathVariable("orderId") String orderId) {
        List<Map<String, Object>> history = orderService.getOrderStatusHistory(orderId);
        return Result.OK(history);
    }

    /**
     * 导出订单报告
     */
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void exportXls(HttpServletRequest request, HttpServletResponse response) {
        // 参数验证
        String customerId = request.getParameter("customerId");
        String status = request.getParameter("status");
        
        // 导出Excel
        orderService.exportOrderReport(request, response, customerId, status);
    }

    /**
     * 打印订单
     */
    @GetMapping(value = "/{orderId}/print")
    public void printOrder(@PathVariable("orderId") String orderId,
                         HttpServletResponse response) {
        orderService.printOrder(orderId, response);
    }

    /**
     * 获取订单统计信息
     */
    @GetMapping(value = "/statistics")
    public Result<OrderStatisticsVO> getStatistics() {
        OrderStatisticsVO statistics = orderService.getStatistics();
        return Result.OK(statistics);
    }

    /**
     * 生成订单编码
     */
    @GetMapping(value = "/generate-code")
    public Result<String> generateOrderCode() {
        String orderCode = orderService.generateOrderCode();
        return Result.OK(orderCode);
    }

    /**
     * 计算订单金额
     */
    @PostMapping(value = "/calculate-amount")
    public Result<Map<String, Object>> calculateAmount(@RequestBody List<OrderItem> orderItems) {
        Map<String, Object> amount = orderService.calculateOrderAmount(orderItems);
        return Result.OK(amount);
    }

    /**
     * 检查库存
     */
    @PostMapping(value = "/check-inventory")
    public Result<String> checkInventory(@RequestBody List<OrderItem> orderItems) {
        String result = orderService.checkAndReserveInventory(orderItems);
        if (result.contains("成功")) {
            return Result.OK(result);
        } else {
            return Result.error(result);
        }
    }

    /**
     * 确认订单
     */
    @AutoLog(value = "订单管理-确认订单")
    @PutMapping(value = "/{orderId}/confirm")
    public Result<String> confirmOrder(@PathVariable("orderId") String orderId) {
        String result = orderService.updateOrderStatus(orderId, "CONFIRMED", "确认订单");
        if (result.contains("成功")) {
            return Result.OK(result);
        } else {
            return Result.error(result);
        }
    }

    /**
     * 开始配送
     */
    @AutoLog(value = "订单管理-开始配送")
    @PutMapping(value = "/{orderId}/ship")
    public Result<String> shipOrder(@PathVariable("orderId") String orderId) {
        String result = orderService.updateOrderStatus(orderId, "SHIPPING", "开始配送");
        if (result.contains("成功")) {
            return Result.OK(result);
        } else {
            return Result.error(result);
        }
    }

    /**
     * 完成订单
     */
    @AutoLog(value = "订单管理-完成订单")
    @PutMapping(value = "/{orderId}/complete")
    public Result<String> completeOrder(@PathVariable("orderId") String orderId) {
        String result = orderService.updateOrderStatus(orderId, "COMPLETED", "订单完成");
        if (result.contains("成功")) {
            return Result.OK(result);
        } else {
            return Result.error(result);
        }
    }

    /**
     * 搜索订单
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
     * 批量处理订单
     */
    @AutoLog(value = "订单管理-批量处理订单")
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
     * 自动确认订单
     */
    @AutoLog(value = "订单管理-自动确认订单")
    @PostMapping(value = "/auto-confirm")
    public Result<Map<String, Object>> autoConfirmOrders() {
        Map<String, Object> result = orderService.autoConfirmOrders();
        return Result.OK(result);
    }

    /**
     * 生成出库单
     */
    @GetMapping(value = "/{orderId}/stock-out-note")
    public void generateStockOutNote(@PathVariable("orderId") String orderId,
                                   HttpServletResponse response) {
        orderService.generateStockOutNote(orderId, response);
    }

    /**
     * 获取订单处理日志
     */
    @GetMapping(value = "/{orderId}/processing-logs")
    public Result<List<Map<String, Object>>> getProcessingLogs(@PathVariable("orderId") String orderId) {
        List<Map<String, Object>> logs = orderService.getOrderProcessingLogs(orderId);
        return Result.OK(logs);
    }

    /**
     * 重新发送订单通知
     */
    @AutoLog(value = "订单管理-重新发送订单通知")
    @PostMapping(value = "/resend-notification")
    public Result<String> resendNotification(@RequestBody Map<String, String> params) {
        String notificationId = params.get("notificationId");
        String result = orderService.resendOrderNotification(notificationId);
        if (result.contains("成功")) {
            return Result.OK(result);
        } else {
            return Result.error(result);
        }
    }

    /**
     * 处理待发送的通知
     */
    @AutoLog(value = "订单管理-处理待发送的通知")
    @PostMapping(value = "/process-notifications")
    public Result<String> processPendingNotifications() {
        String result = orderService.processPendingNotifications();
        return Result.OK(result);
    }

    /**
     * 获取订单处理统计信息
     */
    @GetMapping(value = "/processing-statistics")
    public Result<Map<String, Object>> getProcessingStatistics() {
        Map<String, Object> statistics = orderService.getOrderProcessingStatistics();
        return Result.OK(statistics);
    }
}