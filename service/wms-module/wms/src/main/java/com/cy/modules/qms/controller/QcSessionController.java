package qms.controller;

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
import qms.entity.QcSession;
import qms.service.QcSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@Tag(name = "QMS - Phiên kiểm tra công đoạn")
@RestController
@RequestMapping("/warehouse/qms/session")
public class QcSessionController extends JeecgController<QcSession, QcSessionService> {

    @Autowired
    private QcSessionService sessionService;

    @GetMapping("/list")
    @Operation(summary = "Danh sách phiên kiểm tra")
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
    @AutoLog(value = "Tạo phiên kiểm tra")
    @Operation(summary = "Tạo phiên kiểm tra kèm giá trị tham số")
    public Result<?> add(@RequestBody Map<String, Object> body) {
        QcSession session = extractSession(body);
        List<Map<String, Object>> values = extractValues(body);
        if (session.getSessionCode() == null || session.getSessionCode().isEmpty())
            session.setSessionCode(sessionService.generateSessionCode());
        if (session.getStatus() == null) session.setStatus("draft");
        sessionService.saveWithValues(session, values);
        return Result.OK("Tạo phiên kiểm tra thành công!");
    }

    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    @AutoLog(value = "Sửa phiên kiểm tra", operateType = 3)
    @Operation(summary = "Sửa phiên kiểm tra kèm giá trị tham số")
    public Result<?> edit(@RequestBody Map<String, Object> body) {
        QcSession session = extractSession(body);
        List<Map<String, Object>> values = extractValues(body);
        sessionService.updateWithValues(session, values);
        return Result.OK("Cập nhật phiên kiểm tra thành công!");
    }

    @PutMapping("/complete/{id}")
    @AutoLog(value = "Hoàn thành phiên kiểm tra", operateType = 3)
    @Operation(summary = "Hoàn thành phiên kiểm tra (draft → completed)")
    public Result<?> complete(@PathVariable String id) {
        return Result.OK(sessionService.completeSession(id));
    }

    @DeleteMapping("/delete")
    public Result<?> delete(@RequestParam String id) {
        sessionService.removeById(id);
        return Result.OK("Xóa thành công!");
    }

    @GetMapping("/queryById")
    @Operation(summary = "Chi tiết phiên kiểm tra kèm giá trị")
    public Result<?> queryById(@RequestParam String id) {
        return Result.OK(sessionService.getDetail(id));
    }

    @GetMapping("/getValues")
    @Operation(summary = "Lấy giá trị tham số của phiên kiểm tra")
    public Result<?> getValues(@RequestParam String sessionId) {
        return Result.OK(sessionService.getValues(sessionId));
    }

    @GetMapping("/listByWorkOrder")
    @Operation(summary = "Danh sách phiên kiểm tra theo WO")
    public Result<?> listByWorkOrder(@RequestParam String workOrderId) {
        return Result.OK(sessionService.listByWorkOrder(workOrderId));
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
