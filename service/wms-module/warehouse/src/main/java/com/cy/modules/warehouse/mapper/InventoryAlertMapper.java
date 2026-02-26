package com.cy.modules.warehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cy.modules.warehouse.entity.InventoryAlert;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description: Mapper cảnh báo tồn kho
 * @Author: jeecg
 * @Date: 2025-11-20
 * @Version: V1.0
 */
public interface InventoryAlertMapper extends BaseMapper<InventoryAlert> {

    /**
     * Giải quyết cảnh báo hàng loạt
     * @param alertIds Danh sách ID cảnh báo
     * @param resolvedBy Người giải quyết
     * @return Số hàng bị ảnh hưởng
     */
    int resolveAlertsBatch(@Param("alertIds") List<String> alertIds, @Param("resolvedBy") String resolvedBy);

    /**
     * Bỏ qua cảnh báo hàng loạt
     * @param alertIds Danh sách ID cảnh báo
     * @param resolvedBy Người thao tác
     * @return Số hàng bị ảnh hưởng
     */
    int dismissAlertsBatch(@Param("alertIds") List<String> alertIds, @Param("resolvedBy") String resolvedBy);
}
