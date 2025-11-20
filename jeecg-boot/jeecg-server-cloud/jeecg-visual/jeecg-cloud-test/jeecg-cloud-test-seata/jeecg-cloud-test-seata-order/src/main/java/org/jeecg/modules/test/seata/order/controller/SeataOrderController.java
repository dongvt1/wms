package org.jeecg.modules.test.seata.order.controller;

/**
 * @Description: TODO
 * @author: zyf
 * @date: 2022/01/24
 * @version: V1.0
 */
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import org.jeecg.modules.test.seata.order.dto.PlaceOrderRequest;
import org.jeecg.modules.test.seata.order.service.SeataOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test/seata/order")
@Tag(name = "seatatest")
public class SeataOrderController {

    @Autowired
    private SeataOrderService orderService;

    /**
     * Free order
     */
    @PostMapping("/placeOrder")
    @Operation(summary = "Free order")
    public String placeOrder(@Validated @RequestBody PlaceOrderRequest request) {
        orderService.placeOrder(request);
        return "Order placed successfully";
    }

    /**
     * test商品in stock不足-Exception rollback
     */
    @PostMapping("/test1")
    @Operation(summary = "test商品in stock不足")
    public String test1() {
        //Product unit price10Yuan，in stock20indivual,User balance50Yuan，Simulate a one-time purchase22indivual。 期望Exception rollback
        orderService.placeOrder(new PlaceOrderRequest(1L, 1L, 22));
        return "Order placed successfully";
    }

    /**
     * test用户账户余额不足-Exception rollback
     */
    @PostMapping("/test2")
    @Operation(summary = "test用户账户余额不足")
    public String test2() {
        //Product unit price10Yuan，in stock20indivual，User balance50Yuan，Simulate a one-time purchase6indivual。 期望Exception rollback
        orderService.placeOrder(new PlaceOrderRequest(1L, 1L, 6));
        return "Order placed successfully";
    }
}