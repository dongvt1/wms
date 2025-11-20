package org.jeecg.modules.test.rabbitmq.listener;

import org.jeecg.boot.starter.rabbitmq.core.BaseRabbiMqHandler;
import org.jeecg.boot.starter.rabbitmq.listenter.MqListener;
import org.jeecg.common.annotation.RabbitComponent;
import org.jeecg.common.base.BaseMap;
import org.jeecg.modules.test.rabbitmq.constant.CloudConstant;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.web.client.RestTemplate;

import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;

/**
 * Define the recipient（can be definedNrecipients，Messages will be sent evenly toNof recipients）
 *
 * RabbitMqrecipient1
 * （@RabbitListenerOn the declaration class，A class can only listen to one queue）
 * @author: zyf
 * @date: 2022/04/21
 */
@Slf4j
@RabbitListener(queues = CloudConstant.MQ_JEECG_PLACE_ORDER)
@RabbitComponent(value = "helloReceiver1")
public class HelloReceiver1 extends BaseRabbiMqHandler<BaseMap> {

    @Autowired
    private  RestTemplate restTemplate;

    @RabbitHandler
    public void onMessage(BaseMap baseMap, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        super.onMessage(baseMap, deliveryTag, channel, new MqListener<BaseMap>() {
            @Override
            public void handler(BaseMap map, Channel channel) {
                //business processing
                String orderId = map.get("orderId").toString();
                log.info("【I'm the handler1】 MQ Receiver1，orderId : " + orderId);
               // jeecgTestClient.getMessage("JEECG");
                try{
//                    HttpHeaders requestHeaders = new HttpHeaders();
//                   requestHeaders.add("X-Access-Token", "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE2MzExOTcyOTEsInVzZXJuYW1lIjoiYWRtaW4ifQ.N8mJvwzb4G0i3vYF9A2Bmf5cDKb1LDnOp1RwtpYEu1E");
//                    requestHeaders.add("content-type", MediaType.APPLICATION_JSON_UTF8.toString());
//                    MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
//                    requestBody.add("name", "test");
//                    HttpEntity< MultiValueMap<String, String> > requestEntity = new HttpEntity(requestBody, requestHeaders);
//                    //post
//                    ResponseEntity<String> responseEntity = restTemplate.postForEntity("http://localhost:7002/test/getMessage", requestEntity, String.class);
//                    System.out.println(" responseEntity :"+responseEntity.getBody());
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });
    }

}