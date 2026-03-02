package com.cy.modules.planning.entity;

import java.io.Serializable;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @Description: Phê duyệt ECN theo bộ phận
 * @Author: BMad
 * @Date: 2026-02-26
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Phê duyệt ECN theo bộ phận")
@TableName("pl_ecn_approval")
public class EcnApproval extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** FK tới pl_ecn.id */
    @Schema(description = "FK tới pl_ecn.id")
    private String ecnId;

    /** Bộ phận: production, procurement, quality, engineering */
    @Excel(name = "Bộ phận", width = 15)
    @Schema(description = "Bộ phận: production, procurement, quality, engineering")
    private String department;

    /** ID người duyệt */
    @Schema(description = "ID người duyệt")
    private String approverId;

    /** Tên người duyệt */
    @Excel(name = "Người duyệt", width = 20)
    @Schema(description = "Tên người duyệt")
    private String approverName;

    /** Trạng thái: pending, approved, rejected */
    @Excel(name = "Trạng thái", width = 12)
    @Schema(description = "Trạng thái: pending, approved, rejected")
    private String status;

    /** Nhận xét */
    @Excel(name = "Nhận xét", width = 30)
    @Schema(description = "Nhận xét")
    private String comments;

    /** Ngày phê duyệt */
    @Excel(name = "Ngày duyệt", width = 18, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+7", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Ngày phê duyệt")
    private Date approvedDate;
}
