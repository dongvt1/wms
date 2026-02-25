package org.jeecg.modules.planning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.planning.entity.Bom;
import org.jeecg.modules.planning.entity.BomItem;
import org.jeecg.modules.planning.entity.BomItemRefDes;
import org.jeecg.modules.planning.entity.BomRevision;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @Description: BOM Service
 * @Author: BMad
 * @Date: 2026-02-25
 */
public interface BomService extends IService<Bom> {

    List<Bom> getByProductId(String productId);

    List<Bom> getByStatus(String status);

    boolean isCodeUnique(String bomCode, String excludeId);

    /**
     * Save BOM with items
     */
    boolean saveBomWithItems(Bom bom, List<BomItem> items);

    /**
     * Update BOM with items (replaces existing items)
     */
    boolean updateBomWithItems(Bom bom, List<BomItem> items);

    /**
     * Get BOM items by BOM ID
     */
    List<BomItem> getBomItems(String bomId);

    /**
     * Get full BOM detail with items
     */
    Map<String, Object> getBomDetail(String bomId);

    /**
     * Get BOM tree – cấu trúc cây đệ quy nhiều cấp
     */
    Map<String, Object> getBomTree(String bomId);

    /**
     * Flatten BOM – tính tổng NVL gốc cần cho số lượng sản phẩm
     */
    List<Map<String, Object>> getFlattenedMaterials(String bomId, java.math.BigDecimal quantity);

    // ==== New: Electronics BOM features ====

    /**
     * Where-used: tìm tất cả BOM chứa linh kiện này
     */
    List<Map<String, Object>> whereUsed(String materialId);

    /**
     * So sánh 2 phiên bản BOM (revision)
     */
    Map<String, Object> compareBomRevisions(String revisionId1, String revisionId2);

    /**
     * Tạo bản snapshot phiên bản BOM hiện tại
     */
    BomRevision createRevisionSnapshot(String bomId, String revisionCode, String reason);

    /**
     * Lấy lịch sử revision của BOM
     */
    List<BomRevision> getRevisionHistory(String bomId);

    /**
     * Import BOM từ file CSV (xuất từ Altium/KiCad)
     */
    Map<String, Object> importFromCsv(String bomId, InputStream csvStream);

    /**
     * Lấy danh sách RefDes của BOM Item
     */
    List<BomItemRefDes> getRefDesignators(String bomItemId);

    /**
     * Lưu danh sách RefDes cho BOM Item
     */
    boolean saveRefDesignators(String bomItemId, List<BomItemRefDes> refDesList);
}
