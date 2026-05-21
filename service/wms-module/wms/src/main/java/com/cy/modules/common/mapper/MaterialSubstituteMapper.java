package com.cy.modules.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cy.modules.common.entity.MaterialSubstitute;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description: MaterialSubstitute Mapper – Common Module
 * @Author: BMad
 * @Date: 2026-03-05
 */
public interface MaterialSubstituteMapper extends BaseMapper<MaterialSubstitute> {

    /** Lấy danh sách linh kiện thay thế theo vật tư (kèm tên/mã) */
    List<MaterialSubstitute> selectByMaterialId(@Param("materialId") String materialId);

    /** Xóa tất cả linh kiện thay thế theo vật tư */
    int deleteByMaterialId(@Param("materialId") String materialId);
}
