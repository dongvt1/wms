package com.cy.modules.warehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.cy.modules.warehouse.entity.OrderStatusHistory;

import java.util.List;
import java.util.Map;

/**
 * Mapper interface bảng lịch sử trạng thái đơn hàng
 */
@Mapper
public interface OrderStatusHistoryMapper extends BaseMapper<OrderStatusHistory> {

    /**
     * Lấy lịch sử trạng thái theo ID đơn hàng
     * @param orderId ID đơn hàng
     * @return Danh sách lịch sử trạng thái
     */
    @Select("SELECT osh.*, o.order_code " +
            "FROM order_status_history osh " +
            "LEFT JOIN orders o ON osh.order_id = o.id " +
            "WHERE osh.order_id = #{orderId} " +
            "ORDER BY osh.created_at ASC")
    List<Map<String, Object>> getStatusHistoryByOrderId(@Param("orderId") String orderId);

    /**
     * Lấy lịch sử trạng thái theo mã đơn hàng
     * @param orderCode Mã đơn hàng
     * @return Danh sách lịch sử trạng thái
     */
    @Select("SELECT osh.*, o.order_code " +
            "FROM order_status_history osh " +
            "LEFT JOIN orders o ON osh.order_id = o.id " +
            "WHERE o.order_code = #{orderCode} " +
            "ORDER BY osh.created_at ASC")
    List<Map<String, Object>> getStatusHistoryByOrderCode(@Param("orderCode") String orderCode);

    /**
     * Lấy thống kê thay đổi trạng thái
     * @return Thống kê thay đổi trạng thái
     */
    @Select("SELECT " +
            "to_status, " +
            "COUNT(*) as count, " +
            "DATE(created_at) as date " +
            "FROM order_status_history " +
            "WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) " +
            "GROUP BY to_status, DATE(created_at) " +
            "ORDER BY date DESC, count DESC")
    List<Map<String, Object>> getStatusChangeStatistics();

    /**
     * Lấy thống kê thao tác của người dùng
     * @return Thống kê thao tác người dùng
     */
    @Select("SELECT " +
            "user_id, " +
            "COUNT(*) as operationCount " +
            "FROM order_status_history " +
            "WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) " +
            "GROUP BY user_id " +
            "ORDER BY operationCount DESC")
    List<Map<String, Object>> getUserOperationStatistics();
}