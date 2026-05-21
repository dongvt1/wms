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
import com.cy.modules.qms.entity.QcSession;
import com.cy.modules.qms.service.QcSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@Tag(name = "QMS - Stage Inspection Session")
@RestController
@RequestMapping("/qms/session")
public class QcSessionController extends JeecgController<QcSession, QcSessionService> {

    @Autowired
    private QcSessionService sessionService;

    @GetMapping("/list")
    @Operation(summary = "List of inspection sessions")
    public Result<?> list(QcSession session,
                          @RequestParam(defaultValue = "1") Integer pageNo,
                          @RequestParam(defaultValue = "10") Integer pageSize,
                          HttpServletRequest req) {
        QueryWrapper<QcSession> qw = QueryGenerator.initQueryWrapper(session, req.getParameterMap());
        qw.orderByDesc("create_time");
        IPage<QcSession> page = sessionService.page(new Page<>(pageNo, pageSize), qw);
        return Result.OK(page);
    }

    @PostMapping("/add")
    @AutoLog(value = "Create inspection session")
    @Operation(summary = "Create inspection session with parameter values")
    public Result<?> add(@RequestBody Map<String, Object> body) {
        QcSession session = extractSession(body);
        List<Map<String, Object>> values = extractValues(body);
        if (session.getSessionCode() == null || session.getSessionCode().isEmpty())
            session.setSessionCode(sessionService.generateSessionCode());
        if (session.getStatus() == null) session.setStatus("draft");
        sessionService.saveWithValues(session, values);
        return Result.OK("Inspection session created successfully!");
    }

    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    @AutoLog(value = "Edit inspection session", operateType = 3)
    @Operation(summary = "Edit inspection session with parameter values")
    public Result<?> edit(@RequestBody Map<String, Object> body) {
        QcSession session = extractSession(body);
        List<Map<String, Object>> values = extractValues(body);
        sessionService.updateWithValues(session, values);
        return Result.OK("Inspection session updated successfully!");
    }

    @PutMapping("/complete/{id}")
    @AutoLog(value = "Complete inspection session", operateType = 3)
    @Operation(summary = "Complete inspection session (draft → completed)")
    public Result<?> complete(@PathVariable String id) {
        return Result.OK(sessionService.completeSession(id));
    }

    @DeleteMapping("/delete")
    public Result<?> delete(@RequestParam String id) {
        sessionService.removeById(id);
        return Result.OK("Deleted successfully!");
    }

    @GetMapping("/queryById")
    @Operation(summary = "Inspection session details with values")
    public Result<?> queryById(@RequestParam String id) {
        return Result.OK(sessionService.getDetail(id));
    }

    @GetMapping("/getValues")
    @Operation(summary = "Get parameter values of inspection session")
    public Result<?> getValues(@RequestParam String sessionId) {
        return Result.OK(sessionService.getValues(sessionId));
    }

    @GetMapping("/listByWorkOrder")
    @Operation(summary = "List of inspection sessions by WO")
    public Result<?> listByWorkOrder(@RequestParam String workOrderId) {
        return Result.OK(sessionService.listByWorkOrder(workOrderId));
    }

    @RequestMapping(value = "/export")
    public org.springframework.web.servlet.ModelAndView exportXls(jakarta.servlet.http.HttpServletRequest request, QcSession session) {
        return super.exportXls(request, session, QcSession.class, "QC Session Report");
    }

    @SuppressWarnings("unchecked")
    private QcSession extractSession(Map<String, Object> body) {
        Map<String, Object> m = (Map<String, Object>) body.get("session");
        QcSession s = new QcSession();
        if (m != null) {
            s.setId((String) m.get("id"));
            s.setSessionCode((String) m.get("sessionCode"));
            s.setWorkOrderId((String) m.get("workOrderId"));
            s.setStageId((String) m.get("stageId"));
            s.setStageName((String) m.get("stageName"));
            s.setInspector((String) m.get("inspector"));
            s.setStatus((String) m.get("status"));
            s.setNotes((String) m.get("notes"));
            if (m.get("inspectionDate") != null) {
                try { s.setInspectionDate(new java.text.SimpleDateFormat("yyyy-MM-dd")
                        .parse(m.get("inspectionDate").toString())); }
                catch (Exception e) { log.warn("Cannot parse inspectionDate"); }
            }
        }
        return s;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractValues(Map<String, Object> body) {
        return (List<Map<String, Object>>) body.getOrDefault("values", new ArrayList<>());
    }
}
