package org.jeecg.common.system.annotation;

import java.lang.annotation.*;

/**
 * Convert enumeration class into dictionary data
 * 
 * <<Instructions for use>>
 * 1. The enumeration class needs to be `Enum` ending，And add to the class `@EnumDict` annotation。
 * 2. You need to manually change the package path where the enumeration class is located** add to `org.jeecg.common.system.util.ResourceUtil.BASE_SCAN_PACKAGES` in configuration array。
 * 
 * @Author taoYan
 * @Date 2022/7/8 10:34
 **/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnumDict {

    /**
     * As a unique encoding for dictionary data
     */
    String value() default "";
}
