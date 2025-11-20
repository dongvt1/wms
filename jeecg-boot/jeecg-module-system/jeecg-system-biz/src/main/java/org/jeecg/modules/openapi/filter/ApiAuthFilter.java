package org.jeecg.modules.openapi.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.openapi.entity.OpenApi;
import org.jeecg.modules.openapi.entity.OpenApiAuth;
import org.jeecg.modules.openapi.entity.OpenApiLog;
import org.jeecg.modules.openapi.entity.OpenApiPermission;
import org.jeecg.modules.openapi.service.OpenApiAuthService;
import org.jeecg.modules.openapi.service.OpenApiLogService;
import org.jeecg.modules.openapi.service.OpenApiPermissionService;
import org.jeecg.modules.openapi.service.OpenApiService;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @date 2024/12/19 16:55
 */
@Slf4j
public class ApiAuthFilter implements Filter {

    private OpenApiLogService openApiLogService;
    private OpenApiAuthService openApiAuthService;
    private OpenApiPermissionService openApiPermissionService;
    private OpenApiService openApiService;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        long startTime = System.currentTimeMillis();
        Date callTime = new Date();

        HttpServletRequest request = (HttpServletRequest)servletRequest;
        String ip = request.getRemoteAddr();

        String appkey = request.getHeader("appkey");
        String signature = request.getHeader("signature");
        String timestamp = request.getHeader("timestamp");

        OpenApi openApi = findOpenApi(request);

        // IP Blacklist verification
        checkBlackList(openApi, ip);

        // Signature verification
        checkSignValid(appkey, signature, timestamp);

        OpenApiAuth openApiAuth = openApiAuthService.getByAppkey(appkey);
        // Certification information verification
        checkSignature(appkey, signature, timestamp, openApiAuth);
        // Business verification
        checkPermission(openApi, openApiAuth);

        filterChain.doFilter(servletRequest, servletResponse);
        long endTime = System.currentTimeMillis();

        OpenApiLog openApiLog = new OpenApiLog();
        openApiLog.setApiId(openApi.getId());
        openApiLog.setCallAuthId(openApiAuth.getId());
        openApiLog.setCallTime(callTime);
        openApiLog.setUsedTime(endTime - startTime);
        openApiLog.setResponseTime(new Date());
        openApiLogService.save(openApiLog);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        ServletContext servletContext = filterConfig.getServletContext();
        WebApplicationContext applicationContext = (WebApplicationContext)servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        this.openApiService = applicationContext.getBean(OpenApiService.class);
        this.openApiLogService = applicationContext.getBean(OpenApiLogService.class);
        this.openApiAuthService = applicationContext.getBean(OpenApiAuthService.class);
        this.openApiPermissionService = applicationContext.getBean(OpenApiPermissionService.class);
    }

    /**
     * IP Blacklist verification
     * @param openApi
     * @param ip
     */
    protected void checkBlackList(OpenApi openApi, String ip) {
        if (!StringUtils.hasText(openApi.getBlackList())) {
            return;
        }

        List<String> blackList = Arrays.asList(openApi.getBlackList().split(","));
        if (blackList.contains(ip)) {
            throw new JeecgBootException("Target interface restrictionsIP[" + ip + "]to visit，IPRecorded，Please stop accessing");
        }
    }

    /**
     * Signature verification
     * @param appkey
     * @param signature
     * @param timestamp
     * @return
     */
    protected void checkSignValid(String appkey, String signature, String timestamp) {
        if (!StringUtils.hasText(appkey)) {
            throw new JeecgBootException("appkeyis empty");
        }
        if (!StringUtils.hasText(signature)) {
            throw new JeecgBootException("signatureis empty");
        }
        if (!StringUtils.hasText(timestamp)) {
            throw new JeecgBootException("timastamp时间戳is empty");
        }
        if (!timestamp.matches("[0-9]*")) {
            throw new JeecgBootException("timastampTimestamp is invalid");
        }
        if (System.currentTimeMillis() - Long.parseLong(timestamp) > 5 * 60 * 1000) {
            throw new JeecgBootException("signatureSignature has expired(more than five minutes)");
        }
    }

    /**
     * Certification information verification
     * @param appKey
     * @param signature
     * @param timestamp
     * @param openApiAuth
     * @return
     * @throws Exception
     */
    protected void checkSignature(String appKey, String signature, String timestamp, OpenApiAuth openApiAuth) {
        if(openApiAuth==null){
            throw new JeecgBootException("No authentication information exists");
        }

        if(!appKey.equals(openApiAuth.getAk())){
            throw new JeecgBootException("appkeymistake");
        }

        if (!signature.equals(md5(appKey + openApiAuth.getSk() + timestamp))) {
            throw new JeecgBootException("signature签名mistake");
        }
    }

    protected void checkPermission(OpenApi openApi, OpenApiAuth openApiAuth) {
        List<OpenApiPermission> permissionList = openApiPermissionService.findByAuthId(openApiAuth.getId());

        boolean hasPermission = false;
        for (OpenApiPermission permission : permissionList) {
            if (permission.getApiId().equals(openApi.getId())) {
                hasPermission = true;
                break;
            }
        }

        if (!hasPermission) {
            throw new JeecgBootException("ShouldappKeyUnauthorized current interface");
        }
    }

    /**
     * @return String    Return type
     * @Title: MD5
     * @Description: 【MD5encryption】
     */
    protected static String md5(String sourceStr) {
        String result = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(sourceStr.getBytes("utf-8"));
            byte[] hash = md.digest();
            int i;
            StringBuffer buf = new StringBuffer(32);
            for (int offset = 0; offset < hash.length; offset++) {
                i = hash[offset];
                if (i < 0) {
                    i += 256;
                }
                if (i < 16) {
                    buf.append("0");
                }
                buf.append(Integer.toHexString(i));
            }
            result = buf.toString();
        } catch (Exception e) {
            log.error("sign签名mistake", e);
        }
        return result;
    }

    protected OpenApi findOpenApi(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String path = uri.substring(uri.lastIndexOf("/") + 1);
        return openApiService.findByPath(path);
    }

    public static void main(String[] args) {
        long timestamp = System.currentTimeMillis();
        System.out.println("timestamp:"  + timestamp);
        System.out.println("signature:" + md5("ak-eAU25mrMxhtaZsyS" + "rjxMqB6YyUXpSHAz4DCIz8vZ5aozQQiV" + timestamp));
    }
}
