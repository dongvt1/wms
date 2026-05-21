package com.cy.modules.common.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cy.modules.common.entity.Material;
import com.cy.modules.common.entity.MaterialSubstitute;
import com.cy.modules.common.service.MaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Description: Common Material Controller – endpoint /common/material
 *               Quản lý nguyên vật liệu (bảng riêng biệt, tách khỏi product)
 * @Author: BMad
 * @Date: 2026-03-05
 * @Version: V1.0
 */
@Slf4j
@Tag(name = "[Common] Nguyên vật liệu (Material)")
@RestController
@RequestMapping("/common/material")
public class CommonMaterialController {

    @Autowired
    private MaterialService materialService;

    // ===== CRUD cơ bản =====

    @Operation(summary = "Danh sách vật tư (có phân trang, search)")
    @GetMapping("/list")
    public Result<?> list(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Material> page = new Page<>(pageNo, pageSize);
        IPage<Material> result = materialService.listWithCategory(page, code, name, categoryId, status);
        return Result.OK(result);
    }

    @Operation(summary = "Tất cả vật tư active (cho dropdown)")
    @GetMapping("/listAll")
    public Result<?> listAll() {
        return Result.OK(materialService.listAllActive());
    }

    @Operation(summary = "Chi tiết vật tư kèm linh kiện thay thế")
    @GetMapping("/queryById")
    public Result<?> queryById(@RequestParam String id) {
        Material material = materialService.getById(id);
        if (material == null) return Result.error("Không tìm thấy vật tư!");
        material.setSubstitutes(materialService.getSubstitutes(id));
        return Result.OK(material);
    }

    @AutoLog(value = "[Common] Thêm vật tư")
    @Operation(summary = "Thêm vật tư kèm linh kiện thay thế")
    @PostMapping("/add")
    public Result<?> add(@RequestBody Map<String, Object> body) {
        Material material = extractMaterial(body);
        List<MaterialSubstitute> substitutes = extractSubstitutes(body);
        if (!materialService.isCodeUnique(material.getCode(), null)) {
            return Result.error("Mã vật tư đã tồn tại!");
        }
        materialService.saveMaterialWithSubstitutes(material, substitutes);
        return Result.OK("Thêm vật tư thành công!");
    }

    @AutoLog(value = "[Common] Sửa vật tư", operateType = 3)
    @Operation(summary = "Sửa vật tư kèm linh kiện thay thế")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<?> edit(@RequestBody Map<String, Object> body) {
        Material material = extractMaterial(body);
        List<MaterialSubstitute> substitutes = extractSubstitutes(body);
        if (!materialService.isCodeUnique(material.getCode(), material.getId())) {
            return Result.error("Mã vật tư đã tồn tại!");
        }
        materialService.updateMaterialWithSubstitutes(material, substitutes);
        return Result.OK("Cập nhật vật tư thành công!");
    }

    @AutoLog(value = "[Common] Xóa vật tư")
    @Operation(summary = "Xóa vật tư")
    @DeleteMapping("/delete")
    public Result<?> delete(@RequestParam String id) {
        materialService.removeById(id);
        return Result.OK("Xóa thành công!");
    }

    @Operation(summary = "Xóa hàng loạt vật tư")
    @DeleteMapping("/deleteBatch")
    public Result<?> deleteBatch(@RequestParam String ids) {
        materialService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("Xóa hàng loạt thành công!");
    }

    // ===== Linh kiện thay thế =====

    @Operation(summary = "Lấy danh sách linh kiện thay thế của vật tư")
    @GetMapping("/getSubstitutes")
    public Result<?> getSubstitutes(@RequestParam String materialId) {
        return Result.OK(materialService.getSubstitutes(materialId));
    }

    // ===== Helpers =====

    @SuppressWarnings("unchecked")
    private Material extractMaterial(Map<String, Object> body) {
        Map<String, Object> m = (Map<String, Object>) body.get("material");
        Material mat = new Material();
        if (m != null) {
            mat.setId((String) m.get("id"));
            mat.setCode((String) m.get("code"));
            mat.setName((String) m.get("name"));
            mat.setDescription((String) m.get("description"));
            mat.setUnit((String) m.get("unit"));
            mat.setCategoryId((String) m.get("categoryId"));
            mat.setImage((String) m.get("image"));
            if (m.get("price") != null) mat.setPrice(new java.math.BigDecimal(m.get("price").toString()));
            if (m.get("minStockLevel") != null) mat.setMinStockLevel(Integer.valueOf(m.get("minStockLevel").toString()));
            if (m.get("status") != null) mat.setStatus(Integer.valueOf(m.get("status").toString()));
            if (m.get("weight") != null) mat.setWeight(new java.math.BigDecimal(m.get("weight").toString()));
            if (m.get("length") != null) mat.setLength(new java.math.BigDecimal(m.get("length").toString()));
            if (m.get("width") != null) mat.setWidth(new java.math.BigDecimal(m.get("width").toString()));
            if (m.get("height") != null) mat.setHeight(new java.math.BigDecimal(m.get("height").toString()));
        }
        return mat;
    }

    @SuppressWarnings("unchecked")
    private List<MaterialSubstitute> extractSubstitutes(Map<String, Object> body) {
        List<Map<String, Object>> subMaps = (List<Map<String, Object>>) body.get("substitutes");
        List<MaterialSubstitute> list = new java.util.ArrayList<>();
        if (subMaps != null) {
            for (Map<String, Object> s : subMaps) {
                MaterialSubstitute sub = new MaterialSubstitute();
                sub.setSubstituteMaterialId((String) s.get("substituteMaterialId"));
                sub.setNotes((String) s.get("notes"));
                if (s.get("priority") != null) sub.setPriority(Integer.valueOf(s.get("priority").toString()));
                list.add(sub);
            }
        }
        return list;
    }
}
