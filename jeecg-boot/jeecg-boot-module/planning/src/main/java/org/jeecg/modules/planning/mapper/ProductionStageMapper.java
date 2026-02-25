package org.jeecg.modules.planning.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.planning.entity.ProductionStage;

/**
 * @Description: Production Stage Mapper
 * @Author: BMad
 * @Date: 2026-02-25
 */
public interface ProductionStageMapper extends BaseMapper<ProductionStage> {

    List<ProductionStage> selectByWorkOrderId(@Param("workOrderId") String workOrderId);

    int deleteByWorkOrderId(@Param("workOrderId") String workOrderId);
}
