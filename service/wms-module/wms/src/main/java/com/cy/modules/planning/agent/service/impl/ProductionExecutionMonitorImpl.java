package com.cy.modules.planning.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cy.modules.planning.agent.client.ErpClient;
import com.cy.modules.planning.agent.client.ScadaClient;
import com.cy.modules.planning.agent.dto.MachineStatus;
import com.cy.modules.planning.agent.dto.WarehouseReceiptRequest;
import com.cy.modules.planning.agent.entity.ProductionProgress;
import com.cy.modules.planning.agent.entity.WeeklyPlan;
import com.cy.modules.planning.agent.entity.WeeklyPlanBatch;
import com.cy.modules.planning.agent.enums.BatchStatus;
import com.cy.modules.planning.agent.enums.NotificationType;
import com.cy.modules.planning.agent.enums.PlanStatus;
import com.cy.modules.planning.agent.mapper.ProductionProgressMapper;
import com.cy.modules.planning.agent.mapper.WeeklyPlanBatchMapper;
import com.cy.modules.planning.agent.mapper.WeeklyPlanMapper;
import com.cy.modules.planning.agent.service.MachineSyncService;
import com.cy.modules.planning.agent.service.PlanningNotificationService;
import com.cy.modules.planning.agent.service.ProductionExecutionMonitor;
import com.cy.modules.planning.agent.service.ReschedulingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Implementation của ProductionExecutionMonitor.
 * Thu thập tiến độ sản xuất từ Scada mỗi 5 phút, tính toán kết quả hàng ngày,
 * ghi nhận thành phẩm nhập kho (retry 3 lần), và tạo yêu cầu trả NVL dư.
 */
@Slf4j
@Service
public class ProductionExecutionMonitorImpl implements ProductionExecutionMonitor {

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int CONSECUTIVE_FAILURE_THRESHOLD = 2;

    /** Đếm số lần thu thập Scada thất bại liên tiếp */
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);

    /** Thời điểm thu thập Scada thành công gần nhất */
    private volatile LocalDateTime lastSuccessfulCollection = null;

    @Autowired
    private ScadaClient scadaClient;

    @Autowired
    private ErpClient erpClient;

    @Autowired
    private WeeklyPlanMapper weeklyPlanMapper;

    @Autowired
    private WeeklyPlanBatchMapper weeklyPlanBatchMapper;

    @Autowired
    private ProductionProgressMapper productionProgressMapper;

    @Autowired
    private MachineSyncService machineSyncService;

    @Autowired
    private ReschedulingService reschedulingService;

    @Autowired
    private PlanningNotificationService planningNotificationService;

    // ==================== collectProgress ====================

    @Override
    @Scheduled(fixedRate = 300000) // 5 phút = 300,000 ms
    public void collectProgress() {
        log.info("[ExecutionMonitor] Bắt đầu thu thập tiến độ sản xuất từ Scada");

        // Lấy danh sách batch đang hoạt động (in_progress)
        LambdaQueryWrapper<WeeklyPlanBatch> batchWrapper = new LambdaQueryWrapper<>();
        batchWrapper.eq(WeeklyPlanBatch::getStatus, BatchStatus.IN_PROGRESS.getValue());
        List<WeeklyPlanBatch> activeBatches = weeklyPlanBatchMapper.selectList(batchWrapper);

        if (activeBatches.isEmpty()) {
            log.debug("[ExecutionMonitor] Không có batch nào đang hoạt động");
            return;
        }

        // Lấy danh sách dây chuyền duy nhất
        List<String> lineIds = activeBatches.stream()
                .map(WeeklyPlanBatch::getProductionLineId)
                .distinct()
                .collect(Collectors.toList());

        try {
            // Thu thập trạng thái máy từ Scada
            List<MachineStatus> machineStatuses = scadaClient.getMachineStatuses(lineIds);

            // Thu thập tiến độ sản xuất cho từng dây chuyền
            LocalDate today = LocalDate.now();
            for (String lineId : lineIds) {
                com.cy.modules.planning.agent.dto.ProductionProgress progress =
                        scadaClient.getProductionProgress(lineId, today);

                if (progress != null && progress.getBatches() != null) {
                    saveProgressData(progress, activeBatches, machineStatuses, today);
                }
            }

            // Reset bộ đếm thất bại
            consecutiveFailures.set(0);
            lastSuccessfulCollection = LocalDateTime.now();
            log.info("[ExecutionMonitor] Thu thập tiến độ thành công lúc {}", lastSuccessfulCollection);

        } catch (Exception e) {
            int failures = consecutiveFailures.incrementAndGet();
            log.error("[ExecutionMonitor] Thu thập Scada thất bại (lần thứ {} liên tiếp): {}",
                    failures, e.getMessage());

            // Cảnh báo khi 2 lần liên tiếp thất bại
            if (failures >= CONSECUTIVE_FAILURE_THRESHOLD) {
                alertScadaCollectionFailure(failures);
            }
        }
    }

    // ==================== calculateDailyResults ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void calculateDailyResults(String weeklyPlanId, LocalDate date) {
        log.info("[ExecutionMonitor] Tính toán kết quả hàng ngày cho weeklyPlanId={}, date={}", weeklyPlanId, date);

        // Lấy danh sách batch của kế hoạch tuần
        LambdaQueryWrapper<WeeklyPlanBatch> batchWrapper = new LambdaQueryWrapper<>();
        batchWrapper.eq(WeeklyPlanBatch::getWeeklyPlanId, weeklyPlanId);
        List<WeeklyPlanBatch> batches = weeklyPlanBatchMapper.selectList(batchWrapper);

        if (batches.isEmpty()) {
            log.warn("[ExecutionMonitor] Không có batch nào cho weeklyPlanId={}", weeklyPlanId);
            return;
        }

        Date reportDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());

        for (WeeklyPlanBatch batch : batches) {
            // Kiểm tra xem đã có record cho ngày này chưa
            LambdaQueryWrapper<ProductionProgress> existWrapper = new LambdaQueryWrapper<>();
            existWrapper.eq(ProductionProgress::getBatchId, batch.getId())
                    .eq(ProductionProgress::getReportDate, reportDate);
            ProductionProgress existing = productionProgressMapper.selectOne(existWrapper);

            if (existing == null) {
                existing = new ProductionProgress();
                existing.setWeeklyPlanId(weeklyPlanId);
                existing.setBatchId(batch.getId());
                existing.setProductionLineId(batch.getProductionLineId());
                existing.setReportDate(reportDate);
                existing.setPlannedQty(batch.getQuantity());
                existing.setCreateTime(new Date());
            }

            // Tính toán actual_qty từ dữ liệu Scada đã thu thập
            BigDecimal actualQty = existing.getActualQty() != null ? existing.getActualQty() : BigDecimal.ZERO;
            BigDecimal defectQty = existing.getDefectQty() != null ? existing.getDefectQty() : BigDecimal.ZERO;
            BigDecimal plannedQty = batch.getQuantity();

            // Tính tỷ lệ lỗi: defect_qty / actual_qty
            BigDecimal defectRate = BigDecimal.ZERO;
            if (actualQty.compareTo(BigDecimal.ZERO) > 0) {
                defectRate = defectQty.divide(actualQty, 4, RoundingMode.HALF_UP);
            }

            // Tính phần trăm hoàn thành: actual_qty / planned_qty × 100
            BigDecimal completionPct = BigDecimal.ZERO;
            if (plannedQty.compareTo(BigDecimal.ZERO) > 0) {
                completionPct = actualQty.divide(plannedQty, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);
            }

            // Tính phần trăm sai lệch: ((actual - planned) / planned) × 100
            BigDecimal deviationPct = BigDecimal.ZERO;
            if (plannedQty.compareTo(BigDecimal.ZERO) > 0) {
                deviationPct = actualQty.subtract(plannedQty)
                        .divide(plannedQty, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);
            }

            existing.setDefectRate(defectRate);
            existing.setCompletionPct(completionPct);
            existing.setDeviationPct(deviationPct);

            if (existing.getId() == null) {
                productionProgressMapper.insert(existing);
            } else {
                productionProgressMapper.updateById(existing);
            }

            log.debug("[ExecutionMonitor] Batch={}: actualQty={}, defectRate={}, completionPct={}, deviationPct={}",
                    batch.getId(), actualQty, defectRate, completionPct, deviationPct);
        }

        log.info("[ExecutionMonitor] Hoàn thành tính toán kết quả hàng ngày cho weeklyPlanId={}", weeklyPlanId);
    }

    // ==================== recordFinishedGoods ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordFinishedGoods(String batchId, BigDecimal quantity) {
        log.info("[ExecutionMonitor] Ghi nhận thành phẩm: batchId={}, quantity={}", batchId, quantity);

        WeeklyPlanBatch batch = weeklyPlanBatchMapper.selectById(batchId);
        if (batch == null) {
            log.error("[ExecutionMonitor] Không tìm thấy batch: {}", batchId);
            return;
        }

        // Cập nhật actual_quantity và trạng thái batch
        batch.setActualQuantity(quantity);
        batch.setActualEnd(new Date());
        batch.setStatus(BatchStatus.COMPLETED.getValue());
        weeklyPlanBatchMapper.updateById(batch);

        // Kích hoạt nhập kho trong ERP với retry
        WarehouseReceiptRequest receiptRequest = WarehouseReceiptRequest.builder()
                .batchId(batchId)
                .productId(batch.getProductType())
                .productType(batch.getProductType())
                .quantity(quantity)
                .productionLineId(batch.getProductionLineId())
                .completionTime(LocalDateTime.now())
                .build();

        boolean success = recordWarehouseReceiptWithRetry(receiptRequest);
        if (!success) {
            // Thông báo quản lý sản xuất sau 3 lần thất bại
            Map<String, Object> data = new HashMap<>();
            data.put("batchId", batchId);
            data.put("productType", batch.getProductType());
            data.put("quantity", quantity);
            data.put("reason", "Nhập kho thành phẩm thất bại sau 3 lần thử");

            planningNotificationService.notifyProductionManager(
                    NotificationType.SYSTEM_ERROR,
                    String.format("Nhập kho thất bại cho batch %s (sản phẩm: %s, SL: %s)",
                            batchId, batch.getProductType(), quantity),
                    data
            );
        }
    }

    // ==================== generateMaterialReturn ====================

    @Override
    public void generateMaterialReturn(String batchId) {
        log.info("[ExecutionMonitor] Kiểm tra trả NVL dư cho batchId={}", batchId);

        WeeklyPlanBatch batch = weeklyPlanBatchMapper.selectById(batchId);
        if (batch == null) {
            log.error("[ExecutionMonitor] Không tìm thấy batch: {}", batchId);
            return;
        }

        // Tính số lượng NVL còn lại = gross_quantity - actual_quantity (đã sử dụng)
        BigDecimal grossQty = batch.getGrossQuantity() != null ? batch.getGrossQuantity() : batch.getQuantity();
        BigDecimal actualQty = batch.getActualQuantity() != null ? batch.getActualQuantity() : BigDecimal.ZERO;
        BigDecimal remaining = grossQty.subtract(actualQty);

        // Mức tối thiểu có thể trả (configurable, mặc định = 1)
        BigDecimal minimumReturnableQty = BigDecimal.ONE;

        if (remaining.compareTo(minimumReturnableQty) > 0) {
            log.info("[ExecutionMonitor] NVL dư ({}) vượt mức tối thiểu ({}). Tạo yêu cầu trả NVL cho batch={}",
                    remaining, minimumReturnableQty, batchId);

            // Tạo yêu cầu trả NVL qua WMS (sử dụng ErpClient vì WMS tích hợp trong ERP)
            try {
                WarehouseReceiptRequest returnRequest = WarehouseReceiptRequest.builder()
                        .batchId(batchId)
                        .productId(batch.getProductType())
                        .productType(batch.getProductType())
                        .quantity(remaining)
                        .productionLineId(batch.getProductionLineId())
                        .completionTime(LocalDateTime.now())
                        .targetWarehouse("MATERIAL_RETURN")
                        .build();

                erpClient.recordWarehouseReceipt(returnRequest);
                log.info("[ExecutionMonitor] Yêu cầu trả NVL dư đã được gửi thành công cho batch={}", batchId);
            } catch (Exception e) {
                log.error("[ExecutionMonitor] Gửi yêu cầu trả NVL thất bại cho batch={}: {}", batchId, e.getMessage());
            }
        } else {
            log.debug("[ExecutionMonitor] NVL dư ({}) không vượt mức tối thiểu ({}) cho batch={}",
                    remaining, minimumReturnableQty, batchId);
        }
    }

    // ==================== Private Methods ====================

    /**
     * Lưu dữ liệu tiến độ từ Scada vào bảng ap_production_progress.
     */
    private void saveProgressData(com.cy.modules.planning.agent.dto.ProductionProgress progress,
                                  List<WeeklyPlanBatch> activeBatches,
                                  List<MachineStatus> machineStatuses,
                                  LocalDate today) {
        Date reportDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());

        // Map machine status theo lineId
        Map<String, String> statusByLine = machineStatuses.stream()
                .collect(Collectors.toMap(
                        MachineStatus::getLineId,
                        MachineStatus::getStatus,
                        (a, b) -> b
                ));

        // Lọc batch thuộc dây chuyền này
        List<WeeklyPlanBatch> lineBatches = activeBatches.stream()
                .filter(b -> b.getProductionLineId().equals(progress.getLineId()))
                .collect(Collectors.toList());

        for (WeeklyPlanBatch batch : lineBatches) {
            // Tìm dữ liệu batch tương ứng từ Scada
            BigDecimal producedQty = BigDecimal.ZERO;
            if (progress.getBatches() != null) {
                producedQty = progress.getBatches().stream()
                        .filter(bp -> batch.getId().equals(bp.getBatchId()))
                        .map(com.cy.modules.planning.agent.dto.ProductionProgress.BatchProgress::getProducedQuantity)
                        .findFirst()
                        .orElse(BigDecimal.ZERO);
            }

            // Upsert vào ap_production_progress
            LambdaQueryWrapper<ProductionProgress> existWrapper = new LambdaQueryWrapper<>();
            existWrapper.eq(ProductionProgress::getBatchId, batch.getId())
                    .eq(ProductionProgress::getReportDate, reportDate);
            ProductionProgress existing = productionProgressMapper.selectOne(existWrapper);

            if (existing == null) {
                existing = new ProductionProgress();
                existing.setWeeklyPlanId(batch.getWeeklyPlanId());
                existing.setBatchId(batch.getId());
                existing.setProductionLineId(batch.getProductionLineId());
                existing.setReportDate(reportDate);
                existing.setPlannedQty(batch.getQuantity());
                existing.setCreateTime(new Date());
            }

            existing.setActualQty(producedQty);
            existing.setDefectQty(progress.getDefectCount() != null ? progress.getDefectCount() : BigDecimal.ZERO);
            existing.setMachineStatus(statusByLine.getOrDefault(progress.getLineId(), "unknown"));

            if (existing.getId() == null) {
                productionProgressMapper.insert(existing);
            } else {
                productionProgressMapper.updateById(existing);
            }
        }
    }

    /**
     * Nhập kho thành phẩm với retry logic: 3 lần thử.
     *
     * @return true nếu thành công, false nếu thất bại sau 3 lần
     */
    private boolean recordWarehouseReceiptWithRetry(WarehouseReceiptRequest request) {
        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                erpClient.recordWarehouseReceipt(request);
                log.info("[ExecutionMonitor] Nhập kho thành phẩm thành công cho batch={} (lần thử {})",
                        request.getBatchId(), attempt);
                return true;
            } catch (Exception e) {
                log.warn("[ExecutionMonitor] Nhập kho thất bại lần {} cho batch={}: {}",
                        attempt, request.getBatchId(), e.getMessage());

                if (attempt < MAX_RETRY_ATTEMPTS) {
                    try {
                        Thread.sleep(2000); // Chờ 2 giây giữa các lần thử
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Cảnh báo khi thu thập Scada thất bại liên tiếp.
     */
    private void alertScadaCollectionFailure(int failureCount) {
        Map<String, Object> data = new HashMap<>();
        data.put("consecutiveFailures", failureCount);
        data.put("lastSuccessfulCollection", lastSuccessfulCollection != null
                ? lastSuccessfulCollection.toString() : "Chưa có");

        planningNotificationService.notifyProductionManager(
                NotificationType.SYNC_FAILURE,
                String.format("Thu thập dữ liệu Scada thất bại %d lần liên tiếp. " +
                        "Lần thu thập thành công cuối: %s", failureCount,
                        lastSuccessfulCollection != null ? lastSuccessfulCollection.toString() : "N/A"),
                data
        );

        log.error("[ExecutionMonitor] CẢNH BÁO: Scada thất bại {} lần liên tiếp. " +
                "Last success: {}", failureCount, lastSuccessfulCollection);
    }
}
