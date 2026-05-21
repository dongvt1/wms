package org.jeecg.config.sign.interceptor;

import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.util.PathMatcherUtil;
import org.jeecg.config.JeecgBaseConfig;
import org.jeecg.config.filter.RequestBodyReserveFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.Resource;

/**
 * sign Interceptor configuration
 * @author: jeecg-boot
 */
@Configuration
public class SignAuthConfiguration implements WebMvcConfigurer {
    @Resource
    JeecgBaseConfig jeecgBaseConfig;

    @Bean
    public SignAuthInterceptor signAuthInterceptor() {
        return new SignAuthInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //------------------------------------------------------------
        //查询需要进行sign拦截的接口 signUrls
        String signUrls = jeecgBaseConfig.getSignUrls();
        String[] signUrlsArray = null;
        if (StringUtils.isNotBlank(signUrls)) {
            signUrlsArray = signUrls.split(",");
        } else {
            signUrlsArray = PathMatcherUtil.SIGN_URL_LIST;
        }
        //------------------------------------------------------------
        registry.addInterceptor(signAuthInterceptor()).addPathPatterns(signUrlsArray);
    }

    //update-begin-author:taoyan date:20220427 for: issues/I53J5E postaskX_SIGNsign拦截校验后报错, request body is empty
    @Bean
    public RequestBodyReserveFilter requestBodyReserveFilter(){
        return new RequestBodyReserveFilter();
    }

    @Bean
    public FilterRegistrationBean reqBodyFilterRegistrationBean(){
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(requestBodyReserveFilter());
        registration.setName("requestBodyReserveFilter");
        //------------------------------------------------------------
        //查询需要进行sign拦截的接口 signUrls
        String signUrls = jeecgBaseConfig.getSignUrls();
        String[] signUrlsArray = null;
        if (StringUtils.isNotBlank(signUrls)) {
            signUrlsArray = signUrls.split(",");
        } else {
            signUrlsArray = PathMatcherUtil.SIGN_URL_LIST;
        }
        //------------------------------------------------------------
        // It is recommended to only add herepostask地址而不是所有的都需要走过滤器
        registration.addUrlPatterns(signUrlsArray);
        return registration;
    }
    //update-end-author:taoyan date:20220427 for: issues/I53J5E postaskX_SIGNsign拦截校验后报错, request body is empty

}
