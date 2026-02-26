package org.jeecg.modules.demo.test.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.modules.demo.test.entity.JeecgDemo;
import org.jeecg.modules.demo.test.service.IJeecgDemoService;
import org.jeecg.modules.demo.test.service.IJeecgDynamicDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Description: Dynamic data source testing
 * @Author: zyf
 * @Date:2020-04-21
 */
@Slf4j
@Tag(name = "Dynamic data source testing")
@RestController
@RequestMapping("/test/dynamic")
public class JeecgDynamicDataController extends JeecgController<JeecgDemo, IJeecgDemoService> {

    @Autowired
    private IJeecgDynamicDataService jeecgDynamicDataService;


    /**
     * Dynamically switch data sources

     * @return
     */
    @PostMapping(value = "/test1")
    @AutoLog(value = "Dynamically switch data sources")
    @Operation(summary = "Dynamically switch data sources")
    public Result<List<JeecgDemo>> selectSpelByKey(@RequestParam(required = false) String dsName) {
        List<JeecgDemo> list = jeecgDynamicDataService.selectSpelByKey(dsName);
        return Result.OK(list);
    }


}
