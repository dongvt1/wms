package com.cy.modules.warehouse.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.io.Serializable;
import java.util.Date;

/**
 * Bảng cảnh báo tồn kho
 */
@Data
@TableName("inventory_alerts")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(name = "Bảng cảnh báo tồn kho", description = "Bảng cảnh báo tồn kho")
public class InventoryAlert extends JeecgEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Khóa chính */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "Khóa chính")
    private String id;

    /** ID sản phẩm */
    @Excel(name = "ID sản phẩm", width = 15)
    @Schema(description = "ID sản phẩm")
    private String productId;

    /** Tên sản phẩm */
    @Excel(name = "Tên sản phẩm", width = 30)
    @Schema(description = "Tên sản phẩm")
    @Dict(dictTable = "product", dicCode = "name", dicText = "product_name")
    private String productName;

    /** Mã sản phẩm */
    @Excel(name = "Mã sản phẩm", width = 15)
    @Schema(description = "Mã sản phẩm")
    @Dict(dictTable = "product", dicCode = "code", dicText = "product_code")
    private String productCode;

    /** Loại cảnh báo */
    @Excel(name = "Loại cảnh báo", width = 15, dicCode = "inventory_alert_type")
    @Schema(description = "Loại cảnh báo")
    @Dict(dicCode = "inventory_alert_type")
    private String alertType;

    /** Số lượng hiện tại */
    @Excel(name = "Số lượng hiện tại", width = 15)
    @Schema(description = "Số lượng hiện tại")
    private Integer currentQuantity;

    /** Ngưỡng cảnh báo */
    @Excel(name = "Ngưỡng cảnh báo", width = 15)
    @Schema(description = "Ngưỡng cảnh báo")
    private Integer thresholdValue;

    /** Trạng thái cảnh báo */
    @Excel(name = "Trạng thái cảnh báo", width = 15, dicCode = "inventory_alert_status")
    @Schema(description = "Trạng thái cảnh báo")
    @Dict(dicCode = "inventory_alert_status")
    private String alertStatus;

    /** Thời gian giải quyết */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Thời gian giải quyết")
    @Excel(name = "Thời gian giải quyết", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    private Date resolvedAt;

    /** Người giải quyết */
    @Excel(name = "Người giải quyết", width = 15)
    @Schema(description = "Người giải quyết")
    private String resolvedBy;

    /** Thời gian tạo */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Thời gian tạo")
    @Excel(name = "Thời gian tạo", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;
}