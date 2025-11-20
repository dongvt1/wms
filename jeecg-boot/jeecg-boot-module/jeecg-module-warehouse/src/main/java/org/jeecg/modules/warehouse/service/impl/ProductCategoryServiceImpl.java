package org.jeecg.modules.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.warehouse.entity.ProductCategory;
import org.jeecg.modules.warehouse.mapper.ProductCategoryMapper;
import org.jeecg.modules.warehouse.service.IProductCategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description: Product Category Service Implementation
 * @Author: BMad
 * @Date:   2025-11-20
 * @Version: V1.0
 */
@Slf4j
@Service
public class ProductCategoryServiceImpl extends ServiceImpl<ProductCategoryMapper, ProductCategory> implements IProductCategoryService {

    @Override
    public List<ProductCategory> getTree() {
        return baseMapper.selectTree();
    }

    @Override
    public List<ProductCategory> getByParentId(String parentId) {
        QueryWrapper<ProductCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", parentId);
        queryWrapper.eq("status", 1); // Only active categories
        queryWrapper.orderByAsc("create_time");
        return list(queryWrapper);
    }

    @Override
    public List<ProductCategory> getByStatus(Integer status) {
        QueryWrapper<ProductCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", status);
        queryWrapper.orderByAsc("create_time");
        return list(queryWrapper);
    }

    @Override
    public List<ProductCategory> buildTree(List<ProductCategory> categories) {
        // Simplified implementation - in a real scenario, you might need to extend the entity
        // or use a DTO with children field for tree structure
        return baseMapper.selectTree();
    }
}