package org.jeecg.config.shiro.filters;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.AccessControlFilter;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author Scott
 * @create 2019-02-01 15:56
 * @desc Authentication requestURLAccess blocker
 */
@Slf4j
public class ResourceCheckFilter extends AccessControlFilter {

    private String errorUrl;

    public String getErrorUrl() {
        return errorUrl;
    }

    public void setErrorUrl(String errorUrl) {
        this.errorUrl = errorUrl;
    }

    /**
     * Indicates whether access is allowed ，If access is allowed returntrue，otherwisefalse；
     *
     * @param servletRequest
     * @param servletResponse
     * @param o               Represents the string written in the brackets in the interceptor mappedValue that is [urls] Interceptor parameter section in configuration
     * @return
     * @throws Exception
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest servletRequest, ServletResponse servletResponse, Object o) throws Exception {
        Subject subject = getSubject(servletRequest, servletResponse);
        String url = getPathWithinApplication(servletRequest);
        log.info("The current user is accessing url => " + url);
        return subject.isPermitted(url);
    }

    /**
     * onAccessDenied：Indicates whether it has been processed when access is denied； if return true Indicates the need to continue processing； if return false
     * Indicates that the interceptor instance has been processed，Will return directly。
     *
     * @param servletRequest
     * @param servletResponse
     * @return
     * @throws Exception
     */
    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        log.info("when isAccessAllowed return false when，will be executed method onAccessDenied ");

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        response.sendRedirect(request.getContextPath() + this.errorUrl);

        // return false Indicates that it has been processed，For example, page jump or something like that，Indicates that the following interceptors are no longer used:（If there is any configuration）
        return false;
    }

}