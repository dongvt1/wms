package org.jeecg.common.desensitization.annotation;

import java.lang.annotation.*;

/**
 * Encrypted annotations
 *
 * Declare on method Return methods to sensitive fields in objects encryption/format
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface SensitiveEncode {

    /**
     * Specify the entity class that needs to be desensitizedclass
     * @return
     */
    Class entity() default Object.class;
}
