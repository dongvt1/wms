package com.cy.modules.common.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cy.modules.common.entity.ProductionStage;

/**
 * @Description: Production Stage Mapper
 * @Author: BMad
 * @Date: 2026-02-25
 */
public interface ProductionStageMapper extends BaseMapper<ProductionStage> {

    List<ProductionStage> selectByWorkOrderId(@Param("workOrderId") String workOrderId);

    int deleteByWorkOrderId(@Param("workOrderId") String workOrderId);
}
