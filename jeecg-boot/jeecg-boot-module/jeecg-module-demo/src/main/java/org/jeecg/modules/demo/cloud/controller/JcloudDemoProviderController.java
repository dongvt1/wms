package org.jeecg.modules.demo.cloud.controller;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.demo.cloud.service.JcloudDemoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

/**
 * Server provider——feigninterface
 * 【provided tosystem-startCall test，lookfeignIs it smooth?】
 * @author: jeecg-boot
 */
@Slf4j
@RestController
@RequestMapping("/test")
public class JcloudDemoProviderController {

    @Resource
    private JcloudDemoService jcloudDemoService;

    @GetMapping("/getMessage")
    public String getMessage(@RequestParam(name = "name") String name) {
        String msg = jcloudDemoService.getMessage(name);
        log.info(" Microservice is called：{} ",msg);
        return msg;
    }

}
