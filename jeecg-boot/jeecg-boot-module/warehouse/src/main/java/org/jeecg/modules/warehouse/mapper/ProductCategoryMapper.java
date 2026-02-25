package org.jeecg.modules.warehouse.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.warehouse.entity.ProductCategory;

/**
 * @Description: Product Category Mapper
 * @Author: BMad
 * @Date:   2025-11-20
 * @Version: V1.0
 */
public interface ProductCategoryMapper extends BaseMapper<ProductCategory> {

    /**
     * Get category tree structure
     * @return List of categories in tree structure
     */
    List<ProductCategory> selectTree();

    /**
     * Get categories by parent ID
     * @param parentId Parent category ID
     * @return List of child categories
     */
    List<ProductCategory> selectByParentId(@Param("parentId") String parentId);

    /**
     * Get categories by status
     * @param status Status (0: Inactive, 1: Active)
     * @return List of categories
     */
    List<ProductCategory> selectByStatus(@Param("status") Integer status);
}