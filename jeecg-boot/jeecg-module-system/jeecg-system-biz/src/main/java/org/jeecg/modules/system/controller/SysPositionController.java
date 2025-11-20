package org.jeecg.modules.system.controller;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.ImportExcelUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.modules.system.entity.SysPosition;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.service.ISysPositionService;
import org.jeecg.modules.system.service.ISysUserPositionService;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: job list
 * @Author: jeecg-boot
 * @Date: 2019-09-19
 * @Version: V1.0
 */
@Slf4j
@Tag(name = "job list")
@RestController
@RequestMapping("/sys/position")
public class SysPositionController {

    @Autowired
    private ISysPositionService sysPositionService;

    @Autowired
    private ISysUserPositionService userPositionService;

    @Autowired
    private ISysUserService userService;

    /**
     * Paginated list query
     *
     * @param sysPosition
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "job list-Paginated list query")
    @Operation(summary = "job list-Paginated list query")
    @GetMapping(value = "/list")
    public Result<IPage<SysPosition>> queryPageList(SysPosition sysPosition,
                                                    @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                    @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                    HttpServletRequest req) {
        Result<IPage<SysPosition>> result = new Result<IPage<SysPosition>>();
        //------------------------------------------------------------------------------------------------
        //Whether to enable multi-tenant data isolation of the system management module【SAASMulti-tenant model】
        if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
            sysPosition.setTenantId(oConvertUtils.getInt(TenantContext.getTenant(),0));
        }
        //------------------------------------------------------------------------------------------------
        QueryWrapper<SysPosition> queryWrapper = QueryGenerator.initQueryWrapper(sysPosition, req.getParameterMap());
        queryWrapper.orderByAsc("post_level");
        queryWrapper.orderByDesc("create_time");
        Page<SysPosition> page = new Page<SysPosition>(pageNo, pageSize);
        IPage<SysPosition> pageList = sysPositionService.page(page, queryWrapper);
        result.setSuccess(true);
        result.setResult(pageList);
        return result;
    }

    /**
     * Add to
     *
     * @param sysPosition
     * @return
     */
    @AutoLog(value = "job list-Add to")
    @Operation(summary = "job list-Add to")
    @PostMapping(value = "/add")
    public Result<SysPosition> add(@RequestBody SysPosition sysPosition) {
        Result<SysPosition> result = new Result<SysPosition>();
        try {
            //update-begin---author:wangshuai ---date:20230313  for：【QQYUN-4558】vue3Position function adjustment，Remove encoding and levels，You can hide it first------------
            //Number is empty，No need to judge multi-tenant isolation
            if(oConvertUtils.isEmpty(sysPosition.getCode())){
                //Generate job code10Bit
                sysPosition.setCode(RandomUtil.randomString(10));
            }
            //update-end---author:wangshuai ---date:20230313  for：【QQYUN-4558】vue3Position function adjustment，Remove encoding and levels，You can hide it first-------------
            sysPositionService.save(sysPosition);
            result.success("Add to成功！");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("Operation failed");
        }
        return result;
    }

    /**
     * edit
     *
     * @param sysPosition
     * @return
     */
    @AutoLog(value = "job list-edit")
    @Operation(summary = "job list-edit")
    @RequestMapping(value = "/edit", method ={RequestMethod.PUT, RequestMethod.POST})
    public Result<SysPosition> edit(@RequestBody SysPosition sysPosition) {
        Result<SysPosition> result = new Result<SysPosition>();
        SysPosition sysPositionEntity = sysPositionService.getById(sysPosition.getId());
        if (sysPositionEntity == null) {
            result.error500("No corresponding entity found");
        } else {
            boolean ok = sysPositionService.updateById(sysPosition);
            //TODO returnfalseWhat does it mean？
            if (ok) {
                result.success("Modification successful!");
            }
        }

        return result;
    }

    /**
     * passiddelete
     *
     * @param id
     * @return
     */
    @AutoLog(value = "job list-passiddelete")
    @Operation(summary = "job list-passiddelete")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        try {
            sysPositionService.removeById(id);
            //delete用户职Bit关系表
            userPositionService.removeByPositionId(id);
        } catch (Exception e) {
            log.error("delete失败", e.getMessage());
            return Result.error("delete失败!");
        }
        return Result.ok("delete成功!");
    }

    /**
     * 批量delete
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "job list-批量delete")
    @Operation(summary = "job list-批量delete")
    @DeleteMapping(value = "/deleteBatch")
    public Result<SysPosition> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        Result<SysPosition> result = new Result<SysPosition>();
        if (ids == null || "".equals(ids.trim())) {
            result.error500("Parameter not recognized！");
        } else {
            this.sysPositionService.removeByIds(Arrays.asList(ids.split(",")));
            result.success("delete成功!");
        }
        return result;
    }

    /**
     * passidQuery
     *
     * @param id
     * @return
     */
    @AutoLog(value = "job list-passidQuery")
    @Operation(summary = "job list-passidQuery")
    @GetMapping(value = "/queryById")
    public Result<SysPosition> queryById(@RequestParam(name = "id", required = true) String id) {
        Result<SysPosition> result = new Result<SysPosition>();
        SysPosition sysPosition = sysPositionService.getById(id);
        if (sysPosition == null) {
            result.error500("No corresponding entity found");
        } else {
            result.setResult(sysPosition);
            result.setSuccess(true);
        }
        return result;
    }

    /**
     * Exportexcel
     *
     * @param request
     * @param response
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(SysPosition sysPosition,HttpServletRequest request, HttpServletResponse response) {
        // Step.1 组装Query条件
        QueryWrapper<SysPosition> queryWrapper = null;
        try {
            String paramsStr = request.getParameter("paramsStr");
            if (oConvertUtils.isNotEmpty(paramsStr)) {
                String deString = URLDecoder.decode(paramsStr, "UTF-8");
                sysPosition = JSON.parseObject(deString, SysPosition.class);
                //------------------------------------------------------------------------------------------------
                //Whether to enable multi-tenant data isolation of the system management module【SAASMulti-tenant model】
                if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
                    sysPosition.setTenantId(oConvertUtils.getInt(TenantContext.getTenant(),0));
                }
                //------------------------------------------------------------------------------------------------
            }
            queryWrapper = QueryGenerator.initQueryWrapper(sysPosition, request.getParameterMap());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //update-begin--Author:liusq  Date:20240715 for：[03]职务Export，如果选择数据则只Export相关数据--------------------
        String selections = request.getParameter("selections");
        if(!oConvertUtils.isEmpty(selections)){
            queryWrapper.in("id",selections.split(","));
        }
        //update-end--Author:liusq  Date:20240715 for：[03]职务Export，如果选择数据则只Export相关数据----------------------
        //Step.2 AutoPoi ExportExcel
        ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
        List<SysPosition> pageList = sysPositionService.list(queryWrapper);
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        //Export文件名称
        mv.addObject(NormalExcelConstants.FILE_NAME, "job list列表");
        mv.addObject(NormalExcelConstants.CLASS, SysPosition.class);
        mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("job list列表数据", "Export人:"+user.getRealname(),"Export信息"));
        mv.addObject(NormalExcelConstants.DATA_LIST, pageList);
        return mv;
    }

    /**
     * passexcelImport data
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response)throws IOException {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        // error message
        List<String> errorMessage = new ArrayList<>();
        int successLines = 0, errorLines = 0;
        for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            // Get the uploaded file object
            MultipartFile file = entity.getValue();
            ImportParams params = new ImportParams();
            params.setTitleRows(2);
            params.setHeadRows(1);
            params.setNeedSave(true);
            try {
                List<Object>  listSysPositions = ExcelImportUtil.importExcel(file.getInputStream(), SysPosition.class, params);
                List<String> list = ImportExcelUtil.importDateSave(listSysPositions, ISysPositionService.class, errorMessage,CommonConstant.SQL_INDEX_UNIQ_CODE);
                errorLines+=list.size();
                successLines+=(listSysPositions.size()-errorLines);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return Result.error("File import failed:" + e.getMessage());
            } finally {
                try {
                    file.getInputStream().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return ImportExcelUtil.imporReturnRes(errorLines,successLines,errorMessage);
    }

    /**
     * passcodeQuery
     *
     * @param code
     * @return
     */
    @AutoLog(value = "job list-passcodeQuery")
    @Operation(summary = "job list-passcodeQuery")
    @GetMapping(value = "/queryByCode")
    public Result<SysPosition> queryByCode(@RequestParam(name = "code", required = true) String code) {
        Result<SysPosition> result = new Result<SysPosition>();
        QueryWrapper<SysPosition> queryWrapper = new QueryWrapper<SysPosition>();
        queryWrapper.eq("code",code);
        SysPosition sysPosition = sysPositionService.getOne(queryWrapper);
        if (sysPosition == null) {
            result.error500("No corresponding entity found");
        } else {
            result.setResult(sysPosition);
            result.setSuccess(true);
        }
        return result;
    }


    /**
     * pass多个IDQuery
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "job list-pass多个Query")
    @Operation(summary = "job list-pass多个idQuery")
    @GetMapping(value = "/queryByIds")
    public Result<List<SysPosition>> queryByIds(@RequestParam(name = "ids") String ids) {
        Result<List<SysPosition>> result = new Result<>();
        QueryWrapper<SysPosition> queryWrapper = new QueryWrapper<>();
        queryWrapper.in(true,"id",ids.split(","));
        List<SysPosition> list = sysPositionService.list(queryWrapper);
        if (list == null) {
            result.error500("No corresponding entity found");
        } else {
            result.setResult(list);
            result.setSuccess(true);
        }
        return result;
    }



    /**
     * 获取职Bit用户列表
     *
     * @param pageNo
     * @param pageSize
     * @param positionId
     * @return
     */
    @GetMapping("/getPositionUserList")
    public Result<IPage<SysUser>> getPositionUserList(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                      @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                      @RequestParam(name = "positionId") String positionId) {

        Page<SysUser> page = new Page<>(pageNo, pageSize);
        IPage<SysUser> pageList = userPositionService.getPositionUserList(page, positionId);
        List<String> userIds = pageList.getRecords().stream().map(SysUser::getId).collect(Collectors.toList());
        if (null != userIds && userIds.size() > 0) {
            Map<String, String> useDepNames = userService.getDepNamesByUserIds(userIds);
            pageList.getRecords().forEach(item -> {
                item.setOrgCodeTxt(useDepNames.get(item.getId()));
            });
        }
        return Result.ok(pageList);
    }

    /**
     * Add to成员到用户职Bit关系表
     *
     * @param userIds
     * @param positionId
     * @return
     */
    @PostMapping("/savePositionUser")
    public Result<String> saveUserPosition(@RequestParam(name = "userIds") String userIds,
                                           @RequestParam(name = "positionId") String positionId) {
        userPositionService.saveUserPosition(userIds, positionId);
        return Result.ok("Add to成功");
    }

    /**
     * 职Bit列表移除成员
     *
     * @param userIds
     * @param positionId
     * @return
     */
    @DeleteMapping("/removePositionUser")
    public Result<String> removeUserPosition(@RequestParam(name = "userIds") String userIds,
                                             @RequestParam(name = "positionId") String positionId) {
        userPositionService.removePositionUser(userIds, positionId);
        return Result.OK("Removed member successfully");
    }
}
