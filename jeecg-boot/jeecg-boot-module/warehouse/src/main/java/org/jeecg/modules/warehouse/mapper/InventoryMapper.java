package org.jeecg.modules.warehouse.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: Inventory Mapper
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
public interface InventoryMapper extends BaseMapper<Inventory> {

    /**
     * Get inventory by product ID
     * @param productId Product ID
     * @return Inventory
     */
    @Select("SELECT * FROM inventory WHERE product_id = #{productId}")
    Inventory getByProductId(@Param("productId") String productId);

    /**
     * Update inventory quantity
     * @param productId Product ID
     * @param quantity New quantity
     * @param reservedQuantity Reserved quantity
     * @param availableQuantity Available quantity
     * @param updatedBy Updated by
     * @return Number of affected rows
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
     * Get products with low stock
     * @return List of inventory records with low stock
     */
    @Select("SELECT i.*, p.name as product_name, p.min_stock_level " +
            "FROM inventory i " +
            "JOIN product p ON i.product_id = p.id " +
            "WHERE i.available_quantity <= p.min_stock_level AND p.status = 1")
    List<Inventory> getLowStockProducts();

    /**
     * Get inventory value report
     * @return List of inventory with value
     */
    @Select("SELECT i.*, p.name as product_name, p.price, " +
            "(i.quantity * p.price) as total_value " +
            "FROM inventory i " +
            "JOIN product p ON i.product_id = p.id " +
            "WHERE p.status = 1")
    List<Inventory> getInventoryValueReport();
}