package com.cy.modules.common.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cy.modules.common.entity.BomRevision;

/**
 * @Description: BOM Revision Mapper – Common Module
 * @Author: BMad
 * @Date: 2026-03-02
 */
public interface BomRevisionMapper extends BaseMapper<BomRevision> {

    List<BomRevision> selectByBomId(@Param("bomId") String bomId);
}
