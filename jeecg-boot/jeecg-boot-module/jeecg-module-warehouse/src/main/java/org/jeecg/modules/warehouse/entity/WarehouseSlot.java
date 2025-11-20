package org.jeecg.modules.warehouse.entity;

import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: Vị trí trong kho
 * @Author: BMad
 * @Date: 2025-11-17
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="Vị trí trong kho")
@TableName("warehouse_slot")
public class WarehouseSlot extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID kệ */
    @Excel(name="ID kệ",width=25)
    @Schema(description = "ID kệ")
    private java.lang.String shelfId;

    /** Mã vị trí */
    @Excel(name="Mã vị trí",width=25)
    @Schema(description = "Mã vị trí")
    private java.lang.String slotCode;

    /** Tên vị trí */
    @Excel(name="Tên vị trí",width=25)
    @Schema(description = "Tên vị trí")
    private java.lang.String slotName;

    /** Vị trí (hàng, cột) */
    @Excel(name="Vị trí",width=25)
    @Schema(description = "Vị trí")
    private java.lang.String position;

    /** Mô tả */
    @Excel(name="Mô tả",width=50)
    @Schema(description = "Mô tả")
    private java.lang.String description;

    /** Trạng thái (0: Trống, 1: Đã đặt hàng, 2: Đã có hàng) */
    @Excel(name="Trạng thái",width=15, dicCode="slot_status")
    @Schema(description = "Trạng thái")
    private java.lang.Integer status;

    /** Sức chứa (số lượng sản phẩm tối đa) */
    @Excel(name="Sức chứa",width=15)
    @Schema(description = "Sức chứa")
    private java.lang.Integer capacity;

    /** Sản phẩm đã đặt */
    @Excel(name="Sản phẩm đã đặt",width=15)
    @Schema(description = "Sản phẩm đã đặt")
    private java.lang.Integer usedCapacity;

    /** Mã sản phẩm đã đặt */
    @Excel(name="Mã sản phẩm đã đặt",width=25)
    @Schema(description = "Mã sản phẩm đã đặt")
    private java.lang.String productCode;

    /** Tên kệ (dùng cho hiển thị) */
    @Excel(name="Tên kệ",width=25)
    @Schema(description = "Tên kệ")
    @TableField(exist = false)
    private java.lang.String shelfName;

    /** Tên khu vực (dùng cho hiển thị) */
    @Excel(name="Tên khu vực",width=25)
    @Schema(description = "Tên khu vực")
    @TableField(exist = false)
    private java.lang.String areaName;
}