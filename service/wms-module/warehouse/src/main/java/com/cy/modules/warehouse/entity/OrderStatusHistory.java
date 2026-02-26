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
 * Bảng lịch sử trạng thái đơn hàng
 */
@Data
@TableName("order_status_history")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(name = "Bảng lịch sử trạng thái đơn hàng", description = "Bảng lịch sử trạng thái đơn hàng")
public class OrderStatusHistory extends JeecgEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Khóa chính */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "Khóa chính")
    private String id;

    /** ID đơn hàng */
    @Excel(name = "ID đơn hàng", width = 15)
    @Schema(description = "ID đơn hàng")
    private String orderId;

    /** Mã đơn hàng */
    @Excel(name = "Mã đơn hàng", width = 15)
    @Schema(description = "Mã đơn hàng")
    @Dict(dictTable = "orders", dicCode = "order_code", dicText = "order_code")
    private String orderCode;

    /** Trạng thái cũ */
    @Excel(name = "Trạng thái cũ", width = 15, dicCode = "order_status")
    @Schema(description = "Trạng thái cũ")
    @Dict(dicCode = "order_status")
    private String fromStatus;

    /** Trạng thái mới */
    @Excel(name = "Trạng thái mới", width = 15, dicCode = "order_status")
    @Schema(description = "Trạng thái mới")
    @Dict(dicCode = "order_status")
    private String toStatus;

    /** Lý do thay đổi trạng thái */
    @Excel(name = "Lý do thay đổi trạng thái", width = 30)
    @Schema(description = "Lý do thay đổi trạng thái")
    private String reason;

    /** Người thao tác */
    @Schema(description = "Người thao tác")
    @Excel(name = "Người thao tác", width = 15)
    private String userId;

    /** Thời gian thay đổi trạng thái */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Thời gian thay đổi trạng thái")
    @Excel(name = "Thời gian thay đổi trạng thái", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;
}