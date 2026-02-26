package org.jeecg.modules.system.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.system.entity.SysCheckRule;

/**
 * @Description: Coding verification rules
 * @Author: jeecg-boot
 * @Date: 2020-02-04
 * @Version: V1.0
 */
public interface ISysCheckRuleService extends IService<SysCheckRule> {

    /**
     * pass code Get rules
     *
     * @param ruleCode
     * @return
     */
    SysCheckRule getByCode(String ruleCode);


    /**
     * pass用户设定的自定义校验规则校验传入的值
     *
     * @param checkRule
     * @param value
     * @return return null代表pass校验，否则就是return的错误提示文本
     */
    JSONObject checkValue(SysCheckRule checkRule, String value);

}
