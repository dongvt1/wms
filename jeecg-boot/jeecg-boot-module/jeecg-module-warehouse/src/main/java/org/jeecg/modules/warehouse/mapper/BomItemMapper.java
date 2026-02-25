package org.jeecg.modules.warehouse.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.warehouse.entity.BomItem;

/**
 * @Description: BOM Item Mapper
 * @Author: BMad
 * @Date: 2026-02-25
 */
public interface BomItemMapper extends BaseMapper<BomItem> {

    List<BomItem> selectByBomId(@Param("bomId") String bomId);

    int deleteByBomId(@Param("bomId") String bomId);
}
