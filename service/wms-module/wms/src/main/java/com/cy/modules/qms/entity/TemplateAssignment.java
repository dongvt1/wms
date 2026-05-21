package com.cy.modules.qms.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @Description: Gán template cho sản phẩm/nhóm SP
 * @Author: QMS
 * @Date: 2026-03-15
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Gán template cho sản phẩm/nhóm SP")
@TableName("qms_template_assignment")
public class TemplateAssignment implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Khóa chính */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "ID")
    private String id;

    /** FK → qms_inspection_template */
    @Schema(description = "FK → qms_inspection_template")
    private String templateId;

    /** Loại gán: product, product_group, default */
    @Schema(description = "Loại gán: product, product_group, default")
    private String assignmentType;

    /** ID sản phẩm hoặc nhóm SP (NULL nếu default) */
    @Schema(description = "ID sản phẩm hoặc nhóm SP (NULL nếu default)")
    private String targetId;

    /** 1=đang áp dụng */
    @Schema(description = "1=đang áp dụng")
    private Integer isActive;

    /** Người tạo */
    @Schema(description = "Người tạo")
    private String createBy;

    /** Thời gian tạo */
    @JsonFormat(timezone = "GMT+7", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Thời gian tạo")
    private Date createTime;

    /** Mã tổ chức (multi-tenant) */
    @Schema(description = "Mã tổ chức (multi-tenant)")
    @TableField("sys_org_code")
    private String sysOrgCode;
}
