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
 * RabbitMqrecipient3【I'm the handler3】
 * （@RabbitListenerDeclare class method，A class can listen to multiple queues）
 * @author: zyf
 * @date: 2022/04/21
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = CloudConstant.MQ_JEECG_PLACE_ORDER, consumerGroup = "helloReceiver3")
public class HelloReceiver3 implements RocketMQListener<BaseMap> {

    public void onMessage(BaseMap baseMap) {
        log.info("helloReceiver3receive messages：" + baseMap);
    }

}