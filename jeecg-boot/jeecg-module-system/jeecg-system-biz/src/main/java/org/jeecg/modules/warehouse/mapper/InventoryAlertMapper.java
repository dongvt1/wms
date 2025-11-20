package org.jeecg.modules.warehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.jeecg.modules.warehouse.entity.InventoryAlert;

import java.util.List;

/**
 * 库存预警表 mapper 接口
 */
@Mapper
public interface InventoryAlertMapper extends BaseMapper<InventoryAlert> {

    /**
     * 批量解决预警
     * @param alertIds 预警ID列表
     * @param resolvedBy 解决人
     * @return 影响行数
     */
    @Update("<script>" +
            "UPDATE inventory_alerts " +
            "SET alert_status = 'RESOLVED', resolved_at = NOW(), resolved_by = #{resolvedBy} " +
            "WHERE id IN " +
            "<foreach collection='alertIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int resolveAlertsBatch(@Param("alertIds") List<String> alertIds, @Param("resolvedBy") String resolvedBy);

    /**
     * 批量忽略预警
     * @param alertIds 预警ID列表
     * @param resolvedBy 解决人
     * @return 影响行数
     */
    @Update("<script>" +
            "UPDATE inventory_alerts " +
            "SET alert_status = 'DISMISSED', resolved_at = NOW(), resolved_by = #{resolvedBy} " +
            "WHERE id IN " +
            "<foreach collection='alertIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int dismissAlertsBatch(@Param("alertIds") List<String> alertIds, @Param("resolvedBy") String resolvedBy);
}