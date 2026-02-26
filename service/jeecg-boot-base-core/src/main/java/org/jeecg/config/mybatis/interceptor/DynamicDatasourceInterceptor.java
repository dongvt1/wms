package org.jeecg.config.mybatis.interceptor;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Dynamic data source switching interceptor
 *
 * test：Interception parameters，Automatically switch data sources
 * future plans：Later, through this mechanism，Implement multi-tenant switching data source function
 * @author zyf
 */
@Slf4j
public class DynamicDatasourceInterceptor implements HandlerInterceptor {

    /**
     * Called before request processing（Controllerbefore method call）
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestURI = request.getRequestURI();
        log.info("Through multiple data sourcesInterceptor,The current path is{}", requestURI);
        //Get dynamic data source name
        String dsName = request.getParameter("dsName");
        String dsKey = "master";
        if (StringUtils.isNotEmpty(dsName)) {
            dsKey = dsName;
        }
        DynamicDataSourceContextHolder.push(dsKey);
        return true;
    }

    /**
     * Called after request processing，But before the view is rendered（ControllerAfter method call）
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {

    }

    /**
     * Called after the entire request has finished，That is, inDispatcherServlet Executed after rendering the corresponding view（Mainly used for resource cleanup work）
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        DynamicDataSourceContextHolder.clear();
    }

}