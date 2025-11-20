package org.jeecg.modules.test.feign.controller;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.test.feign.client.JeecgTestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

/**
 * Microservice unit testing
 * @author: zyf
 * @date: 2022/04/21
 */
@Slf4j
@RestController
@RequestMapping("/sys/test")
@Tag(name = "【microservices】Unit testing")
public class JeecgTestFeignController {

    @Autowired
    private JeecgTestClient jeecgTestClient;

    /**
     * fuse： fallbackFactorytake precedence over @SentinelResource
     *
     * @param name
     * @return
     */
    @GetMapping("/getMessage")
    @Operation(summary = "testfeigncalldemoServe1")
    @SentinelResource(value = "test_more_getMessage", fallback = "getDefaultUser")
    public Result<String> getMessage(@RequestParam(value = "name", required = false) String name) {
        log.info("---------Feign fallbackFactoryPriority higher than@SentinelResource-----------------");
        String resultMsg = jeecgTestClient.getMessage(" I am jeecg-system Serve节点，call jeecg-demo!");
        return Result.OK(null, resultMsg);
    }

    /**
     * test方法：closuredemoServe，access request http://127.0.0.1:9999/sys/test/getMessage
     *
     * @param name
     * @return
     */
    @GetMapping("/getMessage2")
    @Operation(summary = "testfeigncalldemoServe2")
    public Result<String> getMessage2(@RequestParam(value = "name", required = false) String name) {
        log.info("---------test Feign fallbackFactory-----------------");
        String resultMsg = jeecgTestClient.getMessage(" I am jeecg-system Serve节点，call jeecg-demo!");
        return Result.OK(null, resultMsg);
    }


    @GetMapping("/fallback")
    @Operation(summary = "testfuse")
    @SentinelResource(value = "test_more_fallback", fallback = "getDefaultUser")
    public Result<Object> test(@RequestParam(value = "name", required = false) String name) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("name param is empty");
        }
        return Result.OK();
    }

    /**
     * fuse，Default callback function
     *
     * @param name
     * @return
     */
    public Result<Object> getDefaultUser(String name) {
        log.info("fuse，Default callback function");
        return Result.error(null, "Access timeout, Customize @SentinelResource Fallback");
    }
}
