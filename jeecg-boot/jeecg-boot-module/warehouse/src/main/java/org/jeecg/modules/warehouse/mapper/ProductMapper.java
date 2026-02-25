package org.jeecg.modules.warehouse.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.warehouse.entity.Product;

/**
 * @Description: Product Mapper
 * @Author: BMad
 * @Date:   2025-11-20
 * @Version: V1.0
 */
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * Get products by category ID
     * @param categoryId Category ID
     * @return List of products
     */
    List<Product> selectByCategoryId(@Param("categoryId") String categoryId);

    /**
     * Get products by status
     * @param status Status (0: Inactive, 1: Active)
     * @return List of products
     */
    List<Product> selectByStatus(@Param("status") Integer status);

    /**
     * Search products by code or name
     * @param keyword Search keyword
     * @return List of products
     */
    List<Product> searchProducts(@Param("keyword") String keyword);

    /**
     * Get products with low stock
     * @return List of products with stock below minimum level
     */
    List<Product> selectLowStockProducts();

    /**
     * Update current stock
     * @param id Product ID
     * @param currentStock Current stock quantity
     * @return Number of affected rows
     */
    int updateCurrentStock(@Param("id") String id, @Param("currentStock") Integer currentStock);
}