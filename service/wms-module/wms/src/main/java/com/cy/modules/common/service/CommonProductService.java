package com.cy.modules.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cy.modules.common.entity.Product;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Description: Product Service – Common Module (dùng chung cho warehouse, planning, qms)
 * @Author: BMad
 * @Date: 2026-03-02
 * @Version: V1.0
 */
public interface CommonProductService extends IService<Product> {

    /** Lấy sản phẩm theo danh mục */
    List<Product> getByCategoryId(String categoryId);

    /** Lấy sản phẩm theo trạng thái */
    List<Product> getByStatus(Integer status);

    /** Tìm kiếm theo mã hoặc tên */
    List<Product> searchProducts(String keyword);

    /** Lấy sản phẩm sắp hết hàng */
    List<Product> getLowStockProducts();

    /** Cập nhật tồn kho */
    boolean updateCurrentStock(String id, Integer currentStock);

    /** Upload ảnh sản phẩm */
    String uploadImage(MultipartFile file);

    /** Kiểm tra mã sản phẩm duy nhất */
    boolean isCodeUnique(String code, String excludeId);

    /** Lấy danh sách theo loại: product / material / semi */
    List<Product> getByType(String type);

    /** Lấy tất cả sản phẩm đang active */
    List<Product> listActive();
}
