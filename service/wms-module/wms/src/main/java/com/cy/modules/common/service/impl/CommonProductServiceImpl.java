package com.cy.modules.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import com.cy.modules.common.entity.Product;
import com.cy.modules.common.mapper.ProductMapper;
import com.cy.modules.common.service.CommonProductService;
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
 * @Description: Product Service Implementation – Common Module
 * @Author: BMad
 * @Date: 2026-03-02
 * @Version: V1.0
 */
@Slf4j
@Service
public class CommonProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements CommonProductService {

    @Value("${jeecg.path.upload}")
    private String uploadPath;

    @Override
    public List<Product> getByCategoryId(String categoryId) {
        QueryWrapper<Product> qw = new QueryWrapper<>();
        qw.eq("category_id", categoryId);
        qw.eq("status", 1);
        qw.orderByAsc("create_time");
        return list(qw);
    }

    @Override
    public List<Product> getByStatus(Integer status) {
        QueryWrapper<Product> qw = new QueryWrapper<>();
        qw.eq("status", status);
        qw.orderByAsc("code");
        return list(qw);
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
            log.error("Lỗi cập nhật tồn kho sản phẩm id={}", id, e);
            return false;
        }
    }

    @Override
    public String uploadImage(MultipartFile file) {
        try {
            Path uploadDir = Paths.get(uploadPath, "product-images");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID() + extension;
            Path filePath = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), filePath);
            return "/product-images/" + filename;
        } catch (IOException e) {
            log.error("Lỗi upload ảnh sản phẩm", e);
            return null;
        }
    }

    @Override
    public boolean isCodeUnique(String code, String excludeId) {
        QueryWrapper<Product> qw = new QueryWrapper<>();
        qw.eq("code", code);
        if (excludeId != null && !excludeId.isEmpty()) {
            qw.ne("id", excludeId);
        }
        return count(qw) == 0;
    }

    @Override
    public List<Product> getByType(String type) {
        return baseMapper.selectByType(type);
    }

    @Override
    public List<Product> listActive() {
        return getByStatus(1);
    }
}
