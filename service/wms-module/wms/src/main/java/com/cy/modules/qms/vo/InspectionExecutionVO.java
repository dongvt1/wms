package com.cy.modules.qms.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Description: VO Inspection Execution (output)
 * @Author: QMS
 * @Date: 2026-03-15
 * @Version: V1.0
 */
@Data
@Schema(description = "VO phiên kiểm tra chất lượng")
public class InspectionExecutionVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private String id;

    @Schema(description = "Mã phiên (EXCyyyyMMddNNN)")
    private String executionCode;

    @Schema(description = "FK → qms_inspection_template")
    private String templateId;

    @Schema(description = "Tên template")
    private String templateName;

    @Schema(description = "FK → product")
    private String productId;

    @Schema(description = "Loại giai đoạn: iqc, pqc, fqc")
    private String stageType;

    @Schema(description = "FK → pl_work_order")
    private String workOrderId;

    @Schema(description = "FK → pl_production_stage")
    private String productionStageId;

    @Schema(description = "Người kiểm tra")
    private String inspector;

    @JsonFormat(timezone = "GMT+7", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Ngày kiểm tra")
    private Date inspectionDate;

    @Schema(description = "Kết quả tổng: pass, fail")
    private String overallResult;

    @Schema(description = "Trạng thái: draft, in_progress, pending_approval, approved, rejected")
    private String status;

    @Schema(description = "Người phê duyệt")
    private String approvedBy;

    @JsonFormat(timezone = "GMT+7", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Thời gian phê duyệt")
    private Date approvedTime;

    @Schema(description = "Ghi chú")
    private String notes;

    @Schema(description = "Người tạo")
    private String createBy;

    @JsonFormat(timezone = "GMT+7", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Ngày tạo")
    private Date createTime;

    @Schema(description = "Danh sách kết quả bước kiểm tra")
    private List<StepResultVO> steps;
}
