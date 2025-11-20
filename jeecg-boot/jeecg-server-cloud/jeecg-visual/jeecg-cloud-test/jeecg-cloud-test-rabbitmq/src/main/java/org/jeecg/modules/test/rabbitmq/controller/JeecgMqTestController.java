package org.jeecg.modules.test.rabbitmq.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

import org.jeecg.boot.starter.rabbitmq.client.RabbitMqClient;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.base.BaseMap;
import org.jeecg.modules.test.rabbitmq.constant.CloudConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.hutool.core.util.RandomUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;


/**
 * RabbitMqClientSend message
 * @author: zyf
 * @date: 2022/04/21
 */
@RestController
@RequestMapping("/sys/test")
@Tag(name = "【microservices】MQUnit testing")
public class JeecgMqTestController {

    @Autowired
    private RabbitMqClient rabbitMqClient;


    /**
     * Test method：Quick click to sendMQinformation
     *  观察三个接受者如何分配处理information：HelloReceiver1、HelloReceiver2、HelloReceiver3，will be distributed evenly
     *
     * @param req
     * @return
     */
    @GetMapping(value = "/rabbitmq")
    @Operation(summary = "testrabbitmq")
    public Result<?> rabbitMqClientTest(HttpServletRequest req) {
        //rabbitmqinformation队列test
        BaseMap map = new BaseMap();
        map.put("orderId", RandomUtil.randomNumbers(10));
        rabbitMqClient.sendMessage(CloudConstant.MQ_JEECG_PLACE_ORDER, map);
        rabbitMqClient.sendMessage(CloudConstant.MQ_JEECG_PLACE_ORDER_TIME, map,10);
        return Result.OK("MQSend message成功");
    }

    @GetMapping(value = "/rabbitmq2")
    @Operation(summary = "rabbitmqinformation总线test")
    public Result<?> rabbitmq2(HttpServletRequest req) {

        //rabbitmqinformation总线test
        BaseMap params = new BaseMap();
        params.put("orderId", "123456");
        rabbitMqClient.publishEvent(CloudConstant.MQ_DEMO_BUS_EVENT, params);
        return Result.OK("MQSend message成功");
    }
}
