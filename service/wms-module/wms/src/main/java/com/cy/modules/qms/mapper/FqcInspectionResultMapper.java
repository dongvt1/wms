package com.cy.modules.qms.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cy.modules.qms.entity.FqcInspectionResult;

/**
 * @Description: FQC Inspection Result Mapper
 * @Author: BMad
 * @Date: 2026-02-25
 */
public interface FqcInspectionResultMapper extends BaseMapper<FqcInspectionResult> {

    List<FqcInspectionResult> selectByInspectionId(@Param("inspectionId") String inspectionId);
}
