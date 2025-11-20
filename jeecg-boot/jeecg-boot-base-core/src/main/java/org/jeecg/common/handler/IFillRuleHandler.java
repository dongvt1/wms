package org.jeecg.common.handler;

import com.alibaba.fastjson.JSONObject;

/**
 * Value filling rule interface
 *
 * @author Yan_East
 * If you need to use the value filling rule function，Rule implementation classes must implement this interface
 */
public interface IFillRuleHandler {

    /**
     * Filling rules
     * @param params Page configuration fixed parameters
     * @param formData  Dynamic form parameters
     * @return
     */
    public Object execute(JSONObject params, JSONObject formData);

}

