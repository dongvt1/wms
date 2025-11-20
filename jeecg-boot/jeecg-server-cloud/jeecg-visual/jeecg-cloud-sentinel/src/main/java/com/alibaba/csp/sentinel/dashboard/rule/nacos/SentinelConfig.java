/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import java.util.List;
import java.util.Properties;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.ApiDefinitionEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.*;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.entity.AuthorityRuleCorrectEntity;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.entity.ParamFlowRuleCorrectEntity;
import com.alibaba.nacos.api.PropertyKeyConst;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.config.ConfigFactory;
import com.alibaba.nacos.api.config.ConfigService;

/**
 *  sentinelConfiguration class
 *
 * @author zyf
 * @date 2022-04-13
 */
@Configuration
public class SentinelConfig {

    @Autowired
    private NacosConfigProperties nacosConfigProperties;


    /**
     * Flow control rules
     * @return
     */
    @Bean
    public Converter<List<FlowRuleEntity>, String> flowRuleEntityEncoder() {
        return JSON::toJSONString;
    }

    @Bean
    public Converter<String, List<FlowRuleEntity>> flowRuleEntityDecoder() {
        return s -> JSON.parseArray(s, FlowRuleEntity.class);
    }
    /**
     *  Downgrade rules
     * @return
     */
    @Bean
    public Converter<List<DegradeRuleEntity>, String> degradeRuleEntityEncoder() {
        return JSON::toJSONString;
    }

    @Bean
    public Converter<String, List<DegradeRuleEntity>> degradeRuleEntityDecoder() {
        return s -> JSON.parseArray(s, DegradeRuleEntity.class);
    }

    /**
     *   Hotspot parameters rule
     * @return
     */
    @Bean
    public Converter<List<ParamFlowRuleCorrectEntity>, String> paramFlowRuleEntityEncoder() {
        return JSON::toJSONString;
    }

    @Bean
    public Converter<String, List<ParamFlowRuleCorrectEntity>> paramFlowRuleEntityDecoder() {
        return s -> JSON.parseArray(s, ParamFlowRuleCorrectEntity.class);
    }

    /**
     * 系统rule
     * @return
     */
    @Bean
    public Converter<List<SystemRuleEntity>, String> systemRuleRuleEntityEncoder() {
        return JSON::toJSONString;
    }

    @Bean
    public Converter<String, List<SystemRuleEntity>> systemRuleRuleEntityDecoder() {
        return s -> JSON.parseArray(s, SystemRuleEntity.class);
    }
    /**
     * 授权rule
     * @return
     */
    @Bean
    public Converter<List<AuthorityRuleCorrectEntity>, String> authorityRuleRuleEntityEncoder() {
        return JSON::toJSONString;
    }

    @Bean
    public Converter<String, List<AuthorityRuleCorrectEntity>> authorityRuleRuleEntityDecoder() {
        return s -> JSON.parseArray(s, AuthorityRuleCorrectEntity.class);
    }

    /**
     * gatewayAPI
     *
     * @return
     * @throws Exception
     */
    @Bean
    public Converter<List<ApiDefinitionEntity>, String> apiDefinitionEntityEncoder() {
        return JSON::toJSONString;
    }

    @Bean
    public Converter<String, List<ApiDefinitionEntity>> apiDefinitionEntityDecoder() {
        return s -> JSON.parseArray(s, ApiDefinitionEntity.class);
    }

    /**
     * gatewayflowRule
     *
     * @return
     * @throws Exception
     */
    @Bean
    public Converter<List<GatewayFlowRuleEntity>, String> gatewayFlowRuleEntityEncoder() {
        return JSON::toJSONString;
    }

    @Bean
    public Converter<String, List<GatewayFlowRuleEntity>> gatewayFlowRuleEntityDecoder() {
        return s -> JSON.parseArray(s, GatewayFlowRuleEntity.class);
    }

    @Bean
    public ConfigService nacosConfigService() throws Exception {
        Properties properties=new Properties();
        properties.put(PropertyKeyConst.SERVER_ADDR,nacosConfigProperties.getServerAddr());
        if(StringUtils.isNotBlank(nacosConfigProperties.getUsername())){
            properties.put(PropertyKeyConst.USERNAME,nacosConfigProperties.getUsername());
        }
        if(StringUtils.isNotBlank(nacosConfigProperties.getPassword())){
            properties.put(PropertyKeyConst.PASSWORD,nacosConfigProperties.getPassword());
        }
        if(StringUtils.isNotBlank(nacosConfigProperties.getNamespace())){
            properties.put(PropertyKeyConst.NAMESPACE,nacosConfigProperties.getNamespace());
        }
        return ConfigFactory.createConfigService(properties);
    }
}
