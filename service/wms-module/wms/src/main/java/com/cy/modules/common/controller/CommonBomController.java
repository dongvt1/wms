package com.cy.modules.common.controller;

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
import com.cy.modules.common.entity.Bom;
import com.cy.modules.common.entity.BomItem;
import com.cy.modules.common.entity.BomItemSubstitute;
import com.cy.modules.common.service.CommonBomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Description: Common BOM Controller – endpoint /common/bom
 *               Dùng chung cho warehouse, planning, qms và tất cả module khác
 * @Author: BMad
 * @Date: 2026-03-02
 * @Version: V1.0
 */
@Slf4j
@Tag(name = "[Common] Định mức nguyên vật liệu (BOM)")
@RestController
@RequestMapping("/common/bom")
public class CommonBomController extends JeecgController<Bom, CommonBomService> {

    @Autowired
    private CommonBomService commonBomService;

    // ===== CRUD cơ bản =====

    @Operation(summary = "Danh sách BOM (có phân trang)")
    @GetMapping(value = "/list")
    public Result<?> list(Bom bom,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            HttpServletRequest req) {
        QueryWrapper<Bom> queryWrapper = QueryGenerator.initQueryWrapper(bom, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<Bom> page = new Page<>(pageNo, pageSize);
        IPage<Bom> pageList = commonBomService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @PostMapping(value = "/add")
    @AutoLog(value = "[Common] Thêm BOM")
    @Operation(summary = "Thêm BOM kèm danh sách NVL")
    public Result<?> add(@RequestBody Map<String, Object> requestBody) {
        Bom bom = extractBom(requestBody);
        List<BomItem> items = extractBomItems(requestBody);
        if (!commonBomService.isCodeUnique(bom.getBomCode(), null)) {
            return Result.error("Mã BOM đã tồn tại!");
        }
        commonBomService.saveBomWithItems(bom, items);
        return Result.OK("Thêm BOM thành công!");
    }

    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    @AutoLog(value = "[Common] Sửa BOM", operateType = 3)
    @Operation(summary = "Sửa BOM kèm danh sách NVL")
    public Result<?> edit(@RequestBody Map<String, Object> requestBody) {
        Bom bom = extractBom(requestBody);
        List<BomItem> items = extractBomItems(requestBody);
        if (!commonBomService.isCodeUnique(bom.getBomCode(), bom.getId())) {
            return Result.error("Mã BOM đã tồn tại!");
        }
        commonBomService.updateBomWithItems(bom, items);
        return Result.OK("Cập nhật BOM thành công!");
    }

    @AutoLog(value = "[Common] Xóa BOM")
    @DeleteMapping(value = "/delete")
    @Operation(summary = "Xóa BOM")
    public Result<?> delete(@RequestParam(name = "id") String id) {
        commonBomService.removeById(id);
        return Result.OK("Xóa thành công!");
    }

    @DeleteMapping(value = "/deleteBatch")
    @Operation(summary = "Xóa hàng loạt BOM")
    public Result<?> deleteBatch(@RequestParam(name = "ids") String ids) {
        commonBomService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("Xóa hàng loạt thành công!");
    }

    @GetMapping(value = "/queryById")
    @Operation(summary = "Xem chi tiết BOM kèm NVL")
    public Result<?> queryById(@RequestParam(name = "id") String id) {
        return Result.OK(commonBomService.getBomDetail(id));
    }

    // ===== NVL & tra cứu =====

    @GetMapping(value = "/getItems")
    @Operation(summary = "Lấy danh sách NVL của BOM")
    public Result<?> getItems(@RequestParam(name = "bomId") String bomId) {
        return Result.OK(commonBomService.getBomItems(bomId));
    }

    @GetMapping(value = "/getByProductId")
    @Operation(summary = "Lấy danh sách BOM theo thành phẩm")
    public Result<?> getByProductId(@RequestParam(name = "productId") String productId) {
        return Result.OK(commonBomService.getByProductId(productId));
    }

    @GetMapping(value = "/listActive")
    @Operation(summary = "Lấy tất cả BOM đang hoạt động")
    public Result<?> listActive() {
        return Result.OK(commonBomService.listActive());
    }

    @PostMapping(value = "/setDefault")
    @AutoLog(value = "[Common] Đặt BOM mặc định")
    @Operation(summary = "Đặt BOM làm mặc định cho sản phẩm")
    public Result<?> setDefault(@RequestParam(name = "bomId") String bomId,
            @RequestParam(name = "productId") String productId) {
        boolean ok = commonBomService.setDefaultBom(bomId, productId);
        return ok ? Result.OK("Đặt BOM mặc định thành công!") : Result.error("Không tìm thấy BOM!");
    }

    // ===== Cấu trúc cây & phân tích =====

    @GetMapping(value = "/tree")
    @Operation(summary = "Xem cấu trúc cây BOM nhiều cấp")
    public Result<?> getBomTree(@RequestParam(name = "bomId") String bomId) {
        Map<String, Object> tree = commonBomService.getBomTree(bomId);
        return tree != null ? Result.OK(tree) : Result.error("Không tìm thấy BOM!");
    }

    @GetMapping(value = "/flattenMaterials")
    @Operation(summary = "Phẳng hoá BOM – tổng NVL gốc cần (bao gồm hao hụt)")
    public Result<?> flattenMaterials(@RequestParam(name = "bomId") String bomId,
            @RequestParam(name = "quantity", defaultValue = "1") BigDecimal quantity) {
        return Result.OK(commonBomService.getFlattenedMaterials(bomId, quantity));
    }

    @GetMapping(value = "/whereUsed")
    @Operation(summary = "Where-used: tìm tất cả BOM chứa NVL này")
    public Result<?> whereUsed(@RequestParam(name = "materialId") String materialId) {
        return Result.OK(commonBomService.whereUsed(materialId));
    }

    // ===== Excel =====

    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, Bom bom) {
        return super.exportXls(request, bom, Bom.class, "BOM");
    }

    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, Bom.class);
    }

    // ===== Helper methods =====

    @Operation(summary = "Lấy danh sách linh kiện thay thế theo BOM Item")
    @GetMapping("/getSubstitutes")
    public Result<?> getSubstitutes(@RequestParam(name = "bomItemId") String bomItemId) {
        return Result.OK(commonBomService.getSubstitutes(bomItemId));
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
                bom.setOutputQuantity(new BigDecimal(bomMap.get("outputQuantity").toString()));
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
                item.setItemType((String) m.get("itemType"));
                item.setChildBomId((String) m.get("childBomId"));
                if (m.get("quantity") != null) item.setQuantity(new BigDecimal(m.get("quantity").toString()));
                if (m.get("wastageRate") != null) item.setWastageRate(new BigDecimal(m.get("wastageRate").toString()));
                if (m.get("purchaseLeadTimeDays") != null) item.setPurchaseLeadTimeDays(Integer.valueOf(m.get("purchaseLeadTimeDays").toString()));
                // Parse substitutes
                List<Map<String, Object>> subMaps = (List<Map<String, Object>>) m.get("substitutes");
                if (subMaps != null) {
                    List<BomItemSubstitute> subs = new java.util.ArrayList<>();
                    for (Map<String, Object> s : subMaps) {
                        BomItemSubstitute sub = new BomItemSubstitute();
                        sub.setSubstituteMaterialId((String) s.get("substituteMaterialId"));
                        sub.setNotes((String) s.get("notes"));
                        if (s.get("priority") != null) sub.setPriority(Integer.valueOf(s.get("priority").toString()));
                        subs.add(sub);
                    }
                    item.setSubstitutes(subs);
                }
                items.add(item);
            }
        }
        return items;
    }
}
