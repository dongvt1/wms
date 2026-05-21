package org.jeecg.config;

import org.jeecgframework.core.util.ApplicationContextUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * @Author: Scott
 * @Date: 2018/2/7
 * @description: autopoi Configuration class
 */
@Lazy(false)
@Configuration
public class AutoPoiConfig {

    /**
     * excelAnnotation dictionary parameter support(Import and export dictionary values，automatic translation)
     * Example： @Excel(name = "gender", width = 15, dicCode = "sex")
     * 1、When exporting, it will be configured according to the dictionary，value1,2translated into：male、female;
     * 2、When importing，会把male、femaletranslated into1,2Save to database;
     * @return
     */
    @Bean
    public ApplicationContextUtil applicationContextUtil() {
        return new org.jeecgframework.core.util.ApplicationContextUtil();
    }

}
