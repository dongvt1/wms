package org.jeecg.modules.test.feign.client;

import org.jeecg.common.api.vo.Result;

import org.jeecg.common.constant.ServiceNameConstants;
import org.jeecg.config.FeignConfig;
import org.jeecg.modules.test.constant.CloudConstant;
import org.jeecg.modules.test.feign.factory.JeecgTestClientFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * conventionalfeignInterface definition
 * @author: zyf
 * @date: 2022/04/21
 */
@FeignClient(value = ServiceNameConstants.SERVICE_DEMO, configuration = FeignConfig.class,fallbackFactory = JeecgTestClientFactory.class)
@Component
public interface JeecgTestClient {

    /**
     * feignTest method
     * @param name
     * @return
     */
    @GetMapping(value = "/test/getMessage")
    String getMessage(@RequestParam(value = "name",required = false) String name);
}
