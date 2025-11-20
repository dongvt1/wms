package org.jeecg.modules.message.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import com.alibaba.fastjson.JSONObject;
import org.jeecg.common.base.BaseMap;
import org.jeecg.common.constant.WebsocketConst;
import org.jeecg.common.modules.redis.client.JeecgRedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author scott
 * @Date 2019/11/29 9:41
 * @Description: This annotation is equivalent to setting accessURL
 */
@Component
@Slf4j
@ServerEndpoint("/websocket/{userId}")
public class WebSocket {
    
    /**Thread safetyMap*/
    private static ConcurrentHashMap<String, Session> sessionPool = new ConcurrentHashMap<>();

    /**
     * RedisTrigger listener name
     */
    public static final String REDIS_TOPIC_NAME = "socketHandler";

    //Avoid a null pointer on the first call
    private static JeecgRedisClient jeecgRedisClient;
    @Autowired
    private void setJeecgRedisClient(JeecgRedisClient jeecgRedisClient){
        WebSocket.jeecgRedisClient = jeecgRedisClient;
    }


    //==========【websocketaccept、Push messages and other methods —— Specific service node pushwsinformation】========================================================================================
    @OnOpen
    public void onOpen(Session session, @PathParam(value = "userId") String userId) {
        try {
            sessionPool.put(userId, session);
            log.debug("【system WebSocket】There is a new connection，The total is:" + sessionPool.size());
        } catch (Exception e) {
        }
    }

    @OnClose
    public void onClose(@PathParam("userId") String userId) {
        try {
            sessionPool.remove(userId);
            log.debug("【system WebSocket】Lost connection，The total is:" + sessionPool.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ws推送information
     *
     * @param userId
     * @param message
     */
    public void pushMessage(String userId, String message) {
        for (Map.Entry<String, Session> item : sessionPool.entrySet()) {
            //userId keyvalue= {userid + "_"+ Log intokenofmd5string}
            //TODO vue2unchangedkeynew rules，Does not affect logic for the time being
            if (item.getKey().contains(userId)) {
                Session session = item.getValue();
                try {
                    //update-begin-author:taoyan date:20211012 for: websocketReport an error https://gitee.com/jeecg/jeecg-boot/issues/I4C0MU
                    synchronized (session){
                        log.debug("【system WebSocket】推送单人information:" + message);
                        session.getBasicRemote().sendText(message);
                    }
                    //update-end-author:taoyan date:20211012 for: websocketReport an error https://gitee.com/jeecg/jeecg-boot/issues/I4C0MU
                } catch (Exception e) {
                    log.error(e.getMessage(),e);
                }
            }
        }
    }

    /**
     * ws遍历群发information
     */
    public void pushMessage(String message) {
        try {
            for (Map.Entry<String, Session> item : sessionPool.entrySet()) {
                try {
                    item.getValue().getAsyncRemote().sendText(message);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
            log.debug("【system WebSocket】群发information:" + message);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }


    /**
     * wsaccept客户端information
     */
    @OnMessage
    public void onMessage(String message, @PathParam(value = "userId") String userId) {
        if(!"ping".equals(message) && !WebsocketConst.CMD_CHECK.equals(message)){
            log.debug("【system WebSocket】收到客户端information:" + message);
        }else{
            log.debug("【system WebSocket】收到客户端information:" + message);
            //update-begin---author:wangshuai---date:2024-05-07---for:【issues/1161】front endwebsocketMonitoring does not work due to heartbeat---
            this.sendMessage(userId, "ping");
            //update-end---author:wangshuai---date:2024-05-07---for:【issues/1161】front endwebsocketMonitoring does not work due to heartbeat---
        }
        
//        //------------------------------------------------------------------------------
//        JSONObject obj = new JSONObject();
//        //Business type
//        obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_CHECK);
//        //information内容
//        obj.put(WebsocketConst.MSG_TXT, "heartbeat response");
//        this.pushMessage(userId, obj.toJSONString());
//        //------------------------------------------------------------------------------
    }

    /**
     * Configuration error message handling
     *
     * @param session
     * @param t
     */
    @OnError
    public void onError(Session session, Throwable t) {
        log.warn("【system WebSocket】information出现错误");
        t.printStackTrace();
    }
    //==========【system WebSocketaccept、Push messages and other methods —— Specific service node pushwsinformation】========================================================================================
    

    //==========【useredispublish-subscribe model——推送information】========================================================================================
    /**
     * 后台发送information到redis
     *
     * @param message
     */
    public void sendMessage(String message) {
        //log.debug("【system WebSocket】广播information:" + message);
        BaseMap baseMap = new BaseMap();
        baseMap.put("userId", "");
        baseMap.put("message", message);
        jeecgRedisClient.sendMessage(WebSocket.REDIS_TOPIC_NAME, baseMap);
    }

    /**
     * 此为单点information redis
     *
     * @param userId
     * @param message
     */
    public void sendMessage(String userId, String message) {
        BaseMap baseMap = new BaseMap();
        baseMap.put("userId", userId);
        baseMap.put("message", message);
        jeecgRedisClient.sendMessage(WebSocket.REDIS_TOPIC_NAME, baseMap);
    }

    /**
     * 此为单点information(multiple people) redis
     *
     * @param userIds
     * @param message
     */
    public void sendMessage(String[] userIds, String message) {
        for (String userId : userIds) {
            sendMessage(userId, message);
        }
    }
    //=======【useredispublish-subscribe model——推送information】==========================================================================================
    
}