package com.cy.modules.qms.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: VO lịch sử kiểm tra (dùng cho report)
 * @Author: QMS
 * @Date: 2026-03-15
 * @Version: V1.0
 */
@Data
@Schema(description = "VO lịch sử kiểm tra")
public class InspectionHistoryVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID phiên kiểm tra")
    private String id;

    @Schema(description = "Mã phiên (EXCyyyyMMddNNN)")
    private String executionCode;

    @Schema(description = "ID template")
    private String templateId;

    @Schema(description = "Tên template")
    private String templateName;

    @Schema(description = "ID sản phẩm")
    private String productId;

    @Schema(description = "Loại giai đoạn: iqc, pqc, fqc")
    private String stageType;

    @Schema(description = "Người kiểm tra")
    private String inspector;

    @JsonFormat(timezone = "GMT+7", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Ngày kiểm tra")
    private Date inspectionDate;

    @Schema(description = "Kết quả tổng: pass, fail")
    private String overallResult;

    @Schema(description = "Trạng thái")
    private String status;

    @Schema(description = "Người phê duyệt")
    private String approvedBy;

    @JsonFormat(timezone = "GMT+7", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Thời gian phê duyệt")
    private Date approvedTime;

    @JsonFormat(timezone = "GMT+7", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Ngày tạo")
    private Date createTime;
}
