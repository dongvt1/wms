package com.cy.modules.planning.agent.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cy.modules.planning.agent.dto.FulfillmentDashboardDto;
import com.cy.modules.planning.agent.entity.ProductionProgress;
import com.cy.modules.planning.agent.entity.WeeklyPlan;
import com.cy.modules.planning.agent.mapper.ProductionProgressMapper;
import com.cy.modules.planning.agent.mapper.WeeklyPlanMapper;
import com.cy.modules.planning.agent.service.FinishedGoodsDispatchService;
import com.cy.modules.planning.agent.service.ProductionExecutionMonitor;
import com.cy.modules.planning.agent.service.ProductionOrderIssuanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * @Description: Controller quản lý thực thi sản xuất (Execution)
 * @Author: AI Planning Agent
 * @Date: 2026-06-20
 * @Version: V1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/planning-agent/execution")
@Tag(name = "Planning Agent - Execution", description = "API quản lý thực thi sản xuất: phát lệnh, giám sát tiến độ, kết quả hàng ngày, dashboard hoàn thành")
public class ExecutionController {

    @Resource
    private ProductionOrderIssuanceService productionOrderIssuanceService;

    @Resource
    private ProductionExecutionMonitor productionExecutionMonitor;

    @Resource
    private FinishedGoodsDispatchService finishedGoodsDispatchService;

    @Resource
    private WeeklyPlanMapper weeklyPlanMapper;

    @Resource
    private ProductionProgressMapper productionProgressMapper;

    /**
     * Phát lệnh sản xuất cho kế hoạch tuần đã được duyệt.
     * Tạo Production Orders trong ERP cho tất cả batch trong kế hoạch tuần,
     * kích hoạt xuất kho nguyên vật liệu theo BOM.
     *
     * @param weeklyPlanId ID kế hoạch tuần đã duyệt
     */
    @PostMapping("/production-orders/{weeklyPlanId}")
    @AutoLog(value = "Planning Agent - Phát lệnh sản xuất")
    @Operation(summary = "Phát lệnh sản xuất",
            description = "Phát lệnh sản xuất cho kế hoạch tuần đã duyệt. Tạo Production Orders trong ERP và kích hoạt xuất kho nguyên vật liệu.")
    public Result<String> issueProductionOrders(@PathVariable("weeklyPlanId") String weeklyPlanId) {
        log.info("[Execution] Nhận yêu cầu phát lệnh sản xuất cho kế hoạch tuần: {}", weeklyPlanId);

        // Kiểm tra kế hoạch tuần tồn tại
        WeeklyPlan weeklyPlan = weeklyPlanMapper.selectById(weeklyPlanId);
        if (weeklyPlan == null) {
            return Result.error("Không tìm thấy kế hoạch tuần với ID: " + weeklyPlanId);
        }

        // Kiểm tra trạng thái kế hoạch phải là "approved"
        if (!"approved".equals(weeklyPlan.getStatus())) {
            return Result.error("Kế hoạch tuần chưa được duyệt. Trạng thái hiện tại: " + weeklyPlan.getStatus());
        }

        try {
            productionOrderIssuanceService.issueProductionOrders(weeklyPlanId);
            return Result.OK("Phát lệnh sản xuất thành công cho kế hoạch tuần: " + weeklyPlan.getPlanCode());
        } catch (Exception e) {
            log.error("[Execution] Lỗi khi phát lệnh sản xuất cho kế hoạch tuần {}: {}",
                    weeklyPlanId, e.getMessage(), e);
            return Result.error("Lỗi khi phát lệnh sản xuất: " + e.getMessage());
        }
    }

    /**
     * Lấy tiến độ thực thi sản xuất cho kế hoạch tuần.
     * Trả về danh sách bản ghi tiến độ sản xuất hàng ngày cho tất cả batch trong kế hoạch.
     *
     * @param weeklyPlanId ID kế hoạch tuần
     */
    @GetMapping("/progress/{weeklyPlanId}")
    @AutoLog(value = "Planning Agent - Xem tiến độ thực thi")
    @Operation(summary = "Lấy tiến độ thực thi sản xuất",
            description = "Trả về danh sách bản ghi tiến độ sản xuất hàng ngày cho kế hoạch tuần, bao gồm số lượng sản xuất, tỷ lệ lỗi, phần trăm hoàn thành.")
    public Result<List<ProductionProgress>> getExecutionProgress(@PathVariable("weeklyPlanId") String weeklyPlanId) {
        log.info("[Execution] Truy vấn tiến độ thực thi cho kế hoạch tuần: {}", weeklyPlanId);

        // Kiểm tra kế hoạch tuần tồn tại
        WeeklyPlan weeklyPlan = weeklyPlanMapper.selectById(weeklyPlanId);
        if (weeklyPlan == null) {
            return Result.error("Không tìm thấy kế hoạch tuần với ID: " + weeklyPlanId);
        }

        LambdaQueryWrapper<ProductionProgress> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductionProgress::getWeeklyPlanId, weeklyPlanId)
                .orderByDesc(ProductionProgress::getReportDate)
                .orderByAsc(ProductionProgress::getProductionLineId);

        List<ProductionProgress> progressList = productionProgressMapper.selectList(queryWrapper);
        return Result.OK(progressList);
    }

    /**
     * Lấy kết quả sản xuất hàng ngày theo ngày.
     * Trả về tất cả bản ghi tiến độ sản xuất cho ngày được chỉ định,
     * bao gồm số lượng sản xuất, tỷ lệ lỗi, phần trăm hoàn thành so với kế hoạch.
     *
     * @param date ngày cần truy vấn (format: yyyy-MM-dd)
     */
    @GetMapping("/daily-results/{date}")
    @AutoLog(value = "Planning Agent - Xem kết quả sản xuất hàng ngày")
    @Operation(summary = "Lấy kết quả sản xuất hàng ngày",
            description = "Trả về kết quả sản xuất cho ngày được chỉ định, bao gồm số lượng, tỷ lệ lỗi, phần trăm hoàn thành.")
    public Result<List<ProductionProgress>> getDailyResults(
            @PathVariable("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        log.info("[Execution] Truy vấn kết quả sản xuất hàng ngày cho ngày: {}", date);

        Date reportDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());

        LambdaQueryWrapper<ProductionProgress> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductionProgress::getReportDate, reportDate)
                .orderByAsc(ProductionProgress::getProductionLineId)
                .orderByAsc(ProductionProgress::getBatchId);

        List<ProductionProgress> dailyResults = productionProgressMapper.selectList(queryWrapper);

        if (dailyResults.isEmpty()) {
            return Result.OK("Không có dữ liệu sản xuất cho ngày: " + date, dailyResults);
        }
        return Result.OK(dailyResults);
    }

    /**
     * Lấy dashboard hoàn thành đơn hàng.
     * Trả về dữ liệu tổng hợp bao gồm: số lượng sản xuất, tồn kho, đã giao,
     * phần trăm hoàn thành cho mỗi đơn hàng.
     */
    @GetMapping("/fulfillment/dashboard")
    @AutoLog(value = "Planning Agent - Xem dashboard hoàn thành đơn hàng")
    @Operation(summary = "Lấy dashboard hoàn thành đơn hàng",
            description = "Trả về dữ liệu dashboard bao gồm số lượng sản xuất, tồn kho, đã giao, phần trăm hoàn thành cho mỗi đơn hàng.")
    public Result<List<FulfillmentDashboardDto>> getFulfillmentDashboard() {
        log.info("[Execution] Truy vấn dashboard hoàn thành đơn hàng");

        try {
            List<FulfillmentDashboardDto> dashboardData = finishedGoodsDispatchService.getDashboardData();
            return Result.OK(dashboardData);
        } catch (Exception e) {
            log.error("[Execution] Lỗi khi lấy dữ liệu dashboard: {}", e.getMessage(), e);
            return Result.error("Lỗi khi lấy dữ liệu dashboard: " + e.getMessage());
        }
    }
}
