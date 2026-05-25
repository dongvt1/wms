package com.cy.modules.planning.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cy.modules.planning.agent.client.ScadaClient;
import com.cy.modules.planning.agent.dto.MachineStatus;
import com.cy.modules.planning.agent.entity.ApSyncStatus;
import com.cy.modules.planning.agent.enums.NotificationType;
import com.cy.modules.planning.agent.enums.SyncStatus;
import com.cy.modules.planning.agent.event.MachineBreakdownEvent;
import com.cy.modules.planning.agent.mapper.ApSyncStatusMapper;
import com.cy.modules.planning.agent.service.MachineSyncService;
import com.cy.modules.planning.agent.service.PlanningNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Triển khai MachineSyncService.
 * Poll Scada mỗi 5 phút để lấy trạng thái máy, cache vào Redis,
 * phát hiện sự cố máy và thông báo khi có lỗi liên tiếp.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MachineSyncServiceImpl implements MachineSyncService {

    private static final String SYSTEM_NAME = "scada";
    private static final String CACHE_KEY_PREFIX = "planning:machine:";
    private static final long CACHE_TTL_MINUTES = 15;
    private static final int MAX_CONSECUTIVE_FAILURES = 2;

    private final ScadaClient scadaClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ApSyncStatusMapper apSyncStatusMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final PlanningNotificationService notificationService;

    @Override
    @Scheduled(fixedRate = 300000) // 5 phút
    public void syncMachineStatuses() {
        log.info("[MachineSyncService] Bắt đầu đồng bộ trạng thái máy từ Scada");

        ApSyncStatus syncStatus = getOrCreateSyncStatus();
        syncStatus.setLastAttemptTime(new Date());

        try {
            // Lấy tất cả lineIds đã cache hoặc dùng danh sách mặc định
            List<String> lineIds = getMonitoredLineIds();
            if (lineIds.isEmpty()) {
                log.warn("[MachineSyncService] Không có dây chuyền nào để giám sát");
                return;
            }

            // Gọi Scada để lấy trạng thái máy
            List<MachineStatus> machineStatuses = scadaClient.getMachineStatuses(lineIds);

            // So sánh với trạng thái cũ và phát hiện breakdown
            detectBreakdowns(machineStatuses);

            // Cache trạng thái mới vào Redis theo lineId
            cacheMachineStatuses(machineStatuses);

            // Cập nhật sync status thành công
            syncStatus.setLastSyncTime(new Date());
            syncStatus.setStatus(SyncStatus.ACTIVE.getValue());
            syncStatus.setConsecutiveFailures(0);
            syncStatus.setLastError(null);
            apSyncStatusMapper.updateById(syncStatus);

            log.info("[MachineSyncService] Đồng bộ thành công {} trạng thái máy", machineStatuses.size());

        } catch (Exception e) {
            log.error("[MachineSyncService] Lỗi đồng bộ trạng thái máy từ Scada", e);
            handleSyncFailure(syncStatus, e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<MachineStatus> getCachedMachineStatuses(String lineId) {
        String cacheKey = CACHE_KEY_PREFIX + lineId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof List) {
            return (List<MachineStatus>) cached;
        }
        return Collections.emptyList();
    }

    /**
     * Phát hiện sự cố máy bằng cách so sánh trạng thái mới với trạng thái đã cache.
     * Nếu trạng thái chuyển sang "breakdown" hoặc "error", publish MachineBreakdownEvent.
     */
    private void detectBreakdowns(List<MachineStatus> newStatuses) {
        for (MachineStatus newStatus : newStatuses) {
            if (isBreakdownStatus(newStatus.getStatus())) {
                // Kiểm tra trạng thái cũ từ cache
                List<MachineStatus> cachedStatuses = getCachedMachineStatuses(newStatus.getLineId());
                MachineStatus previousStatus = findMachineInList(cachedStatuses, newStatus.getMachineId());

                // Chỉ publish event nếu trạng thái trước đó KHÔNG phải breakdown/error
                // (tránh publish lặp lại cho cùng một sự cố)
                if (previousStatus == null || !isBreakdownStatus(previousStatus.getStatus())) {
                    log.warn("[MachineSyncService] Phát hiện sự cố máy: lineId={}, machineId={}, status={}",
                            newStatus.getLineId(), newStatus.getMachineId(), newStatus.getStatus());

                    MachineBreakdownEvent event = new MachineBreakdownEvent(
                            this,
                            newStatus.getLineId(),
                            newStatus.getMachineId(),
                            Instant.now()
                    );
                    eventPublisher.publishEvent(event);
                }
            }
        }
    }

    /**
     * Cache trạng thái máy vào Redis, nhóm theo lineId.
     */
    private void cacheMachineStatuses(List<MachineStatus> machineStatuses) {
        // Nhóm theo lineId
        Map<String, List<MachineStatus>> statusesByLine = new HashMap<>();
        for (MachineStatus status : machineStatuses) {
            statusesByLine.computeIfAbsent(status.getLineId(), k -> new ArrayList<>()).add(status);
        }

        // Lưu vào Redis với TTL
        for (Map.Entry<String, List<MachineStatus>> entry : statusesByLine.entrySet()) {
            String cacheKey = CACHE_KEY_PREFIX + entry.getKey();
            redisTemplate.opsForValue().set(cacheKey, entry.getValue(), CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        }
    }

    /**
     * Xử lý khi đồng bộ thất bại.
     * Nếu 2 lần liên tiếp thất bại: thông báo quản lý sản xuất và hiển thị timestamp lần thành công cuối.
     */
    private void handleSyncFailure(ApSyncStatus syncStatus, Exception e) {
        int failures = (syncStatus.getConsecutiveFailures() != null ? syncStatus.getConsecutiveFailures() : 0) + 1;
        syncStatus.setConsecutiveFailures(failures);
        syncStatus.setStatus(SyncStatus.FAILED.getValue());
        syncStatus.setLastError(e.getMessage());

        // Tính staleness
        if (syncStatus.getLastSyncTime() != null) {
            long stalenessMs = System.currentTimeMillis() - syncStatus.getLastSyncTime().getTime();
            syncStatus.setDataStalenessMinutes((int) (stalenessMs / 60000));
        }

        apSyncStatusMapper.updateById(syncStatus);

        // Nếu đạt ngưỡng 2 lần thất bại liên tiếp → thông báo quản lý
        if (failures >= MAX_CONSECUTIVE_FAILURES) {
            String lastSuccessTime = syncStatus.getLastSyncTime() != null
                    ? syncStatus.getLastSyncTime().toString()
                    : "Chưa có lần đồng bộ thành công nào";

            String message = String.format(
                    "Đồng bộ Scada thất bại %d lần liên tiếp. Lần đồng bộ thành công cuối: %s. Lỗi: %s",
                    failures, lastSuccessTime, e.getMessage()
            );

            Map<String, Object> data = new HashMap<>();
            data.put("systemName", SYSTEM_NAME);
            data.put("consecutiveFailures", failures);
            data.put("lastSuccessTime", syncStatus.getLastSyncTime());
            data.put("lastError", e.getMessage());

            notificationService.notifyProductionManager(NotificationType.SYNC_FAILURE, message, data);
            log.warn("[MachineSyncService] Đã thông báo quản lý: {}", message);
        }
    }

    /**
     * Lấy hoặc tạo bản ghi sync status cho hệ thống Scada.
     */
    private ApSyncStatus getOrCreateSyncStatus() {
        LambdaQueryWrapper<ApSyncStatus> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApSyncStatus::getSystemName, SYSTEM_NAME);
        ApSyncStatus syncStatus = apSyncStatusMapper.selectOne(wrapper);

        if (syncStatus == null) {
            syncStatus = new ApSyncStatus();
            syncStatus.setSystemName(SYSTEM_NAME);
            syncStatus.setStatus(SyncStatus.ACTIVE.getValue());
            syncStatus.setConsecutiveFailures(0);
            apSyncStatusMapper.insert(syncStatus);
        }

        return syncStatus;
    }

    /**
     * Lấy danh sách lineId đang được giám sát.
     * Lấy từ các key Redis đã cache hoặc trả về danh sách mặc định.
     */
    private List<String> getMonitoredLineIds() {
        Set<String> keys = redisTemplate.keys(CACHE_KEY_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            List<String> lineIds = new ArrayList<>();
            for (String key : keys) {
                lineIds.add(key.replace(CACHE_KEY_PREFIX, ""));
            }
            return lineIds;
        }
        // Nếu chưa có cache, trả về empty - sẽ được populate bởi các service khác
        // hoặc có thể lấy từ cấu hình
        return Collections.emptyList();
    }

    /**
     * Kiểm tra trạng thái có phải breakdown/error không.
     */
    private boolean isBreakdownStatus(String status) {
        return "breakdown".equalsIgnoreCase(status) || "error".equalsIgnoreCase(status);
    }

    /**
     * Tìm trạng thái máy trong danh sách theo machineId.
     */
    private MachineStatus findMachineInList(List<MachineStatus> statuses, String machineId) {
        if (statuses == null || machineId == null) {
            return null;
        }
        return statuses.stream()
                .filter(s -> machineId.equals(s.getMachineId()))
                .findFirst()
                .orElse(null);
    }
}
