package org.jeecg.modules.qms.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.qms.entity.IqcInspectionResult;

/**
 * @Description: IQC Inspection Result Mapper
 * @Author: BMad
 * @Date: 2026-02-25
 */
public interface IqcInspectionResultMapper extends BaseMapper<IqcInspectionResult> {

    List<IqcInspectionResult> selectByInspectionId(@Param("inspectionId") String inspectionId);
}
