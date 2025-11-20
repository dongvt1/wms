package org.jeecg.modules.aop;

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
public @interface TenantLog {

	/**
	 * 操作day志类型（1Query，2Add to，3Revise，4delete）
	 * 
	 * @return
	 */
	int value() default 0;

}
