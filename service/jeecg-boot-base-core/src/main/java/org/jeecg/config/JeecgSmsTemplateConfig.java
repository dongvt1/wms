package org.jeecg.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
* @Description: SMS template
*
* @author: wangshuai
* @date: 2024/11/5 afternoon3:44
*/
@Data
@Component("jeecgSmsTemplateConfig")
@ConfigurationProperties(prefix = "jeecg.oss.sms-template")
public class JeecgSmsTemplateConfig {

    /**
     * SMS signature
     */
    private String signature;


    /**
     * SMS templatecode
     *
     * @return
     */
    private Map<String,String> templateCode;
    
}
