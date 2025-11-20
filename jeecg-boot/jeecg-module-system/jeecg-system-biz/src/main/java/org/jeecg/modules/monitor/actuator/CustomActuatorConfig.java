package org.jeecg.modules.monitor.actuator;

import org.jeecg.modules.monitor.actuator.httptrace.CustomInMemoryHttpTraceRepository;
import org.springframework.boot.actuate.autoconfigure.web.exchanges.HttpExchangesAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.web.exchanges.HttpExchangesProperties;
import org.springframework.boot.actuate.web.exchanges.HttpExchangeRepository;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Custom health monitoring configuration class
 *
 * @Author: chenrui
 * @Date: 2024/5/13 17:20
 */
@Configuration
@EnableConfigurationProperties(HttpExchangesProperties.class)
@AutoConfigureBefore(HttpExchangesAutoConfiguration.class)
public class CustomActuatorConfig {

    /**
     * Request tracking
     * @return
     * @author chenrui
     * @date 2024/5/14 14:52
     */
    @Bean
    @ConditionalOnProperty(prefix = "management.trace.http", name = "enabled", matchIfMissing = true)
    @ConditionalOnMissingBean(HttpExchangeRepository.class)
    public CustomInMemoryHttpTraceRepository traceRepository() {
        return new CustomInMemoryHttpTraceRepository();
    }

}
