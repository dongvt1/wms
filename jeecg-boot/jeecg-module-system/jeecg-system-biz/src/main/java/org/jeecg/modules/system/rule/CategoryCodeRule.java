package org.jeecg.modules.system.rule;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.handler.IFillRuleHandler;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.YouBianCodeUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysCategory;
import org.jeecg.modules.system.mapper.SysCategoryMapper;

import java.util.List;

/**
 * @Author scott
 * @Date 2019/12/9 11:32
 * @Description: Classification dictionary encoding generation rules
 */
@Slf4j
public class CategoryCodeRule implements IFillRuleHandler {

    public static final String ROOT_PID_VALUE = "0";

    @Override
    public Object execute(JSONObject params, JSONObject formData) {
        log.info("System custom coding rules[category_code_rule]，params：{} ，formData： {}", params, formData);

        String categoryPid = ROOT_PID_VALUE;
        String categoryCode = null;

        if (formData != null && formData.size() > 0) {
            Object obj = formData.get("pid");
            if (oConvertUtils.isNotEmpty(obj)) {
                categoryPid = obj.toString();
            }
        } else {
            if (params != null) {
                Object obj = params.get("pid");
                if (oConvertUtils.isNotEmpty(obj)) {
                    categoryPid = obj.toString();
                }
            }
        }

        /*
         * Divided into three situations
         * 1.No data in database callYouBianCodeUtil.getNextYouBianCode(null);
         * 2.Add child node，No sibling element YouBianCodeUtil.getSubYouBianCode(parentCode,null);
         * 3.Add child node有兄弟元素 YouBianCodeUtil.getNextYouBianCode(lastCode);
         * */
        //Find similar ones Determine the previous largestcodevalue
        SysCategoryMapper baseMapper = (SysCategoryMapper) SpringContextUtils.getBean("sysCategoryMapper");
        //update-begin---author:wangshuai ---date:20230424  for：【issues/4846】turn onsaasAfter multi-tenant functionality，When the tenant administrator adds a classification dictionary，Report an error------------
        Page<SysCategory> page = new Page<>(1,1);
        List<SysCategory> list = baseMapper.getMaxCategoryCodeByPage(page,categoryPid);
        //update-end---author:wangshuai ---date:20230424  for：【issues/4846】turn onsaasAfter multi-tenant functionality，When the tenant administrator adds a classification dictionary，Report an error------------
        if (list == null || list.size() == 0) {
            if (ROOT_PID_VALUE.equals(categoryPid)) {
                //Condition1
                categoryCode = YouBianCodeUtil.getNextYouBianCode(null);
            } else {
                //Condition2
                //update-begin---author:wangshuai ---date:20230424  for：【issues/4846】turn onsaasAfter multi-tenant functionality，When the tenant administrator adds a classification dictionary，Report an error------------
                SysCategory parent = (SysCategory) baseMapper.selectSysCategoryById(categoryPid);
                //update-end---author:wangshuai ---date:20230424  for：【issues/4846】turn onsaasAfter multi-tenant functionality，When the tenant administrator adds a classification dictionary，Report an error------------
                categoryCode = YouBianCodeUtil.getSubYouBianCode(parent.getCode(), null);
            }
        } else {
            //Condition3
            categoryCode = YouBianCodeUtil.getNextYouBianCode(list.get(0).getCode());
        }
        return categoryCode;
    }
}
