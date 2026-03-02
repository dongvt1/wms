package com.cy.modules.warehouse.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cy.modules.common.entity.Product;

/**
 * @Description: Mapper sản phẩm
 * @Author: BMad
 * @Date:   2025-11-20
 * @Version: V1.0
 */
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * Lấy danh sách sản phẩm theo ID danh mục
     * @param categoryId ID danh mục
     * @return Danh sách sản phẩm
     */
    List<Product> selectByCategoryId(@Param("categoryId") String categoryId);

    /**
     * Lấy danh sách sản phẩm theo trạng thái
     * @param status Trạng thái (0: Không hoạt động, 1: Hoạt động)
     * @return Danh sách sản phẩm
     */
    List<Product> selectByStatus(@Param("status") Integer status);

    /**
     * Tìm kiếm sản phẩm theo mã hoặc tên
     * @param keyword Từ khóa tìm kiếm
     * @return Danh sách sản phẩm
     */
    List<Product> searchProducts(@Param("keyword") String keyword);

    /**
     * Lấy danh sách sản phẩm sắp hết hàng
     * @return Danh sách sản phẩm có tồn kho dưới mức tối thiểu
     */
    List<Product> selectLowStockProducts();

    /**
     * Cập nhật tồn kho hiện tại
     * @param id ID sản phẩm
     * @param currentStock Số lượng tồn kho hiện tại
     * @return Số dòng bị ảnh hưởng
     */
    int updateCurrentStock(@Param("id") String id, @Param("currentStock") Integer currentStock);
}