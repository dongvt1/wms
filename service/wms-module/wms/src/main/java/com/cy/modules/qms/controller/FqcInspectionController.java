package com.cy.modules.qms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import com.cy.modules.qms.entity.FqcInspection;
import com.cy.modules.qms.entity.FqcInspectionResult;
import com.cy.modules.qms.service.FqcInspectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Description: FQC Inspection Controller
 * @Author: BMad
 * @Date: 2026-02-25
 */
@Slf4j
@Tag(name = "QMS - Final Quality Control (FQC)")
@RestController
@RequestMapping("/qms/fqc")
public class FqcInspectionController extends JeecgController<FqcInspection, FqcInspectionService> {

    @Autowired
    private FqcInspectionService fqcService;

    @Operation(summary = "List of FQC inspection forms")
    @GetMapping("/list")
    public Result<?> list(FqcInspection inspection,
                          @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                          @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                          HttpServletRequest req) {
        QueryWrapper<FqcInspection> qw = QueryGenerator.initQueryWrapper(inspection, req.getParameterMap());
        qw.orderByDesc("create_time");
        Page<FqcInspection> page = new Page<>(pageNo, pageSize);
        IPage<FqcInspection> pageList = fqcService.page(page, qw);
        return Result.OK(pageList);
    }

    @PostMapping("/add")
    @RequiresPermissions("qms:inspection:add")
    @AutoLog(value = "Create FQC inspection form")
    @Operation(summary = "Create FQC inspection form")
    public Result<?> add(@RequestBody Map<String, Object> requestBody) {
        FqcInspection inspection = extractInspection(requestBody);
        List<FqcInspectionResult> results = extractResults(requestBody);
        if (inspection.getInspectionCode() == null || inspection.getInspectionCode().isEmpty()) {
            inspection.setInspectionCode(fqcService.generateInspectionCode());
        }
        if (inspection.getStatus() == null) inspection.setStatus("draft");
        fqcService.saveWithResults(inspection, results);
        return Result.OK("FQC inspection form created successfully!");
    }

    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    @RequiresPermissions("qms:inspection:edit")
    @AutoLog(value = "Edit FQC inspection form", operateType = 3)
    @Operation(summary = "Edit FQC inspection form")
    public Result<?> edit(@RequestBody Map<String, Object> requestBody) {
        FqcInspection inspection = extractInspection(requestBody);
        List<FqcInspectionResult> results = extractResults(requestBody);
        fqcService.updateWithResults(inspection, results);
        return Result.OK("FQC inspection form updated successfully!");
    }

    @AutoLog(value = "Delete FQC inspection form")
    @DeleteMapping("/delete")
    @Operation(summary = "Delete FQC inspection form (draft only)")
    public Result<?> delete(@RequestParam(name = "id") String id) {
        FqcInspection inspection = fqcService.getById(id);
        if (inspection == null) {
            return Result.error("FQC inspection not found!");
        }
        if (!"draft".equals(inspection.getStatus())) {
            return Result.error("Only draft FQC inspections can be deleted!");
        }
        fqcService.removeById(id);
        return Result.OK("Deleted successfully!");
    }

    @GetMapping("/queryById")
    @Operation(summary = "View FQC inspection form details")
    public Result<?> queryById(@RequestParam(name = "id") String id) {
        return Result.OK(fqcService.getDetail(id));
    }

    @GetMapping("/getResults")
    @Operation(summary = "Get criteria results of FQC inspection form")
    public Result<?> getResults(@RequestParam(name = "inspectionId") String inspectionId) {
        return Result.OK(fqcService.getResults(inspectionId));
    }

    @PutMapping("/submit/{id}")
    @AutoLog(value = "Submit FQC inspection form for approval", operateType = 3)
    @Operation(summary = "Submit FQC inspection form for approval (in_progress → pending_approval)")
    public Result<?> submit(@PathVariable("id") String id) {
        String msg = fqcService.submitForApproval(id);
        return Result.OK(msg);
    }

    @PutMapping("/approve/{id}")
    @RequiresPermissions("qms:inspection:approve")
    @AutoLog(value = "Approve FQC inspection form", operateType = 3)
    @Operation(summary = "Approve FQC inspection form (pending_approval → passed/failed)")
    public Result<?> approve(@PathVariable("id") String id,
                             @RequestParam(name = "status") String status,
                             @RequestParam(name = "notes", required = false) String notes,
                             @RequestParam(name = "operator", required = false) String operator) {
        String msg = fqcService.approveInspection(id, status, notes, operator);
        return Result.OK(msg);
    }

    @GetMapping("/statistics")
    @Operation(summary = "FQC statistics")
    public Result<?> statistics() {
        return Result.OK(fqcService.getStatistics());
    }

    @GetMapping("/checkOutbound/{orderId}")
    @Operation(summary = "Check if outbound order is allowed (FQC must be passed)")
    public Result<?> checkOutbound(@PathVariable("orderId") String orderId) {
        boolean allowed = fqcService.isOutboundAllowed(orderId);
        if (allowed) {
            return Result.OK("Outbound is allowed", true);
        } else {
            return Result.error("Outbound is blocked: FQC inspection has not passed for this order");
        }
    }

    @RequestMapping(value = "/export")
    public org.springframework.web.servlet.ModelAndView exportXls(HttpServletRequest request, FqcInspection inspection) {
        return super.exportXls(request, inspection, FqcInspection.class, "FQC Inspection Report");
    }

    @SuppressWarnings("unchecked")
    private FqcInspection extractInspection(Map<String, Object> body) {
        Map<String, Object> m = (Map<String, Object>) body.get("inspection");
        FqcInspection ins = new FqcInspection();
        if (m != null) {
            ins.setId((String) m.get("id"));
            ins.setInspectionCode((String) m.get("inspectionCode"));
            ins.setOutboundOrderId((String) m.get("outboundOrderId"));
            ins.setProductId((String) m.get("productId"));
            ins.setCustomerId((String) m.get("customerId"));
            ins.setTemplateId((String) m.get("templateId"));
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
    private List<FqcInspectionResult> extractResults(Map<String, Object> body) {
        List<Map<String, Object>> resultMaps = (List<Map<String, Object>>) body.get("results");
        List<FqcInspectionResult> results = new java.util.ArrayList<>();
        if (resultMaps != null) {
            for (Map<String, Object> m : resultMaps) {
                FqcInspectionResult r = new FqcInspectionResult();
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
