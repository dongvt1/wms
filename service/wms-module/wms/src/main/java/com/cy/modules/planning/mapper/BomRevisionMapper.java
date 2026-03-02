package com.cy.modules.planning.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cy.modules.common.entity.BomRevision;

/**
 * @Description: BOM Revision Mapper – planning module (delegate to common entity)
 * @Author: BMad
 */
public interface BomRevisionMapper extends BaseMapper<BomRevision> {

    List<BomRevision> selectByBomId(@Param("bomId") String bomId);
}
