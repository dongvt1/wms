package org.jeecg.modules.demo.mock.vxe.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.VxeSocketConst;
import org.springframework.stereotype.Component;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * vxe WebSocket，Function for realizing real-time invisible refresh
 * @author: jeecg-boot
 */
@Slf4j
@Component
@ServerEndpoint("/vxeSocket/{userId}/{pageId}")
public class VxeSocket {

    /**
     * current session
     */
    private Session session;
    /**
     * current用户id
     */
    private String userId;
    /**
     * pageid，Used to identify the same user，不同pageofdata
     */
    private String pageId;
    /**
     * currentsocketonlyid
     */
    private String socketId;

    /**
     * User connection pool，Contains all of a single user'ssocketconnect；
     * 因为一个用户可能打开多个page，多个page就会有多个connect；
     * keyyesuserId，valueyesMapobject；sonMapofkeyyespageId，valueyesVXESocketobject
     */
    private static Map<String, Map<String, VxeSocket>> userPool = new HashMap<>();
    /**
     * connect池，Contains allWebSocketconnect；
     * keyyessocketId，valueyesVXESocketobject
     */
    private static Map<String, VxeSocket> socketPool = new HashMap<>();

    /**
     * 获取某个用户所有ofpage
     */
    public static Map<String, VxeSocket> getUserPool(String userId) {
        return userPool.computeIfAbsent(userId, k -> new HashMap<>(5));
    }

    /**
     * 向current用户发送消息
     *
     * @param message Message content
     */
    public void sendMessage(String message) {
        try {
            this.session.getAsyncRemote().sendText(message);
        } catch (Exception e) {
            log.error("【vxeSocket】Message sending failed：" + e.getMessage());
        }
    }

    /**
     * encapsulate messagejson
     *
     * @param data Message content
     */
    public static String packageMessage(String type, Object data) {
        JSONObject message = new JSONObject();
        message.put(VxeSocketConst.TYPE, type);
        message.put(VxeSocketConst.DATA, data);
        return message.toJSONString();
    }

    /**
     * 向指定用户of所有page发送消息
     *
     * @param userId  接收消息of用户ID
     * @param message Message content
     */
    public static void sendMessageTo(String userId, String message) {
        Collection<VxeSocket> values = getUserPool(userId).values();
        if (values.size() > 0) {
            for (VxeSocket socketItem : values) {
                socketItem.sendMessage(message);
            }
        } else {
            log.warn("【vxeSocket】Message sending failed：userId\"" + userId + "\"Does not exist or is not online！");
        }
    }

    /**
     * 向指定用户of指定page发送消息
     *
     * @param userId  接收消息of用户ID
     * @param message Message content
     */
    public static void sendMessageTo(String userId, String pageId, String message) {
        VxeSocket socketItem = getUserPool(userId).get(pageId);
        if (socketItem != null) {
            socketItem.sendMessage(message);
        } else {
            log.warn("【vxeSocket】Message sending failed：userId\"" + userId + "\"ofpageId\"" + pageId + "\"Does not exist or is not online！");
        }
    }

    /**
     * 向多个用户of所有page发送消息
     *
     * @param userIds 接收消息of用户IDarray
     * @param message Message content
     */
    public static void sendMessageTo(String[] userIds, String message) {
        for (String userId : userIds) {
            VxeSocket.sendMessageTo(userId, message);
        }
    }

    /**
     * 向所有用户of所有page发送消息
     *
     * @param message Message content
     */
    public static void sendMessageToAll(String message) {
        for (VxeSocket socketItem : socketPool.values()) {
            socketItem.sendMessage(message);
        }
    }

    /**
     * websocket 开启connect
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId, @PathParam("pageId") String pageId) {
        try {
            this.userId = userId;
            this.pageId = pageId;
            this.socketId = userId + pageId;
            this.session = session;

            socketPool.put(this.socketId, this);
            getUserPool(userId).put(this.pageId, this);

            log.info("【vxeSocket】有新ofconnect，The total is:" + socketPool.size());
        } catch (Exception ignored) {
        }
    }

    /**
     * websocket 断开connect
     */
    @OnClose
    public void onClose() {
        try {
            socketPool.remove(this.socketId);
            getUserPool(this.userId).remove(this.pageId);

            log.info("【vxeSocket】connect断开，The total is:" + socketPool.size());
        } catch (Exception ignored) {
        }
    }

    /**
     * websocket message received
     */
    @OnMessage
    public void onMessage(String message) {
        // log.info("【vxeSocket】onMessage:" + message);
        JSONObject json;
        try {
            json = JSON.parseObject(message);
        } catch (Exception e) {
            log.warn("【vxeSocket】收到不合法of消息:" + message);
            return;
        }
        String type = json.getString(VxeSocketConst.TYPE);
        switch (type) {
            // Heartbeat detection
            case VxeSocketConst.TYPE_HB:
                this.sendMessage(VxeSocket.packageMessage(type, true));
                break;
            // renewformdata
            case VxeSocketConst.TYPE_UVT:
                this.handleUpdateForm(json);
                break;
            default:
                log.warn("【vxeSocket】收到不识别of消息类型:" + type);
                break;
        }


    }

    /**
     * deal with UpdateForm event
     */
    private void handleUpdateForm(JSONObject json) {
        // 将event转发给所有人
        JSONObject data = json.getJSONObject(VxeSocketConst.DATA);
        VxeSocket.sendMessageToAll(VxeSocket.packageMessage(VxeSocketConst.TYPE_UVT, data));
    }

}
