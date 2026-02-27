package com.cy.modules.warehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.cy.modules.warehouse.entity.Order;

import java.util.List;
import java.util.Map;

/**
 * Mapper interface bảng đơn hàng
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * Lấy thống kê đơn hàng
     * @return Thông tin thống kê đơn hàng
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
     * Lấy số lượng đơn hàng hôm nay
     * @return Số lượng đơn hàng hôm nay
     */
    @Select("SELECT " +
            "COUNT(CASE WHEN DATE(order_date) = CURDATE() THEN 1 END) as todayCount, " +
            "COALESCE(SUM(CASE WHEN DATE(order_date) = CURDATE() THEN final_amount ELSE 0 END), 0) as todayAmount " +
            "FROM orders")
    Map<String, Object> getTodayCounts();

    /**
     * Lấy số lượng đơn hàng tuần này
     * @return Số lượng đơn hàng tuần này
     */
    @Select("SELECT " +
            "COUNT(CASE WHEN YEARWEEK(order_date) = YEARWEEK(NOW()) THEN 1 END) as weekCount, " +
            "COALESCE(SUM(CASE WHEN YEARWEEK(order_date) = YEARWEEK(NOW()) THEN final_amount ELSE 0 END), 0) as weekAmount " +
            "FROM orders")
    Map<String, Object> getWeekCounts();

    /**
     * Lấy số lượng đơn hàng tháng này
     * @return Số lượng đơn hàng tháng này
     */
    @Select("SELECT " +
            "COUNT(CASE WHEN YEAR(order_date) = YEAR(NOW()) AND MONTH(order_date) = MONTH(NOW()) THEN 1 END) as monthCount, " +
            "COALESCE(SUM(CASE WHEN YEAR(order_date) = YEAR(NOW()) AND MONTH(order_date) = MONTH(NOW()) THEN final_amount ELSE 0 END), 0) as monthAmount " +
            "FROM orders")
    Map<String, Object> getMonthCounts();

    /**
     * Lấy danh sách đơn hàng theo ID khách hàng
     * @param customerId ID khách hàng
     * @return Danh sách đơn hàng
     */
    @Select("SELECT o.*, c.customer_name " +
            "FROM orders o " +
            "LEFT JOIN customers c ON o.customer_id = c.id " +
            "WHERE o.customer_id = #{customerId} " +
            "ORDER BY o.order_date DESC")
    List<Map<String, Object>> getOrdersByCustomerId(@Param("customerId") String customerId);

    /**
     * Tìm kiếm đơn hàng theo mã đơn
     * @param orderCode Mã đơn hàng
     * @return Thông tin đơn hàng
     */
    @Select("SELECT o.*, c.customer_name " +
            "FROM orders o " +
            "LEFT JOIN customers c ON o.customer_id = c.id " +
            "WHERE o.order_code LIKE CONCAT('%', #{orderCode}, '%') " +
            "ORDER BY o.order_date DESC")
    List<Map<String, Object>> searchOrdersByCode(@Param("orderCode") String orderCode);

    /**
     * Tìm kiếm đơn hàng theo tên khách hàng
     * @param customerName Tên khách hàng
     * @return Danh sách đơn hàng
     */
    @Select("SELECT o.*, c.customer_name " +
            "FROM orders o " +
            "LEFT JOIN customers c ON o.customer_id = c.id " +
            "WHERE c.customer_name LIKE CONCAT('%', #{customerName}, '%') " +
            "ORDER BY o.order_date DESC")
    List<Map<String, Object>> searchOrdersByCustomerName(@Param("customerName") String customerName);
}