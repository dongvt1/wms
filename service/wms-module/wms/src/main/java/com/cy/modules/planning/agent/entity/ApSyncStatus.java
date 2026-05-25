package com.cy.modules.planning.agent.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: Trạng thái đồng bộ hệ thống (Sync Status)
 * @Author: AI Planning Agent
 * @Date: 2026-06-20
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Trạng thái đồng bộ hệ thống")
@TableName("ap_sync_status")
public class ApSyncStatus implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Khóa chính */
    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "ID")
    private String id;

    /** Hệ thống: orderhub, erp, scada, qms */
    @Schema(description = "Tên hệ thống")
    @TableField("system_name")
    private String systemName;

    /** Thời điểm đồng bộ thành công gần nhất */
    @Schema(description = "Thời điểm đồng bộ thành công gần nhất")
    @TableField("last_sync_time")
    private Date lastSyncTime;

    /** Thời điểm thử đồng bộ gần nhất */
    @Schema(description = "Thời điểm thử đồng bộ gần nhất")
    @TableField("last_attempt_time")
    private Date lastAttemptTime;

    /** Trạng thái: active, failed, stale */
    @Schema(description = "Trạng thái đồng bộ")
    private String status;

    /** Số lần thất bại liên tiếp */
    @Schema(description = "Số lần thất bại liên tiếp")
    @TableField("consecutive_failures")
    private Integer consecutiveFailures;

    /** Thông báo lỗi gần nhất */
    @Schema(description = "Lỗi gần nhất")
    @TableField("last_error")
    private String lastError;

    /** Số phút kể từ lần đồng bộ thành công */
    @Schema(description = "Số phút dữ liệu cũ")
    @TableField("data_staleness_minutes")
    private Integer dataStalenessMinutes;

    /** Mã tổ chức (multi-tenant) */
    @Schema(description = "Mã tổ chức")
    @TableField("sys_org_code")
    private String sysOrgCode;
}
