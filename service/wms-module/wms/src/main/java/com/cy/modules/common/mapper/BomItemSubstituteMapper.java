package com.cy.modules.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cy.modules.common.entity.BomItemSubstitute;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description: BomItemSubstitute Mapper – Common Module
 * @Author: BMad
 * @Date: 2026-03-05
 */
public interface BomItemSubstituteMapper extends BaseMapper<BomItemSubstitute> {

    /** Lấy danh sách substitute theo BOM Item ID (kèm tên/mã vật tư) */
    List<BomItemSubstitute> selectByBomItemId(@Param("bomItemId") String bomItemId);

    /** Xóa tất cả substitute theo BOM Item ID */
    int deleteByBomItemId(@Param("bomItemId") String bomItemId);

    /** Xóa tất cả substitute của các BomItem thuộc BOM (dùng khi update BOM) */
    int deleteByBomId(@Param("bomId") String bomId);
}
