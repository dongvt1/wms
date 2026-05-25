package com.cy.modules.planning.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * DTO cập nhật dashboard qua WebSocket
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardUpdate {

    /** Loại cập nhật: fulfillment, progress, quality, sync_status */
    private String updateType;

    /** Thời điểm cập nhật */
    private Instant timestamp;

    /** Dữ liệu cập nhật */
    private Map<String, Object> data;

    /** Mô tả ngắn gọn */
    private String summary;
}
