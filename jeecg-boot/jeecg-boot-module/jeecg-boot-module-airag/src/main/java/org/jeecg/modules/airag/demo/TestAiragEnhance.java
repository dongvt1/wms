package org.jeecg.modules.airag.demo;

import org.jeecg.modules.airag.flow.component.enhance.IAiRagEnhanceJava;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

/**
 * @Description: JavaEnhance node example class
 * @Author: chenrui
 * @Date: 2025/3/6 11:42
 */
@Component("testAiragEnhance")
public class TestAiragEnhance implements IAiRagEnhanceJava {
    @Override
    public Map<String, Object> process(Map<String, Object> inputParams) {
        Object arg1 = inputParams.get("arg1");
        Object arg2 = inputParams.get("arg2");
        return Collections.singletonMap("result",arg1.toString()+"javaSplicing"+arg2.toString());
    }
}