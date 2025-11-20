package org.jeecg.modules.warehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.warehouse.entity.WarehouseSlot;

import java.util.List;

/**
 * @Description: vị trí trong kho
 * @Author: BMad
 * @Date:   2025-11-17
 * @Version: V1.0
 */
public interface IWarehouseSlotService extends IService<WarehouseSlot> {

    /**
     * Lấy danh sách vị trí theo kệ
     * @param shelfId ID kệ
     * @return Danh sách vị trí
     */
    List<WarehouseSlot> getByShelfId(String shelfId);

    /**
     * Lấy danh sách vị trí theo trạng thái
     * @param status Trạng thái (0: Trống, 1: Đã đặt hàng, 2: Đã có hàng)
     * @return Danh sách vị trí
     */
    List<WarehouseSlot> getByStatus(Integer status);

    /**
     * Lấy danh sách vị trí với thông tin kệ và khu vực
     * @return Danh sách vị trí kèm thông tin kệ và khu vực
     */
    List<WarehouseSlot> getWithShelfAndAreaInfo();

    /**
     * Lấy danh sách vị trí trống
     * @return Danh sách vị trí trống
     */
    List<WarehouseSlot> getEmptySlots();

    /**
     * Gán sản phẩm vào vị trí
     * @param id ID vị trí
     * @param productCode Mã sản phẩm
     * @return Kết quả gán sản phẩm
     */
    boolean assignProduct(String id, String productCode);

    /**
     * Xóa sản phẩm khỏi vị trí
     * @param id ID vị trí
     * @return Kết quả xóa sản phẩm
     */
    boolean removeProduct(String id);

    /**
     * Di chuyển sản phẩm giữa các vị trí
     * @param fromId ID vị trí nguồn
     * @param toId ID vị trí đích
     * @return Kết quả di chuyển sản phẩm
     */
    boolean moveProduct(String fromId, String toId);

    /**
     * Tìm kiếm vị trí theo mã sản phẩm
     * @param productCode Mã sản phẩm
     * @return Danh sách vị trí chứa sản phẩm
     */
    List<WarehouseSlot> findByProductCode(String productCode);
}