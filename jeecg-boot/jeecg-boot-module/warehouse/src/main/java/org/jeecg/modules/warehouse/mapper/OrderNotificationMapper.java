package org.jeecg.modules.warehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.warehouse.entity.OrderNotification;

import java.util.List;
import java.util.Map;

/**
 * @Description: Order notifications mapper
 * @Author: jeecg
 * @Date: 2025-11-21
 * @Version: V1.0
 */
@Mapper
public interface OrderNotificationMapper extends BaseMapper<OrderNotification> {

    /**
     * 获取订单通知列表
     * @param orderId 订单ID
     * @return 通知列表
     */
    List<Map<String, Object>> getNotificationsByOrderId(@Param("orderId") String orderId);

    /**
     * 获取待发送的通知列表
     * @return 待发送通知列表
     */
    List<OrderNotification> getPendingNotifications();

    /**
     * 获取失败的通知列表
     * @param maxRetryCount 最大重试次数
     * @return 失败通知列表
     */
    List<OrderNotification> getFailedNotifications(@Param("maxRetryCount") Integer maxRetryCount);
}