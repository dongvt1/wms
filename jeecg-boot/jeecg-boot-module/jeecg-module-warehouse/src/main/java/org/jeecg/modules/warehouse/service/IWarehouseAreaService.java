package org.jeecg.modules.warehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.warehouse.entity.WarehouseArea;

import java.util.List;

/**
 * @Description: khu vực kho
 * @Author: BMad
 * @Date:   2025-11-17
 * @Version: V1.0
 */
public interface IWarehouseAreaService extends IService<WarehouseArea> {

    /**
     * Lấy danh sách khu vực theo trạng thái
     * @param status Trạng thái (0: Không hoạt động, 1: Hoạt động)
     * @return Danh sách khu vực
     */
    List<WarehouseArea> getByStatus(Integer status);

    /**
     * Cập nhật số kệ đã sử dụng
     * @param id ID khu vực
     * @param usedCapacity Số kệ đã sử dụng
     * @return Kết quả cập nhật
     */
    boolean updateUsedCapacity(String id, Integer usedCapacity);

    /**
     * Tăng số kệ đã sử dụng
     * @param id ID khu vực
     * @return Kết quả cập nhật
     */
    boolean increaseUsedCapacity(String id);

    /**
     * Giảm số kệ đã sử dụng
     * @param id ID khu vực
     * @return Kết quả cập nhật
     */
    boolean decreaseUsedCapacity(String id);
}