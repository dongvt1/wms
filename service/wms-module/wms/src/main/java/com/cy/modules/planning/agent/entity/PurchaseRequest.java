package com.cy.modules.planning.agent.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

/**
 * @Description: Yêu cầu mua hàng (Purchase Request)
 * @Author: AI Planning Agent
 * @Date: 2026-06-20
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Yêu cầu mua hàng")
@TableName("ap_purchase_request")
public class PurchaseRequest extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Mã PR (PRyyyyMMddNNN) */
    @Schema(description = "Mã PR")
    @TableField("pr_code")
    private String prCode;

    /** FK → ap_planning_order */
    @Schema(description = "ID đơn hàng")
    @TableField("order_id")
    private String orderId;

    /** Mã nguyên vật liệu */
    @Schema(description = "Mã nguyên vật liệu")
    @TableField("material_id")
    private String materialId;

    /** Tên nguyên vật liệu */
    @Schema(description = "Tên nguyên vật liệu")
    @TableField("material_name")
    private String materialName;

    /** Số lượng cần mua */
    @Schema(description = "Số lượng cần mua")
    @TableField("deficit_qty")
    private BigDecimal deficitQty;

    /** Ngày giao hàng yêu cầu */
    @Schema(description = "Ngày giao hàng yêu cầu")
    @TableField("required_date")
    private Date requiredDate;

    /** Thời gian giao hàng nhà cung cấp đã dùng */
    @Schema(description = "Thời gian giao hàng nhà cung cấp (ngày)")
    @TableField("supplier_lead_days")
    private Integer supplierLeadDays;

    /** Trạng thái: generated, submitted, confirmed, received */
    @Schema(description = "Trạng thái PR")
    private String status;

    /** Ngày giao hàng thực tế */
    @Schema(description = "Ngày giao hàng thực tế")
    @TableField("actual_delivery")
    private Date actualDelivery;

    /** Các phương án thay thế (JSON) */
    @Schema(description = "Phương án thay thế")
    private String alternatives;

    /** Mã tổ chức (multi-tenant) */
    @Schema(description = "Mã tổ chức")
    @TableField("sys_org_code")
    private String sysOrgCode;
}
