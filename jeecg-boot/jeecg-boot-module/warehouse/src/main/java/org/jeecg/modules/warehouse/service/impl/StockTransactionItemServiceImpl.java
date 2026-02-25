package org.jeecg.modules.warehouse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.warehouse.entity.StockTransactionItem;
import org.jeecg.modules.warehouse.mapper.StockTransactionItemMapper;
import org.jeecg.modules.warehouse.service.StockTransactionItemService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description: StockTransactionItem Service Implementation
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
@Slf4j
@Service
public class StockTransactionItemServiceImpl extends ServiceImpl<StockTransactionItemMapper, StockTransactionItem> implements StockTransactionItemService {

    @Override
    public List<StockTransactionItem> getByTransactionId(String transactionId) {
        return baseMapper.getByTransactionId(transactionId);
    }

    @Override
    public List<StockTransactionItem> getByProductId(String productId) {
        return baseMapper.getByProductId(productId);
    }

    @Override
    public List<StockTransactionItem> getItemsWithProductInfo() {
        return baseMapper.getItemsWithProductInfo();
    }
}