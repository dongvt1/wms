package org.jeecg.modules.system.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysAppVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
* @Description: appSystem configuration
* @Author: jeecg-boot
* @Date:   2025-07-05
* @Version: V1.0
*/
@Tag(name="appSystem configuration")
@RestController
@RequestMapping("/sys/version")
@Slf4j
public class SysAppVersionController{

    @Autowired
    private RedisUtil redisUtil;

    /**
     * APPcache prefix
     */
    private String APP3_VERSION = "app3:version";
    /**
     * app3Version information
     * @return
     */
    @Operation(summary="appVersion")
    @GetMapping(value = "/app3version")
    public Result<SysAppVersion> app3Version(@RequestParam(name="key", required = false)String appKey) throws Exception {
        Object appConfig = redisUtil.get(APP3_VERSION + appKey);
        if (oConvertUtils.isNotEmpty(appConfig)) {
            try {
                SysAppVersion sysAppVersion = (SysAppVersion)appConfig;
                return Result.OK(sysAppVersion);
            } catch (Exception e) {
                log.error(e.toString(),e);
                return Result.error("appVersion information获取失败：" + e.getMessage());
            }
        }
        return Result.OK();
    }


    /**
     *   saveAPP3
     *
     * @param sysAppVersion
     * @return
     */
    @RequiresRoles({"admin"})
    @Operation(summary="appSystem configuration-save")
    @PostMapping(value = "/saveVersion")
    public Result<?> saveVersion(@RequestBody SysAppVersion sysAppVersion) {
        String id = sysAppVersion.getId();
        redisUtil.set(APP3_VERSION + id,sysAppVersion);
        return Result.OK();
    }
}
