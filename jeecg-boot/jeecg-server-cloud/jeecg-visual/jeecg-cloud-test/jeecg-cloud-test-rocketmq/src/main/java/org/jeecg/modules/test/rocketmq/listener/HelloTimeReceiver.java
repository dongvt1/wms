package org.jeecg.modules.test.rocketmq.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.jeecg.common.base.BaseMap;
import org.jeecg.modules.test.rocketmq.constant.CloudConstant;
import org.springframework.stereotype.Component;

/**
 * Define the recipient（can be definedNrecipients，Messages will be sent evenly toNof recipients）
 * @author: zyf
 * @date: 2022/04/21
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = CloudConstant.MQ_JEECG_PLACE_ORDER_TIME, consumerGroup = "helloTimeReceiver")
public class HelloTimeReceiver implements RocketMQListener<BaseMap> {

    public void onMessage(BaseMap baseMap) {
        log.info("helloTimeReceiverreceive messages：" + baseMap);
    }

}