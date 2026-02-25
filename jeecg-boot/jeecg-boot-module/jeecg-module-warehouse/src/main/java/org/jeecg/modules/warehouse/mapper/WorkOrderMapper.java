package org.jeecg.modules.warehouse.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.warehouse.entity.WorkOrder;

/**
 * @Description: Work Order Mapper
 * @Author: BMad
 * @Date: 2026-02-25
 */
public interface WorkOrderMapper extends BaseMapper<WorkOrder> {

    List<WorkOrder> selectByStatus(@Param("status") String status);

    List<WorkOrder> selectByProductionLineId(@Param("productionLineId") String productionLineId);

    List<WorkOrder> selectByBomId(@Param("bomId") String bomId);
}
