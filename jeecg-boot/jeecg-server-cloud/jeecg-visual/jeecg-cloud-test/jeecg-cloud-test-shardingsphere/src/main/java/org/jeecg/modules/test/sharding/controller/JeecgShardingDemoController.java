package org.jeecg.modules.test.sharding.controller;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.modules.test.sharding.entity.ShardingSysLog;
import org.jeecg.modules.test.sharding.service.IShardingSysLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description: Sub-database and sub-table test
 * @author: zyf
 * @date: 2022/01/24
 * @version: V1.0
 */
@Slf4j
@Tag(name = "Sub-database and sub-table test")
@RestController
@RequestMapping("/demo/sharding")
public class JeecgShardingDemoController extends JeecgController<ShardingSysLog, IShardingSysLogService> {
    @Autowired
    private IShardingSysLogService shardingSysLogService;

    /**
     * Single database table —— insert
     * @return
     */
    @PostMapping(value = "/insert")
    @Operation(summary = "Single database tableinsert")
    public Result<?> insert() {
        log.info("---------------------------------Single database tableinsert--------------------------------");
        int size = 10;
        for (int i = 0; i < size; i++) {
            ShardingSysLog shardingSysLog = new ShardingSysLog();
            shardingSysLog.setLogContent("useshardingsphereImplement sub-database and sub-table，insert测试！");
            shardingSysLog.setLogType(i);
            shardingSysLog.setOperateType(i);
            shardingSysLogService.save(shardingSysLog);
        }
        return Result.OK("Single database tableinsert10piece of data completed！");
    }

    /**
     * Single database table —— Query
     * @return
     */
    @PostMapping(value = "/list")
    @Operation(summary = "Single database tableQuery")
    public Result<?> list() {
        return Result.OK(shardingSysLogService.list());
    }

    /**
     * Sub-database and sub-table - insert
     * @return
     */
    @PostMapping(value = "/insert2")
    @Operation(summary = "Sub-database and sub-tableinsert")
    public Result<?> insert2() {
        int start=20;
        int size=30;
        for (int i = start; i <= size; i++) {
            ShardingSysLog shardingSysLog = new ShardingSysLog();
            shardingSysLog.setLogContent("Sub-database and sub-table test");
            shardingSysLog.setLogType(0);
            shardingSysLog.setOperateType(i);
            shardingSysLogService.save(shardingSysLog);
        }
        return Result.OK("Sub-database and sub-tableinsert10piece of data completed！");
    }

    /**
     * Sub-database and sub-table - Query
     * @return
     */
    @PostMapping(value = "/list2")
    @Operation(summary = "Sub-database and sub-tableQuery")
    public Result<?> list2() {
        return Result.OK(shardingSysLogService.list());
    }

}
