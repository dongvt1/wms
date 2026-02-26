package org.jeecg.common.aspect.annotation;

import java.lang.annotation.*;

/**
 * dynamictableswitch
 *
 * @author :zyf
 * @date:2020-04-25
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DynamicTable {
    /**
     * 需要dynamic解析的表名
     * @return
     */
    String value();
}
