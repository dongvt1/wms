package org.jeecg.modules.warehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.warehouse.entity.Order;

import java.util.List;
import java.util.Map;

/**
 * 订单表 mapper 接口
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 获取订单统计信息
     * @return 订单统计信息
     */
    @Select("SELECT " +
            "COUNT(DISTINCT o.id) as totalOrders, " +
            "COALESCE(SUM(o.final_amount), 0) as totalAmount, " +
            "COUNT(CASE WHEN o.status = 'PENDING' THEN 1 END) as pendingCount, " +
            "COUNT(CASE WHEN o.status = 'CONFIRMED' THEN 1 END) as confirmedCount, " +
            "COUNT(CASE WHEN o.status = 'SHIPPING' THEN 1 END) as shippingCount, " +
            "COUNT(CASE WHEN o.status = 'COMPLETED' THEN 1 END) as completedCount, " +
            "COUNT(CASE WHEN o.status = 'CANCELLED' THEN 1 END) as cancelledCount " +
            "FROM orders o")
    Map<String, Object> getStatistics();

    /**
     * 获取今日订单数量
     * @return 今日订单数量
     */
    @Select("SELECT " +
            "COUNT(CASE WHEN DATE(order_date) = CURDATE() THEN 1 END) as todayCount, " +
            "COALESCE(SUM(CASE WHEN DATE(order_date) = CURDATE() THEN final_amount ELSE 0 END), 0) as todayAmount " +
            "FROM orders")
    Map<String, Object> getTodayCounts();

    /**
     * 获取本周订单数量
     * @return 本周订单数量
     */
    @Select("SELECT " +
            "COUNT(CASE WHEN YEARWEEK(order_date) = YEARWEEK(NOW()) THEN 1 END) as weekCount, " +
            "COALESCE(SUM(CASE WHEN YEARWEEK(order_date) = YEARWEEK(NOW()) THEN final_amount ELSE 0 END), 0) as weekAmount " +
            "FROM orders")
    Map<String, Object> getWeekCounts();

    /**
     * 获取本月订单数量
     * @return 本月订单数量
     */
    @Select("SELECT " +
            "COUNT(CASE WHEN YEAR(order_date) = YEAR(NOW()) AND MONTH(order_date) = MONTH(NOW()) THEN 1 END) as monthCount, " +
            "COALESCE(SUM(CASE WHEN YEAR(order_date) = YEAR(NOW()) AND MONTH(order_date) = MONTH(NOW()) THEN final_amount ELSE 0 END), 0) as monthAmount " +
            "FROM orders")
    Map<String, Object> getMonthCounts();

    /**
     * 根据客户ID获取订单列表
     * @param customerId 客户ID
     * @return 订单列表
     */
    @Select("SELECT o.*, c.customer_name " +
            "FROM orders o " +
            "LEFT JOIN customers c ON o.customer_id = c.id " +
            "WHERE o.customer_id = #{customerId} " +
            "ORDER BY o.order_date DESC")
    List<Map<String, Object>> getOrdersByCustomerId(@Param("customerId") String customerId);

    /**
     * 根据订单编码搜索订单
     * @param orderCode 订单编码
     * @return 订单信息
     */
    @Select("SELECT o.*, c.customer_name " +
            "FROM orders o " +
            "LEFT JOIN customers c ON o.customer_id = c.id " +
            "WHERE o.order_code LIKE CONCAT('%', #{orderCode}, '%') " +
            "ORDER BY o.order_date DESC")
    List<Map<String, Object>> searchOrdersByCode(@Param("orderCode") String orderCode);

    /**
     * 根据客户名称搜索订单
     * @param customerName 客户名称
     * @return 订单列表
     */
    @Select("SELECT o.*, c.customer_name " +
            "FROM orders o " +
            "LEFT JOIN customers c ON o.customer_id = c.id " +
            "WHERE c.customer_name LIKE CONCAT('%', #{customerName}, '%') " +
            "ORDER BY o.order_date DESC")
    List<Map<String, Object>> searchOrdersByCustomerName(@Param("customerName") String customerName);
}