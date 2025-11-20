package org.jeecg.modules.warehouse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.warehouse.entity.InventoryAdjustment;
import org.jeecg.modules.warehouse.mapper.InventoryAdjustmentMapper;
import org.jeecg.modules.warehouse.service.IInventoryAdjustmentService;
import org.springframework.stereotype.Service;

/**
 * 库存调整服务实现类
 */
@Service
@Slf4j
public class InventoryAdjustmentServiceImpl extends ServiceImpl<InventoryAdjustmentMapper, InventoryAdjustment> implements IInventoryAdjustmentService {

}