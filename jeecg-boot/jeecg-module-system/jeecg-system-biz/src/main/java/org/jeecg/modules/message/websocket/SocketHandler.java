package org.jeecg.modules.message.websocket;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.base.BaseMap;
import org.jeecg.common.constant.CommonSendStatus;
import org.jeecg.common.modules.redis.listener.JeecgRedisListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Listen for messages(passredispublishsubscribe，push message)
 * This plan：Solve cluster deployment problems，Multiple instance nodes（That is to say, the sending end first sends the message toredismiddle，Each service node receivesredisinformation，Trigger specificwspush）
 * @author: jeecg-boot
 */
@Slf4j
@Component(WebSocket.REDIS_TOPIC_NAME)
public class SocketHandler implements JeecgRedisListener {

    @Autowired
    private WebSocket webSocket;

    @Override
    public void onMessage(BaseMap map) {
        log.debug("【Redispublishsubscribe模式】redis Listener: {}，parameter：{}",WebSocket.REDIS_TOPIC_NAME, map.toString());

        String userId = map.get("userId");
        String message = map.get("message");
        if (ObjectUtil.isNotEmpty(userId)) {
            //pc端informationpush具体人
            webSocket.pushMessage(userId, message);
            //app端informationpush具体人
            webSocket.pushMessage(userId+CommonSendStatus.APP_SESSION_SUFFIX, message);
        } else {
            //push全部
            webSocket.pushMessage(message);
        }

    }
}