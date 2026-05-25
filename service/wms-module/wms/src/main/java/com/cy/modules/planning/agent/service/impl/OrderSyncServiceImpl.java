package com.cy.modules.planning.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cy.modules.planning.agent.client.OrderHubClient;
import com.cy.modules.planning.agent.dto.ExternalOrder;
import com.cy.modules.planning.agent.entity.ApSyncStatus;
import com.cy.modules.planning.agent.entity.PlanningOrder;
import com.cy.modules.planning.agent.enums.SyncStatus;
import com.cy.modules.planning.agent.event.OrdersReceivedEvent;
import com.cy.modules.planning.agent.mapper.ApSyncStatusMapper;
import com.cy.modules.planning.agent.mapper.PlanningOrderMapper;
import com.cy.modules.planning.agent.service.OrderSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Description: Triển khai đồng bộ đơn hàng từ OrderHub
 * @Author: AI Planning Agent
 * @Date: 2026-06-20
 * @Version: V1.0
 */
@Slf4j
@Service
public class OrderSyncServiceImpl implements OrderSyncService {

    private static final String SYSTEM_NAME = "orderhub";

    @Resource
    private OrderHubClient orderHubClient;

    @Resource
    private PlanningOrderMapper planningOrderMapper;

    @Resource
    private ApSyncStatusMapper apSyncStatusMapper;

    @Resource
    private ApplicationEventPublisher eventPublisher;

    @Override
    @Scheduled(fixedRate = 300000)
    public void syncOrders() {
        log.info("[OrderSync] Bắt đầu đồng bộ đơn hàng từ OrderHub");
        try {
            doSync();
        } catch (Exception e) {
            log.error("[OrderSync] Lỗi đồng bộ đơn hàng: {}", e.getMessage(), e);
            handleSyncFailure(e);
        }
    }

    @Override
    public void triggerManualSync() {
        log.info("[OrderSync] Kích hoạt đồng bộ thủ công");
        syncOrders();
    }

    private void doSync() {
        // 1. Lấy thời điểm đồng bộ gần nhất từ ap_sync_status
        ApSyncStatus syncStatus = getOrCreateSyncStatus();
        Instant since = determineSyncSince(syncStatus);

        // 2. Cập nhật thời điểm thử đồng bộ
        syncStatus.setLastAttemptTime(new Date());
        apSyncStatusMapper.updateById(syncStatus);

        // 3. Gọi OrderHubClient để lấy đơn hàng mới
        List<ExternalOrder> externalOrders = orderHubClient.fetchNewOrders(since);
        log.info("[OrderSync] Nhận được {} đơn hàng mới từ OrderHub", externalOrders.size());

        if (externalOrders.isEmpty()) {
            // Không có đơn hàng mới, cập nhật trạng thái thành công
            updateSyncSuccess(syncStatus);
            return;
        }

        // 4. Chuyển đổi và lưu đơn hàng với xử lý trùng lặp
        List<String> newOrderIds = new ArrayList<>();
        for (ExternalOrder externalOrder : externalOrders) {
            try {
                PlanningOrder planningOrder = transformToPlanningOrder(externalOrder);
                planningOrderMapper.insert(planningOrder);
                newOrderIds.add(planningOrder.getId());
                log.debug("[OrderSync] Lưu đơn hàng mới: externalOrderId={}", externalOrder.getOrderId());
            } catch (DuplicateKeyException e) {
                // Đơn hàng đã tồn tại (deduplication via external_order_id unique key)
                log.debug("[OrderSync] Bỏ qua đơn hàng trùng lặp: externalOrderId={}", externalOrder.getOrderId());
            }
        }

        // 5. Cập nhật trạng thái đồng bộ thành công
        updateSyncSuccess(syncStatus);

        // 6. Phát event nếu có đơn hàng mới
        if (!newOrderIds.isEmpty()) {
            log.info("[OrderSync] Phát OrdersReceivedEvent với {} đơn hàng mới", newOrderIds.size());
            eventPublisher.publishEvent(new OrdersReceivedEvent(this, newOrderIds));
        }
    }

    /**
     * Lấy hoặc tạo mới bản ghi sync status cho OrderHub.
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
            log.info("[OrderSync] Tạo mới bản ghi sync status cho {}", SYSTEM_NAME);
        }
        return syncStatus;
    }

    /**
     * Xác định thời điểm bắt đầu lấy đơn hàng.
     * Nếu chưa có lần đồng bộ nào, lấy từ 24 giờ trước.
     */
    private Instant determineSyncSince(ApSyncStatus syncStatus) {
        if (syncStatus.getLastSyncTime() != null) {
            return syncStatus.getLastSyncTime().toInstant();
        }
        // Lần đầu đồng bộ: lấy đơn hàng từ 24 giờ trước
        return Instant.now().minusSeconds(24 * 60 * 60);
    }

    /**
     * Chuyển đổi ExternalOrder từ OrderHub sang PlanningOrder entity.
     */
    private PlanningOrder transformToPlanningOrder(ExternalOrder externalOrder) {
        PlanningOrder order = new PlanningOrder();
        order.setExternalOrderId(externalOrder.getOrderId());
        order.setProductType(externalOrder.getProductType());
        order.setCustomerName(externalOrder.getCustomerName());
        order.setQuantity(externalOrder.getQuantity() != null ? externalOrder.getQuantity() : BigDecimal.ZERO);
        order.setDeadline(localDateToDate(externalOrder.getDeadline()));
        order.setReceiptTimestamp(instantToDate(externalOrder.getReceiptTimestamp()));
        order.setStatus("pending");
        order.setValidationStatus("valid");
        order.setFulfillmentQty(BigDecimal.ZERO);
        return order;
    }

    /**
     * Cập nhật trạng thái đồng bộ thành công.
     */
    private void updateSyncSuccess(ApSyncStatus syncStatus) {
        Date now = new Date();
        syncStatus.setLastSyncTime(now);
        syncStatus.setLastAttemptTime(now);
        syncStatus.setStatus(SyncStatus.ACTIVE.getValue());
        syncStatus.setConsecutiveFailures(0);
        syncStatus.setLastError(null);
        syncStatus.setDataStalenessMinutes(0);
        apSyncStatusMapper.updateById(syncStatus);
        log.info("[OrderSync] Đồng bộ thành công tại {}", now);
    }

    /**
     * Xử lý khi đồng bộ thất bại: tăng consecutive_failures và ghi lỗi.
     */
    private void handleSyncFailure(Exception e) {
        try {
            ApSyncStatus syncStatus = getOrCreateSyncStatus();
            int failures = (syncStatus.getConsecutiveFailures() != null ? syncStatus.getConsecutiveFailures() : 0) + 1;
            syncStatus.setConsecutiveFailures(failures);
            syncStatus.setLastAttemptTime(new Date());
            syncStatus.setStatus(SyncStatus.FAILED.getValue());
            syncStatus.setLastError(truncateError(e.getMessage()));

            // Tính staleness nếu có last_sync_time
            if (syncStatus.getLastSyncTime() != null) {
                long minutesSinceLastSync = (System.currentTimeMillis() - syncStatus.getLastSyncTime().getTime()) / (60 * 1000);
                syncStatus.setDataStalenessMinutes((int) minutesSinceLastSync);
            }

            apSyncStatusMapper.updateById(syncStatus);
            log.warn("[OrderSync] Ghi nhận thất bại lần thứ {} cho {}", failures, SYSTEM_NAME);
        } catch (Exception ex) {
            log.error("[OrderSync] Không thể cập nhật trạng thái thất bại: {}", ex.getMessage(), ex);
        }
    }

    /**
     * Cắt ngắn thông báo lỗi để tránh vượt quá giới hạn cột DB.
     */
    private String truncateError(String errorMessage) {
        if (errorMessage == null) {
            return "Unknown error";
        }
        return errorMessage.length() > 500 ? errorMessage.substring(0, 500) : errorMessage;
    }

    private Date localDateToDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private Date instantToDate(Instant instant) {
        if (instant == null) {
            return new Date();
        }
        return Date.from(instant);
    }
}
