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
 * @Description: Phiếu kiểm tra chất lượng đầu vào (IQC)
 * @Author: BMad
 * @Date: 2026-02-25
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Phiếu kiểm tra chất lượng đầu vào (IQC)")
@TableName("qms_iqc_inspection")
public class IqcInspection extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Mã phiếu IQC */
    @Excel(name = "Mã phiếu", width = 20)
    @Schema(description = "Mã phiếu IQC (IQCyyyyMMddNNN)")
    private String inspectionCode;

    /** Sản phẩm */
    @Excel(name = "Sản phẩm", width = 25, dictTable = "product", dicText = "name", dicCode = "id")
    @Schema(description = "ID sản phẩm / nguyên vật liệu")
    private String productId;

    /** Nhà cung cấp */
    @Excel(name = "Nhà cung cấp", width = 25, dictTable = "supplier", dicText = "name", dicCode = "id")
    @Schema(description = "ID nhà cung cấp")
    private String supplierId;

    /** Phiếu nhập kho liên kết */
    @Schema(description = "ID phiếu nhập kho liên kết")
    private String stockTransactionId;

    /** Mẫu checklist */
    @Schema(description = "ID mẫu checklist áp dụng")
    private String templateId;

    /** Số lượng nhận */
    @Excel(name = "SL nhận", width = 15)
    @Schema(description = "Số lượng nhận")
    private BigDecimal quantityReceived;

    /** Số lượng đạt */
    @Excel(name = "SL đạt", width = 15)
    @Schema(description = "Số lượng đạt")
    private BigDecimal quantityPassed;

    /** Số lượng không đạt */
    @Excel(name = "SL không đạt", width = 15)
    @Schema(description = "Số lượng không đạt")
    private BigDecimal quantityFailed;

    /** Người kiểm tra */
    @Excel(name = "Người KT", width = 20)
    @Schema(description = "Người kiểm tra")
    private String inspector;

    /** Ngày kiểm tra */
    @Excel(name = "Ngày KT", width = 18, format = "yyyy-MM-dd")
    @JsonFormat(timezone = "GMT+7", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Ngày kiểm tra")
    private Date inspectionDate;

    /** Trạng thái */
    @Excel(name = "Trạng thái", width = 18)
    @Schema(description = "Trạng thái: draft, in_progress, passed, failed, conditional")
    private String status;

    /** Ghi chú */
    @Excel(name = "Ghi chú", width = 40)
    @Schema(description = "Ghi chú")
    private String notes;
}
