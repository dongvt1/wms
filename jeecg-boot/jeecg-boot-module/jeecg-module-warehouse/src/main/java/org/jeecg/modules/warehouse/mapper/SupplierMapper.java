package org.jeecg.modules.warehouse.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.warehouse.entity.Supplier;

/**
 * @Description: Supplier Mapper
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
public interface SupplierMapper extends BaseMapper<Supplier> {

    /**
     * Get supplier by code
     * @param supplierCode Supplier code
     * @return Supplier
     */
    @Select("SELECT * FROM suppliers WHERE supplier_code = #{supplierCode}")
    Supplier getByCode(@Param("supplierCode") String supplierCode);

    /**
     * Check if supplier code is unique
     * @param supplierCode Supplier code
     * @param id Supplier ID to exclude
     * @return Count of suppliers with the code
     */
    @Select("SELECT COUNT(*) FROM suppliers WHERE supplier_code = #{supplierCode} AND id != #{id}")
    int countByCodeAndId(@Param("supplierCode") String supplierCode, @Param("id") String id);

    /**
     * Get active suppliers
     * @return List of active suppliers
     */
    @Select("SELECT * FROM suppliers WHERE status = 1 ORDER BY supplier_name")
    java.util.List<Supplier> getActiveSuppliers();
}