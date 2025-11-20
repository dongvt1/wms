package org.jeecg.modules.test.seata.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import org.jeecg.modules.test.seata.order.enums.OrderStatus;

import java.math.BigDecimal;

/**
 * @Description: Order
 * @author: zyf
 * @date: 2022/01/24
 * @version: V1.0
 */
@Builder
@Data
@TableName("p_order")
public class SeataOrder {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * userID
     */
    private Long userId;
    /**
     * commodityID
     */
    private Long productId;
    /**
     * Order状态
     */
    private OrderStatus status;
    /**
     * quantity
     */
    private Integer count;
    /**
     * lump sum
     */
    private BigDecimal totalPrice;
}
