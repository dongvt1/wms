package org.jeecg.config.firewall.interceptor;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.CommonAPI;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.CommonUtils;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.config.JeecgBaseConfig;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

/**
 * low code mode（dev:development mode，prod:Release mode——Close all online development and configuration capabilities）
 * <p>
 * prodWhen turned on, the following functions will be turned off，Keep only functional tests（haveadminRole account，Configuration capabilities are available）
 * 1.onlineAll configuration functions of the form，Code generation and import table functionality
 * 2.onlineAll configuration features for reports，andsqlparse
 * 3.onlineAll configuration functions for charts，andsqlparse
 * 4.Dashboard online configuration function，andsqlparse
 * 5.Large screen online configuration function，andsqlparse
 * 
 * The logic of building blocks is handled separately
 * 1.Online configuration function of building block report，andsqlparse
 *
 * @author qinfeng
 * @date 20230904
 */
@Slf4j
@Component
public class LowCodeModeInterceptor implements HandlerInterceptor {
    /**
     * 低代码development mode
     */
    public static final String LOW_CODE_MODE_DEV = "dev";
    public static final String LOW_CODE_MODE_PROD = "prod";

    @Resource
    private JeecgBaseConfig jeecgBaseConfig;
    
    /**
     * Called before request processing
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        CommonAPI commonAPI = null;
        log.info("low code mode，Intercept request path：" + request.getRequestURI());
        
        //1、验证是否开启低代码development mode控制
        if (jeecgBaseConfig == null) {
            jeecgBaseConfig = SpringContextUtils.getBean(JeecgBaseConfig.class);
        }
        if (commonAPI == null) {
            commonAPI = SpringContextUtils.getBean(CommonAPI.class);
        }

        if (jeecgBaseConfig.getFirewall()!=null && LowCodeModeInterceptor.LOW_CODE_MODE_PROD.equals(jeecgBaseConfig.getFirewall().getLowCodeMode())) {
            String requestURI = request.getRequestURI().substring(request.getContextPath().length());
            log.info("low code mode，Intercept request path：" + requestURI);
            LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            Set<String> hasRoles = null;
            if (loginUser == null) {
                loginUser = commonAPI.getUserByName(JwtUtil.getUserNameByToken(SpringContextUtils.getHttpServletRequest()));
            }

            if (loginUser != null) {
                //当前登录人have的角色
                hasRoles = commonAPI.queryUserRolesById(loginUser.getId());
            }
            
            log.info("get loginUser info: {}", loginUser);
            log.info("get loginRoles info: {}", hasRoles != null ? hasRoles.toArray() : "null");
            
            //have的角色 and Allow development roles to overlap
            boolean hasIntersection = CommonUtils.hasIntersection(hasRoles, CommonConstant.allowDevRoles);
            //If you are a super administrator or Roles allowed to be developed，no restrictions
            if (loginUser!=null && ("admin".equals(loginUser.getUsername()) || hasIntersection)) {
                return true;
            }
            
            this.returnErrorMessage(response);
            return false;
        }
        return true;
    }


    /**
     * Return results
     *
     * @param response
     */
    private void returnErrorMessage(HttpServletResponse response) {
        //If verification fails, return to the front end
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        PrintWriter out = null;
        try {
            out = response.getWriter();
            Result<?> result = Result.error("低代码development mode为Release mode，Online configuration is not allowed！！");
            out.print(JSON.toJSON(result));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

