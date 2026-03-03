package com.cy.modules.common.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cy.modules.common.entity.ProductionLine;

/**
 * @Description: Production Line Mapper
 * @Author: BMad
 * @Date: 2026-02-25
 */
public interface ProductionLineMapper extends BaseMapper<ProductionLine> {

    List<ProductionLine> selectByStatus(@Param("status") String status);
}
