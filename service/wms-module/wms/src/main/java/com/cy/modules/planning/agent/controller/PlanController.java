package com.cy.modules.planning.agent.controller;

import com.cy.modules.planning.agent.entity.MonthlyPlan;
import com.cy.modules.planning.agent.entity.OptimizationScore;
import com.cy.modules.planning.agent.entity.QuarterlyPlan;
import com.cy.modules.planning.agent.entity.RescheduleRecord;
import com.cy.modules.planning.agent.entity.WeeklyPlan;
import com.cy.modules.planning.agent.event.PlanApprovedEvent;
import com.cy.modules.planning.agent.mapper.QuarterlyPlanMapper;
import com.cy.modules.planning.agent.service.PlanOptimizationService;
import com.cy.modules.planning.agent.service.QuarterlyPlanService;
import com.cy.modules.planning.agent.service.ReschedulingService;
import com.cy.modules.planning.agent.service.StalenessManagementService;
import com.cy.modules.planning.agent.service.WeeklyPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * @Description: Controller quản lý kế hoạch sản xuất (quý, tháng, tuần)
 * @Author: AI Planning Agent
 * @Date: 2026-06-20
 * @Version: V1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/planning-agent/plans")
@Tag(name = "Planning Agent - Plans", description = "API quản lý kế hoạch sản xuất quý, tháng, tuần")
public class PlanController {

    @Resource
    private QuarterlyPlanService quarterlyPlanService;

    @Resource
    private WeeklyPlanService weeklyPlanService;

    @Resource
    private PlanOptimizationService planOptimizationService;

    @Resource
    private ReschedulingService reschedulingService;

    @Resource
    private StalenessManagementService stalenessManagementService;

    @Resource
    private QuarterlyPlanMapper quarterlyPlanMapper;

    @Resource
    private ApplicationEventPublisher eventPublisher;

    // ==================== Quarterly Plan ====================

    /**
     * Tạo kế hoạch quý: phân loại nhu cầu theo loại sản phẩm cho từng tháng,
     * xác nhận công suất, tạo phương án thay thế nếu nhu cầu vượt công suất.
     *
     * @param year    năm kế hoạch
     * @param quarter quý (1-4)
     * @return kế hoạch quý đã tạo
     */
    @PostMapping("/quarterly")
    @Operation(summary = "Tạo kế hoạch quý",
            description = "Phân loại nhu cầu sản xuất theo loại sản phẩm cho từng tháng trong quý, xác nhận công suất")
    public Result<QuarterlyPlan> generateQuarterlyPlan(
            @RequestParam("year") int year,
            @RequestParam("quarter") int quarter) {

        if (quarter < 1 || quarter > 4) {
            return Result.error("Quý không hợp lệ: " + quarter + ". Giá trị hợp lệ: 1-4");
        }

        if (stalenessManagementService.isPlanningBlocked()) {
            return Result.error("Không thể tạo kế hoạch: dữ liệu đồng bộ quá cũ (>60 phút). Vui lòng đồng bộ lại trước.");
        }

        log.info("[PlanController] Tạo kế hoạch quý: year={}, quarter={}", year, quarter);

        try {
            QuarterlyPlan plan = quarterlyPlanService.generateQuarterlyPlan(year, quarter);
            return Result.OK("Tạo kế hoạch quý thành công", plan);
        } catch (Exception e) {
            log.error("[PlanController] Lỗi khi tạo kế hoạch quý: {}", e.getMessage(), e);
            return Result.error("Lỗi khi tạo kế hoạch quý: " + e.getMessage());
        }
    }

    /**
     * Lấy chi tiết kế hoạch quý theo ID.
     *
     * @param id ID kế hoạch quý
     * @return chi tiết kế hoạch quý
     */
    @GetMapping("/quarterly/{id}")
    @Operation(summary = "Lấy chi tiết kế hoạch quý",
            description = "Trả về thông tin chi tiết kế hoạch quý bao gồm demand summary và capacity gaps")
    public Result<QuarterlyPlan> getQuarterlyPlan(@PathVariable("id") String id) {
        log.info("[PlanController] Lấy chi tiết kế hoạch quý: id={}", id);

        QuarterlyPlan plan = quarterlyPlanMapper.selectById(id);
        if (plan == null) {
            return Result.error("Không tìm thấy kế hoạch quý với ID: " + id);
        }
        return Result.OK(plan);
    }

    // ==================== Monthly Plan ====================

    /**
     * Tạo phương án kế hoạch tháng (1-3 phương án xếp hạng) từ kế hoạch quý.
     *
     * @param quarterlyPlanId ID kế hoạch quý
     * @param year            năm
     * @param month           tháng (1-12)
     * @return danh sách phương án kế hoạch tháng
     */
    @PostMapping("/monthly")
    @Operation(summary = "Tạo phương án kế hoạch tháng",
            description = "Tạo 1-3 phương án kế hoạch tháng xếp hạng với số lượng, timeline, dây chuyền, ngày hoàn thành")
    public Result<List<MonthlyPlan>> generateMonthlyPlanOptions(
            @RequestParam("quarterlyPlanId") String quarterlyPlanId,
            @RequestParam("year") int year,
            @RequestParam("month") int month) {

        if (month < 1 || month > 12) {
            return Result.error("Tháng không hợp lệ: " + month + ". Giá trị hợp lệ: 1-12");
        }

        if (stalenessManagementService.isPlanningBlocked()) {
            return Result.error("Không thể tạo kế hoạch: dữ liệu đồng bộ quá cũ (>60 phút). Vui lòng đồng bộ lại trước.");
        }

        log.info("[PlanController] Tạo phương án kế hoạch tháng: quarterlyPlanId={}, year={}, month={}",
                quarterlyPlanId, year, month);

        try {
            List<MonthlyPlan> options = quarterlyPlanService.generateMonthlyPlanOptions(quarterlyPlanId, year, month);
            return Result.OK("Tạo phương án kế hoạch tháng thành công", options);
        } catch (Exception e) {
            log.error("[PlanController] Lỗi khi tạo phương án kế hoạch tháng: {}", e.getMessage(), e);
            return Result.error("Lỗi khi tạo phương án kế hoạch tháng: " + e.getMessage());
        }
    }

    /**
     * Duyệt kế hoạch tháng: đặt status='approved', từ chối các phương án khác cùng tháng.
     *
     * @param id ID kế hoạch tháng được duyệt
     * @return kế hoạch tháng đã duyệt
     */
    @PutMapping("/monthly/{id}/approve")
    @Operation(summary = "Duyệt kế hoạch tháng",
            description = "Duyệt phương án kế hoạch tháng được chọn, từ chối các phương án khác cùng tháng")
    public Result<MonthlyPlan> approveMonthlyPlan(@PathVariable("id") String id) {
        log.info("[PlanController] Duyệt kế hoạch tháng: id={}", id);

        try {
            MonthlyPlan approved = quarterlyPlanService.approveMonthlyPlan(id);

            // Publish PlanApprovedEvent
            String approvedBy = approved.getApprovedBy() != null ? approved.getApprovedBy() : "system";
            eventPublisher.publishEvent(new PlanApprovedEvent(this, "monthly", id, approvedBy));

            return Result.OK("Duyệt kế hoạch tháng thành công", approved);
        } catch (Exception e) {
            log.error("[PlanController] Lỗi khi duyệt kế hoạch tháng: {}", e.getMessage(), e);
            return Result.error("Lỗi khi duyệt kế hoạch tháng: " + e.getMessage());
        }
    }

    // ==================== Weekly Plan ====================

    /**
     * Tạo kế hoạch tuần từ kế hoạch tháng đã duyệt.
     * Phân rã thành kế hoạch tuần chi tiết với product, quantity, timeline, line, machine.
     *
     * @param monthlyPlanId ID kế hoạch tháng đã duyệt
     * @return danh sách kế hoạch tuần đã tạo
     */
    @PostMapping("/weekly")
    @Operation(summary = "Tạo kế hoạch tuần",
            description = "Phân rã kế hoạch tháng đã duyệt thành kế hoạch tuần chi tiết với gán dây chuyền và máy")
    public Result<List<WeeklyPlan>> generateWeeklyPlans(
            @RequestParam("monthlyPlanId") String monthlyPlanId) {

        if (stalenessManagementService.isPlanningBlocked()) {
            return Result.error("Không thể tạo kế hoạch: dữ liệu đồng bộ quá cũ (>60 phút). Vui lòng đồng bộ lại trước.");
        }

        log.info("[PlanController] Tạo kế hoạch tuần từ kế hoạch tháng: monthlyPlanId={}", monthlyPlanId);

        try {
            List<WeeklyPlan> weeklyPlans = weeklyPlanService.generateWeeklyPlans(monthlyPlanId);
            return Result.OK("Tạo kế hoạch tuần thành công", weeklyPlans);
        } catch (Exception e) {
            log.error("[PlanController] Lỗi khi tạo kế hoạch tuần: {}", e.getMessage(), e);
            return Result.error("Lỗi khi tạo kế hoạch tuần: " + e.getMessage());
        }
    }

    /**
     * Duyệt kế hoạch tuần: đặt status='approved', ghi nhận approved_by và approved_time.
     *
     * @param id ID kế hoạch tuần cần duyệt
     * @return kế hoạch tuần đã duyệt
     */
    @PutMapping("/weekly/{id}/approve")
    @Operation(summary = "Duyệt kế hoạch tuần",
            description = "Duyệt kế hoạch tuần, ghi nhận người duyệt và thời điểm duyệt")
    public Result<WeeklyPlan> approveWeeklyPlan(@PathVariable("id") String id) {
        log.info("[PlanController] Duyệt kế hoạch tuần: id={}", id);

        try {
            WeeklyPlan approved = weeklyPlanService.approveWeeklyPlan(id);

            // Publish PlanApprovedEvent - triggers production order issuance for weekly plans
            String approvedBy = approved.getApprovedBy() != null ? approved.getApprovedBy() : "system";
            eventPublisher.publishEvent(new PlanApprovedEvent(this, "weekly", id, approvedBy));

            return Result.OK("Duyệt kế hoạch tuần thành công", approved);
        } catch (Exception e) {
            log.error("[PlanController] Lỗi khi duyệt kế hoạch tuần: {}", e.getMessage(), e);
            return Result.error("Lỗi khi duyệt kế hoạch tuần: " + e.getMessage());
        }
    }

    /**
     * Lấy chi tiết điểm tối ưu của kế hoạch tuần.
     *
     * @param id ID kế hoạch tuần
     * @return chi tiết điểm tối ưu
     */
    @GetMapping("/weekly/{id}/optimization")
    @Operation(summary = "Lấy chi tiết tối ưu hóa",
            description = "Trả về chi tiết điểm tối ưu bao gồm các trọng số, điểm thành phần, và vi phạm ràng buộc")
    public Result<OptimizationScore> getOptimizationDetails(@PathVariable("id") String id) {
        log.info("[PlanController] Lấy chi tiết tối ưu hóa: weeklyPlanId={}", id);

        try {
            OptimizationScore score = planOptimizationService.optimizeWeeklyPlan(id);
            return Result.OK(score);
        } catch (Exception e) {
            log.error("[PlanController] Lỗi khi lấy chi tiết tối ưu hóa: {}", e.getMessage(), e);
            return Result.error("Lỗi khi lấy chi tiết tối ưu hóa: " + e.getMessage());
        }
    }

    /**
     * Lấy các phương án điều chỉnh kế hoạch (rescheduling options) cho kế hoạch tuần.
     *
     * @param id ID kế hoạch tuần
     * @return danh sách phương án điều chỉnh
     */
    @GetMapping("/weekly/{id}/reschedule-options")
    @Operation(summary = "Lấy phương án điều chỉnh kế hoạch",
            description = "Trả về ≥2 phương án điều chỉnh xếp hạng theo optimization score với ảnh hưởng đến delivery dates")
    public Result<List<RescheduleRecord>> getRescheduleOptions(@PathVariable("id") String id) {
        log.info("[PlanController] Lấy phương án điều chỉnh: weeklyPlanId={}", id);

        try {
            List<RescheduleRecord> options = reschedulingService.getReschedulingOptions(id);
            if (options == null || options.isEmpty()) {
                return Result.OK("Không có phương án điều chỉnh nào cho kế hoạch tuần này", options);
            }
            return Result.OK(options);
        } catch (Exception e) {
            log.error("[PlanController] Lỗi khi lấy phương án điều chỉnh: {}", e.getMessage(), e);
            return Result.error("Lỗi khi lấy phương án điều chỉnh: " + e.getMessage());
        }
    }
}
