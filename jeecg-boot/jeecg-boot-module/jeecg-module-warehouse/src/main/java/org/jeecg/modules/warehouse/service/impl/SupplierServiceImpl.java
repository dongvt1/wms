package org.jeecg.modules.warehouse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.warehouse.entity.Supplier;
import org.jeecg.modules.warehouse.mapper.SupplierMapper;
import org.jeecg.modules.warehouse.service.ISupplierService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description: Supplier Service Implementation
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
@Slf4j
@Service
public class SupplierServiceImpl extends ServiceImpl<SupplierMapper, Supplier> implements ISupplierService {

    @Override
    public Supplier getByCode(String supplierCode) {
        return baseMapper.getByCode(supplierCode);
    }

    @Override
    public boolean isCodeUnique(String supplierCode, String id) {
        int count = baseMapper.countByCodeAndId(supplierCode, id);
        return count == 0;
    }

    @Override
    public List<Supplier> getActiveSuppliers() {
        return baseMapper.getActiveSuppliers();
    }
}