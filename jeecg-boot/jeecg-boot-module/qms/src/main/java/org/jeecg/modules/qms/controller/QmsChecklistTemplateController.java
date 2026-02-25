package org.jeecg.modules.qms.controller;

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
import org.jeecg.modules.qms.entity.QmsChecklistItem;
import org.jeecg.modules.qms.entity.QmsChecklistTemplate;
import org.jeecg.modules.qms.service.QmsChecklistTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Description: QMS Checklist Template Controller
 * @Author: BMad
 * @Date: 2026-02-25
 */
@Slf4j
@Tag(name = "QMS - Mẫu bộ tiêu chí kiểm tra")
@RestController
@RequestMapping("/warehouse/qms/checklist")
public class QmsChecklistTemplateController extends JeecgController<QmsChecklistTemplate, QmsChecklistTemplateService> {

    @Autowired
    private QmsChecklistTemplateService templateService;

    @Operation(summary = "Danh sách mẫu checklist")
    @GetMapping("/list")
    public Result<?> list(QmsChecklistTemplate template,
                          @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                          @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                          HttpServletRequest req) {
        QueryWrapper<QmsChecklistTemplate> qw = QueryGenerator.initQueryWrapper(template, req.getParameterMap());
        qw.orderByDesc("create_time");
        Page<QmsChecklistTemplate> page = new Page<>(pageNo, pageSize);
        IPage<QmsChecklistTemplate> pageList = templateService.page(page, qw);
        return Result.OK(pageList);
    }

    @PostMapping("/add")
    @AutoLog(value = "Thêm mẫu checklist QMS")
    @Operation(summary = "Thêm mẫu checklist")
    public Result<?> add(@RequestBody Map<String, Object> requestBody) {
        QmsChecklistTemplate template = extractTemplate(requestBody);
        List<QmsChecklistItem> items = extractItems(requestBody);
        if (!templateService.isCodeUnique(template.getTemplateCode(), null)) {
            return Result.error("Mã mẫu checklist đã tồn tại!");
        }
        templateService.saveWithItems(template, items);
        return Result.OK("Thêm mẫu checklist thành công!");
    }

    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    @AutoLog(value = "Sửa mẫu checklist QMS", operateType = 3)
    @Operation(summary = "Sửa mẫu checklist")
    public Result<?> edit(@RequestBody Map<String, Object> requestBody) {
        QmsChecklistTemplate template = extractTemplate(requestBody);
        List<QmsChecklistItem> items = extractItems(requestBody);
        if (!templateService.isCodeUnique(template.getTemplateCode(), template.getId())) {
            return Result.error("Mã mẫu checklist đã tồn tại!");
        }
        templateService.updateWithItems(template, items);
        return Result.OK("Cập nhật mẫu checklist thành công!");
    }

    @AutoLog(value = "Xóa mẫu checklist QMS")
    @DeleteMapping("/delete")
    @Operation(summary = "Xóa mẫu checklist")
    public Result<?> delete(@RequestParam(name = "id") String id) {
        templateService.removeById(id);
        return Result.OK("Xóa thành công!");
    }

    @DeleteMapping("/deleteBatch")
    @Operation(summary = "Xóa hàng loạt mẫu checklist")
    public Result<?> deleteBatch(@RequestParam(name = "ids") String ids) {
        templateService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("Xóa hàng loạt thành công!");
    }

    @GetMapping("/queryById")
    @Operation(summary = "Xem chi tiết mẫu checklist")
    public Result<?> queryById(@RequestParam(name = "id") String id) {
        return Result.OK(templateService.getTemplateDetail(id));
    }

    @GetMapping("/getItems")
    @Operation(summary = "Lấy danh sách tiêu chí của mẫu")
    public Result<?> getItems(@RequestParam(name = "templateId") String templateId) {
        return Result.OK(templateService.getItems(templateId));
    }

    @GetMapping("/listActive")
    @Operation(summary = "Lấy mẫu checklist đang active theo loại")
    public Result<?> listActive(@RequestParam(name = "inspectionType", required = false) String inspectionType) {
        return Result.OK(templateService.getActiveByType(inspectionType));
    }

    @SuppressWarnings("unchecked")
    private QmsChecklistTemplate extractTemplate(Map<String, Object> body) {
        Map<String, Object> m = (Map<String, Object>) body.get("template");
        QmsChecklistTemplate t = new QmsChecklistTemplate();
        if (m != null) {
            t.setId((String) m.get("id"));
            t.setTemplateCode((String) m.get("templateCode"));
            t.setTemplateName((String) m.get("templateName"));
            t.setInspectionType((String) m.get("inspectionType"));
            t.setProductId((String) m.get("productId"));
            t.setStatus((String) m.getOrDefault("status", "active"));
            t.setNotes((String) m.get("notes"));
        }
        return t;
    }

    @SuppressWarnings("unchecked")
    private List<QmsChecklistItem> extractItems(Map<String, Object> body) {
        List<Map<String, Object>> itemMaps = (List<Map<String, Object>>) body.get("items");
        List<QmsChecklistItem> items = new java.util.ArrayList<>();
        if (itemMaps != null) {
            for (Map<String, Object> m : itemMaps) {
                QmsChecklistItem item = new QmsChecklistItem();
                item.setId((String) m.get("id"));
                item.setCriterionName((String) m.get("criterionName"));
                item.setStandardValue((String) m.get("standardValue"));
                item.setInputType((String) m.getOrDefault("inputType", "pass_fail"));
                item.setOptions((String) m.get("options"));
                item.setNotes((String) m.get("notes"));
                if (m.get("itemOrder") != null) item.setItemOrder(Integer.parseInt(m.get("itemOrder").toString()));
                if (m.get("isRequired") != null) item.setIsRequired(Integer.parseInt(m.get("isRequired").toString()));
                else item.setIsRequired(1);
                items.add(item);
            }
        }
        return items;
    }
}
