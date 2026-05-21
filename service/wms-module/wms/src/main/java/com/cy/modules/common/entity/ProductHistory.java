package com.cy.modules.common.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: Lịch sử sản phẩm
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="Lịch sử sản phẩm")
@TableName("product_history")
public class ProductHistory extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID sản phẩm */
    @Excel(name="ID sản phẩm",width=25)
    @Schema(description = "ID sản phẩm")
    private java.lang.String productId;

    /** Hành động (TẠO MỚI, CẬP NHẬT, XÓA) */
    @Excel(name="Hành động",width=15, dicCode="product_history_action")
    @Schema(description = "Hành động")
    private java.lang.String action;

    /** Dữ liệu cũ (định dạng JSON) */
    @Excel(name="Dữ liệu cũ",width=50)
    @Schema(description = "Dữ liệu cũ")
    private java.lang.String oldData;

    /** Dữ liệu mới (định dạng JSON) */
    @Excel(name="Dữ liệu mới",width=50)
    @Schema(description = "Dữ liệu mới")
    private java.lang.String newData;

    /** ID người thực hiện hành động */
    @Excel(name="ID người dùng",width=25)
    @Schema(description = "ID người dùng")
    private java.lang.String userId;
}