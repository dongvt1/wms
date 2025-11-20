package org.jeecg.modules.warehouse.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.warehouse.entity.WarehouseShelf;

/**
 * @Description: kệ kho
 * @Author: BMad
 * @Date:   2025-11-17
 * @Version: V1.0
 */
public interface WarehouseShelfMapper extends BaseMapper<WarehouseShelf> {

    /**
     * Lấy danh sách kệ theo khu vực
     * @param areaId ID khu vực
     * @return Danh sách kệ
     */
    List<WarehouseShelf> selectByAreaId(@Param("areaId") String areaId);

    /**
     * Lấy danh sách kệ theo trạng thái
     * @param status Trạng thái (0: Không hoạt động, 1: Hoạt động)
     * @return Danh sách kệ
     */
    List<WarehouseShelf> selectByStatus(@Param("status") Integer status);

    /**
     * Lấy danh sách kệ với thông tin khu vực
     * @return Danh sách kệ kèm thông tin khu vực
     */
    List<WarehouseShelf> selectWithAreaInfo();

    /**
     * Cập nhật số vị trí đã sử dụng
     * @param id ID kệ
     * @param usedCapacity Số vị trí đã sử dụng
     * @return Số dòng ảnh hưởng
     */
    int updateUsedCapacity(@Param("id") String id, @Param("usedCapacity") Integer usedCapacity);
}