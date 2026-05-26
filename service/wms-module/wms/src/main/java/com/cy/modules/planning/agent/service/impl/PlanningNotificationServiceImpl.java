package com.cy.modules.planning.agent.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cy.modules.planning.agent.dto.DashboardUpdate;
import com.cy.modules.planning.agent.enums.NotificationType;
import com.cy.modules.planning.agent.service.PlanningNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Triển khai PlanningNotificationService với WebSocket push cho real-time alerts.
 * Sử dụng Jakarta WebSocket API (tương tự pattern JeecgBoot WebSocket).
 *
 * WebSocket endpoint: /ws/planning-agent/{userId}
 * Gửi thông báo real-time đến quản lý sản xuất và chủ đơn hàng.
 */
@Slf4j
@Service
@ServerEndpoint("/ws/planning-agent/{userId}")
public class PlanningNotificationServiceImpl implements PlanningNotificationService {

    /** Pool kết nối WebSocket: userId -> Session */
    private static final ConcurrentHashMap<String, Session> SESSION_POOL = new ConcurrentHashMap<>();

    /** Topic cho broadcast thông báo planning */
    private static final String TOPIC_PLANNING = "planning";

    /** Cmd type cho thông báo planning agent */
    private static final String CMD_PLANNING_NOTIFICATION = "planning_notification";
    private static final String CMD_PLANNING_DASHBOARD = "planning_dashboard";

    // ==================== WebSocket Lifecycle ====================

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        SESSION_POOL.put(userId, session);
        log.debug("[PlanningNotification] WebSocket connected: userId={}, total={}", userId, SESSION_POOL.size());
    }

    @OnClose
    public void onClose(@PathParam("userId") String userId) {
        SESSION_POOL.remove(userId);
        log.debug("[PlanningNotification] WebSocket disconnected: userId={}, total={}", userId, SESSION_POOL.size());
    }

    @OnMessage
    public void onMessage(String message, @PathParam("userId") String userId) {
        if ("ping".equals(message)) {
            pushToUser(userId, "{\"cmd\":\"pong\"}");
        }
        log.debug("[PlanningNotification] Received message from userId={}: {}", userId, message);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.warn("[PlanningNotification] WebSocket error: {}", error.getMessage());
    }

    // ==================== PlanningNotificationService Interface ====================

    @Override
    public void notifyProductionManager(NotificationType type, String message, Map<String, Object> data) {
        log.info("[PlanningNotification] Thông báo quản lý: type={}, message={}", type.getValue(), message);

        JSONObject notification = buildNotification(type, message, data);

        // Broadcast đến tất cả session đang kết nối (production managers)
        broadcastToAll(notification.toJSONString());
    }

    @Override
    public void notifyOrderOwners(List<String> orderIds, String message) {
        log.info("[PlanningNotification] Thông báo chủ đơn hàng: orderIds={}, message={}", orderIds, message);

        JSONObject notification = new JSONObject();
        notification.put("cmd", CMD_PLANNING_NOTIFICATION);
        notification.put("type", "ORDER_OWNER_NOTIFICATION");
        notification.put("message", message);
        notification.put("orderIds", orderIds);
        notification.put("timestamp", Instant.now().toString());

        // Broadcast đến tất cả session (trong thực tế sẽ filter theo user role/permission)
        broadcastToAll(notification.toJSONString());
    }

    @Override
    public void pushDashboardUpdate(DashboardUpdate update) {
        log.debug("[PlanningNotification] Push dashboard update: type={}", update.getUpdateType());

        JSONObject payload = new JSONObject();
        payload.put("cmd", CMD_PLANNING_DASHBOARD);
        payload.put("updateType", update.getUpdateType());
        payload.put("timestamp", update.getTimestamp() != null ? update.getTimestamp().toString() : Instant.now().toString());
        payload.put("data", update.getData());
        payload.put("summary", update.getSummary());

        broadcastToAll(payload.toJSONString());
    }

    // ==================== Private Helper Methods ====================

    /**
     * Xây dựng JSON thông báo chuẩn.
     */
    private JSONObject buildNotification(NotificationType type, String message, Map<String, Object> data) {
        JSONObject notification = new JSONObject();
        notification.put("cmd", CMD_PLANNING_NOTIFICATION);
        notification.put("type", type.getValue());
        notification.put("typeDescription", type.getDescription());
        notification.put("message", message);
        notification.put("timestamp", Instant.now().toString());
        notification.put("severity", determineSeverity(type));

        if (data != null && !data.isEmpty()) {
            notification.put("data", new JSONObject(data));
        }

        return notification;
    }

    /**
     * Xác định mức độ nghiêm trọng dựa trên loại thông báo.
     */
    private String determineSeverity(NotificationType type) {
        return switch (type) {
            case SYSTEM_ERROR, SYNC_FAILURE -> "critical";
            case MATERIAL_SHORTAGE, DEADLINE_AT_RISK, QUALITY_ALERT, RESCHEDULE_NEEDED -> "warning";
            case DEVIATION_DETECTED, ORDER_INCOMPLETE, ORDER_INVALID -> "info";
            case PLAN_GENERATED -> "success";
        };
    }

    /**
     * Gửi message đến một user cụ thể qua WebSocket.
     */
    private void pushToUser(String userId, String message) {
        Session session = SESSION_POOL.get(userId);
        if (session != null && session.isOpen()) {
            try {
                synchronized (session) {
                    session.getBasicRemote().sendText(message);
                }
            } catch (Exception e) {
                log.error("[PlanningNotification] Lỗi gửi WebSocket đến userId={}: {}", userId, e.getMessage());
                SESSION_POOL.remove(userId);
            }
        }
    }

    /**
     * Broadcast message đến tất cả session đang kết nối.
     */
    private void broadcastToAll(String message) {
        if (SESSION_POOL.isEmpty()) {
            log.debug("[PlanningNotification] Không có WebSocket session nào đang kết nối, bỏ qua broadcast");
            return;
        }

        for (Map.Entry<String, Session> entry : SESSION_POOL.entrySet()) {
            Session session = entry.getValue();
            if (session.isOpen()) {
                try {
                    session.getAsyncRemote().sendText(message);
                } catch (Exception e) {
                    log.error("[PlanningNotification] Lỗi broadcast đến userId={}: {}",
                            entry.getKey(), e.getMessage());
                    SESSION_POOL.remove(entry.getKey());
                }
            } else {
                SESSION_POOL.remove(entry.getKey());
            }
        }

        log.debug("[PlanningNotification] Broadcast thành công đến {} session", SESSION_POOL.size());
    }
}
