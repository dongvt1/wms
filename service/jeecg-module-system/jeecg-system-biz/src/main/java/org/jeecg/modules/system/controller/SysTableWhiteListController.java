package org.jeecg.modules.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.system.entity.SysTableWhiteList;
import org.jeecg.modules.system.service.ISysTableWhiteListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @Description: System whitelist
 * @Author: jeecg-boot
 * @Date: 2023-09-12
 * @Version: V1.0
 */
@Slf4j
@Tag(name = "System whitelist")
@RestController
@RequestMapping("/sys/tableWhiteList")
public class SysTableWhiteListController extends JeecgController<SysTableWhiteList, ISysTableWhiteListService> {

    @Autowired
    private ISysTableWhiteListService sysTableWhiteListService;

    /**
     * Paginated list query
     *
     * @param sysTableWhiteList
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    //@RequiresRoles("admin")
    @RequiresPermissions("system:tableWhite:list")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(
            SysTableWhiteList sysTableWhiteList,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            HttpServletRequest req
    ) {
        QueryWrapper<SysTableWhiteList> queryWrapper = QueryGenerator.initQueryWrapper(sysTableWhiteList, req.getParameterMap());
        Page<SysTableWhiteList> page = new Page<>(pageNo, pageSize);
        IPage<SysTableWhiteList> pageList = sysTableWhiteListService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * Add to
     *
     * @param sysTableWhiteList
     * @return
     */
    @AutoLog(value = "System whitelist-Add to")
    @Operation(summary = "System whitelist-Add to")
    //@RequiresRoles("admin")
    @RequiresPermissions("system:tableWhite:add")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody SysTableWhiteList sysTableWhiteList) {
        if (sysTableWhiteListService.add(sysTableWhiteList)) {
            return Result.OK("Add to成功！");
        } else {
            return Result.error("Add to失败！");
        }
    }

    /**
     * edit
     *
     * @param sysTableWhiteList
     * @return
     */
    @AutoLog(value = "System whitelist-edit")
    @Operation(summary = "System whitelist-edit")
    //@RequiresRoles("admin")
    @RequiresPermissions("system:tableWhite:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<?> edit(@RequestBody SysTableWhiteList sysTableWhiteList) {
        if (sysTableWhiteListService.edit(sysTableWhiteList)) {
            return Result.OK("edit成功！");
        } else {
            return Result.error("edit失败！");
        }
    }

    /**
     * passiddelete
     *
     * @param id
     * @return
     */
    @AutoLog(value = "System whitelist-passiddelete")
    @Operation(summary = "System whitelist-passiddelete")
//    @RequiresRoles("admin")
    @RequiresPermissions("system:tableWhite:delete")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id") String id) {
        if (sysTableWhiteListService.deleteByIds(id)) {
            return Result.OK("delete成功！");
        } else {
            return Result.error("delete失败！");
        }
    }

    /**
     * 批量delete
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "System whitelist-批量delete")
    @Operation(summary = "System whitelist-批量delete")
//    @RequiresRoles("admin")
    @RequiresPermissions("system:tableWhite:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids") String ids) {
        if (sysTableWhiteListService.deleteByIds(ids)) {
            return Result.OK("批量delete成功！");
        } else {
            return Result.error("批量delete失败！");
        }
    }

    /**
     * passidQuery
     *
     * @param id
     * @return
     */
    @AutoLog(value = "System whitelist-passidQuery")
    @Operation(summary = "System whitelist-passidQuery")
//    @RequiresRoles("admin")
    @RequiresPermissions("system:tableWhite:queryById")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        SysTableWhiteList sysTableWhiteList = sysTableWhiteListService.getById(id);
        return Result.OK(sysTableWhiteList);
    }

}
