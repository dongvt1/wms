package org.jeecg.common.aspect.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
  *  Data permission annotation
 * @Author taoyan
 * @Date 2019Year4moon11day
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.METHOD})
@Documented
public @interface PermissionData {
	/**
	 * Not useful at the moment
	 * @return
	 */
	String value() default "";
	
	
	/**
	 * Component path of configuration menu,for data permissions
	 */
	String pageComponent() default "";
}