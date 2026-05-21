package com.cy.modules.common.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cy.modules.common.entity.BomItem;

/**
 * @Description: BOM Item Mapper – Common Module
 * @Author: BMad
 * @Date: 2026-03-02
 */
public interface BomItemMapper extends BaseMapper<BomItem> {

    /** Lấy danh sách NVL theo BOM ID */
    List<BomItem> selectByBomId(@Param("bomId") String bomId);

    /** Lấy danh sách NVL kèm thông tin BOM con */
    List<Map<String, Object>> selectBomItemsWithChildren(@Param("bomId") String bomId);

    /** Xóa tất cả NVL theo BOM ID */
    int deleteByBomId(@Param("bomId") String bomId);
}
