package com.cy.modules.planning.controller;

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
import com.cy.modules.planning.entity.ProductionStage;
import com.cy.modules.planning.entity.WorkOrder;
import com.cy.modules.planning.service.WorkOrderService;
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
 * @Description: Work Order Controller – Lệnh sản xuất
 * @Author: BMad
 * @Date: 2026-02-25
 * @Version: V1.0
 */
@Slf4j
@Tag(name = "Lệnh sản xuất (Work Order)")
@RestController
@RequestMapping("/warehouse/workOrder")
public class WorkOrderController extends JeecgController<WorkOrder, WorkOrderService> {

    @Autowired
    private WorkOrderService workOrderService;

    @Operation(summary = "Danh sách lệnh sản xuất")
    @GetMapping(value = "/list")
    public Result<?> list(WorkOrder workOrder,
                          @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                          @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                          HttpServletRequest req) {
        QueryWrapper<WorkOrder> queryWrapper = QueryGenerator.initQueryWrapper(workOrder, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<WorkOrder> page = new Page<>(pageNo, pageSize);
        IPage<WorkOrder> pageList = workOrderService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @PostMapping(value = "/add")
    @AutoLog(value = "Tạo lệnh sản xuất")
    @Operation(summary = "Tạo lệnh sản xuất")
    public Result<?> add(@RequestBody Map<String, Object> requestBody) {
        WorkOrder workOrder = extractWorkOrder(requestBody);
        @SuppressWarnings("unchecked")
        List<ProductionStage> stages = (List<ProductionStage>) requestBody.get("stages");

        if (!workOrderService.isCodeUnique(workOrder.getOrderCode(), null)) {
            return Result.error("Mã lệnh sản xuất đã tồn tại!");
        }
        if (workOrder.getOrderCode() == null || workOrder.getOrderCode().isEmpty()) {
            workOrder.setOrderCode(workOrderService.generateOrderCode());
        }
        workOrderService.saveWithStages(workOrder, stages);
        return Result.OK("Tạo lệnh sản xuất thành công!");
    }

    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    @AutoLog(value = "Sửa lệnh sản xuất", operateType = 3)
    @Operation(summary = "Sửa lệnh sản xuất")
    public Result<?> edit(@RequestBody WorkOrder workOrder) {
        WorkOrder existing = workOrderService.getById(workOrder.getId());
        if (existing == null) return Result.error("Không tìm thấy lệnh sản xuất");
        if ("completed".equals(existing.getStatus()) || "cancelled".equals(existing.getStatus())) {
            return Result.error("Không thể sửa lệnh đã hoàn thành hoặc đã hủy");
        }
        workOrderService.updateById(workOrder);
        return Result.OK("Cập nhật thành công!");
    }

    @AutoLog(value = "Xóa lệnh sản xuất")
    @DeleteMapping(value = "/delete")
    @Operation(summary = "Xóa lệnh sản xuất")
    public Result<?> delete(@RequestParam(name = "id") String id) {
        WorkOrder wo = workOrderService.getById(id);
        if (wo != null && ("in_progress".equals(wo.getStatus()) || "completed".equals(wo.getStatus()))) {
            return Result.error("Không thể xóa lệnh đang sản xuất hoặc đã hoàn thành!");
        }
        workOrderService.removeById(id);
        return Result.OK("Xóa thành công!");
    }

    @DeleteMapping(value = "/deleteBatch")
    @Operation(summary = "Xóa hàng loạt lệnh sản xuất")
    public Result<?> deleteBatch(@RequestParam(name = "ids") String ids) {
        this.workOrderService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("Xóa hàng loạt thành công!");
    }

    @GetMapping(value = "/queryById")
    @Operation(summary = "Xem chi tiết lệnh sản xuất")
    public Result<?> queryById(@RequestParam(name = "id") String id) {
        Map<String, Object> detail = workOrderService.getWorkOrderDetail(id);
        return Result.OK(detail);
    }

    @PutMapping(value = "/start/{id}")
    @AutoLog(value = "Bắt đầu sản xuất", operateType = 3)
    @Operation(summary = "Bắt đầu sản xuất")
    public Result<?> start(@PathVariable("id") String id,
                           @RequestParam(name = "operator", required = false) String operator) {
        String result = workOrderService.startProduction(id, operator);
        if (result.contains("thành công")) {
            return Result.OK(result);
        }
        return Result.error(result);
    }

    @PutMapping(value = "/complete/{id}")
    @AutoLog(value = "Hoàn thành sản xuất", operateType = 3)
    @Operation(summary = "Hoàn thành sản xuất – trừ kho NVL và nhập kho thành phẩm")
    public Result<?> complete(@PathVariable("id") String id,
                              @RequestParam(name = "actualQuantity") BigDecimal actualQuantity,
                              @RequestParam(name = "operator", required = false) String operator) {
        String result = workOrderService.completeProduction(id, actualQuantity, operator);
        if (result.contains("thành công")) {
            return Result.OK(result);
        }
        return Result.error(result);
    }

    @PutMapping(value = "/cancel/{id}")
    @AutoLog(value = "Hủy lệnh sản xuất", operateType = 3)
    @Operation(summary = "Hủy lệnh sản xuất")
    public Result<?> cancel(@PathVariable("id") String id,
                            @RequestParam(name = "reason", required = false) String reason,
                            @RequestParam(name = "operator", required = false) String operator) {
        String result = workOrderService.cancelWorkOrder(id, reason, operator);
        if (result.contains("thành công")) {
            return Result.OK(result);
        }
        return Result.error(result);
    }

    @PutMapping(value = "/stage/update")
    @AutoLog(value = "Cập nhật công đoạn sản xuất", operateType = 3)
    @Operation(summary = "Cập nhật trạng thái công đoạn sản xuất")
    public Result<?> updateStage(@RequestParam(name = "stageId") String stageId,
                                 @RequestParam(name = "status") String status,
                                 @RequestParam(name = "actualDurationHours", required = false) BigDecimal actualDurationHours,
                                 @RequestParam(name = "notes", required = false) String notes,
                                 @RequestParam(name = "operator", required = false) String operator) {
        String result = workOrderService.updateStage(stageId, status, actualDurationHours, notes, operator);
        if (result.contains("thành công")) {
            return Result.OK(result);
        }
        return Result.error(result);
    }

    @GetMapping(value = "/stages")
    @Operation(summary = "Lấy công đoạn của lệnh sản xuất")
    public Result<?> getStages(@RequestParam(name = "workOrderId") String workOrderId) {
        List<ProductionStage> stages = workOrderService.getStages(workOrderId);
        return Result.OK(stages);
    }

    @GetMapping(value = "/statistics")
    @Operation(summary = "Thống kê tổng quan sản xuất")
    public Result<?> getStatistics() {
        Map<String, Object> stats = workOrderService.getStatistics();
        return Result.OK(stats);
    }

    @GetMapping(value = "/getByStatus")
    @Operation(summary = "Lấy lệnh SX theo trạng thái")
    public Result<?> getByStatus(@RequestParam(name = "status") String status) {
        List<WorkOrder> list = workOrderService.getByStatus(status);
        return Result.OK(list);
    }

    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, WorkOrder workOrder) {
        return super.exportXls(request, workOrder, WorkOrder.class, "Lệnh sản xuất");
    }

    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, WorkOrder.class);
    }

    @SuppressWarnings("unchecked")
    private WorkOrder extractWorkOrder(Map<String, Object> body) {
        Map<String, Object> woMap = body.containsKey("workOrder")
                ? (Map<String, Object>) body.get("workOrder") : body;
        WorkOrder wo = new WorkOrder();
        wo.setId((String) woMap.get("id"));
        wo.setOrderCode((String) woMap.get("orderCode"));
        wo.setBomId((String) woMap.get("bomId"));
        wo.setProductionLineId((String) woMap.get("productionLineId"));
        wo.setStatus((String) woMap.getOrDefault("status", "draft"));
        wo.setPriority((String) woMap.getOrDefault("priority", "normal"));
        wo.setNotes((String) woMap.get("notes"));
        if (woMap.get("plannedQuantity") != null)
            wo.setPlannedQuantity(new BigDecimal(woMap.get("plannedQuantity").toString()));
        return wo;
    }
}
