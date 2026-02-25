package org.jeecg.modules.warehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.warehouse.entity.Bom;
import org.jeecg.modules.warehouse.entity.BomItem;

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
}
