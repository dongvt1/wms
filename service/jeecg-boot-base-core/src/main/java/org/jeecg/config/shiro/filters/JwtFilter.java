package org.jeecg.config.shiro.filters;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.shiro.JwtToken;
import org.jeecg.config.shiro.ignore.InMemoryIgnoreAuth;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @Description: Authentication login interceptor
 * @Author: Scott
 * @Date: 2018/10/7
 **/
@Slf4j
public class JwtFilter extends BasicHttpAuthenticationFilter {

    /**
     * Cross-domain settings are enabled by default（Use a singleton）
     * In the case of microservices，This property is set tofalse
     */
    private boolean allowOrigin = true;

    public JwtFilter(){}
    public JwtFilter(boolean allowOrigin){
        this.allowOrigin = allowOrigin;
    }

    /**
     * Perform login authentication
     *
     * @param request
     * @param response
     * @param mappedValue
     * @return
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        try {
            // Determine whether the current path is annotated@IngoreAuthpath，in the case of，then let go of verification
            if (InMemoryIgnoreAuth.contains(((HttpServletRequest) request).getServletPath())) {
                return true;
            }
            
            executeLogin(request, response);
            return true;
        } catch (Exception e) {
            JwtUtil.responseError((HttpServletResponse)response,401,CommonConstant.TOKEN_IS_INVALID_MSG);
            return false;
            //throw new AuthenticationException("TokenInvalid，Please log in again", e);
        }
    }

    /**
     *
     */
    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String token = httpServletRequest.getHeader(CommonConstant.X_ACCESS_TOKEN);
        // update-begin--Author:lvdandan Date:20210105 for：JT-355 OAChat addedtokenverify，Gettokenparameter
        if (oConvertUtils.isEmpty(token)) {
            token = httpServletRequest.getParameter("token");
        }
        // update-end--Author:lvdandan Date:20210105 for：JT-355 OAChat addedtokenverify，Gettokenparameter

        JwtToken jwtToken = new JwtToken(token);
        // Submit torealmLog in，If there is an error it will throw an exception and be caught
        getSubject(request, response).login(jwtToken);
        // If no exception is thrown, the login is successful.，returntrue
        return true;
    }

    /**
     * Provide support for cross-domain
     */
    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        if(allowOrigin){
            httpServletResponse.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, httpServletRequest.getHeader(HttpHeaders.ORIGIN));
            // Allow clients to request methods
            httpServletResponse.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,OPTIONS,PUT,DELETE");
            // Allow clients to submitHeader
            String requestHeaders = httpServletRequest.getHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS);
            if (StringUtils.isNotEmpty(requestHeaders)) {
                httpServletResponse.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, requestHeaders);
            }
            // Allow clients to carry credential information(Whether to allow sendingCookie)
            httpServletResponse.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        }
        // When crossing domains, aoptionask，Here we giveoptionask直接return正常状态
        if (RequestMethod.OPTIONS.name().equalsIgnoreCase(httpServletRequest.getMethod())) {
            httpServletResponse.setStatus(HttpStatus.OK.value());
            return false;
        }
        //update-begin-author:taoyan date:20200708 for:Used by multi-tenants
        String tenantId = httpServletRequest.getHeader(CommonConstant.TENANT_ID);
        TenantContext.setTenant(tenantId);
        //update-end-author:taoyan date:20200708 for:Used by multi-tenants

        return super.preHandle(request, response);
    }

    /**
     * JwtFiltermiddleThreadLocalNeed to be cleared in time #3634
     *
     * @param request
     * @param response
     * @param exception
     * @throws Exception
     */
    @Override
    public void afterCompletion(ServletRequest request, ServletResponse response, Exception exception) throws Exception {
        //log.info("------清空线程middle多租户的ID={}------",TenantContext.getTenant());
        TenantContext.clear();
    }
}
