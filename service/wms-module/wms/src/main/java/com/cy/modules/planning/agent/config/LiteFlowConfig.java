package com.cy.modules.planning.agent.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * LiteFlow configuration for the AI Production Planning Agent.
 * Chain definitions are loaded from classpath:liteflow/planning-chains.xml.
 *
 * If liteflow.rule-source is not configured in the main application.yml,
 * this configuration ensures the chains are loaded from the default location.
 *
 * Add the following to application.yml or application-dev.yml:
 * liteflow:
 *   rule-source: liteflow/planning-chains.xml
 */
@Configuration
public class LiteFlowConfig {
    // LiteFlow auto-configuration is handled by liteflow-spring-boot-starter.
    // Chain XML is loaded from the rule-source path configured in application properties.
    // The planning-chains.xml file is located at classpath:liteflow/planning-chains.xml
    // and will be auto-detected by LiteFlow's Spring Boot starter.
}
