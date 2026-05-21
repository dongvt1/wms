package org.jeecg.common.desensitization.annotation;

import java.lang.annotation.*;

/**
 * Decrypt annotation
 *
 * defined on method Return methods to sensitive fields in objects Decrypt，It should be noted that，If it is not encrypted，Decrypt会出问题，Return the original string
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface SensitiveDecode {

    /**
     * Specify the entity class that needs to be desensitizedclass
     * @return
     */
    Class entity() default Object.class;
}
