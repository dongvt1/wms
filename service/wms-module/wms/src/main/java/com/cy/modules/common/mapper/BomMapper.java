package com.cy.modules.common.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cy.modules.common.entity.Bom;

/**
 * @Description: BOM Mapper – Common Module
 * @Author: BMad
 * @Date: 2026-03-02
 */
public interface BomMapper extends BaseMapper<Bom> {

    List<Bom> selectByProductId(@Param("productId") String productId);

    List<Bom> selectByStatus(@Param("status") String status);
}
