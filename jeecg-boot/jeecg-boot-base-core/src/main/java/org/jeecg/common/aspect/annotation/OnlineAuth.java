package org.jeecg.common.aspect.annotation;

import java.lang.annotation.*;

/**
 * onlineRequest interception-specific annotations
 * @author: jeecg-boot
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.METHOD})
@Documented
public @interface OnlineAuth {

    /**
     * Request keyword，existxxx/codeprevious string
     * @return
     */
    String value();
}
