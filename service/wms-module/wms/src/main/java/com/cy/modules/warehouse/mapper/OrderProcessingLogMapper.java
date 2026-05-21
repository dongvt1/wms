package com.cy.modules.warehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.cy.modules.warehouse.entity.OrderProcessingLog;

import java.util.List;
import java.util.Map;

/**
 * @Description: Mapper nhật ký xử lý đơn hàng
 * @Author: jeecg
 * @Date: 2025-11-21
 * @Version: V1.0
 */
@Mapper
public interface OrderProcessingLogMapper extends BaseMapper<OrderProcessingLog> {

    /**
     * Lấy danh sách nhật ký xử lý theo đơn hàng
     * @param orderId ID đơn hàng
     * @return Danh sách nhật ký xử lý
     */
    @Select("SELECT opl.*, o.order_code " +
            "FROM order_processing_log opl " +
            "LEFT JOIN orders o ON opl.order_id = o.id " +
            "WHERE opl.order_id = #{orderId} " +
            "ORDER BY opl.create_time ASC")
    List<Map<String, Object>> getProcessingLogsByOrderId(@Param("orderId") String orderId);

    /**
     * Lấy danh sách nhật ký xử lý thất bại
     * @return Danh sách nhật ký thất bại
     */
    @Select("SELECT * FROM order_processing_log WHERE status = 'FAILED' ORDER BY create_time DESC")
    List<OrderProcessingLog> getFailedProcessingLogs();

    /**
     * Lấy thống kê xử lý
     * @return Thông tin thống kê
     */
    @Select("SELECT " +
            "COUNT(*) as totalLogs, " +
            "COUNT(CASE WHEN status = 'SUCCESS' THEN 1 END) as successCount, " +
            "COUNT(CASE WHEN status = 'FAILED' THEN 1 END) as failedCount, " +
            "COUNT(CASE WHEN action = 'CREATE' THEN 1 END) as createCount, " +
            "COUNT(CASE WHEN action = 'CANCEL' THEN 1 END) as cancelCount, " +
            "COUNT(CASE WHEN action = 'STATUS_UPDATE' THEN 1 END) as statusUpdateCount " +
            "FROM order_processing_log " +
            "WHERE create_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)")
    Map<String, Object> getProcessingStatistics();

    /**
     * Lấy nhật ký xử lý theo khoảng thời gian
     * @param startDate Thời gian bắt đầu
     * @param endDate Thời gian kết thúc
     * @return Danh sách nhật ký xử lý
     */
    @Select("SELECT * FROM order_processing_log " +
            "WHERE create_time BETWEEN #{startDate} AND #{endDate} " +
            "ORDER BY create_time DESC")
    List<OrderProcessingLog> getProcessingLogsByDateRange(@Param("startDate") String startDate,
                                                          @Param("endDate") String endDate);
}