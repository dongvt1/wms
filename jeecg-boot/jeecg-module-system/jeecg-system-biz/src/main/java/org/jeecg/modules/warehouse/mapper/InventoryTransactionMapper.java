package org.jeecg.modules.warehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.warehouse.entity.InventoryTransaction;

/**
 * 库存交易表 mapper 接口
 */
@Mapper
public interface InventoryTransactionMapper extends BaseMapper<InventoryTransaction> {

}