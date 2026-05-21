package com.cy.modules.qms.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cy.modules.qms.entity.FqcInspection;

/**
 * @Description: FQC Inspection Mapper
 * @Author: BMad
 * @Date: 2026-02-25
 */
public interface FqcInspectionMapper extends BaseMapper<FqcInspection> {

    List<FqcInspection> selectByProductId(@Param("productId") String productId);

    List<FqcInspection> selectByStatus(@Param("status") String status);

    List<FqcInspection> selectByOutboundOrderId(@Param("outboundOrderId") String outboundOrderId);
}
