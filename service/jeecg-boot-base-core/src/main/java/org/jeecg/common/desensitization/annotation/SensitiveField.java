package org.jeecg.common.desensitization.annotation;


import org.jeecg.common.desensitization.enums.SensitiveEnum;

import java.lang.annotation.*;

/**
 * Define on field The information stored in the identification field is sensitive
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SensitiveField {

    /**
     * Different types are treated differently
     * @return
     */
    SensitiveEnum type() default SensitiveEnum.ENCODE;
}
