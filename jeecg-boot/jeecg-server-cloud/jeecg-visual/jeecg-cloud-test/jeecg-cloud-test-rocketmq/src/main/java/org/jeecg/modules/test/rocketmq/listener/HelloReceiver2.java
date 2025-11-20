package org.jeecg.modules.test.rocketmq.listener;//package org.jeecg.modules.cloud.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.jeecg.common.base.BaseMap;
import org.jeecg.modules.test.rocketmq.constant.CloudConstant;
import org.springframework.stereotype.Component;

/**
 * Define the recipient（can be definedNrecipients，Messages will be sent evenly toNof recipients）
 *
 * RabbitMqrecipient2
 * （@RabbitListenerOn the declaration class，A class can only listen to one queue）
 * @author: zyf
 * @date: 2022/04/21
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = CloudConstant.MQ_JEECG_PLACE_ORDER, consumerGroup = "helloReceiver2")
public class HelloReceiver2 implements RocketMQListener<BaseMap> {

    public void onMessage(BaseMap baseMap) {
        log.info("helloReceiver2receive messages：" + baseMap);
    }

}