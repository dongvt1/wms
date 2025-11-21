package org.jeecg.modules.warehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.warehouse.entity.OrderItem;

import java.util.List;
import java.util.Map;

/**
 * 订单项表 mapper 接口
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    /**
     * 根据订单ID获取订单项列表
     * @param orderId 订单ID
     * @return 订单项列表
     */
    @Select("SELECT oi.*, p.name as product_name, p.code as product_code " +
            "FROM order_items oi " +
            "LEFT JOIN product p ON oi.product_id = p.id " +
            "WHERE oi.order_id = #{orderId} " +
            "ORDER BY oi.create_time ASC")
    List<Map<String, Object>> getOrderItemsByOrderId(@Param("orderId") String orderId);

    /**
     * 根据产品ID获取订单项列表
     * @param productId 产品ID
     * @return 订单项列表
     */
    @Select("SELECT oi.*, o.order_code, o.order_date, c.customer_name " +
            "FROM order_items oi " +
            "LEFT JOIN orders o ON oi.order_id = o.id " +
            "LEFT JOIN customers c ON o.customer_id = c.id " +
            "WHERE oi.product_id = #{productId} " +
            "ORDER BY o.order_date DESC")
    List<Map<String, Object>> getOrderItemsByProductId(@Param("productId") String productId);

    /**
     * 获取产品销售统计
     * @return 产品销售统计
     */
    @Select("SELECT oi.product_id, p.name as product_name, p.code as product_code, " +
            "SUM(oi.quantity) as totalQuantity, " +
            "SUM(oi.final_amount) as totalAmount, " +
            "COUNT(DISTINCT oi.order_id) as orderCount " +
            "FROM order_items oi " +
            "LEFT JOIN product p ON oi.product_id = p.id " +
            "GROUP BY oi.product_id, p.name, p.code " +
            "ORDER BY totalAmount DESC")
    List<Map<String, Object>> getProductSalesStatistics();

    /**
     * 获取热销产品
     * @param limit 限制数量
     * @return 热销产品列表
     */
    @Select("SELECT oi.product_id, p.name as product_name, p.code as product_code, " +
            "SUM(oi.quantity) as totalQuantity, " +
            "SUM(oi.final_amount) as totalAmount " +
            "FROM order_items oi " +
            "LEFT JOIN product p ON oi.product_id = p.id " +
            "GROUP BY oi.product_id, p.name, p.code " +
            "ORDER BY totalQuantity DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getTopSellingProducts(@Param("limit") Integer limit);
}