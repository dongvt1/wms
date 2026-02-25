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
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: Bước trong quy trình công nghệ (Routing Step)
 * @Author: BMad
 * @Date: 2026-02-25
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Bước trong quy trình công nghệ")
@TableName("wh_routing_step")
public class RoutingStep implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "ID")
    private String id;

    /** ID quy trình */
    @Schema(description = "ID quy trình")
    private String routingId;

    /** Thứ tự bước */
    @Excel(name = "TT", width = 6)
    @Schema(description = "Thứ tự bước")
    private Integer stepOrder;

    /** Tên bước */
    @Excel(name = "Tên bước", width = 25)
    @Schema(description = "Tên bước (Bào, Cắt, Sơn, Lắp ráp...)")
    private String stepName;

    /** ID trung tâm sản xuất */
    @Excel(name = "TTSX", width = 25, dictTable = "wh_work_center", dicText = "center_name", dicCode = "id")
    @Schema(description = "ID trung tâm sản xuất thực hiện bước này")
    private String workCenterId;

    /** Thời gian chuẩn bị (phút) */
    @Excel(name = "Setup (phút)", width = 14)
    @Schema(description = "Thời gian chuẩn bị (phút)")
    private Integer setupTimeMinutes;

    /** Thời gian chạy/đơn vị (phút) */
    @Excel(name = "Run/ĐV (phút)", width = 14)
    @Schema(description = "Thời gian chạy trên mỗi đơn vị sản phẩm (phút)")
    private Integer runTimeMinutes;

    /** Thời gian chờ (phút) */
    @Excel(name = "Wait (phút)", width = 14)
    @Schema(description = "Thời gian chờ (phút)")
    private Integer waitTimeMinutes;

    /** Thời gian di chuyển (phút) */
    @Excel(name = "Move (phút)", width = 14)
    @Schema(description = "Thời gian di chuyển tới bước tiếp theo (phút)")
    private Integer moveTimeMinutes;

    /** Tổng lead time bước (giờ) */
    @Excel(name = "Lead Time (giờ)", width = 15)
    @Schema(description = "Tổng lead time bước = (setup + run + wait + move) chia 60")
    private BigDecimal leadTimeHours;

    /** Mô tả */
    @Excel(name = "Mô tả", width = 40)
    @Schema(description = "Mô tả")
    private String description;
}
