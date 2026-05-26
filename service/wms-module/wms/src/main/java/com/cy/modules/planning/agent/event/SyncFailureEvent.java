package com.cy.modules.planning.agent.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;

/**
 * Sự kiện đồng bộ thất bại với hệ thống bên ngoài.
 * Được publish khi bất kỳ SyncService nào gặp lỗi liên tiếp vượt ngưỡng cho phép.
 */
@Getter
public class SyncFailureEvent extends ApplicationEvent {

    /** Tên hệ thống bị lỗi: orderhub, erp, scada, qms */
    private final String systemName;

    /** Số lần thất bại liên tiếp */
    private final int consecutiveFailures;

    /** Thông báo lỗi */
    private final String errorMessage;

    /** Thời điểm đồng bộ thành công gần nhất */
    private final Instant lastSuccessTime;

    public SyncFailureEvent(Object source, String systemName, int consecutiveFailures,
                            String errorMessage, Instant lastSuccessTime) {
        super(source);
        this.systemName = systemName;
        this.consecutiveFailures = consecutiveFailures;
        this.errorMessage = errorMessage;
        this.lastSuccessTime = lastSuccessTime;
    }
}
