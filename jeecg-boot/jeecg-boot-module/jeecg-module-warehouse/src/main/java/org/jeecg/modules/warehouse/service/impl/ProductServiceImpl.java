package org.jeecg.modules.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.warehouse.entity.Product;
import org.jeecg.modules.warehouse.mapper.ProductMapper;
import org.jeecg.modules.warehouse.service.ProductService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * @Description: Product Service Implementation
 * @Author: BMad
 * @Date:   2025-11-20
 * @Version: V1.0
 */
@Slf4j
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Value("${jeecg.path.upload}")
    private String uploadPath;

    @Override
    public List<Product> getByCategoryId(String categoryId) {
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category_id", categoryId);
        queryWrapper.eq("status", 1); // Only active products
        queryWrapper.orderByAsc("create_time");
        return list(queryWrapper);
    }

    @Override
    public List<Product> getByStatus(Integer status) {
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", status);
        queryWrapper.orderByAsc("create_time");
        return list(queryWrapper);
    }

    @Override
    public List<Product> searchProducts(String keyword) {
        return baseMapper.searchProducts(keyword);
    }

    @Override
    public List<Product> getLowStockProducts() {
        return baseMapper.selectLowStockProducts();
    }

    @Override
    public boolean updateCurrentStock(String id, Integer currentStock) {
        try {
            return baseMapper.updateCurrentStock(id, currentStock) > 0;
        } catch (Exception e) {
            log.error("Failed to update current stock", e);
            return false;
        }
    }

    @Override
    public String uploadImage(MultipartFile file) {
        try {
            // Create upload directory if it doesn't exist
            Path uploadDir = Paths.get(uploadPath, "product-images");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;

            // Save file
            Path filePath = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            // Return relative path
            return "/product-images/" + filename;
        } catch (IOException e) {
            log.error("Failed to upload product image", e);
            return null;
        }
    }

    @Override
    public boolean isCodeUnique(String code, String excludeId) {
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("code", code);
        if (excludeId != null && !excludeId.isEmpty()) {
            queryWrapper.ne("id", excludeId);
        }
        Long count = count(queryWrapper);
        return count == 0;
    }
}