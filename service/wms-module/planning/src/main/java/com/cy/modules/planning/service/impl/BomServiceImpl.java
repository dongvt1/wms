package com.cy.modules.planning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cy.modules.planning.entity.Bom;
import com.cy.modules.planning.entity.BomItem;
import com.cy.modules.planning.entity.BomItemRefDes;
import com.cy.modules.planning.entity.BomRevision;
import com.cy.modules.planning.mapper.BomItemMapper;
import com.cy.modules.planning.mapper.BomItemRefDesMapper;
import com.cy.modules.planning.mapper.BomMapper;
import com.cy.modules.planning.mapper.BomRevisionMapper;
import com.cy.modules.planning.service.BomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * @Description: BOM Service Implementation
 * @Author: BMad
 * @Date: 2026-02-25
 */
@Service
public class BomServiceImpl extends ServiceImpl<BomMapper, Bom> implements BomService {

    @Autowired
    private BomItemMapper bomItemMapper;

    @Autowired
    private BomItemRefDesMapper bomItemRefDesMapper;

    @Autowired
    private BomRevisionMapper bomRevisionMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

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
        if (excludeId != null) {
            qw.ne("id", excludeId);
        }
        return count(qw) == 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBomWithItems(Bom bom, List<BomItem> items) {
        this.save(bom);
        if (items != null && !items.isEmpty()) {
            for (BomItem item : items) {
                item.setBomId(bom.getId());
                bomItemMapper.insert(item);
            }
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateBomWithItems(Bom bom, List<BomItem> items) {
        this.updateById(bom);
        // Delete existing items and reinsert
        bomItemMapper.deleteByBomId(bom.getId());
        if (items != null && !items.isEmpty()) {
            for (BomItem item : items) {
                item.setBomId(bom.getId());
                bomItemMapper.insert(item);
            }
        }
        return true;
    }

    @Override
    public List<BomItem> getBomItems(String bomId) {
        return bomItemMapper.selectByBomId(bomId);
    }

    @Override
    public Map<String, Object> getBomDetail(String bomId) {
        Map<String, Object> result = new HashMap<>();
        Bom bom = this.getById(bomId);
        result.put("bom", bom);
        List<BomItem> items = bomItemMapper.selectByBomId(bomId);
        result.put("items", items);
        return result;
    }

    @Override
    public Map<String, Object> getBomTree(String bomId) {
        return buildBomTreeNode(bomId, new HashSet<>());
    }

    private Map<String, Object> buildBomTreeNode(String bomId, Set<String> visited) {
        if (bomId == null || visited.contains(bomId)) {
            return null; // prevent circular reference
        }
        visited.add(bomId);

        Map<String, Object> node = new HashMap<>();
        Bom bom = this.getById(bomId);
        if (bom == null)
            return null;
        node.put("bom", bom);

        List<Map<String, Object>> itemsWithChildren = bomItemMapper.selectBomItemsWithChildren(bomId);
        List<Map<String, Object>> children = new ArrayList<>();
        for (Map<String, Object> item : itemsWithChildren) {
            Map<String, Object> childNode = new HashMap<>(item);
            Object childBomId = item.get("child_bom_id");
            if (childBomId != null && !childBomId.toString().isEmpty()) {
                Map<String, Object> subTree = buildBomTreeNode(childBomId.toString(), visited);
                childNode.put("childBomTree", subTree);
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
            Map<String, Map<String, Object>> materialMap,
            Set<String> visited) {
        if (bomId == null || visited.contains(bomId))
            return;
        visited.add(bomId);

        Bom bom = this.getById(bomId);
        if (bom == null)
            return;
        BigDecimal outputQty = bom.getOutputQuantity() != null ? bom.getOutputQuantity() : BigDecimal.ONE;
        BigDecimal ratio = multiplier.divide(outputQty, 6, RoundingMode.HALF_UP);

        List<BomItem> items = bomItemMapper.selectByBomId(bomId);
        for (BomItem item : items) {
            BigDecimal qty = item.getQuantity().multiply(ratio);
            BigDecimal qtyBeforeWastage = qty;

            // Apply wastage rate if present
            if (item.getWastageRate() != null && item.getWastageRate().compareTo(BigDecimal.ZERO) > 0) {
                qty = qty.multiply(BigDecimal.ONE
                        .add(item.getWastageRate().divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP)));
            }

            if (item.getChildBomId() != null && !item.getChildBomId().isEmpty()) {
                // Sub-assembly: recurse into child BOM
                flattenBomRecursive(item.getChildBomId(), qty, materialMap, visited);
            } else {
                // Raw material: aggregate
                String matId = item.getMaterialId();
                if (materialMap.containsKey(matId)) {
                    Map<String, Object> existing = materialMap.get(matId);
                    BigDecimal existQty = (BigDecimal) existing.get("totalQuantity");
                    existing.put("totalQuantity", existQty.add(qty));
                    BigDecimal existNetQty = (BigDecimal) existing.get("netQuantity");
                    existing.put("netQuantity", existNetQty.add(qtyBeforeWastage));
                } else {
                    Map<String, Object> mat = new HashMap<>();
                    mat.put("materialId", matId);
                    mat.put("unit", item.getUnit());
                    mat.put("netQuantity", qtyBeforeWastage.setScale(4, RoundingMode.HALF_UP));
                    mat.put("totalQuantity", qty.setScale(4, RoundingMode.HALF_UP));
                    mat.put("wastageRate", item.getWastageRate());
                    mat.put("purchaseLeadTimeDays", item.getPurchaseLeadTimeDays());
                    materialMap.put(matId, mat);
                }
            }
        }
    }

    // ==== New: Electronics BOM features ====

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
    public Map<String, Object> compareBomRevisions(String revisionId1, String revisionId2) {
        Map<String, Object> result = new HashMap<>();
        BomRevision rev1 = bomRevisionMapper.selectById(revisionId1);
        BomRevision rev2 = bomRevisionMapper.selectById(revisionId2);

        if (rev1 == null || rev2 == null) {
            result.put("error", "Không tìm thấy một hoặc cả hai phiên bản!");
            return result;
        }

        result.put("revision1", rev1.getRevisionCode());
        result.put("revision2", rev2.getRevisionCode());

        try {
            // Parse snapshot JSON
            List<Map<String, Object>> items1 = parseSnapshotItems(rev1.getSnapshotData());
            List<Map<String, Object>> items2 = parseSnapshotItems(rev2.getSnapshotData());

            // Index by materialId
            Map<String, Map<String, Object>> map1 = indexByField(items1, "materialId");
            Map<String, Map<String, Object>> map2 = indexByField(items2, "materialId");

            List<Map<String, Object>> added = new ArrayList<>();
            List<Map<String, Object>> removed = new ArrayList<>();
            List<Map<String, Object>> modified = new ArrayList<>();

            // Items in rev2 but not in rev1 → added
            for (Map.Entry<String, Map<String, Object>> entry : map2.entrySet()) {
                if (!map1.containsKey(entry.getKey())) {
                    Map<String, Object> change = new HashMap<>(entry.getValue());
                    change.put("changeType", "added");
                    added.add(change);
                }
            }

            // Items in rev1 but not in rev2 → removed
            for (Map.Entry<String, Map<String, Object>> entry : map1.entrySet()) {
                if (!map2.containsKey(entry.getKey())) {
                    Map<String, Object> change = new HashMap<>(entry.getValue());
                    change.put("changeType", "removed");
                    removed.add(change);
                }
            }

            // Items in both → check for quantity changes
            for (Map.Entry<String, Map<String, Object>> entry : map1.entrySet()) {
                if (map2.containsKey(entry.getKey())) {
                    Map<String, Object> item1 = entry.getValue();
                    Map<String, Object> item2 = map2.get(entry.getKey());
                    Object qty1 = item1.get("quantity");
                    Object qty2 = item2.get("quantity");
                    if (qty1 != null && qty2 != null && !qty1.toString().equals(qty2.toString())) {
                        Map<String, Object> change = new HashMap<>();
                        change.put("materialId", entry.getKey());
                        change.put("changeType", "modified");
                        change.put("oldQuantity", qty1);
                        change.put("newQuantity", qty2);
                        modified.add(change);
                    }
                }
            }

            result.put("added", added);
            result.put("removed", removed);
            result.put("modified", modified);
            result.put("totalChanges", added.size() + removed.size() + modified.size());
        } catch (Exception e) {
            result.put("error", "Lỗi khi phân tích snapshot: " + e.getMessage());
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseSnapshotItems(String snapshotData) {
        try {
            Map<String, Object> snapshot = objectMapper.readValue(snapshotData,
                    new TypeReference<Map<String, Object>>() {
                    });
            Object items = snapshot.get("items");
            if (items instanceof List) {
                return (List<Map<String, Object>>) items;
            }
        } catch (Exception e) {
            // ignore parse errors
        }
        return new ArrayList<>();
    }

    private Map<String, Map<String, Object>> indexByField(List<Map<String, Object>> items, String field) {
        Map<String, Map<String, Object>> index = new LinkedHashMap<>();
        for (Map<String, Object> item : items) {
            Object key = item.get(field);
            if (key != null) {
                index.put(key.toString(), item);
            }
        }
        return index;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BomRevision createRevisionSnapshot(String bomId, String revisionCode, String reason) {
        Bom bom = this.getById(bomId);
        if (bom == null)
            return null;

        List<BomItem> items = bomItemMapper.selectByBomId(bomId);

        BomRevision revision = new BomRevision();
        revision.setBomId(bomId);
        revision.setRevisionCode(revisionCode);
        revision.setReason(reason);
        revision.setStatus("active");

        // Mark old active revisions as superseded
        List<BomRevision> oldRevisions = bomRevisionMapper.selectByBomId(bomId);
        for (BomRevision old : oldRevisions) {
            if ("active".equals(old.getStatus())) {
                old.setStatus("superseded");
                bomRevisionMapper.updateById(old);
            }
        }

        // Create snapshot
        try {
            Map<String, Object> snapshot = new HashMap<>();
            snapshot.put("bom", bom);
            snapshot.put("items", items);
            revision.setSnapshotData(objectMapper.writeValueAsString(snapshot));
        } catch (Exception e) {
            revision.setSnapshotData("{}");
        }

        bomRevisionMapper.insert(revision);

        // Update BOM version
        bom.setVersion(revisionCode);
        this.updateById(bom);

        return revision;
    }

    @Override
    public List<BomRevision> getRevisionHistory(String bomId) {
        return bomRevisionMapper.selectByBomId(bomId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importFromCsv(String bomId, InputStream csvStream) {
        Map<String, Object> result = new HashMap<>();
        List<BomItem> importedItems = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int lineNum = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(csvStream))) {
            String headerLine = reader.readLine(); // Skip header
            lineNum++;
            if (headerLine == null) {
                result.put("success", false);
                result.put("error", "File CSV rỗng!");
                return result;
            }

            // Expected CSV columns: Designator, Comment/Value, Footprint, Quantity,
            // MaterialId (optional)
            String line;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                try {
                    String[] cols = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                    if (cols.length < 3) {
                        errors.add("Dòng " + lineNum + ": Thiếu cột dữ liệu");
                        continue;
                    }

                    BomItem item = new BomItem();
                    item.setBomId(bomId);
                    item.setItemType("raw_material");

                    // RefDes (col 0)
                    String refDes = cols[0].trim().replace("\"", "");
                    item.setRefDesignators(refDes);

                    // Value/Comment (col 1) → notes
                    String comment = cols.length > 1 ? cols[1].trim().replace("\"", "") : "";
                    item.setNotes(comment);

                    // Footprint (col 2) → unit placeholder
                    String footprint = cols.length > 2 ? cols[2].trim().replace("\"", "") : "";
                    item.setUnit(footprint);

                    // Quantity (col 3)
                    if (cols.length > 3 && !cols[3].trim().isEmpty()) {
                        item.setQuantity(new BigDecimal(cols[3].trim().replace("\"", "")));
                    } else {
                        // Count refdes entries as quantity
                        int qty = refDes.isEmpty() ? 1 : refDes.split("[,;\\s]+").length;
                        item.setQuantity(new BigDecimal(qty));
                    }

                    // MaterialId (col 4, optional)
                    if (cols.length > 4 && !cols[4].trim().isEmpty()) {
                        item.setMaterialId(cols[4].trim().replace("\"", ""));
                    }

                    importedItems.add(item);
                } catch (Exception e) {
                    errors.add("Dòng " + lineNum + ": " + e.getMessage());
                }
            }

            // Insert items
            for (BomItem item : importedItems) {
                bomItemMapper.insert(item);
            }

            result.put("success", true);
            result.put("importedCount", importedItems.size());
            result.put("errors", errors);
            result.put("errorCount", errors.size());
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "Lỗi đọc file CSV: " + e.getMessage());
        }

        return result;
    }

    @Override
    public List<BomItemRefDes> getRefDesignators(String bomItemId) {
        return bomItemRefDesMapper.selectByBomItemId(bomItemId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveRefDesignators(String bomItemId, List<BomItemRefDes> refDesList) {
        // Delete existing RefDes
        bomItemRefDesMapper.deleteByBomItemId(bomItemId);

        // Insert new ones
        StringBuilder refDesStr = new StringBuilder();
        if (refDesList != null) {
            for (BomItemRefDes refDes : refDesList) {
                refDes.setBomItemId(bomItemId);
                bomItemRefDesMapper.insert(refDes);
                if (refDesStr.length() > 0)
                    refDesStr.append(",");
                refDesStr.append(refDes.getRefDesignator());
            }
        }

        // Update quick-access field on BomItem
        BomItem bomItem = bomItemMapper.selectById(bomItemId);
        if (bomItem != null) {
            bomItem.setRefDesignators(refDesStr.toString());
            bomItemMapper.updateById(bomItem);
        }

        return true;
    }
}
