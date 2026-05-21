package com.cy.modules.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cy.modules.common.entity.Bom;
import com.cy.modules.common.entity.BomItem;
import com.cy.modules.common.entity.BomItemRefDes;
import com.cy.modules.common.entity.BomRevision;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @Description: BOM Service – planning module (delegate to common entities)
 * @Author: BMad
 * @Date: 2026-02-25
 */
public interface BomService extends IService<Bom> {

    List<Bom> getByProductId(String productId);

    List<Bom> getByStatus(String status);

    boolean isCodeUnique(String bomCode, String excludeId);

    /** Lưu BOM kèm NVL */
    boolean saveBomWithItems(Bom bom, List<BomItem> items);

    /** Cập nhật BOM kèm NVL (xóa cũ, thêm mới) */
    boolean updateBomWithItems(Bom bom, List<BomItem> items);

    /** Lấy NVL theo BOM */
    List<BomItem> getBomItems(String bomId);

    /** Lấy chi tiết BOM kèm NVL */
    Map<String, Object> getBomDetail(String bomId);

    /** Cây BOM nhiều cấp */
    Map<String, Object> getBomTree(String bomId);

    /** Phẳng hoá BOM */
    List<Map<String, Object>> getFlattenedMaterials(String bomId, java.math.BigDecimal quantity);

    /** Where-used */
    List<Map<String, Object>> whereUsed(String materialId);

    /** So sánh 2 revision */
    Map<String, Object> compareBomRevisions(String revisionId1, String revisionId2);

    /** Tạo snapshot revision */
    BomRevision createRevisionSnapshot(String bomId, String revisionCode, String reason);

    /** Lịch sử revision */
    List<BomRevision> getRevisionHistory(String bomId);

    /** Import từ CSV */
    Map<String, Object> importFromCsv(String bomId, InputStream csvStream);

    /** Lấy RefDes */
    List<BomItemRefDes> getRefDesignators(String bomItemId);

    /** Lưu RefDes */
    boolean saveRefDesignators(String bomItemId, List<BomItemRefDes> refDesList);
}
