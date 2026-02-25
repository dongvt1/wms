package org.jeecg.modules.planning.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.planning.entity.BomRevision;

/**
 * @Description: BOM Revision Mapper
 * @Author: BMad
 * @Date: 2026-02-26
 */
public interface BomRevisionMapper extends BaseMapper<BomRevision> {

    List<BomRevision> selectByBomId(@Param("bomId") String bomId);
}
