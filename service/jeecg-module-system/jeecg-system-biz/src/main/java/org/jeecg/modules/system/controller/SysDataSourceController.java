package org.jeecg.modules.system.controller;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.dynamic.db.DataSourceCachePool;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.common.util.security.JdbcSecurityUtil;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.modules.system.entity.SysDataSource;
import org.jeecg.modules.system.service.ISysDataSourceService;
import org.jeecg.modules.system.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: Multiple data source management
 * @Author: jeecg-boot
 * @Date: 2019-12-25
 * @Version: V1.0
 */
@Slf4j
@Tag(name = "Multiple data source management")
@RestController
@RequestMapping("/sys/dataSource")
public class SysDataSourceController extends JeecgController<SysDataSource, ISysDataSourceService> {

    @Autowired
    private ISysDataSourceService sysDataSourceService;


    /**
     * Paginated list query
     *
     * @param sysDataSource
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "Multiple data source management-Paginated list query")
    @Operation(summary = "Multiple data source management-Paginated list query")
    @RequiresPermissions("system:datasource:list")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(
            SysDataSource sysDataSource,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            HttpServletRequest req) {
        //------------------------------------------------------------------------------------------------
        //Whether to enable multi-tenant data isolation of the system management module【SAASMulti-tenant model】
        if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
            sysDataSource.setTenantId(oConvertUtils.getInt(TenantContext.getTenant(), 0));
        }
        //------------------------------------------------------------------------------------------------
        QueryWrapper<SysDataSource> queryWrapper = QueryGenerator.initQueryWrapper(sysDataSource, req.getParameterMap());
        Page<SysDataSource> page = new Page<>(pageNo, pageSize);
        IPage<SysDataSource> pageList = sysDataSourceService.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    @GetMapping(value = "/options")
    public Result<?> queryOptions(SysDataSource sysDataSource, HttpServletRequest req) {
        //------------------------------------------------------------------------------------------------
        //Whether to enable multi-tenant data isolation of the system management module【SAASMulti-tenant model】
        if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
            sysDataSource.setTenantId(oConvertUtils.getInt(TenantContext.getTenant(), 0));
        }
        //------------------------------------------------------------------------------------------------
        
        QueryWrapper<SysDataSource> queryWrapper = QueryGenerator.initQueryWrapper(sysDataSource, req.getParameterMap());
        List<SysDataSource> pageList = sysDataSourceService.list(queryWrapper);
        JSONArray array = new JSONArray(pageList.size());
        for (SysDataSource item : pageList) {
            JSONObject option = new JSONObject(3);
            option.put("value", item.getCode());
            option.put("label", item.getName());
            option.put("text", item.getName());
            array.add(option);
        }
        return Result.ok(array);
    }

    /**
     * Add to
     *
     * @param sysDataSource
     * @return
     */
    @AutoLog(value = "Multiple data source management-Add to")
    @Operation(summary = "Multiple data source management-Add to")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody SysDataSource sysDataSource) {
        //update-begin-author:taoyan date:2022-8-10 for: jdbcConnection address vulnerability problem
        try {
            JdbcSecurityUtil.validate(sysDataSource.getDbUrl());
        }catch (JeecgBootException e){
            log.error(e.toString());
            return Result.error("Operation failed：" + e.getMessage());
        }
        //update-end-author:taoyan date:2022-8-10 for: jdbcConnection address vulnerability problem
        return sysDataSourceService.saveDataSource(sysDataSource);
    }

    /**
     * edit
     *
     * @param sysDataSource
     * @return
     */
    @AutoLog(value = "Multiple data source management-edit")
    @Operation(summary = "Multiple data source management-edit")
    @RequestMapping(value = "/edit", method ={RequestMethod.PUT, RequestMethod.POST})
    public Result<?> edit(@RequestBody SysDataSource sysDataSource) {
        //update-begin-author:taoyan date:2022-8-10 for: jdbcConnection address vulnerability problem
        try {
            JdbcSecurityUtil.validate(sysDataSource.getDbUrl());
        } catch (JeecgBootException e) {
            log.error(e.toString());
            return Result.error("Operation failed：" + e.getMessage());
        }
        //update-end-author:taoyan date:2022-8-10 for: jdbcConnection address vulnerability problem
        return sysDataSourceService.editDataSource(sysDataSource);
    }

    /**
     * passiddelete
     *
     * @param id
     * @return
     */
    @AutoLog(value = "Multiple data source management-passiddelete")
    @Operation(summary = "Multiple data source management-passiddelete")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id") String id) {
        return sysDataSourceService.deleteDataSource(id);
    }

    /**
     * 批量delete
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "Multiple data source management-批量delete")
    @Operation(summary = "Multiple data source management-批量delete")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids") String ids) {
        List<String> idList = Arrays.asList(ids.split(","));
        idList.forEach(item->{
            SysDataSource sysDataSource = sysDataSourceService.getById(item);
            DataSourceCachePool.removeCache(sysDataSource.getCode());
        });
        this.sysDataSourceService.removeByIds(idList);
        return Result.ok("批量delete成功！");
    }

    /**
     * passidQuery
     *
     * @param id
     * @return
     */
    @AutoLog(value = "Multiple data source management-passidQuery")
    @Operation(summary = "Multiple data source management-passidQuery")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id") String id) throws InterruptedException {
        SysDataSource sysDataSource = sysDataSourceService.getById(id);
        //Password decryption
        String dbPassword = sysDataSource.getDbPassword();
        if(StringUtils.isNotBlank(dbPassword)){
            String decodedStr = SecurityUtil.jiemi(dbPassword);
            sysDataSource.setDbPassword(decodedStr);
        }
        return Result.ok(sysDataSource);
    }

    /**
     * Exportexcel
     *
     * @param request
     * @param sysDataSource
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, SysDataSource sysDataSource) {
        //------------------------------------------------------------------------------------------------
        //Whether to enable multi-tenant data isolation of the system management module【SAASMulti-tenant model】
        if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
            sysDataSource.setTenantId(oConvertUtils.getInt(TenantContext.getTenant(), 0));
        }
        //------------------------------------------------------------------------------------------------
        return super.exportXls(request, sysDataSource, SysDataSource.class, "Multiple data source management");
    }

    /**
     * passexcelImport data
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, SysDataSource.class);
    }



}
