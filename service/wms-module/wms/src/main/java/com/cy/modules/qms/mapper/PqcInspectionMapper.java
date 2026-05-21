package com.cy.modules.qms.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cy.modules.qms.entity.PqcInspection;

/**
 * @Description: PQC Inspection Mapper
 * @Author: BMad
 * @Date: 2026-02-25
 */
public interface PqcInspectionMapper extends BaseMapper<PqcInspection> {

    List<PqcInspection> selectByWorkOrderId(@Param("workOrderId") String workOrderId);

    List<PqcInspection> selectByStatus(@Param("status") String status);
}
