package org.jeecg.modules.test.seata.order.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

/**
 * Distributed transaction productsfeignclient
 * @author: zyf
 * @date: 2022/04/21
 */
@FeignClient(value ="seata-product")
public interface ProductClient {
    /**
     * deduction inventory
     *
     * @param productId
     * @param count
     * @return
     */
    @PostMapping("/test/seata/product/reduceStock")
    BigDecimal reduceStock(@RequestParam("productId") Long productId, @RequestParam("count") Integer count);
}
