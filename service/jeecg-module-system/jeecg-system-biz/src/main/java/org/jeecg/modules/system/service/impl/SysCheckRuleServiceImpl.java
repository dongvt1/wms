package org.jeecg.modules.system.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.jeecg.modules.system.entity.SysCheckRule;
import org.jeecg.modules.system.mapper.SysCheckRuleMapper;
import org.jeecg.modules.system.service.ISysCheckRuleService;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * @Description: Coding verification rules
 * @Author: jeecg-boot
 * @Date: 2020-02-04
 * @Version: V1.0
 */
@Service
public class SysCheckRuleServiceImpl extends ServiceImpl<SysCheckRuleMapper, SysCheckRule> implements ISysCheckRuleService {

    /**
     * Special symbols for digits，for checking the entire value，rather than cutting a certain section
     */
    private final String CHECK_ALL_SYMBOL = "*";

    @Override
    public SysCheckRule getByCode(String ruleCode) {
        LambdaQueryWrapper<SysCheckRule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysCheckRule::getRuleCode, ruleCode);
        return super.getOne(queryWrapper);
    }

    /**
     * Verify the incoming value through custom verification rules set by the user
     *
     * @param checkRule
     * @param value
     * @return return nullRepresents passing the verification，否则就是return的错误提示文本
     */
    @Override
    public JSONObject checkValue(SysCheckRule checkRule, String value) {
        if (checkRule != null && StringUtils.isNotBlank(value)) {
            String ruleJson = checkRule.getRuleJson();
            if (StringUtils.isNotBlank(ruleJson)) {
                // The subscript to start intercepting，Increasingly according to the order of the rules，but * No. is not included in the increment range
                int beginIndex = 0;
                JSONArray rules = JSON.parseArray(ruleJson);
                for (int i = 0; i < rules.size(); i++) {
                    JSONObject result = new JSONObject();
                    JSONObject rule = rules.getJSONObject(i);
                    // Number of digits
                    String digits = rule.getString("digits");
                    result.put("digits", digits);
                    // Validation rules
                    String pattern = rule.getString("pattern");
                    result.put("pattern", pattern);
                    // Prompt text when failed
                    String message = rule.getString("message");
                    result.put("message", message);

                    // According to the interval set by the user，Intercept string for verification
                    String checkValue;
                    // Whether to check the entire value without truncating
                    if (CHECK_ALL_SYMBOL.equals(digits)) {
                        checkValue = value;
                    } else {
                        int num = Integer.parseInt(digits);
                        int endIndex = beginIndex + num;
                        // If the ending subscript is greater than the length of the given value，then get the last digit
                        endIndex = endIndex > value.length() ? value.length() : endIndex;
                        // If the starting subscript is greater than the ending subscript，It means that the user has not entered the location yet.，Assign a null value directly
                        if (beginIndex > endIndex) {
                            checkValue = "";
                        } else {
                            checkValue = value.substring(beginIndex, endIndex);
                        }
                        result.put("beginIndex", beginIndex);
                        result.put("endIndex", endIndex);
                        beginIndex += num;
                    }
                    result.put("checkValue", checkValue);
                    boolean passed = Pattern.matches(pattern, checkValue);
                    result.put("passed", passed);
                    // 如果没有通过校验就return错误信息
                    if (!passed) {
                        return result;
                    }
                }
            }
        }
        return null;
    }

}
