package com.cy.modules.planning.agent.config;

import org.springframework.context.annotation.Configuration;

/**
 * WebSocket cấu hình cho Planning Agent.
 *
 * ServerEndpointExporter đã được cấu hình trong jeecg-boot-base-core (WebSocketConfig).
 * Nó sẽ tự động quét và đăng ký @ServerEndpoint annotated classes,
 * bao gồm PlanningNotificationServiceImpl (/ws/planning-agent/{userId}).
 *
 * Class này giữ lại như placeholder cho các cấu hình WebSocket bổ sung trong tương lai.
 */
@Configuration
public class PlanningWebSocketConfig {
    // ServerEndpointExporter đã được đăng ký bởi org.jeecg.config.WebSocketConfig
    // PlanningNotificationServiceImpl sẽ được tự động đăng ký nhờ @ServerEndpoint annotation
}
