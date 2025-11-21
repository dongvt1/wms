package org.jeecg.modules.warehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.warehouse.entity.OrderProcessingLog;

import java.util.List;
import java.util.Map;

/**
 * @Description: Order processing logs mapper
 * @Author: jeecg
 * @Date: 2025-11-21
 * @Version: V1.0
 */
@Mapper
public interface OrderProcessingLogMapper extends BaseMapper<OrderProcessingLog> {

    /**
     * 获取订单处理日志列表
     * @param orderId 订单ID
     * @return 处理日志列表
     */
    @Select("SELECT opl.*, o.order_code " +
            "FROM order_processing_log opl " +
            "LEFT JOIN orders o ON opl.order_id = o.id " +
            "WHERE opl.order_id = #{orderId} " +
            "ORDER BY opl.create_time ASC")
    List<Map<String, Object>> getProcessingLogsByOrderId(@Param("orderId") String orderId);

    /**
     * 获取失败的处理日志列表
     * @return 失败处理日志列表
     */
    @Select("SELECT * FROM order_processing_log WHERE status = 'FAILED' ORDER BY create_time DESC")
    List<OrderProcessingLog> getFailedProcessingLogs();

    /**
     * 获取处理统计信息
     * @return 统计信息
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
     * 获取指定时间范围内的处理日志
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return 处理日志列表
     */
    @Select("SELECT * FROM order_processing_log " +
            "WHERE create_time BETWEEN #{startDate} AND #{endDate} " +
            "ORDER BY create_time DESC")
    List<OrderProcessingLog> getProcessingLogsByDateRange(@Param("startDate") String startDate,
                                                          @Param("endDate") String endDate);
}