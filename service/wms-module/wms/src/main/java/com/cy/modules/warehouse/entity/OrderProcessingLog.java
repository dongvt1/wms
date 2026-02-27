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
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: Nhật ký xử lý đơn hàng
 * @Author: jeecg
 * @Date: 2025-11-21
 * @Version: V1.0
 */
@Data
@TableName("order_processing_logs")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(name="Đối tượng order_processing_logs", description="Nhật ký xử lý đơn hàng")
public class OrderProcessingLog implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "Khóa chính")
    private String id;

    @Schema(description = "Người tạo")
    private String createBy;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Ngày tạo")
    private Date createTime;

    @Schema(description = "Người cập nhật")
    private String updateBy;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Ngày cập nhật")
    private Date updateTime;

    @Schema(description = "Phòng ban")
    private String sysOrgCode;

    @Excel(name = "ID đơn hàng", width = 15)
    @Schema(description = "ID đơn hàng")
    private String orderId;

    @Excel(name = "Hành động", width = 20)
    @Schema(description = "Hành động xử lý (CONFIRM, CANCEL, SHIP, COMPLETE, v.v.)")
    private String action;

    @Excel(name = "Chi tiết", width = 50)
    @Schema(description = "Chi tiết xử lý")
    private String details;

    @Excel(name = "Trạng thái", width = 15, dicCode = "processing_log_status")
    @Dict(dicCode = "processing_log_status")
    @Schema(description = "Trạng thái xử lý (SUCCESS, FAILED, PENDING)")
    private String status;

    @Excel(name = "Thông báo lỗi", width = 50)
    @Schema(description = "Thông báo lỗi nếu xử lý thất bại")
    private String errorMessage;

    @Excel(name = "ID người dùng", width = 15)
    @Schema(description = "Người thực hiện hành động")
    private String userId;

    @Excel(name = "Thời gian xử lý", width = 15)
    @Schema(description = "Thời gian xử lý (mili giây)")
    private Integer processingTime;
}