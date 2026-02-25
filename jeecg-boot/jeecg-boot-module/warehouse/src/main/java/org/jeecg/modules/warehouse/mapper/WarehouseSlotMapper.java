package org.jeecg.modules.warehouse.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.warehouse.entity.WarehouseSlot;

/**
 * @Description: vị trí trong kho
 * @Author: BMad
 * @Date:   2025-11-17
 * @Version: V1.0
 */
public interface WarehouseSlotMapper extends BaseMapper<WarehouseSlot> {

    /**
     * Lấy danh sách vị trí theo kệ
     * @param shelfId ID kệ
     * @return Danh sách vị trí
     */
    List<WarehouseSlot> selectByShelfId(@Param("shelfId") String shelfId);

    /**
     * Lấy danh sách vị trí theo trạng thái
     * @param status Trạng thái (0: Trống, 1: Đã đặt hàng, 2: Đã có hàng)
     * @return Danh sách vị trí
     */
    List<WarehouseSlot> selectByStatus(@Param("status") Integer status);

    /**
     * Lấy danh sách vị trí với thông tin kệ và khu vực
     * @return Danh sách vị trí kèm thông tin kệ và khu vực
     */
    List<WarehouseSlot> selectWithShelfAndAreaInfo();

    /**
     * Lấy danh sách vị trí trống
     * @return Danh sách vị trí trống
     */
    List<WarehouseSlot> selectEmptySlots();

    /**
     * Gán sản phẩm vào vị trí
     * @param id ID vị trí
     * @param productCode Mã sản phẩm
     * @param status Trạng thái mới
     * @return Số dòng ảnh hưởng
     */
    int assignProduct(@Param("id") String id, @Param("productCode") String productCode, @Param("status") Integer status);

    /**
     * Xóa sản phẩm khỏi vị trí
     * @param id ID vị trí
     * @return Số dòng ảnh hưởng
     */
    int removeProduct(@Param("id") String id);

    /**
     * Di chuyển sản phẩm giữa các vị trí
     * @param fromId ID vị trí nguồn
     * @param toId ID vị trí đích
     * @return Số dòng ảnh hưởng
     */
    int moveProduct(@Param("fromId") String fromId, @Param("toId") String toId);
}