package com.cy.modules.qms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Review và phê duyệt toàn bộ quá trình kiểm tra của WO")
@TableName("qms_qc_review")
public class QcReview extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "Mã review (RVyyyyMMddNNN)")
    private String reviewCode;

    @Schema(description = "FK → pl_work_order")
    private String workOrderId;

    @Schema(description = "Tổng số phiên kiểm tra")
    private Integer totalSessions;

    @Schema(description = "Số phiên đạt")
    private Integer passedSessions;

    @Schema(description = "Số phiên không đạt")
    private Integer failedSessions;

    @Schema(description = "Kết quả tổng thể: passed | failed | conditional")
    private String overallResult;

    @Schema(description = "Trạng thái: draft | pending_approval | approved | rejected")
    private String status;

    @Schema(description = "Người review")
    private String reviewer;

    @Schema(description = "Người phê duyệt")
    private String approver;

    @JsonFormat(timezone = "GMT+7", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Ngày phê duyệt")
    private Date approvalDate;

    @Schema(description = "Lý do từ chối")
    private String rejectionReason;

    @Schema(description = "Ghi chú")
    private String notes;
}
