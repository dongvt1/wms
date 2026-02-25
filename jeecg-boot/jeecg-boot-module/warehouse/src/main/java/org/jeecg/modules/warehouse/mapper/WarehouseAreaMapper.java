package org.jeecg.modules.warehouse.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.warehouse.entity.WarehouseArea;

/**
 * @Description: khu vực kho
 * @Author: BMad
 * @Date:   2025-11-17
 * @Version: V1.0
 */
public interface WarehouseAreaMapper extends BaseMapper<WarehouseArea> {

    /**
     * Lấy danh sách khu vực theo trạng thái
     * @param status Trạng thái (0: Không hoạt động, 1: Hoạt động)
     * @return Danh sách khu vực
     */
    List<WarehouseArea> selectByStatus(@Param("status") Integer status);

    /**
     * Cập nhật số kệ đã sử dụng
     * @param id ID khu vực
     * @param usedCapacity Số kệ đã sử dụng
     * @return Số dòng ảnh hưởng
     */
    int updateUsedCapacity(@Param("id") String id, @Param("usedCapacity") Integer usedCapacity);
}