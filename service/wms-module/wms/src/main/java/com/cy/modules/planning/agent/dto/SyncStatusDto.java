package com.cy.modules.planning.agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: DTO phản hồi trạng thái đồng bộ hệ thống
 * @Author: AI Planning Agent
 * @Date: 2026-06-20
 * @Version: V1.0
 */
@Data
@Accessors(chain = true)
@Schema(description = "Trạng thái đồng bộ hệ thống")
public class SyncStatusDto implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "Tên hệ thống (orderhub, erp, scada, qms)")
    private String systemName;

    @Schema(description = "Trạng thái đồng bộ: active, failed, stale")
    private String status;

    @Schema(description = "Thời điểm đồng bộ thành công gần nhất")
    private Date lastSyncTime;

    @Schema(description = "Thời điểm thử đồng bộ gần nhất")
    private Date lastAttemptTime;

    @Schema(description = "Số lần thất bại liên tiếp")
    private Integer consecutiveFailures;

    @Schema(description = "Số phút dữ liệu đã cũ (staleness)")
    private Long dataStalenessMinutes;

    @Schema(description = "Có bị chặn lập kế hoạch hay không (staleness > 60 phút)")
    private Boolean isBlocked;
}
