package org.jeecg.modules.warehouse.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.warehouse.entity.PqcInspectionResult;

/**
 * @Description: PQC Inspection Result Mapper
 * @Author: BMad
 * @Date: 2026-02-25
 */
public interface PqcInspectionResultMapper extends BaseMapper<PqcInspectionResult> {

    List<PqcInspectionResult> selectByInspectionId(@Param("inspectionId") String inspectionId);
}
