package com.cy.modules.qms.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @Description: Phiên kiểm tra chất lượng
 * @Author: QMS
 * @Date: 2026-03-15
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Phiên kiểm tra chất lượng")
@TableName("qms_inspection_execution")
public class InspectionExecution extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Mã phiên (EXCyyyyMMddNNN) */
    @Schema(description = "Mã phiên (EXCyyyyMMddNNN)")
    private String executionCode;

    /** FK → qms_inspection_template */
    @Schema(description = "FK → qms_inspection_template")
    private String templateId;

    /** Snapshot cấu hình template tại thời điểm tạo */
    @Schema(description = "Snapshot cấu hình template tại thời điểm tạo")
    private String templateSnapshot;

    /** FK → product */
    @Schema(description = "FK → product")
    private String productId;

    /** Loại giai đoạn: iqc, pqc, fqc */
    @Schema(description = "Loại giai đoạn: iqc, pqc, fqc")
    private String stageType;

    /** FK → pl_work_order */
    @Schema(description = "FK → pl_work_order")
    private String workOrderId;

    /** FK → pl_production_stage */
    @Schema(description = "FK → pl_production_stage")
    private String productionStageId;

    /** Người kiểm tra */
    @Schema(description = "Người kiểm tra")
    private String inspector;

    /** Ngày kiểm tra */
    @JsonFormat(timezone = "GMT+7", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Ngày kiểm tra")
    private Date inspectionDate;

    /** Kết quả tổng: pass, fail */
    @Schema(description = "Kết quả tổng: pass, fail")
    private String overallResult;

    /** Trạng thái: draft, in_progress, pending_approval, approved, rejected */
    @Schema(description = "Trạng thái: draft, in_progress, pending_approval, approved, rejected")
    private String status;

    /** Người phê duyệt */
    @Schema(description = "Người phê duyệt")
    private String approvedBy;

    /** Thời gian phê duyệt */
    @JsonFormat(timezone = "GMT+7", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Thời gian phê duyệt")
    private Date approvedTime;

    /** Ghi chú */
    @Schema(description = "Ghi chú")
    private String notes;

    /** Mã tổ chức (multi-tenant) */
    @Schema(description = "Mã tổ chức (multi-tenant)")
    @TableField("sys_org_code")
    private String sysOrgCode;
}
