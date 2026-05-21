package org.jeecg.modules.system.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.system.entity.SysCheckRule;
import org.jeecg.modules.system.service.ISysCheckRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;

/**
 * @Description: Coding verification rules
 * @Author: jeecg-boot
 * @Date: 2020-02-04
 * @Version: V1.0
 */
@Slf4j
@Tag(name = "Coding verification rules")
@RestController
@RequestMapping("/sys/checkRule")
public class SysCheckRuleController extends JeecgController<SysCheckRule, ISysCheckRuleService> {

    @Autowired
    private ISysCheckRuleService sysCheckRuleService;

    /**
     * Paginated list query
     *
     * @param sysCheckRule
     * @param pageNo
     * @param pageSize
     * @param request
     * @return
     */
    @AutoLog(value = "Coding verification rules-Paginated list query")
    @Operation(summary = "Coding verification rules-Paginated list query")
    @GetMapping(value = "/list")
    public Result queryPageList(
            SysCheckRule sysCheckRule,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            HttpServletRequest request
    ) {
        QueryWrapper<SysCheckRule> queryWrapper = QueryGenerator.initQueryWrapper(sysCheckRule, request.getParameterMap());
        Page<SysCheckRule> page = new Page<>(pageNo, pageSize);
        IPage<SysCheckRule> pageList = sysCheckRuleService.page(page, queryWrapper);
        return Result.ok(pageList);
    }


    /**
     * passidQuery
     *
     * @param ruleCode
     * @return
     */
    @AutoLog(value = "Coding verification rules-passCodeVerify the value passed in")
    @Operation(summary = "Coding verification rules-passCodeVerify the value passed in")
    @GetMapping(value = "/checkByCode")
    public Result checkByCode(
            @RequestParam(name = "ruleCode") String ruleCode,
            @RequestParam(name = "value") String value
    ) throws UnsupportedEncodingException {
        SysCheckRule sysCheckRule = sysCheckRuleService.getByCode(ruleCode);
        if (sysCheckRule == null) {
            return Result.error("The encoding does not exist");
        }
        JSONObject errorResult = sysCheckRuleService.checkValue(sysCheckRule, URLDecoder.decode(value, "UTF-8"));
        if (errorResult == null) {
            return Result.ok();
        } else {
            Result<Object> r = Result.error(errorResult.getString("message"));
            r.setResult(errorResult);
            return r;
        }
    }

    /**
     * Add to
     *
     * @param sysCheckRule
     * @return
     */
    @AutoLog(value = "Coding verification rules-Add to")
    @Operation(summary = "Coding verification rules-Add to")
    @PostMapping(value = "/add")
    public Result add(@RequestBody SysCheckRule sysCheckRule) {
        sysCheckRuleService.save(sysCheckRule);
        return Result.ok("Add to成功！");
    }

    /**
     * edit
     *
     * @param sysCheckRule
     * @return
     */
    @AutoLog(value = "Coding verification rules-edit")
    @Operation(summary = "Coding verification rules-edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
    public Result edit(@RequestBody SysCheckRule sysCheckRule) {
        sysCheckRuleService.updateById(sysCheckRule);
        return Result.ok("edit成功!");
    }

    /**
     * passiddelete
     *
     * @param id
     * @return
     */
    @AutoLog(value = "Coding verification rules-passiddelete")
    @Operation(summary = "Coding verification rules-passiddelete")
    @DeleteMapping(value = "/delete")
    public Result delete(@RequestParam(name = "id", required = true) String id) {
        sysCheckRuleService.removeById(id);
        return Result.ok("delete成功!");
    }

    /**
     * 批量delete
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "Coding verification rules-批量delete")
    @Operation(summary = "Coding verification rules-批量delete")
    @DeleteMapping(value = "/deleteBatch")
    public Result deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.sysCheckRuleService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.ok("批量delete成功！");
    }

    /**
     * passidQuery
     *
     * @param id
     * @return
     */
    @AutoLog(value = "Coding verification rules-passidQuery")
    @Operation(summary = "Coding verification rules-passidQuery")
    @GetMapping(value = "/queryById")
    public Result queryById(@RequestParam(name = "id", required = true) String id) {
        SysCheckRule sysCheckRule = sysCheckRuleService.getById(id);
        return Result.ok(sysCheckRule);
    }

    /**
     * Exportexcel
     *
     * @param request
     * @param sysCheckRule
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, SysCheckRule sysCheckRule) {
        return super.exportXls(request, sysCheckRule, SysCheckRule.class, "Coding verification rules");
    }

    /**
     * passexcelImport data
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, SysCheckRule.class);
    }

}
