package org.jeecg.common.aspect.annotation;

import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.enums.ModuleType;

import java.lang.annotation.*;

/**
 * System log comments
 * 
 * @Author scott
 * @email jeecgos@163.com
 * @Date 2019Year1moon14day
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoLog {

	/**
	 * day志内容
	 * 
	 * @return
	 */
	String value() default "";

	/**
	 * day志类型
	 * 
	 * @return 0:操作day志;1:登录day志;2:scheduled tasks;
	 */
	int logType() default CommonConstant.LOG_TYPE_2;
	
	/**
	 * 操作day志类型
	 * 
	 * @return （1Query，2Add to，3Revise，4delete）
	 */
	int operateType() default 0;

	/**
	 * module type Default iscommon
	 * @return
	 */
	ModuleType module() default ModuleType.COMMON;
}
