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
 * @Description: Kệ kho
 * @Author: BMad
 * @Date: 2025-11-17
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="Kệ kho")
@TableName("warehouse_shelf")
public class WarehouseShelf extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID khu vực */
    @Excel(name="ID khu vực",width=25)
    @Schema(description = "ID khu vực")
    private java.lang.String areaId;

    /** Mã kệ */
    @Excel(name="Mã kệ",width=25)
    @Schema(description = "Mã kệ")
    private java.lang.String shelfCode;

    /** Tên kệ */
    @Excel(name="Tên kệ",width=25)
    @Schema(description = "Tên kệ")
    private java.lang.String shelfName;

    /** Mô tả */
    @Excel(name="Mô tả",width=50)
    @Schema(description = "Mô tả")
    private java.lang.String description;

    /** Trạng thái (0: Không hoạt động, 1: Hoạt động) */
    @Excel(name="Trạng thái",width=15, dicCode="warehouse_status")
    @Schema(description = "Trạng thái")
    private java.lang.Integer status;

    /** Sức chứa (số vị trí tối đa) */
    @Excel(name="Sức chứa",width=15)
    @Schema(description = "Sức chứa")
    private java.lang.Integer capacity;

    /** Số vị trí đã sử dụng */
    @Excel(name="Số vị trí đã sử dụng",width=15)
    @Schema(description = "Số vị trí đã sử dụng")
    private java.lang.Integer usedCapacity;

    /** Tên khu vực (dùng cho hiển thị) */
    @Excel(name="Tên khu vực",width=25)
    @Schema(description = "Tên khu vực")
    @TableField(exist = false)
    private java.lang.String areaName;
}