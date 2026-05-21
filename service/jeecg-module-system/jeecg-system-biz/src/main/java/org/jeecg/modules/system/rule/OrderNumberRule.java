package org.jeecg.modules.system.rule;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.jeecg.common.handler.IFillRuleHandler;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Filling rulesDemo：Generate order number
 * 【Test example】
 */
public class OrderNumberRule implements IFillRuleHandler {

    @Override
    public Object execute(JSONObject params, JSONObject formData) {
        String prefix = "CN";
        //The order prefix defaults toCN If the rule parameter is not empty，Then take the custom prefix
        if (params != null) {
            Object obj = params.get("prefix");
            if (obj != null) prefix = obj.toString();
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        int random = RandomUtils.nextInt(90) + 10;
        String value = prefix + format.format(new Date()) + random;
        // according toformDataThe difference in the value of，Generate different order numbers
        String name = formData.getString("name");
        if (!StringUtils.isEmpty(name)) {
            value += name;
        }
        return value;
    }

}
