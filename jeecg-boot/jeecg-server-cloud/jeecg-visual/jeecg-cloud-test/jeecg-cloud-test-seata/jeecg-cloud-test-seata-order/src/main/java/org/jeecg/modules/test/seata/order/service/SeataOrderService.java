package org.jeecg.modules.test.seata.order.service;


import org.jeecg.modules.test.seata.order.dto.PlaceOrderRequest;

/**
 * @Description: Order interface
 * @author: zyf
 * @date: 2022/01/24
 * @version: V1.0
 */
public interface SeataOrderService {
    /**
     * Place an order
     *
     * @param placeOrderRequest Order request parameters
     */
    void placeOrder(PlaceOrderRequest placeOrderRequest);
}
