package org.jeecg.modules.planning.entity;

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
 * @Description: Vị trí linh kiện trên PCB (Reference Designator)
 * @Author: BMad
 * @Date: 2026-02-26
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Vị trí linh kiện trên PCB (Reference Designator)")
@TableName("wh_bom_item_refdes")
public class BomItemRefDes implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "ID")
    private String id;

    /** FK tới wh_bom_item.id */
    @Schema(description = "FK tới wh_bom_item.id")
    private String bomItemId;

    /** Ký hiệu vị trí: C1, R5, U3, Q2... */
    @Schema(description = "Ký hiệu vị trí: C1, R5, U3, Q2...")
    private String refDesignator;

    /** Toạ độ X trên PCB (mm) */
    @Schema(description = "Toạ độ X trên PCB (mm)")
    private BigDecimal positionX;

    /** Toạ độ Y trên PCB (mm) */
    @Schema(description = "Toạ độ Y trên PCB (mm)")
    private BigDecimal positionY;

    /** Góc xoay (độ) */
    @Schema(description = "Góc xoay (độ)")
    private BigDecimal rotation;

    /** Layer: top, bottom */
    @Schema(description = "Layer: top, bottom")
    private String layer;
}
