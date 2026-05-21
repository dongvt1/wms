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
import com.cy.modules.qms.entity.QcStage;
import com.cy.modules.qms.entity.QcStageParam;
import com.cy.modules.qms.service.QcStageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Tag(name = "QMS - Cấu hình Công đoạn Kiểm tra")
@RestController
@RequestMapping("/qms/stage")
public class QcStageController extends JeecgController<QcStage, QcStageService> {

    @Autowired
    private QcStageService stageService;

    @GetMapping("/list")
    @Operation(summary = "Danh sách công đoạn kiểm tra")
    public Result<?> list(QcStage stage,
                          @RequestParam(defaultValue = "1") Integer pageNo,
                          @RequestParam(defaultValue = "10") Integer pageSize,
                          HttpServletRequest req) {
        QueryWrapper<QcStage> qw = QueryGenerator.initQueryWrapper(stage, req.getParameterMap());
        qw.orderByAsc("sort_order");
        IPage<QcStage> page = stageService.page(new Page<>(pageNo, pageSize), qw);
        return Result.OK(page);
    }

    @PostMapping("/add")
    @RequiresPermissions("qms:template:manage")
    @AutoLog(value = "Tạo công đoạn kiểm tra")
    @Operation(summary = "Tạo công đoạn kiểm tra kèm tham số")
    public Result<?> add(@RequestBody Map<String, Object> body) {
        QcStage stage = extractStage(body);
        List<QcStageParam> params = extractParams(body);
        if (stage.getStageCode() == null || stage.getStageCode().isEmpty())
            stage.setStageCode(stageService.generateStageCode());
        if (stage.getStatus() == null) stage.setStatus("active");
        stageService.saveWithParams(stage, params);
        return Result.OK("Tạo công đoạn thành công!");
    }

    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    @RequiresPermissions("qms:template:manage")
    @AutoLog(value = "Sửa công đoạn kiểm tra", operateType = 3)
    @Operation(summary = "Sửa công đoạn kiểm tra kèm tham số")
    public Result<?> edit(@RequestBody Map<String, Object> body) {
        QcStage stage = extractStage(body);
        List<QcStageParam> params = extractParams(body);
        stageService.updateWithParams(stage, params);
        return Result.OK("Cập nhật thành công!");
    }

    @DeleteMapping("/delete")
    @RequiresPermissions("qms:template:manage")
    @AutoLog(value = "Xóa công đoạn kiểm tra")
    public Result<?> delete(@RequestParam String id) {
        stageService.removeById(id);
        return Result.OK("Xóa thành công!");
    }

    @GetMapping("/queryById")
    @Operation(summary = "Chi tiết công đoạn kèm tham số")
    public Result<?> queryById(@RequestParam String id) {
        return Result.OK(stageService.getDetail(id));
    }

    @GetMapping("/getParams")
    @Operation(summary = "Lấy danh sách tham số của công đoạn")
    public Result<?> getParams(@RequestParam String stageId) {
        return Result.OK(stageService.getParams(stageId));
    }

    @GetMapping("/listActive")
    @Operation(summary = "Danh sách công đoạn đang hoạt động")
    public Result<?> listActive() {
        return Result.OK(stageService.listActive());
    }

    @RequestMapping(value = "/export")
    public org.springframework.web.servlet.ModelAndView exportXls(jakarta.servlet.http.HttpServletRequest request, QcStage stage) {
        return super.exportXls(request, stage, QcStage.class, "QC Stage Report");
    }

    @SuppressWarnings("unchecked")
    private QcStage extractStage(Map<String, Object> body) {
        Map<String, Object> m = (Map<String, Object>) body.get("stage");
        QcStage s = new QcStage();
        if (m != null) {
            s.setId((String) m.get("id"));
            s.setStageCode((String) m.get("stageCode"));
            s.setStageName((String) m.get("stageName"));
            s.setDescription((String) m.get("description"));
            s.setStatus((String) m.get("status"));
            if (m.get("sortOrder") != null) s.setSortOrder(Integer.parseInt(m.get("sortOrder").toString()));
        }
        return s;
    }

    @SuppressWarnings("unchecked")
    private List<QcStageParam> extractParams(Map<String, Object> body) {
        List<Map<String, Object>> paramMaps = (List<Map<String, Object>>) body.get("params");
        List<QcStageParam> params = new ArrayList<>();
        if (paramMaps == null) return params;
        for (Map<String, Object> m : paramMaps) {
            QcStageParam p = new QcStageParam();
            p.setParamName((String) m.get("paramName"));
            p.setParamCode((String) m.get("paramCode"));
            p.setInputType((String) m.get("inputType"));
            p.setUnit((String) m.get("unit"));
            p.setDefaultValue((String) m.get("defaultValue"));
            p.setOptionsJson((String) m.get("optionsJson"));
            p.setNotes((String) m.get("notes"));
            if (m.get("minValue") != null) p.setMinValue(new BigDecimal(m.get("minValue").toString()));
            if (m.get("maxValue") != null) p.setMaxValue(new BigDecimal(m.get("maxValue").toString()));
            if (m.get("isRequired") != null) p.setIsRequired(Integer.parseInt(m.get("isRequired").toString()));
            if (m.get("sortOrder") != null) p.setSortOrder(Integer.parseInt(m.get("sortOrder").toString()));
            params.add(p);
        }
        return params;
    }
}
