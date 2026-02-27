package com.cy.modules.warehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.cy.modules.warehouse.entity.OrderItem;

import java.util.List;
import java.util.Map;

/**
 * Mapper interface bảng sản phẩm trong đơn hàng
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    /**
     * Lấy danh sách sản phẩm theo ID đơn hàng
     * @param orderId ID đơn hàng
     * @return Danh sách sản phẩm trong đơn
     */
    @Select("SELECT oi.*, p.name as product_name, p.code as product_code " +
            "FROM order_items oi " +
            "LEFT JOIN product p ON oi.product_id = p.id " +
            "WHERE oi.order_id = #{orderId} " +
            "ORDER BY oi.create_time ASC")
    List<Map<String, Object>> getOrderItemsByOrderId(@Param("orderId") String orderId);

    /**
     * Lấy danh sách sản phẩm theo ID sản phẩm
     * @param productId ID sản phẩm
     * @return Danh sách sản phẩm trong đơn
     */
    @Select("SELECT oi.*, o.order_code, o.order_date, c.customer_name " +
            "FROM order_items oi " +
            "LEFT JOIN orders o ON oi.order_id = o.id " +
            "LEFT JOIN customers c ON o.customer_id = c.id " +
            "WHERE oi.product_id = #{productId} " +
            "ORDER BY o.order_date DESC")
    List<Map<String, Object>> getOrderItemsByProductId(@Param("productId") String productId);

    /**
     * Lấy thống kê doanh số sản phẩm
     * @return Thống kê doanh số sản phẩm
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
     * Lấy sản phẩm bán chạy
     * @param limit Giới hạn số lượng
     * @return Danh sách sản phẩm bán chạy
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