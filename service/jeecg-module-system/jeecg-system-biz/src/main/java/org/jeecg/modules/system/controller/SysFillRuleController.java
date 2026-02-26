package org.jeecg.modules.system.controller;

import com.alibaba.fastjson.JSONArray;
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
import org.jeecg.common.util.FillRuleUtil;
import org.jeecg.modules.system.entity.SysFillRule;
import org.jeecg.modules.system.service.ISysFillRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
 * @Description: Filling rules
 * @Author: jeecg-boot
 * @Date: 2019-11-07
 * @Version: V1.0
 */
@Slf4j
@Tag(name = "Filling rules")
@RestController
@RequestMapping("/sys/fillRule")
public class SysFillRuleController extends JeecgController<SysFillRule, ISysFillRuleService> {
    @Autowired
    private ISysFillRuleService sysFillRuleService;

    /**
     * Paginated list query
     *
     * @param sysFillRule
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "Filling rules-Paginated list query")
    @Operation(summary = "Filling rules-Paginated list query")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(SysFillRule sysFillRule,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                   HttpServletRequest req) {
        QueryWrapper<SysFillRule> queryWrapper = QueryGenerator.initQueryWrapper(sysFillRule, req.getParameterMap());
        Page<SysFillRule> page = new Page<>(pageNo, pageSize);
        IPage<SysFillRule> pageList = sysFillRuleService.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    /**
     * test ruleCode
     *
     * @param ruleCode
     * @return
     */
    @GetMapping(value = "/testFillRule")
    public Result testFillRule(@RequestParam("ruleCode") String ruleCode) {
        Object result = FillRuleUtil.executeRule(ruleCode, new JSONObject());
        return Result.ok(result);
    }

    /**
     * Add to
     *
     * @param sysFillRule
     * @return
     */
    @AutoLog(value = "Filling rules-Add to")
    @Operation(summary = "Filling rules-Add to")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody SysFillRule sysFillRule) {
        sysFillRuleService.save(sysFillRule);
        return Result.ok("Add to成功！");
    }

    /**
     * edit
     *
     * @param sysFillRule
     * @return
     */
    @AutoLog(value = "Filling rules-edit")
    @Operation(summary = "Filling rules-edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
    public Result<?> edit(@RequestBody SysFillRule sysFillRule) {
        sysFillRuleService.updateById(sysFillRule);
        return Result.ok("edit成功!");
    }

    /**
     * passiddelete
     *
     * @param id
     * @return
     */
    @AutoLog(value = "Filling rules-passiddelete")
    @Operation(summary = "Filling rules-passiddelete")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        sysFillRuleService.removeById(id);
        return Result.ok("delete成功!");
    }

    /**
     * 批量delete
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "Filling rules-批量delete")
    @Operation(summary = "Filling rules-批量delete")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.sysFillRuleService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.ok("批量delete成功！");
    }

    /**
     * passidQuery
     *
     * @param id
     * @return
     */
    @AutoLog(value = "Filling rules-passidQuery")
    @Operation(summary = "Filling rules-passidQuery")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        SysFillRule sysFillRule = sysFillRuleService.getById(id);
        return Result.ok(sysFillRule);
    }

    /**
     * Exportexcel
     *
     * @param request
     * @param sysFillRule
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, SysFillRule sysFillRule) {
        return super.exportXls(request, sysFillRule, SysFillRule.class, "Filling rules");
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
        return super.importExcel(request, response, SysFillRule.class);
    }

    /**
     * pass ruleCode 执行自定义Filling rules
     *
     * @param ruleCode 要执行ofFilling rules编码
     * @param formData form data，可根据form dataof不同生成不同of填值结果
     * @return The result after running
     */
    @PutMapping("/executeRuleByCode/{ruleCode}")
    public Result executeByRuleCode(@PathVariable("ruleCode") String ruleCode, @RequestBody JSONObject formData) {
        Object result = FillRuleUtil.executeRule(ruleCode, formData);
        return Result.ok(result);
    }


    /**
     * 批量pass ruleCode 执行自定义Filling rules
     *
     * @param ruleData 要执行ofFilling rulesJSONarray：
     *                 Example： { "commonFormData": {}, rules: [ { "ruleCode": "xxx", "formData": null } ] }
     * @return The result after running，返回Example： [{"ruleCode": "order_num_rule", "result": "CN2019111117212984"}]
     *
     */
    @PutMapping("/executeRuleByCodeBatch")
    public Result executeByRuleCodeBatch(@RequestBody JSONObject ruleData) {
        JSONObject commonFormData = ruleData.getJSONObject("commonFormData");
        JSONArray rules = ruleData.getJSONArray("rules");
        // Traverse rules ，Batch execution rules
        JSONArray results = new JSONArray(rules.size());
        for (int i = 0; i < rules.size(); i++) {
            JSONObject rule = rules.getJSONObject(i);
            String ruleCode = rule.getString("ruleCode");
            JSONObject formData = rule.getJSONObject("formData");
            // if not passed formData，Just usecommonof
            if (formData == null) {
                formData = commonFormData;
            }
            // 执行Filling rules
            Object result = FillRuleUtil.executeRule(ruleCode, formData);
            JSONObject obj = new JSONObject(rules.size());
            obj.put("ruleCode", ruleCode);
            obj.put("result", result);
            results.add(obj);
        }
        return Result.ok(results);
    }

}