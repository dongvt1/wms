package org.jeecg.modules.warehouse.controller;

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
import org.jeecg.modules.warehouse.entity.IqcInspection;
import org.jeecg.modules.warehouse.entity.IqcInspectionResult;
import org.jeecg.modules.warehouse.service.IqcInspectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Description: IQC Inspection Controller
 * @Author: BMad
 * @Date: 2026-02-25
 */
@Slf4j
@Tag(name = "QMS - Kiểm tra chất lượng đầu vào (IQC)")
@RestController
@RequestMapping("/warehouse/qms/iqc")
public class IqcInspectionController extends JeecgController<IqcInspection, IqcInspectionService> {

    @Autowired
    private IqcInspectionService iqcService;

    @Operation(summary = "Danh sách phiếu IQC")
    @GetMapping("/list")
    public Result<?> list(IqcInspection inspection,
                          @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                          @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                          HttpServletRequest req) {
        QueryWrapper<IqcInspection> qw = QueryGenerator.initQueryWrapper(inspection, req.getParameterMap());
        qw.orderByDesc("create_time");
        Page<IqcInspection> page = new Page<>(pageNo, pageSize);
        IPage<IqcInspection> pageList = iqcService.page(page, qw);
        return Result.OK(pageList);
    }

    @PostMapping("/add")
    @AutoLog(value = "Tạo phiếu IQC")
    @Operation(summary = "Tạo phiếu kiểm tra IQC")
    public Result<?> add(@RequestBody Map<String, Object> requestBody) {
        IqcInspection inspection = extractInspection(requestBody);
        List<IqcInspectionResult> results = extractResults(requestBody);
        if (inspection.getInspectionCode() == null || inspection.getInspectionCode().isEmpty()) {
            inspection.setInspectionCode(iqcService.generateInspectionCode());
        } else if (!iqcService.isCodeUnique(inspection.getInspectionCode(), null)) {
            return Result.error("Mã phiếu IQC đã tồn tại!");
        }
        if (inspection.getStatus() == null) inspection.setStatus("draft");
        iqcService.saveWithResults(inspection, results);
        return Result.OK("Tạo phiếu IQC thành công!");
    }

    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    @AutoLog(value = "Sửa phiếu IQC", operateType = 3)
    @Operation(summary = "Sửa phiếu IQC")
    public Result<?> edit(@RequestBody Map<String, Object> requestBody) {
        IqcInspection inspection = extractInspection(requestBody);
        List<IqcInspectionResult> results = extractResults(requestBody);
        iqcService.updateWithResults(inspection, results);
        return Result.OK("Cập nhật phiếu IQC thành công!");
    }

    @AutoLog(value = "Xóa phiếu IQC")
    @DeleteMapping("/delete")
    @Operation(summary = "Xóa phiếu IQC")
    public Result<?> delete(@RequestParam(name = "id") String id) {
        iqcService.removeById(id);
        return Result.OK("Xóa thành công!");
    }

    @DeleteMapping("/deleteBatch")
    @Operation(summary = "Xóa hàng loạt phiếu IQC")
    public Result<?> deleteBatch(@RequestParam(name = "ids") String ids) {
        iqcService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("Xóa hàng loạt thành công!");
    }

    @GetMapping("/queryById")
    @Operation(summary = "Xem chi tiết phiếu IQC")
    public Result<?> queryById(@RequestParam(name = "id") String id) {
        return Result.OK(iqcService.getDetail(id));
    }

    @GetMapping("/getResults")
    @Operation(summary = "Lấy kết quả tiêu chí của phiếu IQC")
    public Result<?> getResults(@RequestParam(name = "inspectionId") String inspectionId) {
        return Result.OK(iqcService.getResults(inspectionId));
    }

    @PutMapping("/approve/{id}")
    @AutoLog(value = "Duyệt phiếu IQC", operateType = 3)
    @Operation(summary = "Duyệt phiếu IQC (passed/failed/conditional)")
    public Result<?> approve(@PathVariable("id") String id,
                             @RequestParam(name = "status") String status,
                             @RequestParam(name = "notes", required = false) String notes,
                             @RequestParam(name = "operator", required = false) String operator) {
        String msg = iqcService.approveInspection(id, status, notes, operator);
        return Result.OK(msg);
    }

    @GetMapping("/statistics")
    @Operation(summary = "Thống kê IQC")
    public Result<?> statistics() {
        return Result.OK(iqcService.getStatistics());
    }

    @SuppressWarnings("unchecked")
    private IqcInspection extractInspection(Map<String, Object> body) {
        Map<String, Object> m = (Map<String, Object>) body.get("inspection");
        IqcInspection ins = new IqcInspection();
        if (m != null) {
            ins.setId((String) m.get("id"));
            ins.setInspectionCode((String) m.get("inspectionCode"));
            ins.setProductId((String) m.get("productId"));
            ins.setSupplierId((String) m.get("supplierId"));
            ins.setStockTransactionId((String) m.get("stockTransactionId"));
            ins.setTemplateId((String) m.get("templateId"));
            ins.setInspector((String) m.get("inspector"));
            ins.setStatus((String) m.get("status"));
            ins.setNotes((String) m.get("notes"));
            if (m.get("quantityReceived") != null)
                ins.setQuantityReceived(new BigDecimal(m.get("quantityReceived").toString()));
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
    private List<IqcInspectionResult> extractResults(Map<String, Object> body) {
        List<Map<String, Object>> resultMaps = (List<Map<String, Object>>) body.get("results");
        List<IqcInspectionResult> results = new java.util.ArrayList<>();
        if (resultMaps != null) {
            for (Map<String, Object> m : resultMaps) {
                IqcInspectionResult r = new IqcInspectionResult();
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
