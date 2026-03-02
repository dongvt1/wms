package com.cy.modules.planning.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cy.modules.common.entity.BomItemRefDes;

/**
 * @Description: BOM Item RefDes Mapper – planning module (delegate to common entity)
 * @Author: BMad
 */
public interface BomItemRefDesMapper extends BaseMapper<BomItemRefDes> {

    List<BomItemRefDes> selectByBomItemId(@Param("bomItemId") String bomItemId);

    int deleteByBomItemId(@Param("bomItemId") String bomItemId);
}
