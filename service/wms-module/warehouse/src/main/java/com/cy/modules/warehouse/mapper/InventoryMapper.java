package com.cy.modules.warehouse.mapper;

import java.util.List;

import com.cy.modules.warehouse.entity.Inventory;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: Mapper tồn kho
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
public interface InventoryMapper extends BaseMapper<Inventory> {

    /**
     * Lấy tồn kho theo ID sản phẩm
     * @param productId ID sản phẩm
     * @return Thông tin tồn kho
     */
    @Select("SELECT * FROM inventory WHERE product_id = #{productId}")
    Inventory getByProductId(@Param("productId") String productId);

    /**
     * Cập nhật số lượng tồn kho
     * @param productId ID sản phẩm
     * @param quantity Số lượng mới
     * @param reservedQuantity Số lượng đặt trước
     * @param availableQuantity Số lượng có thể bán
     * @param updatedBy Người cập nhật
     * @return Số dòng bị ảnh hưởng
     */
    @Update("UPDATE inventory SET quantity = #{quantity}, reserved_quantity = #{reservedQuantity}, " +
            "available_quantity = #{availableQuantity}, last_updated = NOW(), updated_by = #{updatedBy} " +
            "WHERE product_id = #{productId}")
    int updateQuantity(@Param("productId") String productId, 
                      @Param("quantity") Integer quantity,
                      @Param("reservedQuantity") Integer reservedQuantity,
                      @Param("availableQuantity") Integer availableQuantity,
                      @Param("updatedBy") String updatedBy);

    /**
     * Lấy các sản phẩm sắp hết hàng
     * @return Danh sách tồn kho gần hết
     */
    @Select("SELECT i.*, p.name as product_name, p.min_stock_level " +
            "FROM inventory i " +
            "JOIN product p ON i.product_id = p.id " +
            "WHERE i.available_quantity <= p.min_stock_level AND p.status = 1")
    List<Inventory> getLowStockProducts();

    /**
     * Lấy báo cáo giá trị tồn kho
     * @return Danh sách tồn kho kèm giá trị
     */
    @Select("SELECT i.*, p.name as product_name, p.price, " +
            "(i.quantity * p.price) as total_value " +
            "FROM inventory i " +
            "JOIN product p ON i.product_id = p.id " +
            "WHERE p.status = 1")
    List<Inventory> getInventoryValueReport();
}