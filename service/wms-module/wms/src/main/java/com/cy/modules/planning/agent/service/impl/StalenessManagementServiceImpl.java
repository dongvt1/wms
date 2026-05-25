package com.cy.modules.planning.agent.service.impl;

import com.cy.modules.planning.agent.dto.SyncStatusDto;
import com.cy.modules.planning.agent.entity.ApSyncStatus;
import com.cy.modules.planning.agent.enums.NotificationType;
import com.cy.modules.planning.agent.enums.SyncStatus;
import com.cy.modules.planning.agent.mapper.ApSyncStatusMapper;
import com.cy.modules.planning.agent.service.InventorySyncService;
import com.cy.modules.planning.agent.service.MachineSyncService;
import com.cy.modules.planning.agent.service.OrderSyncService;
import com.cy.modules.planning.agent.service.PlanningNotificationService;
import com.cy.modules.planning.agent.service.QualitySyncService;
import com.cy.modules.planning.agent.service.StalenessManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: Triển khai quản lý độ cũ dữ liệu (staleness) của các hệ thống tích hợp
 * @Author: AI Planning Agent
 * @Date: 2026-06-20
 * @Version: V1.0
 */
@Slf4j
@Service
public class StalenessManagementServiceImpl implements StalenessManagementService {

    /** Ngưỡng chặn lập kế hoạch: 60 phút */
    private static final long BLOCKING_THRESHOLD_MINUTES = 60;

    /** Danh sách các hệ thống cần theo dõi */
    private static final String[] MONITORED_SYSTEMS = {"orderhub", "erp", "scada", "qms"};

    @Resource
    private ApSyncStatusMapper apSyncStatusMapper;

    @Resource
    private OrderSyncService orderSyncService;

    @Resource
    private InventorySyncService inventorySyncService;

    @Resource
    private MachineSyncService machineSyncService;

    @Resource
    private QualitySyncService qualitySyncService;

    @Resource
    private PlanningNotificationService planningNotificationService;

    @Override
    public boolean isPlanningBlocked() {
        List<ApSyncStatus> allStatuses = apSyncStatusMapper.selectList(null);
        for (ApSyncStatus status : allStatuses) {
            long stalenessMinutes = calculateStalenessMinutes(status);
            if (stalenessMinutes > BLOCKING_THRESHOLD_MINUTES) {
                log.warn("[Staleness] Lập kế hoạch bị chặn: hệ thống {} có staleness {} phút (ngưỡng: {} phút)",
                        status.getSystemName(), stalenessMinutes, BLOCKING_THRESHOLD_MINUTES);
                return true;
            }
        }
        return false;
    }

    @Override
    public Map<String, SyncStatusDto> getAllSyncStatuses() {
        Map<String, SyncStatusDto> result = new HashMap<>();
        List<ApSyncStatus> allStatuses = apSyncStatusMapper.selectList(null);

        for (ApSyncStatus entity : allStatuses) {
            SyncStatusDto dto = convertToDto(entity);
            result.put(entity.getSystemName(), dto);
        }

        // Đảm bảo tất cả hệ thống đều có trong kết quả (kể cả chưa có bản ghi)
        for (String system : MONITORED_SYSTEMS) {
            if (!result.containsKey(system)) {
                SyncStatusDto dto = new SyncStatusDto()
                        .setSystemName(system)
                        .setStatus("unknown")
                        .setConsecutiveFailures(0)
                        .setDataStalenessMinutes(null)
                        .setIsBlocked(false);
                result.put(system, dto);
            }
        }

        return result;
    }

    @Override
    @Async
    public void triggerReconciliation(String systemName) {
        log.info("[Staleness] Kích hoạt đối soát dữ liệu đầy đủ cho hệ thống: {}", systemName);
        try {
            switch (systemName.toLowerCase()) {
                case "orderhub":
                    orderSyncService.triggerManualSync();
                    break;
                case "erp":
                    inventorySyncService.syncInventoryData();
                    break;
                case "scada":
                    machineSyncService.syncMachineStatuses();
                    break;
                case "qms":
                    qualitySyncService.triggerManualSync();
                    break;
                default:
                    log.warn("[Staleness] Hệ thống không xác định: {}", systemName);
                    return;
            }
            log.info("[Staleness] Đối soát dữ liệu hoàn thành cho hệ thống: {}", systemName);
        } catch (Exception e) {
            log.error("[Staleness] Lỗi đối soát dữ liệu cho hệ thống {}: {}", systemName, e.getMessage(), e);
            Map<String, Object> data = new HashMap<>();
            data.put("systemName", systemName);
            data.put("error", e.getMessage());
            planningNotificationService.notifyProductionManager(
                    NotificationType.SYNC_FAILURE,
                    "Đối soát dữ liệu thất bại cho hệ thống: " + systemName,
                    data
            );
        }
    }

    @Override
    @Scheduled(fixedRate = 60000) // Kiểm tra mỗi 1 phút
    public void checkAndUpdateStaleness() {
        log.debug("[Staleness] Bắt đầu cập nhật staleness cho tất cả hệ thống");
        List<ApSyncStatus> allStatuses = apSyncStatusMapper.selectList(null);

        for (ApSyncStatus status : allStatuses) {
            long stalenessMinutes = calculateStalenessMinutes(status);
            int previousFailures = status.getConsecutiveFailures() != null ? status.getConsecutiveFailures() : 0;

            // Cập nhật giá trị staleness trong DB
            status.setDataStalenessMinutes((int) stalenessMinutes);

            // Cập nhật trạng thái dựa trên staleness
            if (stalenessMinutes > BLOCKING_THRESHOLD_MINUTES) {
                if (!SyncStatus.STALE.getValue().equals(status.getStatus())) {
                    status.setStatus(SyncStatus.STALE.getValue());
                    log.warn("[Staleness] Hệ thống {} chuyển sang trạng thái STALE (staleness: {} phút)",
                            status.getSystemName(), stalenessMinutes);
                    // Thông báo quản lý sản xuất
                    Map<String, Object> data = new HashMap<>();
                    data.put("systemName", status.getSystemName());
                    data.put("stalenessMinutes", stalenessMinutes);
                    data.put("blocked", true);
                    planningNotificationService.notifyProductionManager(
                            NotificationType.SYNC_FAILURE,
                            String.format("Hệ thống %s không khả dụng, dữ liệu đã cũ %d phút. Lập kế hoạch bị chặn.",
                                    status.getSystemName(), stalenessMinutes),
                            data
                    );
                }
            }

            // Phát hiện khôi phục đồng bộ: consecutive_failures từ >0 về 0
            if (previousFailures > 0 && status.getConsecutiveFailures() != null
                    && status.getConsecutiveFailures() == 0) {
                log.info("[Staleness] Phát hiện khôi phục đồng bộ cho hệ thống: {}", status.getSystemName());
                triggerReconciliation(status.getSystemName());
            }

            apSyncStatusMapper.updateById(status);
        }
    }

    /**
     * Tính toán staleness thực tế: now() - lastSyncTime (tính bằng phút).
     */
    private long calculateStalenessMinutes(ApSyncStatus status) {
        if (status.getLastSyncTime() == null) {
            // Chưa bao giờ đồng bộ thành công → coi như staleness vô hạn
            return Long.MAX_VALUE;
        }
        long diffMillis = System.currentTimeMillis() - status.getLastSyncTime().getTime();
        return diffMillis / (60 * 1000);
    }

    /**
     * Chuyển đổi entity sang DTO với tính toán staleness thời gian thực.
     */
    private SyncStatusDto convertToDto(ApSyncStatus entity) {
        long stalenessMinutes = calculateStalenessMinutes(entity);
        boolean blocked = stalenessMinutes > BLOCKING_THRESHOLD_MINUTES;

        return new SyncStatusDto()
                .setSystemName(entity.getSystemName())
                .setStatus(entity.getStatus())
                .setLastSyncTime(entity.getLastSyncTime())
                .setLastAttemptTime(entity.getLastAttemptTime())
                .setConsecutiveFailures(entity.getConsecutiveFailures() != null ? entity.getConsecutiveFailures() : 0)
                .setDataStalenessMinutes(stalenessMinutes == Long.MAX_VALUE ? null : stalenessMinutes)
                .setIsBlocked(blocked);
    }
}
