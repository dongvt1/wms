package org.jeecg.common.aspect.annotation;

import java.lang.annotation.*;

/**
 * The interface declared by this annotation，Automatically implement dictionary translation
 * 
 * @Author scott
 * @email jeecgos@163.com
 * @Date 2022Year01moon05day
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoDict {

	/**
	 * Temporarily useless
	 * @return
	 */
	String value() default "";

}
