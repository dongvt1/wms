package org.jeecg.modules.planning.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.planning.entity.ProductionLine;

/**
 * @Description: Production Line Mapper
 * @Author: BMad
 * @Date: 2026-02-25
 */
public interface ProductionLineMapper extends BaseMapper<ProductionLine> {

    List<ProductionLine> selectByStatus(@Param("status") String status);
}
