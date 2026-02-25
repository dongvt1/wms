package org.jeecg.modules.warehouse.entity;

import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: Khu vực kho
 * @Author: BMad
 * @Date: 2025-11-17
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="Khu vực kho")
@TableName("warehouse_area")
public class WarehouseArea extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Mã khu vực */
    @Excel(name="Mã khu vực",width=25)
    @Schema(description = "Mã khu vực")
    private java.lang.String areaCode;

    /** Tên khu vực */
    @Excel(name="Tên khu vực",width=25)
    @Schema(description = "Tên khu vực")
    private java.lang.String areaName;

    /** Mô tả */
    @Excel(name="Mô tả",width=50)
    @Schema(description = "Mô tả")
    private java.lang.String description;

    /** Trạng thái (0: Không hoạt động, 1: Hoạt động) */
    @Excel(name="Trạng thái",width=15, dicCode="warehouse_status")
    @Schema(description = "Trạng thái")
    private java.lang.Integer status;

    /** Sức chứa (số lượng kệ tối đa) */
    @Excel(name="Sức chứa",width=15)
    @Schema(description = "Sức chứa")
    private java.lang.Integer capacity;

    /** Số kệ đã sử dụng */
    @Excel(name="Số kệ đã sử dụng",width=15)
    @Schema(description = "Số kệ đã sử dụng")
    private java.lang.Integer usedCapacity;
}