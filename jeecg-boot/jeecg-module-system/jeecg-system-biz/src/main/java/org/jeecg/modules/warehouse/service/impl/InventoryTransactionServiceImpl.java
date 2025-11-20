package org.jeecg.modules.warehouse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.warehouse.entity.InventoryTransaction;
import org.jeecg.modules.warehouse.mapper.InventoryTransactionMapper;
import org.jeecg.modules.warehouse.service.IInventoryTransactionService;
import org.springframework.stereotype.Service;

/**
 * 库存交易服务实现类
 */
@Service
@Slf4j
public class InventoryTransactionServiceImpl extends ServiceImpl<InventoryTransactionMapper, InventoryTransaction> implements IInventoryTransactionService {

}