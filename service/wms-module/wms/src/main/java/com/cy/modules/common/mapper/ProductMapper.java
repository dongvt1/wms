package com.cy.modules.common.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cy.modules.common.entity.Product;

/**
 * @Description: Product Mapper – Common Module
 * @Author: BMad
 * @Date: 2026-03-02
 */
public interface ProductMapper extends BaseMapper<Product> {

    /** Lấy danh sách sản phẩm theo ID danh mục */
    List<Product> selectByCategoryId(@Param("categoryId") String categoryId);

    /** Lấy danh sách sản phẩm theo trạng thái */
    List<Product> selectByStatus(@Param("status") Integer status);

    /** Tìm kiếm sản phẩm theo mã hoặc tên */
    List<Product> searchProducts(@Param("keyword") String keyword);

    /** Lấy danh sách sản phẩm sắp hết hàng */
    List<Product> selectLowStockProducts();

    /** Cập nhật tồn kho hiện tại */
    int updateCurrentStock(@Param("id") String id, @Param("currentStock") Integer currentStock);

    /** Lấy danh sách theo loại (product / material / semi) */
    List<Product> selectByType(@Param("type") String type);
}
