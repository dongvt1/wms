package org.jeecg.modules.test.seata.product.service;

import java.math.BigDecimal;

/**
 * @Description: Product interface
 * @author: zyf
 * @date: 2022/01/24
 * @version: V1.0
 */
public interface SeataProductService {
    /**
     * deduction inventory
     *
     * @param productId commodity ID
     * @param count    Deduction quantity
     * @return commodity总价
     */
    BigDecimal reduceStock(Long productId, Integer count);
}
