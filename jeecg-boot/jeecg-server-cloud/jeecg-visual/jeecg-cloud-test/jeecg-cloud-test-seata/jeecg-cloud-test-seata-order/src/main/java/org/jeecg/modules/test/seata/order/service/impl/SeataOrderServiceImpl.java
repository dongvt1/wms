package org.jeecg.modules.test.seata.order.service.impl;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;

import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.test.seata.order.dto.PlaceOrderRequest;
import org.jeecg.modules.test.seata.order.entity.SeataOrder;
import org.jeecg.modules.test.seata.order.enums.OrderStatus;
import org.jeecg.modules.test.seata.order.feign.AccountClient;
import org.jeecg.modules.test.seata.order.feign.ProductClient;
import org.jeecg.modules.test.seata.order.mapper.SeataOrderMapper;
import org.jeecg.modules.test.seata.order.service.SeataOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.math.BigDecimal;

/**
 * @Description: Order service class
 * @author: zyf
 * @date: 2022/01/24
 * @version: V1.0
 */
@Slf4j
@Service
public class SeataOrderServiceImpl implements SeataOrderService {

    @Resource
    private SeataOrderMapper orderMapper;
    @Resource
    private AccountClient accountClient;
    @Resource
    private ProductClient productClient;

    @DS("order")
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional
    public void placeOrder(PlaceOrderRequest request) {
        log.info("xid:"+RootContext.getXID());
        log.info("=============ORDER START=================");
        Long userId = request.getUserId();
        Long productId = request.getProductId();
        Integer count = request.getCount();
        log.info("Receive order request,user:{}, commodity:{},quantity:{}", userId, productId, count);


        SeataOrder order = SeataOrder.builder()
                .userId(userId)
                .productId(productId)
                .status(OrderStatus.INIT)
                .count(count)
                .build();

        orderMapper.insert(order);
        log.info("Order generation in one stage，Waiting for inventory deduction payment");
        // Deduct inventory and calculate total price
        BigDecimal amount = productClient.reduceStock(productId, count);
        // Deduction balance
        String str = accountClient.reduceBalance(userId, amount);
        // feignThe response is re-encapsulated，Determine to roll back the main transaction
        JSONObject jsonObject = JSONObject.parseObject(str);
        if (jsonObject.getInteger("code") != 200) {
            throw new RuntimeException();
        }

        order.setStatus(OrderStatus.SUCCESS);
        order.setTotalPrice(amount);
        orderMapper.updateById(order);
        log.info("The order has been successfully placed");
        log.info("=============ORDER END=================");
    }
}