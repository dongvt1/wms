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
 * @Description: Engineering Change Notice (Thông báo thay đổi kỹ thuật)
 * @Author: BMad
 * @Date: 2026-02-26
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Engineering Change Notice (ECN)")
@TableName("pl_ecn")
public class Ecn extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Mã ECN */
    @Excel(name = "Mã ECN", width = 20)
    @Schema(description = "Mã ECN")
    private String ecnCode;

    /** Tiêu đề thay đổi */
    @Excel(name = "Tiêu đề", width = 30)
    @Schema(description = "Tiêu đề thay đổi")
    private String title;

    /** Mô tả chi tiết */
    @Schema(description = "Mô tả chi tiết")
    private String description;

    /** FK tới pl_bom.id */
    @Schema(description = "FK tới pl_bom.id")
    private String bomId;

    /** Phiên bản BOM trước thay đổi */
    @Excel(name = "Từ phiên bản", width = 12)
    @Schema(description = "Phiên bản BOM trước thay đổi")
    private String fromRevision;

    /** Phiên bản BOM sau thay đổi */
    @Excel(name = "Đến phiên bản", width = 12)
    @Schema(description = "Phiên bản BOM sau thay đổi")
    private String toRevision;

    /** Trạng thái: draft, pending, approved, rejected, applied */
    @Excel(name = "Trạng thái", width = 12)
    @Schema(description = "Trạng thái: draft, pending, approved, rejected, applied")
    private String status;

    /** Người yêu cầu */
    @Excel(name = "Người yêu cầu", width = 15)
    @Schema(description = "Người yêu cầu")
    private String requestedBy;

    /** Người phê duyệt cuối cùng */
    @Schema(description = "Người phê duyệt cuối cùng")
    private String approvedBy;

    /** Ngày phê duyệt */
    @JsonFormat(timezone = "GMT+7", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Ngày phê duyệt")
    private Date approvedDate;

    /** Ngày áp dụng vào BOM */
    @JsonFormat(timezone = "GMT+7", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Ngày áp dụng vào BOM")
    private Date appliedDate;
}
