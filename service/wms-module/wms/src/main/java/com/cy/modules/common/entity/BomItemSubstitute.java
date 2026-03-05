package com.cy.modules.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Description: Override linh kiện thay thế trong BOM Item (per-BOM)
 * @Author: BMad
 * @Date: 2026-03-05
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Override linh kiện thay thế trong BOM Item")
@TableName("pl_bom_item_substitute")
public class BomItemSubstitute implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "ID")
    private String id;

    /** ID BOM Item */
    @Schema(description = "ID BOM Item (FK → pl_bom_item.id)")
    private String bomItemId;

    /** ID vật tư thay thế */
    @Schema(description = "ID vật tư thay thế (FK → material.id)")
    private String substituteMaterialId;

    /** Thứ tự ưu tiên (1 = cao nhất) */
    @Schema(description = "Thứ tự ưu tiên (1 = cao nhất)")
    private Integer priority;

    /** Ghi chú */
    @Schema(description = "Ghi chú")
    private String notes;

    /** Thời gian tạo */
    private LocalDateTime createTime;

    /** Thời gian cập nhật */
    private LocalDateTime updateTime;

    // ── Transient fields (join query) ──
    /** Tên vật tư thay thế */
    @Schema(description = "Tên vật tư thay thế")
    private transient String substituteName;

    /** Mã vật tư thay thế */
    @Schema(description = "Mã vật tư thay thế")
    private transient String substituteCode;

    /** Đơn vị vật tư thay thế */
    @Schema(description = "Đơn vị vật tư thay thế")
    private transient String substituteUnit;
}
