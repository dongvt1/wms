package org.jeecg.modules.warehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.warehouse.entity.ProductCategory;

import java.util.List;

/**
 * @Description: Product Category Service
 * @Author: BMad
 * @Date:   2025-11-20
 * @Version: V1.0
 */
public interface ProductCategoryService extends IService<ProductCategory> {

    /**
     * Get category tree structure
     * @return List of categories in tree structure
     */
    List<ProductCategory> getTree();

    /**
     * Get categories by parent ID
     * @param parentId Parent category ID
     * @return List of child categories
     */
    List<ProductCategory> getByParentId(String parentId);

    /**
     * Get categories by status
     * @param status Status (0: Inactive, 1: Active)
     * @return List of categories
     */
    List<ProductCategory> getByStatus(Integer status);

    /**
     * Build category tree from flat list
     * @param categories List of categories
     * @return Tree structure of categories
     */
    List<ProductCategory> buildTree(List<ProductCategory> categories);
}