package com.cy.modules.warehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.cy.modules.warehouse.entity.OrderNotification;

import java.util.List;
import java.util.Map;

/**
 * @Description: Mapper thông báo đơn hàng
 * @Author: jeecg
 * @Date: 2025-11-21
 * @Version: V1.0
 */
@Mapper
public interface OrderNotificationMapper extends BaseMapper<OrderNotification> {

    /**
     * Lấy danh sách thông báo theo đơn hàng
     * @param orderId ID đơn hàng
     * @return Danh sách thông báo
     */
    List<Map<String, Object>> getNotificationsByOrderId(@Param("orderId") String orderId);

    /**
     * Lấy danh sách thông báo đang chờ gửi
     * @return Danh sách thông báo chờ gửi
     */
    List<OrderNotification> getPendingNotifications();

    /**
     * Lấy danh sách thông báo thất bại
     * @param maxRetryCount Số lần thử lại tối đa
     * @return Danh sách thông báo thất bại
     */
    List<OrderNotification> getFailedNotifications(@Param("maxRetryCount") Integer maxRetryCount);
}