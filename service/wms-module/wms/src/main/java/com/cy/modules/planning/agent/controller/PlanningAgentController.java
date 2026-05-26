package com.cy.modules.planning.agent.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cy.modules.planning.agent.dto.GeneratePrRequest;
import com.cy.modules.planning.agent.dto.MaterialAvailabilityResult;
import com.cy.modules.planning.agent.entity.PlanningOrder;
import com.cy.modules.planning.agent.entity.PurchaseRequest;
import com.cy.modules.planning.agent.mapper.PlanningOrderMapper;
import com.cy.modules.planning.agent.service.MaterialAvailabilityService;
import com.cy.modules.planning.agent.service.OrderIngestionService;
import com.cy.modules.planning.agent.service.OrderSyncService;
import com.cy.modules.planning.agent.service.ProcurementCoordinationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.aspect.annotation.PermissionData;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * @Description: Controller quản lý đơn hàng và mua sắm của Planning Agent
 * @Author: AI Planning Agent
 * @Date: 2026-06-20
 * @Version: V1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/planning-agent")
@Tag(name = "Planning Agent - Orders & Procurement", description = "API quản lý đơn hàng và phối hợp mua sắm")
public class PlanningAgentController {

    @Resource
    private OrderSyncService orderSyncService;

    @Resource
    private OrderIngestionService orderIngestionService;

    @Resource
    private MaterialAvailabilityService materialAvailabilityService;

    @Resource
    private ProcurementCoordinationService procurementCoordinationService;

    @Resource
    private PlanningOrderMapper planningOrderMapper;

    /**
     * Kích hoạt đồng bộ đơn hàng thủ công từ OrderHub.
     */
    @PostMapping("/orders/sync")
    @AutoLog(value = "Planning Agent - Đồng bộ đơn hàng thủ công")
    @Operation(summary = "Kích hoạt đồng bộ đơn hàng thủ công",
            description = "Kích hoạt đồng bộ đơn hàng từ OrderHub ngay lập tức thay vì chờ lịch trình 5 phút")
    public Result<String> triggerOrderSync() {
        log.info("[PlanningAgent] Nhận yêu cầu đồng bộ đơn hàng thủ công");
        try {
            orderSyncService.triggerManualSync();
            return Result.OK("Đồng bộ đơn hàng đã được kích hoạt thành công");
        } catch (Exception e) {
            log.error("[PlanningAgent] Lỗi khi đồng bộ đơn hàng: {}", e.getMessage(), e);
            return Result.error("Lỗi khi đồng bộ đơn hàng: " + e.getMessage());
        }
    }

    /**
     * Lấy hàng đợi đơn hàng ưu tiên với phân trang.
     * Áp dụng data isolation theo sys_org_code.
     */
    @GetMapping("/orders/queue")
    @AutoLog(value = "Planning Agent - Lấy hàng đợi đơn hàng ưu tiên")
    @Operation(summary = "Lấy hàng đợi đơn hàng ưu tiên",
            description = "Trả về danh sách đơn hàng đã sắp xếp theo deadline ASC, receipt_timestamp ASC với phân trang")
    @PermissionData(pageComponent = "planning/agent/orderQueue")
    public Result<IPage<PlanningOrder>> getOrderQueue(
            PlanningOrder planningOrder,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            HttpServletRequest req) {
        QueryWrapper<PlanningOrder> queryWrapper = QueryGenerator.initQueryWrapper(planningOrder, req.getParameterMap());
        // Chỉ lấy đơn hàng hợp lệ và đang chờ xử lý
        queryWrapper.eq("validation_status", "valid");
        queryWrapper.in("status", "pending", "confirmed");
        // Sắp xếp theo ưu tiên: deadline ASC, receipt_timestamp ASC
        queryWrapper.orderByAsc("deadline");
        queryWrapper.orderByAsc("receipt_timestamp");

        Page<PlanningOrder> page = new Page<>(pageNo, pageSize);
        IPage<PlanningOrder> pageList = planningOrderMapper.selectPage(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * Kiểm tra tình trạng nguyên vật liệu cho một đơn hàng.
     */
    @GetMapping("/orders/{id}/material-check")
    @AutoLog(value = "Planning Agent - Kiểm tra nguyên vật liệu")
    @Operation(summary = "Kiểm tra tình trạng nguyên vật liệu cho đơn hàng",
            description = "Truy vấn tồn kho so với yêu cầu BOM, tính toán thiếu hụt và xác thực thời gian giao hàng")
    public Result<MaterialAvailabilityResult> checkMaterialAvailability(@PathVariable("id") String id) {
        log.info("[PlanningAgent] Kiểm tra nguyên vật liệu cho đơn hàng: {}", id);
        try {
            // Xác thực đơn hàng tồn tại và thuộc org hiện tại
            PlanningOrder order = planningOrderMapper.selectById(id);
            if (order == null) {
                return Result.error("Không tìm thấy đơn hàng với ID: " + id);
            }

            // Kiểm tra data isolation
            LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (loginUser != null && loginUser.getOrgCode() != null
                    && order.getSysOrgCode() != null
                    && !order.getSysOrgCode().equals(loginUser.getOrgCode())) {
                return Result.error("Không có quyền truy cập đơn hàng này");
            }

            MaterialAvailabilityResult result = materialAvailabilityService.checkMaterialAvailability(id);
            if (!result.isSuccess()) {
                return Result.OK("Kiểm tra nguyên vật liệu thất bại: " + result.getErrorMessage(), result);
            }
            return Result.OK(result);
        } catch (Exception e) {
            log.error("[PlanningAgent] Lỗi kiểm tra nguyên vật liệu cho đơn hàng {}: {}", id, e.getMessage(), e);
            return Result.error("Lỗi kiểm tra nguyên vật liệu: " + e.getMessage());
        }
    }

    /**
     * Tạo Purchase Request cho nguyên vật liệu thiếu hụt.
     */
    @PostMapping("/procurement/pr")
    @AutoLog(value = "Planning Agent - Tạo Purchase Request")
    @Operation(summary = "Tạo Purchase Request",
            description = "Tạo yêu cầu mua hàng cho nguyên vật liệu thiếu hụt với ngày giao hàng tính từ lịch sản xuất")
    public Result<PurchaseRequest> generatePurchaseRequest(@Valid @RequestBody GeneratePrRequest request) {
        log.info("[PlanningAgent] Tạo PR cho đơn hàng: {}, nguyên vật liệu: {}, số lượng: {}",
                request.getOrderId(), request.getMaterialId(), request.getDeficitQty());
        try {
            // Xác thực đơn hàng tồn tại và thuộc org hiện tại
            PlanningOrder order = planningOrderMapper.selectById(request.getOrderId());
            if (order == null) {
                return Result.error("Không tìm thấy đơn hàng với ID: " + request.getOrderId());
            }

            // Kiểm tra data isolation
            LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (loginUser != null && loginUser.getOrgCode() != null
                    && order.getSysOrgCode() != null
                    && !order.getSysOrgCode().equals(loginUser.getOrgCode())) {
                return Result.error("Không có quyền truy cập đơn hàng này");
            }

            PurchaseRequest pr = procurementCoordinationService.generatePurchaseRequest(
                    request.getOrderId(),
                    request.getMaterialId(),
                    request.getDeficitQty(),
                    request.getProductionStartDate()
            );
            return Result.OK(pr);
        } catch (Exception e) {
            log.error("[PlanningAgent] Lỗi tạo PR: {}", e.getMessage(), e);
            return Result.error("Lỗi tạo Purchase Request: " + e.getMessage());
        }
    }

    /**
     * Lấy các phương án thay thế khi lead time vượt deadline.
     */
    @GetMapping("/procurement/alternatives/{orderId}")
    @AutoLog(value = "Planning Agent - Lấy phương án thay thế")
    @Operation(summary = "Lấy phương án thay thế cho đơn hàng",
            description = "Trả về ít nhất 2 phương án thay thế khi thời gian giao hàng vượt deadline: vận chuyển nhanh, nhà cung cấp thay thế, điều chỉnh lịch")
    public Result<String> getAlternativeScenarios(@PathVariable("orderId") String orderId) {
        log.info("[PlanningAgent] Lấy phương án thay thế cho đơn hàng: {}", orderId);
        try {
            // Xác thực đơn hàng tồn tại và thuộc org hiện tại
            PlanningOrder order = planningOrderMapper.selectById(orderId);
            if (order == null) {
                return Result.error("Không tìm thấy đơn hàng với ID: " + orderId);
            }

            // Kiểm tra data isolation
            LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (loginUser != null && loginUser.getOrgCode() != null
                    && order.getSysOrgCode() != null
                    && !order.getSysOrgCode().equals(loginUser.getOrgCode())) {
                return Result.error("Không có quyền truy cập đơn hàng này");
            }

            // Chuyển đổi deadline từ Date sang LocalDate
            LocalDate deadline = order.getDeadline().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            String alternatives = procurementCoordinationService.generateAlternatives(orderId, deadline);
            return Result.OK(alternatives);
        } catch (Exception e) {
            log.error("[PlanningAgent] Lỗi lấy phương án thay thế cho đơn hàng {}: {}", orderId, e.getMessage(), e);
            return Result.error("Lỗi lấy phương án thay thế: " + e.getMessage());
        }
    }
}
