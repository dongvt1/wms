package com.cy.modules.planning.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @Description: Chi tiết thay đổi trong ECN
 * @Author: BMad
 * @Date: 2026-02-26
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Chi tiết thay đổi trong ECN")
@TableName("pl_ecn_item")
public class EcnItem implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "ID")
    private String id;

    /** FK tới pl_ecn.id */
    @Schema(description = "FK tới pl_ecn.id")
    private String ecnId;

    /** Loại thay đổi: add, remove, modify */
    @Schema(description = "Loại thay đổi: add, remove, modify")
    private String changeType;

    /** FK tới pl_bom_item.id (cho modify/remove) */
    @Schema(description = "FK tới pl_bom_item.id (cho modify/remove)")
    private String bomItemId;

    /** ID linh kiện cũ */
    @Schema(description = "ID linh kiện cũ")
    private String oldMaterialId;

    /** ID linh kiện mới */
    @Schema(description = "ID linh kiện mới")
    private String newMaterialId;

    /** Số lượng cũ */
    @Schema(description = "Số lượng cũ")
    private BigDecimal oldQuantity;

    /** Số lượng mới */
    @Schema(description = "Số lượng mới")
    private BigDecimal newQuantity;

    /** Lý do thay đổi dòng này */
    @Schema(description = "Lý do thay đổi")
    private String reason;
}
