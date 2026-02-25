package org.jeecg.modules.warehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.warehouse.entity.Supplier;

import java.util.List;

/**
 * @Description: Supplier Service Interface
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
public interface SupplierService extends IService<Supplier> {

    /**
     * Get supplier by code
     * @param supplierCode Supplier code
     * @return Supplier
     */
    Supplier getByCode(String supplierCode);

    /**
     * Check if supplier code is unique
     * @param supplierCode Supplier code
     * @param id Supplier ID to exclude
     * @return True if unique, false otherwise
     */
    boolean isCodeUnique(String supplierCode, String id);

    /**
     * Get active suppliers
     * @return List of active suppliers
     */
    List<Supplier> getActiveSuppliers();
}