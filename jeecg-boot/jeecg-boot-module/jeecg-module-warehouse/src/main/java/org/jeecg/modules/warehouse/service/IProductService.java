package org.jeecg.modules.warehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.warehouse.entity.Product;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Description: Product Service
 * @Author: BMad
 * @Date:   2025-11-20
 * @Version: V1.0
 */
public interface IProductService extends IService<Product> {

    /**
     * Get products by category ID
     * @param categoryId Category ID
     * @return List of products
     */
    List<Product> getByCategoryId(String categoryId);

    /**
     * Get products by status
     * @param status Status (0: Inactive, 1: Active)
     * @return List of products
     */
    List<Product> getByStatus(Integer status);

    /**
     * Search products by code or name
     * @param keyword Search keyword
     * @return List of products
     */
    List<Product> searchProducts(String keyword);

    /**
     * Get products with low stock
     * @return List of products with stock below minimum level
     */
    List<Product> getLowStockProducts();

    /**
     * Update current stock
     * @param id Product ID
     * @param currentStock Current stock quantity
     * @return Update result
     */
    boolean updateCurrentStock(String id, Integer currentStock);

    /**
     * Upload product image
     * @param file Image file
     * @return Image URL
     */
    String uploadImage(MultipartFile file);

    /**
     * Check if product code is unique
     * @param code Product code
     * @param excludeId Product ID to exclude from check (for update)
     * @return True if code is unique
     */
    boolean isCodeUnique(String code, String excludeId);
}