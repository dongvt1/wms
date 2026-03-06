package com.cy.modules.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cy.modules.common.entity.Bom;
import com.cy.modules.common.entity.BomItem;
import com.cy.modules.common.entity.BomItemSubstitute;

import java.util.List;
import java.util.Map;

/**
 * @Description: BOM Service – Common Module (dùng chung cho warehouse, planning, qms)
 * @Author: BMad
 * @Date: 2026-03-02
 * @Version: V1.0
 */
public interface CommonBomService extends IService<Bom> {

    /** Lấy BOM theo thành phẩm */
    List<Bom> getByProductId(String productId);

    /** Lấy BOM theo trạng thái */
    List<Bom> getByStatus(String status);

    /** Kiểm tra mã BOM duy nhất */
    boolean isCodeUnique(String bomCode, String excludeId);

    /** Lưu BOM kèm danh sách NVL */
    boolean saveBomWithItems(Bom bom, List<BomItem> items);

    /** Cập nhật BOM kèm danh sách NVL (xóa cũ, thêm mới) */
    boolean updateBomWithItems(Bom bom, List<BomItem> items);

    /** Lấy danh sách NVL theo BOM */
    List<BomItem> getBomItems(String bomId);

    /** Lấy chi tiết BOM kèm NVL */
    Map<String, Object> getBomDetail(String bomId);

    /** Cấu trúc cây BOM nhiều cấp */
    Map<String, Object> getBomTree(String bomId);

    /** Phẳng hoá BOM – tổng NVL gốc cần (bao gồm hao hụt) */
    List<Map<String, Object>> getFlattenedMaterials(String bomId, java.math.BigDecimal quantity);

    /** Where-used: tìm tất cả BOM chứa NVL này */
    List<Map<String, Object>> whereUsed(String materialId);

    /** Đặt BOM làm mặc định cho sản phẩm */
    boolean setDefaultBom(String bomId, String productId);

    /** Lấy tất cả BOM đang active */
    List<Bom> listActive();

    /** Lấy danh sách linh kiện thay thế của 1 BOM Item */
    List<BomItemSubstitute> getSubstitutes(String bomItemId);
}

