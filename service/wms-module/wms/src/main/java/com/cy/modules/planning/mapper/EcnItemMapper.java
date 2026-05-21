package com.cy.modules.planning.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cy.modules.planning.entity.EcnItem;

/**
 * @Description: ECN Item Mapper
 * @Author: BMad
 * @Date: 2026-02-26
 */
public interface EcnItemMapper extends BaseMapper<EcnItem> {

    List<EcnItem> selectByEcnId(@Param("ecnId") String ecnId);
}
