package com.cy.modules.planning.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cy.modules.planning.agent.client.QmsClient;
import com.cy.modules.planning.agent.dto.DefectClassification;
import com.cy.modules.planning.agent.dto.QualityReport;
import com.cy.modules.planning.agent.entity.PlanningOrder;
import com.cy.modules.planning.agent.entity.WeeklyPlanBatch;
import com.cy.modules.planning.agent.enums.NotificationType;
import com.cy.modules.planning.agent.event.QualityAlertEvent;
import com.cy.modules.planning.agent.mapper.PlanningOrderMapper;
import com.cy.modules.planning.agent.mapper.WeeklyPlanBatchMapper;
import com.cy.modules.planning.agent.service.PlanningNotificationService;
import com.cy.modules.planning.agent.service.QualityIntegrationService;
import com.cy.modules.planning.agent.service.QualitySyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation của QualityIntegrationService.
 * Tích hợp dữ liệu chất lượng từ QMS vào quy trình lập kế hoạch sản xuất.
 */
@Slf4j
@Service
public class QualityIntegrationServiceImpl implements QualityIntegrationService {

    /** Ngưỡng chênh lệch tỷ lệ lỗi so với trung bình 30 ngày (5 điểm phần trăm) */
    private static final BigDecimal DEFECT_RATE_THRESHOLD = new BigDecimal("5.00");

    @Autowired
    private QmsClient qmsClient;

    @Autowired
    private QualitySyncService qualitySyncService;

    @Autowired
    private WeeklyPlanBatchMapper weeklyPlanBatchMapper;

    @Autowired
    private PlanningOrderMapper planningOrderMapper;

    @Autowired
    private PlanningNotificationService planningNotificationService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public void checkQualityAlerts(String batchId) {
        log.info("[QualityIntegration] Kiểm tra cảnh báo chất lượng cho batchId={}", batchId);

        WeeklyPlanBatch batch = weeklyPlanBatchMapper.selectById(batchId);
        if (batch == null) {
            log.error("[QualityIntegration] Không tìm thấy batch: {}", batchId);
            return;
        }

        String productId = batch.getProductType();
        String lineId = batch.getProductionLineId();

        // Lấy tỷ lệ lỗi trung bình 30 ngày
        BigDecimal avg30DayDefectRate = qualitySyncService.getDefectRate30Day(productId, lineId);
        if (avg30DayDefectRate == null) {
            log.warn("[QualityIntegration] Không có dữ liệu tỷ lệ lỗi 30 ngày cho product={}, line={}", productId, lineId);
            return;
        }

        // Lấy tỷ lệ lỗi hiện tại từ QMS
        QualityReport currentReport = qmsClient.getQualityData(productId, lineId,
                LocalDate.now().minusDays(1), LocalDate.now());
        if (currentReport == null || currentReport.getAverageDefectRate() == null) {
            log.warn("[QualityIntegration] Không có dữ liệu chất lượng hiện tại cho product={}, line={}", productId, lineId);
            return;
        }

        BigDecimal currentDefectRate = currentReport.getAverageDefectRate();
        BigDecimal difference = currentDefectRate.subtract(avg30DayDefectRate);

        // Cảnh báo nếu chênh lệch > 5 điểm phần trăm
        if (difference.compareTo(DEFECT_RATE_THRESHOLD) > 0) {
            log.warn("[QualityIntegration] Tỷ lệ lỗi vượt ngưỡng: hiện tại={}%, TB 30 ngày={}%, chênh lệch={}%",
                    currentDefectRate, avg30DayDefectRate, difference);

            // Publish QualityAlertEvent
            eventPublisher.publishEvent(new QualityAlertEvent(
                    this, batchId, productId, lineId, currentDefectRate, avg30DayDefectRate));

            Map<String, Object> alertData = new HashMap<>();
            alertData.put("batchId", batchId);
            alertData.put("productId", productId);
            alertData.put("lineId", lineId);
            alertData.put("currentDefectRate", currentDefectRate);
            alertData.put("avg30DayDefectRate", avg30DayDefectRate);
            alertData.put("difference", difference);
            alertData.put("suggestions", new String[]{
                    "Tăng sản lượng sản xuất để bù đắp tổn thất yield",
                    "Chuyển sang dây chuyền sản xuất khác",
                    "Tạm dừng sản xuất để điều tra chất lượng"
            });

            planningNotificationService.notifyProductionManager(
                    NotificationType.QUALITY_ALERT,
                    String.format("Tỷ lệ lỗi batch %s vượt ngưỡng: %.2f%% (TB 30 ngày: %.2f%%, chênh lệch: +%.2f%%)",
                            batchId, currentDefectRate, avg30DayDefectRate, difference),
                    alertData
            );
        } else {
            log.info("[QualityIntegration] Tỷ lệ lỗi trong ngưỡng cho batch={}: hiện tại={}%, TB 30 ngày={}%",
                    batchId, currentDefectRate, avg30DayDefectRate);
        }
    }

    @Override
    public BigDecimal calculateGrossQuantity(String productId, String lineId, BigDecimal netQuantity) {
        log.info("[QualityIntegration] Tính sản lượng gộp: product={}, line={}, net={}", productId, lineId, netQuantity);

        if (netQuantity == null || netQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("[QualityIntegration] Sản lượng ròng không hợp lệ: {}", netQuantity);
            return netQuantity;
        }

        // Lấy yield rate 90 ngày từ QualitySyncService
        BigDecimal yieldRate = qualitySyncService.getYieldRate90Day(productId, lineId);

        // Nếu dữ liệu QMS không khả dụng >30 phút, QualitySyncService đã trả về yield rate gần nhất
        if (yieldRate == null || yieldRate.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("[QualityIntegration] Không có yield rate cho product={}, line={}. Sử dụng 100%", productId, lineId);
            return netQuantity;
        }

        // Cảnh báo nếu dữ liệu QMS đã cũ
        if (qualitySyncService.isDataStale()) {
            log.warn("[QualityIntegration] Dữ liệu QMS đã cũ >30 phút. Sử dụng yield rate lịch sử gần nhất: {}%", yieldRate);
        }

        // gross = net / (yieldRate / 100)
        BigDecimal yieldDecimal = yieldRate.divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);
        BigDecimal grossQuantity = netQuantity.divide(yieldDecimal, 2, RoundingMode.CEILING);

        log.info("[QualityIntegration] Sản lượng gộp: gross={} (net={}, yieldRate={}%)", grossQuantity, netQuantity, yieldRate);
        return grossQuantity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void classifyDefects(String batchId) {
        log.info("[QualityIntegration] Phân loại sản phẩm lỗi cho batchId={}", batchId);

        WeeklyPlanBatch batch = weeklyPlanBatchMapper.selectById(batchId);
        if (batch == null) {
            log.error("[QualityIntegration] Không tìm thấy batch: {}", batchId);
            return;
        }

        // Gọi QMS để phân loại lỗi
        DefectClassification classification = qmsClient.classifyDefects(batchId);
        if (classification == null) {
            log.warn("[QualityIntegration] Không nhận được kết quả phân loại từ QMS cho batch={}", batchId);
            return;
        }

        BigDecimal destroyableQty = classification.getDestroyableQuantity();
        if (destroyableQty == null) {
            destroyableQty = BigDecimal.ZERO;
        }

        log.info("[QualityIntegration] Kết quả phân loại batch={}: tổng lỗi={}, sửa chữa={}, hủy={}",
                batchId, classification.getTotalDefects(),
                classification.getRepairableQuantity(), destroyableQty);

        // Trừ số lượng phải hủy khỏi sản lượng thực tế (net output)
        if (destroyableQty.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal currentActual = batch.getActualQuantity() != null ? batch.getActualQuantity() : BigDecimal.ZERO;
            BigDecimal adjustedQuantity = currentActual.subtract(destroyableQty);
            if (adjustedQuantity.compareTo(BigDecimal.ZERO) < 0) {
                adjustedQuantity = BigDecimal.ZERO;
            }
            batch.setActualQuantity(adjustedQuantity);
            weeklyPlanBatchMapper.updateById(batch);

            log.info("[QualityIntegration] Đã trừ {} sản phẩm hủy từ batch={}. Sản lượng ròng mới: {}",
                    destroyableQty, batchId, adjustedQuantity);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjustForYieldLoss(String batchId) {
        log.info("[QualityIntegration] Điều chỉnh kế hoạch do tổn thất yield cho batchId={}", batchId);

        WeeklyPlanBatch batch = weeklyPlanBatchMapper.selectById(batchId);
        if (batch == null) {
            log.error("[QualityIntegration] Không tìm thấy batch: {}", batchId);
            return;
        }

        // Lấy đơn hàng liên quan
        PlanningOrder order = planningOrderMapper.selectById(batch.getOrderId());
        if (order == null) {
            log.error("[QualityIntegration] Không tìm thấy đơn hàng: {}", batch.getOrderId());
            return;
        }

        // Tính tổng sản lượng ròng hiện tại cho đơn hàng (từ tất cả batch liên quan)
        LambdaQueryWrapper<WeeklyPlanBatch> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WeeklyPlanBatch::getOrderId, order.getId());
        var relatedBatches = weeklyPlanBatchMapper.selectList(queryWrapper);

        BigDecimal totalNetOutput = relatedBatches.stream()
                .map(b -> b.getActualQuantity() != null ? b.getActualQuantity() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal orderRequirement = order.getQuantity();
        BigDecimal fulfillmentQty = order.getFulfillmentQty() != null ? order.getFulfillmentQty() : BigDecimal.ZERO;
        BigDecimal remainingRequired = orderRequirement.subtract(fulfillmentQty).subtract(totalNetOutput);

        log.info("[QualityIntegration] Đơn hàng {}: yêu cầu={}, đã hoàn thành={}, sản lượng ròng hiện tại={}, còn thiếu={}",
                order.getId(), orderRequirement, fulfillmentQty, totalNetOutput, remainingRequired);

        // Kích hoạt lập kế hoạch bổ sung nếu sản lượng ròng < yêu cầu đơn hàng
        if (remainingRequired.compareTo(BigDecimal.ZERO) > 0) {
            log.warn("[QualityIntegration] Sản lượng ròng thấp hơn yêu cầu. Cần sản xuất bổ sung: {}", remainingRequired);

            Map<String, Object> data = new HashMap<>();
            data.put("batchId", batchId);
            data.put("orderId", order.getId());
            data.put("productType", batch.getProductType());
            data.put("lineId", batch.getProductionLineId());
            data.put("orderRequirement", orderRequirement);
            data.put("currentNetOutput", totalNetOutput);
            data.put("fulfillmentQty", fulfillmentQty);
            data.put("additionalQuantityNeeded", remainingRequired);

            planningNotificationService.notifyProductionManager(
                    NotificationType.RESCHEDULE_NEEDED,
                    String.format("Cần sản xuất bổ sung cho đơn hàng %s: thiếu %s đơn vị (sản phẩm: %s)",
                            order.getExternalOrderId(), remainingRequired, batch.getProductType()),
                    data
            );
        } else {
            log.info("[QualityIntegration] Sản lượng ròng đủ đáp ứng yêu cầu đơn hàng {}", order.getId());
        }
    }
}
