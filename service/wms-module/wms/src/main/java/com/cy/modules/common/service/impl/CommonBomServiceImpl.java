package com.cy.modules.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cy.modules.common.entity.Bom;
import com.cy.modules.common.entity.BomItem;
import com.cy.modules.common.entity.BomItemSubstitute;
import com.cy.modules.common.mapper.BomItemMapper;
import com.cy.modules.common.mapper.BomItemSubstituteMapper;
import com.cy.modules.common.mapper.BomMapper;
import com.cy.modules.common.service.CommonBomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @Description: BOM Service Implementation – Common Module
 * @Author: BMad
 * @Date: 2026-03-02
 * @Version: V1.0
 */
@Service
public class CommonBomServiceImpl extends ServiceImpl<BomMapper, Bom> implements CommonBomService {

    @Autowired
    private BomItemMapper bomItemMapper;

    @Autowired
    private BomItemSubstituteMapper bomItemSubstituteMapper;

    @Override
    public List<Bom> getByProductId(String productId) {
        return baseMapper.selectByProductId(productId);
    }

    @Override
    public List<Bom> getByStatus(String status) {
        return baseMapper.selectByStatus(status);
    }

    @Override
    public boolean isCodeUnique(String bomCode, String excludeId) {
        QueryWrapper<Bom> qw = new QueryWrapper<>();
        qw.eq("bom_code", bomCode);
        if (excludeId != null) qw.ne("id", excludeId);
        return count(qw) == 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBomWithItems(Bom bom, List<BomItem> items) {
        this.save(bom);
        if (items != null && !items.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            for (BomItem item : items) {
                item.setBomId(bom.getId());
                bomItemMapper.insert(item);
                // Save substitutes for this item
                saveItemSubstitutes(item.getId(), item.getSubstitutes(), now);
            }
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateBomWithItems(Bom bom, List<BomItem> items) {
        this.updateById(bom);
        // Delete old substitutes first (before deleting items)
        bomItemSubstituteMapper.deleteByBomId(bom.getId());
        bomItemMapper.deleteByBomId(bom.getId());
        if (items != null && !items.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            for (BomItem item : items) {
                item.setId(null);          // IMPORTANT: reset ID sẽ MyBatis-Plus generate mới
                item.setBomId(bom.getId());
                bomItemMapper.insert(item); // sau câu này item.getId() đã có ID mới
                saveItemSubstitutes(item.getId(), item.getSubstitutes(), now);
            }
        }
        return true;
    }

    @Override
    public List<BomItem> getBomItems(String bomId) {
        List<BomItem> items = bomItemMapper.selectByBomId(bomId);
        for (BomItem item : items) {
            item.setSubstitutes(bomItemSubstituteMapper.selectByBomItemId(item.getId()));
        }
        return items;
    }

    @Override
    public Map<String, Object> getBomDetail(String bomId) {
        Map<String, Object> result = new HashMap<>();
        result.put("bom", this.getById(bomId));
        List<BomItem> items = bomItemMapper.selectByBomId(bomId);
        for (BomItem item : items) {
            item.setSubstitutes(bomItemSubstituteMapper.selectByBomItemId(item.getId()));
        }
        result.put("items", items);
        return result;
    }

    /** Helper: lấy substitutes 1 BomItem */
    public List<BomItemSubstitute> getSubstitutes(String bomItemId) {
        return bomItemSubstituteMapper.selectByBomItemId(bomItemId);
    }

    /** Helper: lưu substitutes cho 1 BomItem */
    private void saveItemSubstitutes(String bomItemId, List<BomItemSubstitute> substitutes, LocalDateTime now) {
        if (substitutes == null || substitutes.isEmpty()) return;
        for (BomItemSubstitute sub : substitutes) {
            sub.setId(null);
            sub.setBomItemId(bomItemId);
            sub.setCreateTime(now);
            sub.setUpdateTime(now);
            bomItemSubstituteMapper.insert(sub);
        }
    }

    @Override
    public Map<String, Object> getBomTree(String bomId) {
        return buildBomTreeNode(bomId, new HashSet<>());
    }

    private Map<String, Object> buildBomTreeNode(String bomId, Set<String> visited) {
        if (bomId == null || visited.contains(bomId)) return null;
        visited.add(bomId);
        Bom bom = this.getById(bomId);
        if (bom == null) return null;
        Map<String, Object> node = new HashMap<>();
        node.put("bom", bom);
        List<Map<String, Object>> rawItems = bomItemMapper.selectBomItemsWithChildren(bomId);
        List<Map<String, Object>> children = new ArrayList<>();
        for (Map<String, Object> item : rawItems) {
            Map<String, Object> childNode = new HashMap<>(item);
            Object childBomId = item.get("child_bom_id");
            if (childBomId != null && !childBomId.toString().isEmpty()) {
                childNode.put("childBomTree", buildBomTreeNode(childBomId.toString(), visited));
            }
            children.add(childNode);
        }
        node.put("items", children);
        return node;
    }

    @Override
    public List<Map<String, Object>> getFlattenedMaterials(String bomId, BigDecimal quantity) {
        Map<String, Map<String, Object>> materialMap = new HashMap<>();
        flattenBomRecursive(bomId, quantity, materialMap, new HashSet<>());
        return new ArrayList<>(materialMap.values());
    }

    private void flattenBomRecursive(String bomId, BigDecimal multiplier,
            Map<String, Map<String, Object>> materialMap, Set<String> visited) {
        if (bomId == null || visited.contains(bomId)) return;
        visited.add(bomId);
        Bom bom = this.getById(bomId);
        if (bom == null) return;
        BigDecimal outputQty = bom.getOutputQuantity() != null ? bom.getOutputQuantity() : BigDecimal.ONE;
        BigDecimal ratio = multiplier.divide(outputQty, 6, RoundingMode.HALF_UP);
        for (BomItem item : bomItemMapper.selectByBomId(bomId)) {
            BigDecimal qty = item.getQuantity().multiply(ratio);
            BigDecimal netQty = qty;
            if (item.getWastageRate() != null && item.getWastageRate().compareTo(BigDecimal.ZERO) > 0) {
                qty = qty.multiply(BigDecimal.ONE.add(
                    item.getWastageRate().divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP)));
            }
            if (item.getChildBomId() != null && !item.getChildBomId().isEmpty()) {
                flattenBomRecursive(item.getChildBomId(), qty, materialMap, visited);
            } else {
                String matId = item.getMaterialId();
                if (materialMap.containsKey(matId)) {
                    Map<String, Object> e = materialMap.get(matId);
                    e.put("totalQuantity", ((BigDecimal) e.get("totalQuantity")).add(qty));
                    e.put("netQuantity", ((BigDecimal) e.get("netQuantity")).add(netQty));
                } else {
                    Map<String, Object> mat = new HashMap<>();
                    mat.put("materialId", matId);
                    mat.put("unit", item.getUnit());
                    mat.put("netQuantity", netQty.setScale(4, RoundingMode.HALF_UP));
                    mat.put("totalQuantity", qty.setScale(4, RoundingMode.HALF_UP));
                    mat.put("wastageRate", item.getWastageRate());
                    mat.put("purchaseLeadTimeDays", item.getPurchaseLeadTimeDays());
                    materialMap.put(matId, mat);
                }
            }
        }
    }

    @Override
    public List<Map<String, Object>> whereUsed(String materialId) {
        QueryWrapper<BomItem> qw = new QueryWrapper<>();
        qw.eq("material_id", materialId);
        List<BomItem> bomItems = bomItemMapper.selectList(qw);
        List<Map<String, Object>> result = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        for (BomItem bomItem : bomItems) {
            if (!visited.contains(bomItem.getBomId())) {
                visited.add(bomItem.getBomId());
                Bom bom = this.getById(bomItem.getBomId());
                if (bom != null) {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("bom", bom);
                    entry.put("quantity", bomItem.getQuantity());
                    entry.put("refDesignators", bomItem.getRefDesignators());
                    result.add(entry);
                }
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setDefaultBom(String bomId, String productId) {
        // Bỏ mặc định tất cả BOM hiện tại của sản phẩm
        List<Bom> bomList = getByProductId(productId);
        for (Bom bom : bomList) {
            bom.setIsDefault(false);
            updateById(bom);
        }
        // Đặt BOM được chọn làm mặc định
        Bom target = getById(bomId);
        if (target == null) return false;
        target.setIsDefault(true);
        return updateById(target);
    }

    @Override
    public List<Bom> listActive() {
        return getByStatus("active");
    }
}
