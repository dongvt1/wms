package com.cy.modules.common.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cy.modules.common.entity.ProductionLog;

/**
 * @Description: Production Log Mapper
 * @Author: BMad
 * @Date: 2026-02-25
 */
public interface ProductionLogMapper extends BaseMapper<ProductionLog> {

    List<ProductionLog> selectByWorkOrderId(@Param("workOrderId") String workOrderId);
}
