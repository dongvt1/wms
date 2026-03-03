package com.cy.modules.qms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import com.cy.modules.qms.entity.PqcInspection;
import com.cy.modules.qms.entity.PqcInspectionResult;
import com.cy.modules.qms.service.PqcInspectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Description: PQC Inspection Controller
 * @Author: BMad
 * @Date: 2026-02-25
 */
@Slf4j
@Tag(name = "QMS - Production Quality Control (PQC)")
@RestController
@RequestMapping("/qms/pqc")
public class PqcInspectionController extends JeecgController<PqcInspection, PqcInspectionService> {

    @Autowired
    private PqcInspectionService pqcService;

    @Operation(summary = "List of PQC inspection forms")
    @GetMapping("/list")
    public Result<?> list(PqcInspection inspection,
                          @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                          @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                          HttpServletRequest req) {
        QueryWrapper<PqcInspection> qw = QueryGenerator.initQueryWrapper(inspection, req.getParameterMap());
        qw.orderByDesc("create_time");
        Page<PqcInspection> page = new Page<>(pageNo, pageSize);
        IPage<PqcInspection> pageList = pqcService.page(page, qw);
        return Result.OK(pageList);
    }

    @PostMapping("/add")
    @AutoLog(value = "Create PQC inspection form")
    @Operation(summary = "Create PQC inspection form")
    public Result<?> add(@RequestBody Map<String, Object> requestBody) {
        PqcInspection inspection = extractInspection(requestBody);
        List<PqcInspectionResult> results = extractResults(requestBody);
        if (inspection.getInspectionCode() == null || inspection.getInspectionCode().isEmpty()) {
            inspection.setInspectionCode(pqcService.generateInspectionCode());
        } else if (!pqcService.isCodeUnique(inspection.getInspectionCode(), null)) {
            return Result.error("PQC inspection code already exists!");
        }
        if (inspection.getStatus() == null) inspection.setStatus("draft");
        pqcService.saveWithResults(inspection, results);
        return Result.OK("PQC inspection form created successfully!");
    }

    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    @AutoLog(value = "Edit PQC inspection form", operateType = 3)
    @Operation(summary = "Edit PQC inspection form")
    public Result<?> edit(@RequestBody Map<String, Object> requestBody) {
        PqcInspection inspection = extractInspection(requestBody);
        List<PqcInspectionResult> results = extractResults(requestBody);
        pqcService.updateWithResults(inspection, results);
        return Result.OK("PQC inspection form updated successfully!");
    }

    @AutoLog(value = "Delete PQC inspection form")
    @DeleteMapping("/delete")
    @Operation(summary = "Delete PQC inspection form")
    public Result<?> delete(@RequestParam(name = "id") String id) {
        pqcService.removeById(id);
        return Result.OK("Deleted successfully!");
    }

    @DeleteMapping("/deleteBatch")
    @Operation(summary = "Batch delete PQC inspection forms")
    public Result<?> deleteBatch(@RequestParam(name = "ids") String ids) {
        pqcService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("Batch delete successful!");
    }

    @GetMapping("/queryById")
    @Operation(summary = "View PQC inspection form details")
    public Result<?> queryById(@RequestParam(name = "id") String id) {
        return Result.OK(pqcService.getDetail(id));
    }

    @GetMapping("/getResults")
    @Operation(summary = "Get criteria results of PQC inspection form")
    public Result<?> getResults(@RequestParam(name = "inspectionId") String inspectionId) {
        return Result.OK(pqcService.getResults(inspectionId));
    }

    @PutMapping("/submit/{id}")
    @AutoLog(value = "Submit PQC inspection form for approval", operateType = 3)
    @Operation(summary = "Submit PQC inspection form for approval (in_progress → pending_approval)")
    public Result<?> submit(@PathVariable("id") String id) {
        String msg = pqcService.submitForApproval(id);
        return Result.OK(msg);
    }

    @PutMapping("/approve/{id}")
    @AutoLog(value = "Approve PQC inspection form", operateType = 3)
    @Operation(summary = "Approve PQC inspection form (pending_approval → passed/failed)")
    public Result<?> approve(@PathVariable("id") String id,
                             @RequestParam(name = "status") String status,
                             @RequestParam(name = "notes", required = false) String notes,
                             @RequestParam(name = "operator", required = false) String operator) {
        String msg = pqcService.approveInspection(id, status, notes, operator);
        return Result.OK(msg);
    }

    @GetMapping("/statistics")
    @Operation(summary = "PQC statistics")
    public Result<?> statistics() {
        return Result.OK(pqcService.getStatistics());
    }

    @RequestMapping(value = "/export")
    public org.springframework.web.servlet.ModelAndView exportXls(jakarta.servlet.http.HttpServletRequest request, PqcInspection inspection) {
        return super.exportXls(request, inspection, PqcInspection.class, "PQC Inspection Report");
    }

    @SuppressWarnings("unchecked")
    private PqcInspection extractInspection(Map<String, Object> body) {
        Map<String, Object> m = (Map<String, Object>) body.get("inspection");
        PqcInspection ins = new PqcInspection();
        if (m != null) {
            ins.setId((String) m.get("id"));
            ins.setInspectionCode((String) m.get("inspectionCode"));
            ins.setWorkOrderId((String) m.get("workOrderId"));
            ins.setProductId((String) m.get("productId"));
            ins.setTemplateId((String) m.get("templateId"));
            ins.setStageId((String) m.get("stageId"));
            ins.setInspector((String) m.get("inspector"));
            ins.setStatus((String) m.get("status"));
            ins.setNotes((String) m.get("notes"));
            if (m.get("quantityInspected") != null)
                ins.setQuantityInspected(new BigDecimal(m.get("quantityInspected").toString()));
            if (m.get("quantityPassed") != null)
                ins.setQuantityPassed(new BigDecimal(m.get("quantityPassed").toString()));
            if (m.get("quantityFailed") != null)
                ins.setQuantityFailed(new BigDecimal(m.get("quantityFailed").toString()));
            if (m.get("inspectionDate") != null) {
                try {
                    ins.setInspectionDate(new java.text.SimpleDateFormat("yyyy-MM-dd")
                            .parse(m.get("inspectionDate").toString()));
                } catch (Exception e) { log.warn("Cannot parse inspectionDate"); }
            }
        }
        return ins;
    }

    @SuppressWarnings("unchecked")
    private List<PqcInspectionResult> extractResults(Map<String, Object> body) {
        List<Map<String, Object>> resultMaps = (List<Map<String, Object>>) body.get("results");
        List<PqcInspectionResult> results = new java.util.ArrayList<>();
        if (resultMaps != null) {
            for (Map<String, Object> m : resultMaps) {
                PqcInspectionResult r = new PqcInspectionResult();
                r.setId((String) m.get("id"));
                r.setChecklistItemId((String) m.get("checklistItemId"));
                r.setCriterionName((String) m.get("criterionName"));
                r.setStandardValue((String) m.get("standardValue"));
                r.setActualValue((String) m.get("actualValue"));
                r.setResult((String) m.get("result"));
                r.setNotes((String) m.get("notes"));
                results.add(r);
            }
        }
        return results;
    }
}
