package org.jeecg.modules.warehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.warehouse.entity.OrderStatusHistory;

import java.util.List;
import java.util.Map;

/**
 * 订单状态历史表 mapper 接口
 */
@Mapper
public interface OrderStatusHistoryMapper extends BaseMapper<OrderStatusHistory> {

    /**
     * 根据订单ID获取状态历史
     * @param orderId 订单ID
     * @return 状态历史列表
     */
    @Select("SELECT osh.*, o.order_code " +
            "FROM order_status_history osh " +
            "LEFT JOIN orders o ON osh.order_id = o.id " +
            "WHERE osh.order_id = #{orderId} " +
            "ORDER BY osh.created_at ASC")
    List<Map<String, Object>> getStatusHistoryByOrderId(@Param("orderId") String orderId);

    /**
     * 根据订单编码获取状态历史
     * @param orderCode 订单编码
     * @return 状态历史列表
     */
    @Select("SELECT osh.*, o.order_code " +
            "FROM order_status_history osh " +
            "LEFT JOIN orders o ON osh.order_id = o.id " +
            "WHERE o.order_code = #{orderCode} " +
            "ORDER BY osh.created_at ASC")
    List<Map<String, Object>> getStatusHistoryByOrderCode(@Param("orderCode") String orderCode);

    /**
     * 获取状态变更统计
     * @return 状态变更统计
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
     * 获取用户操作统计
     * @return 用户操作统计
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