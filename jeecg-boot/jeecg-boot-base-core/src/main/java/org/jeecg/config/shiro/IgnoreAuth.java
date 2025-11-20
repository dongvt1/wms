package org.jeecg.config.shiro;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * waivedTokenCertification annotations
 * 
 * Authentication system integrationspring MVCof@RequestMapping获取请求路径进行waived登录配置
 * @author eightmonth
 * @date 2024/2/28 9:58
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreAuth {
}
