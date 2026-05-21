package com.cy.modules.qms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import com.cy.modules.qms.entity.Ncr;
import com.cy.modules.qms.service.NcrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @Description: NCR (Non-Conformance Report) Controller
 * @Author: BMad
 * @Date: 2026-02-25
 */
@Slf4j
@Tag(name = "QMS - Non-Conformance Report (NCR)")
@RestController
@RequestMapping("/qms/ncr")
public class NcrController extends JeecgController<Ncr, NcrService> {

    @Autowired
    private NcrService ncrService;

    /**
     * Paginated list with QueryWrapper filters (status, severity, sourceType, supplierId)
     */
    @Operation(summary = "Danh sách NCR phân trang")
    @GetMapping("/list")
    public Result<?> list(Ncr ncr,
                          @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                          @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                          HttpServletRequest req) {
        QueryWrapper<Ncr> qw = QueryGenerator.initQueryWrapper(ncr, req.getParameterMap());
        qw.orderByDesc("create_time");
        Page<Ncr> page = new Page<>(pageNo, pageSize);
        IPage<Ncr> pageList = ncrService.page(page, qw);
        return Result.OK(pageList);
    }

    /**
     * Create NCR — can use createFromInspection if sourceId provided
     */
    @PostMapping("/add")
    @RequiresPermissions("qms:ncr:add")
    @AutoLog(value = "Tạo NCR")
    @Operation(summary = "Tạo NCR mới")
    public Result<?> add(@RequestBody Ncr ncr) {
        // If sourceId is provided, use createFromInspection to auto-link supplier
        if (ncr.getSourceId() != null && !ncr.getSourceId().isEmpty()) {
            Ncr created = ncrService.createFromInspection(ncr, ncr.getSourceId(), ncr.getSourceType());
            return Result.OK("Tạo NCR thành công!", created);
        }
        // Otherwise, create normally with auto-generated code
        if (ncr.getNcrCode() == null || ncr.getNcrCode().isEmpty()) {
            ncr.setNcrCode(ncrService.generateNcrCode());
        }
        if (ncr.getStatus() == null) {
            ncr.setStatus("open");
        }
        ncrService.save(ncr);
        return Result.OK("Tạo NCR thành công!", ncr);
    }

    /**
     * Update NCR — only if status is open or investigating
     */
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    @AutoLog(value = "Cập nhật NCR", operateType = 3)
    @Operation(summary = "Cập nhật NCR (chỉ khi trạng thái open hoặc investigating)")
    public Result<?> edit(@RequestBody Ncr ncr) {
        Ncr existing = ncrService.getById(ncr.getId());
        if (existing == null) {
            return Result.error("Không tìm thấy NCR");
        }
        String status = existing.getStatus();
        if (!"open".equals(status) && !"investigating".equals(status)) {
            return Result.error("Chỉ có thể cập nhật NCR ở trạng thái open hoặc investigating");
        }
        ncrService.updateById(ncr);
        return Result.OK("Cập nhật NCR thành công!");
    }

    /**
     * Delete NCR — only if status is open
     */
    @AutoLog(value = "Xóa NCR")
    @DeleteMapping("/delete")
    @Operation(summary = "Xóa NCR (chỉ khi trạng thái open)")
    public Result<?> delete(@RequestParam(name = "id") String id) {
        Ncr existing = ncrService.getById(id);
        if (existing == null) {
            return Result.error("Không tìm thấy NCR");
        }
        if (!"open".equals(existing.getStatus())) {
            return Result.error("Chỉ có thể xóa NCR ở trạng thái open");
        }
        ncrService.removeById(id);
        return Result.OK("Xóa NCR thành công!");
    }

    /**
     * Get NCR detail by ID
     */
    @GetMapping("/queryById")
    @Operation(summary = "Xem chi tiết NCR")
    public Result<?> queryById(@RequestParam(name = "id") String id) {
        Ncr ncr = ncrService.getById(id);
        if (ncr == null) {
            return Result.error("Không tìm thấy NCR");
        }
        return Result.OK(ncr);
    }

    /**
     * State transition with @AutoLog — accepts targetStatus + notes params
     */
    @PutMapping("/transition/{id}")
    @AutoLog(value = "Chuyển trạng thái NCR", operateType = 3)
    @Operation(summary = "Chuyển trạng thái NCR theo state machine")
    public Result<?> transition(@PathVariable("id") String id,
                                @RequestParam(name = "targetStatus") String targetStatus,
                                @RequestParam(name = "notes", required = false) String notes) {
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String operator = loginUser != null ? loginUser.getUsername() : null;
        String msg = ncrService.transition(id, targetStatus, notes, operator);
        if (msg.contains("thành công")) {
            return Result.OK(msg);
        }
        return Result.error(msg);
    }

    /**
     * Close NCR with @AutoLog — accepts confirmationNotes param
     */
    @PutMapping("/close/{id}")
    @RequiresPermissions("qms:ncr:close")
    @AutoLog(value = "Đóng NCR", operateType = 3)
    @Operation(summary = "Đóng NCR (yêu cầu xác nhận hành động khắc phục)")
    public Result<?> close(@PathVariable("id") String id,
                           @RequestParam(name = "confirmationNotes") String confirmationNotes) {
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String operator = loginUser != null ? loginUser.getUsername() : null;
        String msg = ncrService.close(id, confirmationNotes, operator);
        if (msg.contains("thành công")) {
            return Result.OK(msg);
        }
        return Result.error(msg);
    }

    /**
     * Get NCR statistics
     */
    @GetMapping("/statistics")
    @Operation(summary = "Thống kê NCR")
    public Result<?> statistics() {
        Map<String, Object> stats = ncrService.getStatistics();
        return Result.OK(stats);
    }

    /**
     * Get NCR history for a supplier
     */
    @GetMapping("/bySupplier/{supplierId}")
    @Operation(summary = "Lịch sử NCR theo nhà cung cấp")
    public Result<?> bySupplier(@PathVariable("supplierId") String supplierId) {
        List<Ncr> history = ncrService.getSupplierHistory(supplierId);
        return Result.OK(history);
    }
}
