package com.cy.modules.planning.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import com.cy.modules.planning.entity.Bom;
import com.cy.modules.planning.entity.BomItem;
import com.cy.modules.planning.entity.BomItemRefDes;
import com.cy.modules.planning.entity.BomRevision;
import com.cy.modules.planning.service.BomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Description: BOM Controller
 * @Author: BMad
 * @Date: 2026-02-25
 * @Version: V1.0
 */
@Slf4j
@Tag(name = "Định mức nguyên vật liệu (BOM)")
@RestController
@RequestMapping("/warehouse/bom")
public class BomController extends JeecgController<Bom, BomService> {

    @Autowired
    private BomService bomService;

    @Operation(summary = "Danh sách BOM")
    @GetMapping(value = "/list")
    public Result<?> list(Bom bom,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            HttpServletRequest req) {
        QueryWrapper<Bom> queryWrapper = QueryGenerator.initQueryWrapper(bom, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<Bom> page = new Page<>(pageNo, pageSize);
        IPage<Bom> pageList = bomService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @PostMapping(value = "/add")
    @AutoLog(value = "Thêm BOM")
    @Operation(summary = "Thêm BOM")
    public Result<?> add(@RequestBody Map<String, Object> requestBody) {
        // Extract bom and items from request
        Bom bom = extractBom(requestBody);
        List<BomItem> items = extractBomItems(requestBody);
        if (!bomService.isCodeUnique(bom.getBomCode(), null)) {
            return Result.error("Mã BOM đã tồn tại!");
        }
        bomService.saveBomWithItems(bom, items);
        return Result.OK("Thêm BOM thành công!");
    }

    @RequestMapping(value = "/edit", method = { RequestMethod.PUT, RequestMethod.POST })
    @AutoLog(value = "Sửa BOM", operateType = 3)
    @Operation(summary = "Sửa BOM")
    public Result<?> edit(@RequestBody Map<String, Object> requestBody) {
        Bom bom = extractBom(requestBody);
        List<BomItem> items = extractBomItems(requestBody);
        if (!bomService.isCodeUnique(bom.getBomCode(), bom.getId())) {
            return Result.error("Mã BOM đã tồn tại!");
        }
        bomService.updateBomWithItems(bom, items);
        return Result.OK("Cập nhật BOM thành công!");
    }

    @AutoLog(value = "Xóa BOM")
    @DeleteMapping(value = "/delete")
    @Operation(summary = "Xóa BOM")
    public Result<?> delete(@RequestParam(name = "id") String id) {
        bomService.removeById(id);
        return Result.OK("Xóa thành công!");
    }

    @DeleteMapping(value = "/deleteBatch")
    @Operation(summary = "Xóa hàng loạt BOM")
    public Result<?> deleteBatch(@RequestParam(name = "ids") String ids) {
        this.bomService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("Xóa hàng loạt thành công!");
    }

    @GetMapping(value = "/queryById")
    @Operation(summary = "Xem chi tiết BOM")
    public Result<?> queryById(@RequestParam(name = "id") String id) {
        Map<String, Object> detail = bomService.getBomDetail(id);
        return Result.OK(detail);
    }

    @GetMapping(value = "/getItems")
    @Operation(summary = "Lấy danh sách NVL của BOM")
    public Result<?> getItems(@RequestParam(name = "bomId") String bomId) {
        List<BomItem> items = bomService.getBomItems(bomId);
        return Result.OK(items);
    }

    @GetMapping(value = "/getByProductId")
    @Operation(summary = "Lấy BOM theo thành phẩm")
    public Result<?> getByProductId(@RequestParam(name = "productId") String productId) {
        List<Bom> list = bomService.getByProductId(productId);
        return Result.OK(list);
    }

    @GetMapping(value = "/listActive")
    @Operation(summary = "Lấy tất cả BOM đang hoạt động")
    public Result<?> listActive() {
        List<Bom> list = bomService.getByStatus("active");
        return Result.OK(list);
    }

    @GetMapping(value = "/tree")
    @Operation(summary = "Lấy cấu trúc cây BOM nhiều cấp")
    public Result<?> getBomTree(@RequestParam(name = "bomId") String bomId) {
        Map<String, Object> tree = bomService.getBomTree(bomId);
        if (tree == null) {
            return Result.error("Không tìm thấy BOM!");
        }
        return Result.OK(tree);
    }

    @GetMapping(value = "/flattenMaterials")
    @Operation(summary = "Phẳng hoá BOM – tính tổng NVL gốc cần cho số lượng sản phẩm (bao gồm hao hụt)")
    public Result<?> flattenMaterials(@RequestParam(name = "bomId") String bomId,
            @RequestParam(name = "quantity", defaultValue = "1") java.math.BigDecimal quantity) {
        List<Map<String, Object>> materials = bomService.getFlattenedMaterials(bomId, quantity);
        return Result.OK(materials);
    }

    // ==== New: Electronics BOM features ====

    @GetMapping(value = "/whereUsed")
    @Operation(summary = "Where-used: tìm tất cả BOM chứa linh kiện này")
    public Result<?> whereUsed(@RequestParam(name = "materialId") String materialId) {
        List<Map<String, Object>> result = bomService.whereUsed(materialId);
        return Result.OK(result);
    }

    @PostMapping(value = "/createRevision")
    @AutoLog(value = "Tạo phiên bản BOM")
    @Operation(summary = "Tạo bản snapshot phiên bản BOM hiện tại")
    public Result<?> createRevision(@RequestParam(name = "bomId") String bomId,
            @RequestParam(name = "revisionCode") String revisionCode,
            @RequestParam(name = "reason", required = false) String reason) {
        BomRevision revision = bomService.createRevisionSnapshot(bomId, revisionCode, reason);
        if (revision == null) {
            return Result.error("Không tìm thấy BOM!");
        }
        return Result.OK(revision);
    }

    @GetMapping(value = "/revisionHistory")
    @Operation(summary = "Lấy lịch sử phiên bản BOM")
    public Result<?> revisionHistory(@RequestParam(name = "bomId") String bomId) {
        List<BomRevision> history = bomService.getRevisionHistory(bomId);
        return Result.OK(history);
    }

    @GetMapping(value = "/compareRevisions")
    @Operation(summary = "So sánh 2 phiên bản BOM")
    public Result<?> compareRevisions(@RequestParam(name = "revisionId1") String revisionId1,
            @RequestParam(name = "revisionId2") String revisionId2) {
        Map<String, Object> comparison = bomService.compareBomRevisions(revisionId1, revisionId2);
        return Result.OK(comparison);
    }

    @PostMapping(value = "/importCsv")
    @AutoLog(value = "Import BOM từ CSV")
    @Operation(summary = "Import BOM từ file CSV (Altium/KiCad export)")
    public Result<?> importCsv(@RequestParam(name = "bomId") String bomId,
            @RequestParam(name = "file") MultipartFile file) {
        try {
            Map<String, Object> result = bomService.importFromCsv(bomId, file.getInputStream());
            Boolean success = (Boolean) result.get("success");
            if (Boolean.TRUE.equals(success)) {
                return Result.OK(result);
            } else {
                return Result.error(result.get("error") != null ? result.get("error").toString() : "Import thất bại!");
            }
        } catch (Exception e) {
            log.error("Import CSV error: ", e);
            return Result.error("Lỗi import file: " + e.getMessage());
        }
    }

    @GetMapping(value = "/getRefDesignators")
    @Operation(summary = "Lấy danh sách RefDes của BOM Item")
    public Result<?> getRefDesignators(@RequestParam(name = "bomItemId") String bomItemId) {
        List<BomItemRefDes> refDesList = bomService.getRefDesignators(bomItemId);
        return Result.OK(refDesList);
    }

    @PostMapping(value = "/saveRefDesignators")
    @AutoLog(value = "Lưu RefDes")
    @Operation(summary = "Lưu danh sách vị trí linh kiện trên PCB (RefDes)")
    public Result<?> saveRefDesignators(@RequestParam(name = "bomItemId") String bomItemId,
            @RequestBody List<BomItemRefDes> refDesList) {
        bomService.saveRefDesignators(bomItemId, refDesList);
        return Result.OK("Lưu RefDes thành công!");
    }

    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, Bom bom) {
        return super.exportXls(request, bom, Bom.class, "BOM");
    }

    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, Bom.class);
    }

    @SuppressWarnings("unchecked")
    private Bom extractBom(Map<String, Object> body) {
        Map<String, Object> bomMap = (Map<String, Object>) body.get("bom");
        Bom bom = new Bom();
        if (bomMap != null) {
            bom.setId((String) bomMap.get("id"));
            bom.setBomCode((String) bomMap.get("bomCode"));
            bom.setBomName((String) bomMap.get("bomName"));
            bom.setProductId((String) bomMap.get("productId"));
            bom.setUnit((String) bomMap.get("unit"));
            bom.setVersion((String) bomMap.getOrDefault("version", "1.0"));
            bom.setStatus((String) bomMap.getOrDefault("status", "active"));
            bom.setNotes((String) bomMap.get("notes"));
            if (bomMap.get("outputQuantity") != null) {
                bom.setOutputQuantity(new java.math.BigDecimal(bomMap.get("outputQuantity").toString()));
            }
        }
        return bom;
    }

    @SuppressWarnings("unchecked")
    private List<BomItem> extractBomItems(Map<String, Object> body) {
        List<Map<String, Object>> itemMaps = (List<Map<String, Object>>) body.get("items");
        List<BomItem> items = new java.util.ArrayList<>();
        if (itemMaps != null) {
            for (Map<String, Object> m : itemMaps) {
                BomItem item = new BomItem();
                item.setId((String) m.get("id"));
                item.setMaterialId((String) m.get("materialId"));
                item.setUnit((String) m.get("unit"));
                item.setNotes((String) m.get("notes"));
                item.setRefDesignators((String) m.get("refDesignators"));
                if (m.get("quantity") != null) {
                    item.setQuantity(new java.math.BigDecimal(m.get("quantity").toString()));
                }
                if (m.get("wastageRate") != null) {
                    item.setWastageRate(new java.math.BigDecimal(m.get("wastageRate").toString()));
                }
                items.add(item);
            }
        }
        return items;
    }
}
