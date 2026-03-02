package com.cy.modules.planning.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cy.modules.common.entity.BomItem;

/**
 * @Description: BOM Item Mapper – planning module (delegate to common entity)
 * @Author: BMad
 */
public interface BomItemMapper extends BaseMapper<BomItem> {

    List<BomItem> selectByBomId(@Param("bomId") String bomId);

    List<Map<String, Object>> selectBomItemsWithChildren(@Param("bomId") String bomId);

    int deleteByBomId(@Param("bomId") String bomId);
}
