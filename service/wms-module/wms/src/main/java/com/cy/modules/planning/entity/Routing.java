package com.cy.modules.planning.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: Quy trình công nghệ (Routing)
 * @Author: BMad
 * @Date: 2026-02-25
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Quy trình công nghệ (Routing)")
@TableName("pl_routing")
public class Routing extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Mã quy trình */
    @Excel(name = "Mã quy trình", width = 20)
    @Schema(description = "Mã quy trình")
    private String routingCode;

    /** Tên quy trình */
    @Excel(name = "Tên quy trình", width = 30)
    @Schema(description = "Tên quy trình")
    private String routingName;

    /** ID sản phẩm */
    @Excel(name = "Sản phẩm", width = 25, dictTable = "product", dicText = "name", dicCode = "id")
    @Schema(description = "ID sản phẩm")
    private String productId;

    /** ID BOM (tuỳ chọn) */
    @Excel(name = "BOM", width = 25, dictTable = "pl_bom", dicText = "bom_name", dicCode = "id")
    @Schema(description = "ID BOM liên quan (tuỳ chọn)")
    private String bomId;

    /** Phiên bản */
    @Excel(name = "Phiên bản", width = 10)
    @Schema(description = "Phiên bản quy trình")
    private String version;

    /** Trạng thái: active, inactive, draft */
    @Excel(name = "Trạng thái", width = 12)
    @Schema(description = "Trạng thái: active, inactive, draft")
    private String status;

    /** Tổng thời gian sản xuất (giờ) – tính toán từ các bước */
    @Excel(name = "Tổng LT (giờ)", width = 15)
    @Schema(description = "Tổng thời gian sản xuất (giờ)")
    private BigDecimal totalLeadTimeHours;

    /** Ghi chú */
    @Excel(name = "Ghi chú", width = 40)
    @Schema(description = "Ghi chú")
    private String notes;
}
