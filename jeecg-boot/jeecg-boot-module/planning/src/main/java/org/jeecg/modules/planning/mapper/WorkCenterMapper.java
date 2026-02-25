package org.jeecg.modules.planning.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.planning.entity.WorkCenter;

/**
 * @Description: Work Center Mapper
 * @Author: BMad
 * @Date: 2026-02-25
 */
public interface WorkCenterMapper extends BaseMapper<WorkCenter> {

    List<WorkCenter> selectByStatus(@Param("status") String status);

    List<WorkCenter> selectByProductionLineId(@Param("productionLineId") String productionLineId);
}
