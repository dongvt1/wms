package org.jeecg.config;

import org.jeecg.filter.GlobalAccessTokenFilter;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

/**
 * @author scott
 * @date 2020/5/26
 * Router current limiting configuration
 */
@Configuration
public class RateLimiterConfiguration {
    /**
     * IPCurrent limiting (passexchangeThe object can obtain the request information，Used hereHostName)
     */
    @Bean
    @Primary
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
    }

    /**
     * 用户Current limiting (passexchangeThe object can obtain the request information，Get the currently requested user TOKEN)
     */
    @Bean
    public KeyResolver userKeyResolver() {
        //使用这种方式Current limiting，askHeaderMust carry inX-Access-Tokenparameter
        return exchange -> Mono.just(exchange.getRequest().getHeaders().getFirst(GlobalAccessTokenFilter.X_ACCESS_TOKEN));
    }

    /**
     * 接口Current limiting (获取ask地址的uri作为Current limitingkey)
     */
    @Bean
    public KeyResolver apiKeyResolver() {
        return exchange -> Mono.just(exchange.getRequest().getPath().value());
    }

}
