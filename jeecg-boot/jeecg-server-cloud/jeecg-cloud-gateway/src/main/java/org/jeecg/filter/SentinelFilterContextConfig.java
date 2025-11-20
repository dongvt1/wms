//package org.jeecg.filter;
//
//import com.alibaba.csp.sentinel.adapter.servlet.CommonFilter;
//import org.springframework.boot.web.servlet.FilterRegistrationBean;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
///** upgradespring boot 3back，Unable to find replacement
// * @author: zyf
// * @date: 20210715
// */
//@Configuration
//public class SentinelFilterContextConfig {
//    @Bean
//    public FilterRegistrationBean sentinelFilterRegistration() {
//        FilterRegistrationBean registration = new FilterRegistrationBean();
//        registration.setFilter(new CommonFilter());
//        registration.addUrlPatterns("/*");
//        // Entry resource close aggregation
//        registration.addInitParameter(CommonFilter.WEB_CONTEXT_UNIFY, "false");
//        registration.setName("sentinelFilter");
//        registration.setOrder(1);
//        return registration;
//    }
//}