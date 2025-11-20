package org.jeecg.modules.warehouse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.warehouse.entity.ProductHistory;
import org.jeecg.modules.warehouse.mapper.ProductHistoryMapper;
import org.jeecg.modules.warehouse.service.IProductHistoryService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description: Product History Service Implementation
 * @Author: BMad
 * @Date:   2025-11-20
 * @Version: V1.0
 */
@Slf4j
@Service
public class ProductHistoryServiceImpl extends ServiceImpl<ProductHistoryMapper, ProductHistory> implements IProductHistoryService {

    @Override
    public List<ProductHistory> getByProductId(String productId) {
        return baseMapper.selectByProductId(productId);
    }

    @Override
    public List<ProductHistory> getByUserId(String userId) {
        return baseMapper.selectByUserId(userId);
    }

    @Override
    public List<ProductHistory> getByAction(String action) {
        return baseMapper.selectByAction(action);
    }

    @Override
    public ProductHistory recordHistory(String productId, String action, String oldData, String newData, String userId) {
        try {
            ProductHistory history = new ProductHistory();
            history.setProductId(productId);
            history.setAction(action);
            history.setOldData(oldData);
            history.setNewData(newData);
            history.setUserId(userId);
            
            save(history);
            return history;
        } catch (Exception e) {
            log.error("Failed to record product history", e);
            return null;
        }
    }
}