package org.jeecg.common.desensitization.annotation;


import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.jeecg.common.desensitization.SensitiveSerialize;
import org.jeecg.common.desensitization.enums.SensitiveEnum;

import java.lang.annotation.*;

/**
 * Define on field The information stored in the identification field is sensitive
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@JacksonAnnotationsInside
@JsonSerialize(using = SensitiveSerialize.class)
public @interface Sensitive {

    /**
     * Different types are treated differently
     * @return
     */
    SensitiveEnum type() default SensitiveEnum.ENCODE;
}
