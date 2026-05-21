package com.cy.modules.qms.entity;

import java.io.Serializable;
import java.math.BigDecimal;
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
 * @Description: Báo cáo sự không phù hợp (NCR - Non-Conformance Report)
 * @Author: BMad
 * @Date: 2026-02-25
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Báo cáo sự không phù hợp (NCR)")
@TableName("qms_ncr")
public class Ncr extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Mã NCR */
    @Excel(name = "Mã NCR", width = 20)
    @Schema(description = "Mã NCR (NCRyyyyMMddNNN)")
    private String ncrCode;

    /** Nguồn phát hiện */
    @Excel(name = "Nguồn phát hiện", width = 15)
    @Schema(description = "Nguồn phát hiện: iqc/pqc/fqc/other")
    private String sourceType;

    /** Phiếu kiểm tra liên kết */
    @Schema(description = "ID phiếu kiểm tra liên kết")
    private String sourceId;

    /** Sản phẩm */
    @Excel(name = "Sản phẩm", width = 25, dictTable = "product", dicText = "name", dicCode = "id")
    @Schema(description = "ID sản phẩm")
    private String productId;

    /** Nhà cung cấp */
    @Excel(name = "Nhà cung cấp", width = 25, dictTable = "supplier", dicText = "name", dicCode = "id")
    @Schema(description = "ID nhà cung cấp (tự động từ IQC)")
    private String supplierId;

    /** Mô tả lỗi */
    @Excel(name = "Mô tả lỗi", width = 40)
    @Schema(description = "Mô tả lỗi")
    private String description;

    /** Mức độ nghiêm trọng */
    @Excel(name = "Mức độ", width = 15)
    @Schema(description = "Mức độ nghiêm trọng: critical/major/minor")
    private String severity;

    /** Số lượng lỗi */
    @Excel(name = "SL lỗi", width = 15)
    @Schema(description = "Số lượng lỗi")
    private BigDecimal quantityDefective;

    /** Hành động đề xuất */
    @Excel(name = "Hành động đề xuất", width = 20)
    @Schema(description = "Hành động đề xuất: return/repair/scrap/accept_conditional")
    private String proposedAction;

    /** Hành động khắc phục thực tế */
    @Excel(name = "Hành động khắc phục", width = 40)
    @Schema(description = "Hành động khắc phục thực tế")
    private String correctiveAction;

    /** Trạng thái */
    @Excel(name = "Trạng thái", width = 18)
    @Schema(description = "Trạng thái: open/investigating/action_taken/verified/closed")
    private String status;

    /** Người được giao xử lý */
    @Excel(name = "Người xử lý", width = 20)
    @Schema(description = "Người được giao xử lý")
    private String assignedTo;

    /** Người đóng NCR */
    @Excel(name = "Người đóng", width = 20)
    @Schema(description = "Người đóng NCR")
    private String closedBy;

    /** Ngày đóng */
    @Excel(name = "Ngày đóng", width = 18, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+7", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Ngày đóng NCR")
    private Date closedDate;

    /** Ghi chú */
    @Excel(name = "Ghi chú", width = 40)
    @Schema(description = "Ghi chú")
    private String notes;
}
