package org.jeecg.modules.test.seata.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
/**
 * @Description: product
 * @author: zyf
 * @date: 2022/01/24
 * @version: V1.0
 */
@Data
@Builder
@TableName("product")
public class SeataProduct {

    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     * price
     */
    private BigDecimal price;
    /**
     * in stock
     */
    private Integer stock;

    private Date lastUpdateTime;
}