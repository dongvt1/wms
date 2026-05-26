package com.cy.modules.planning.agent.event;

import com.cy.modules.planning.agent.enums.NotificationType;
import com.cy.modules.planning.agent.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Trung tâm lắng nghe sự kiện của Planning Agent.
 * Nhận các Spring ApplicationEvent và kích hoạt các service method tương ứng.
 *
 * Lưu ý: Một số event listener đã được đặt trực tiếp trong service impl
 * (ví dụ: OrderIngestionServiceImpl lắng nghe OrdersReceivedEvent,
 *  ReschedulingServiceImpl lắng nghe MachineBreakdownEvent).
 * Listener này xử lý các event còn lại và đảm bảo notification được gửi đi.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlanningEventListener {

    private final PlanningNotificationService notificationService;
    private final ReschedulingService reschedulingService;
    private final ProductionOrderIssuanceService productionOrderIssuanceService;

    /**
     * Lắng nghe sự kiện thiếu nguyên vật liệu.
     * Thông báo quản lý sản xuất về tình trạng thiếu hụt.
     */
    @Async
    @EventListener
    public void onMaterialShortage(MaterialShortageEvent event) {
        log.info("[PlanningEventListener] Nhận MaterialShortageEvent: orderId={}, deficits={}",
                event.getOrderId(), event.getMaterialDeficits().size());

        Map<String, Object> data = new HashMap<>();
        data.put("orderId", event.getOrderId());
        data.put("materialDeficits", event.getMaterialDeficits());
        data.put("deficitCount", event.getMaterialDeficits().size());

        notificationService.notifyProductionManager(
                NotificationType.MATERIAL_SHORTAGE,
                String.format("Phát hiện thiếu %d loại nguyên vật liệu cho đơn hàng %s",
                        event.getMaterialDeficits().size(), event.getOrderId()),
                data
        );
    }

    /**
     * Lắng nghe sự kiện phát hiện sai lệch sản xuất.
     * Kích hoạt quy trình kiểm tra sai lệch hàng ngày và tạo khuyến nghị điều chỉnh.
     */
    @Async
    @EventListener
    public void onDeviationDetected(DeviationDetectedEvent event) {
        log.info("[PlanningEventListener] Nhận DeviationDetectedEvent: weeklyPlanId={}, batchId={}, deviation={}%",
                event.getWeeklyPlanId(), event.getBatchId(), event.getDeviationPercentage());

        // Kích hoạt kiểm tra sai lệch và tạo phương án điều chỉnh
        try {
            reschedulingService.checkDailyDeviation(event.getWeeklyPlanId());
        } catch (Exception e) {
            log.error("[PlanningEventListener] Lỗi xử lý deviation cho weeklyPlanId={}: {}",
                    event.getWeeklyPlanId(), e.getMessage(), e);

            Map<String, Object> errorData = new HashMap<>();
            errorData.put("weeklyPlanId", event.getWeeklyPlanId());
            errorData.put("batchId", event.getBatchId());
            errorData.put("error", e.getMessage());

            notificationService.notifyProductionManager(
                    NotificationType.SYSTEM_ERROR,
                    "Lỗi xử lý sai lệch sản xuất: " + e.getMessage(),
                    errorData
            );
        }
    }

    /**
     * Lắng nghe sự kiện kế hoạch được phê duyệt.
     * Kích hoạt phát hành lệnh sản xuất cho kế hoạch tuần được phê duyệt.
     */
    @Async
    @EventListener
    public void onPlanApproved(PlanApprovedEvent event) {
        log.info("[PlanningEventListener] Nhận PlanApprovedEvent: planType={}, planId={}, approvedBy={}",
                event.getPlanType(), event.getPlanId(), event.getApprovedBy());

        if ("weekly".equals(event.getPlanType())) {
            // Kích hoạt phát hành lệnh sản xuất cho kế hoạch tuần
            try {
                productionOrderIssuanceService.issueProductionOrders(event.getPlanId());
            } catch (Exception e) {
                log.error("[PlanningEventListener] Lỗi phát hành lệnh sản xuất cho planId={}: {}",
                        event.getPlanId(), e.getMessage(), e);

                Map<String, Object> errorData = new HashMap<>();
                errorData.put("planId", event.getPlanId());
                errorData.put("planType", event.getPlanType());
                errorData.put("error", e.getMessage());

                notificationService.notifyProductionManager(
                        NotificationType.SYSTEM_ERROR,
                        "Lỗi phát hành lệnh sản xuất: " + e.getMessage(),
                        errorData
                );
            }
        }

        // Thông báo kế hoạch đã được phê duyệt
        Map<String, Object> data = new HashMap<>();
        data.put("planType", event.getPlanType());
        data.put("planId", event.getPlanId());
        data.put("approvedBy", event.getApprovedBy());

        notificationService.notifyProductionManager(
                NotificationType.PLAN_GENERATED,
                String.format("Kế hoạch %s (ID: %s) đã được phê duyệt bởi %s",
                        event.getPlanType(), event.getPlanId(), event.getApprovedBy()),
                data
        );
    }

    /**
     * Lắng nghe sự kiện cảnh báo chất lượng.
     * Thông báo quản lý sản xuất về tỷ lệ lỗi vượt ngưỡng.
     */
    @Async
    @EventListener
    public void onQualityAlert(QualityAlertEvent event) {
        log.info("[PlanningEventListener] Nhận QualityAlertEvent: batchId={}, product={}, line={}, defectRate={}%",
                event.getBatchId(), event.getProductId(), event.getLineId(), event.getCurrentDefectRate());

        Map<String, Object> data = new HashMap<>();
        data.put("batchId", event.getBatchId());
        data.put("productId", event.getProductId());
        data.put("lineId", event.getLineId());
        data.put("currentDefectRate", event.getCurrentDefectRate());
        data.put("averageDefectRate", event.getAverageDefectRate());
        data.put("difference", event.getCurrentDefectRate().subtract(event.getAverageDefectRate()));
        data.put("suggestions", new String[]{
                "Tăng sản lượng sản xuất để bù đắp tổn thất yield",
                "Chuyển sang dây chuyền sản xuất khác",
                "Tạm dừng sản xuất để điều tra chất lượng"
        });

        notificationService.notifyProductionManager(
                NotificationType.QUALITY_ALERT,
                String.format("Cảnh báo chất lượng batch %s: tỷ lệ lỗi %.2f%% vượt TB 30 ngày %.2f%%",
                        event.getBatchId(), event.getCurrentDefectRate(), event.getAverageDefectRate()),
                data
        );
    }

    /**
     * Lắng nghe sự kiện đồng bộ thất bại.
     * Thông báo quản lý sản xuất về hệ thống không khả dụng.
     */
    @Async
    @EventListener
    public void onSyncFailure(SyncFailureEvent event) {
        log.warn("[PlanningEventListener] Nhận SyncFailureEvent: system={}, failures={}, lastSuccess={}",
                event.getSystemName(), event.getConsecutiveFailures(), event.getLastSuccessTime());

        Map<String, Object> data = new HashMap<>();
        data.put("systemName", event.getSystemName());
        data.put("consecutiveFailures", event.getConsecutiveFailures());
        data.put("errorMessage", event.getErrorMessage());
        data.put("lastSuccessTime", event.getLastSuccessTime());

        notificationService.notifyProductionManager(
                NotificationType.SYNC_FAILURE,
                String.format("Đồng bộ %s thất bại %d lần liên tiếp. Lỗi: %s",
                        event.getSystemName(), event.getConsecutiveFailures(), event.getErrorMessage()),
                data
        );
    }
}
