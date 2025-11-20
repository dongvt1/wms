package org.jeecg.modules.warehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.warehouse.entity.WarehouseShelf;

import java.util.List;

/**
 * @Description: kệ kho
 * @Author: BMad
 * @Date:   2025-11-17
 * @Version: V1.0
 */
public interface IWarehouseShelfService extends IService<WarehouseShelf> {

    /**
     * Lấy danh sách kệ theo khu vực
     * @param areaId ID khu vực
     * @return Danh sách kệ
     */
    List<WarehouseShelf> getByAreaId(String areaId);

    /**
     * Lấy danh sách kệ theo trạng thái
     * @param status Trạng thái (0: Không hoạt động, 1: Hoạt động)
     * @return Danh sách kệ
     */
    List<WarehouseShelf> getByStatus(Integer status);

    /**
     * Lấy danh sách kệ với thông tin khu vực
     * @return Danh sách kệ kèm thông tin khu vực
     */
    List<WarehouseShelf> getWithAreaInfo();

    /**
     * Cập nhật số vị trí đã sử dụng
     * @param id ID kệ
     * @param usedCapacity Số vị trí đã sử dụng
     * @return Kết quả cập nhật
     */
    boolean updateUsedCapacity(String id, Integer usedCapacity);

    /**
     * Tăng số vị trí đã sử dụng
     * @param id ID kệ
     * @return Kết quả cập nhật
     */
    boolean increaseUsedCapacity(String id);

    /**
     * Giảm số vị trí đã sử dụng
     * @param id ID kệ
     * @return Kết quả cập nhật
     */
    boolean decreaseUsedCapacity(String id);
}