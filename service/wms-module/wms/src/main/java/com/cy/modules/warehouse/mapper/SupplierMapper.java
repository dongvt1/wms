package com.cy.modules.warehouse.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cy.modules.warehouse.entity.Supplier;

/**
 * @Description: Mapper nhà cung cấp
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
public interface SupplierMapper extends BaseMapper<Supplier> {

    /**
     * Lấy nhà cung cấp theo mã
     * @param supplierCode Mã nhà cung cấp
     * @return Thông tin nhà cung cấp
     */
    @Select("SELECT * FROM suppliers WHERE supplier_code = #{supplierCode}")
    Supplier getByCode(@Param("supplierCode") String supplierCode);

    /**
     * Kiểm tra mã nhà cung cấp có duy nhất không
     * @param supplierCode Mã nhà cung cấp
     * @param id ID nhà cung cấp cần loại trừ
     * @return Số nhà cung cấp trùng mã
     */
    @Select("SELECT COUNT(*) FROM suppliers WHERE supplier_code = #{supplierCode} AND id != #{id}")
    int countByCodeAndId(@Param("supplierCode") String supplierCode, @Param("id") String id);

    /**
     * Lấy danh sách nhà cung cấp đang hoạt động
     * @return Danh sách nhà cung cấp hoạt động
     */
    @Select("SELECT * FROM suppliers WHERE status = 1 ORDER BY supplier_name")
    java.util.List<Supplier> getActiveSuppliers();
}