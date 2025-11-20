package org.jeecg.config.sign.interceptor;


import java.io.PrintWriter;
import java.util.SortedMap;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.sign.util.BodyReaderHttpServletRequestWrapper;
import org.jeecg.config.sign.util.HttpUtils;
import org.jeecg.config.sign.util.SignUtil;
import org.springframework.web.servlet.HandlerInterceptor;

import com.alibaba.fastjson.JSON;

import lombok.extern.slf4j.Slf4j;

/**
 * signature interceptor
 * @author qinfeng
 */
@Slf4j
public class SignAuthInterceptor implements HandlerInterceptor {
    /**
     * 5Validity in minutes
     */
    private final static long MAX_EXPIRE = 5 * 60;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("Sign Interceptor request URI = " + request.getRequestURI());
        HttpServletRequest requestWrapper = new BodyReaderHttpServletRequestWrapper(request);
        //Get all parameters(includeURLandbodyon)
        SortedMap<String, String> allParams = HttpUtils.getAllParams(requestWrapper);
        //Signature verification of parameters
        String headerSign = request.getHeader(CommonConstant.X_SIGN);
        String xTimestamp = request.getHeader(CommonConstant.X_TIMESTAMP);
        
        if(oConvertUtils.isEmpty(xTimestamp)){
            Result<?> result = Result.error("SignSignature verification failed，Timestamp is empty！");
            log.error("Sign Signature verification failed！Header xTimestamp is empty");
            //If verification fails, return to the front end
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=utf-8");
            PrintWriter out = response.getWriter();
            out.print(JSON.toJSON(result));
            return false;
        }

        //client time
        Long clientTimestamp = Long.parseLong(xTimestamp);

        int length = 14;
        int length1000 = 1000;
        //1.Verification signature time（compatibleX_TIMESTAMPold and new formats）
        if (xTimestamp.length() == length) {
            //a. X_TIMESTAMPThe format is yyyyMMddHHmmss (example：20220308152143)
            if ((DateUtils.getCurrentTimestamp() - clientTimestamp) > MAX_EXPIRE) {
                log.error("Signature verification failed:X-TIMESTAMPExpired，注意系统时间and服务器时间是否有误差！");
                throw new IllegalArgumentException("Signature verification failed:X-TIMESTAMPExpired");
            }
        } else {
            //b. X_TIMESTAMPThe format is Timestamp (example：1646552406000)
            if ((System.currentTimeMillis() - clientTimestamp) > (MAX_EXPIRE * length1000)) {
                log.error("Signature verification failed:X-TIMESTAMPExpired，注意系统时间and服务器时间是否有误差！");
                throw new IllegalArgumentException("Signature verification failed:X-TIMESTAMPExpired");
            }
        }

        //2.Verify signature
        boolean isSigned = SignUtil.verifySign(allParams,headerSign);

        if (isSigned) {
            log.debug("Sign Signature passed！Header Sign : {}",headerSign);
            return true;
        } else {
            log.info("sign allParams: {}", allParams);
            log.error("request URI = " + request.getRequestURI());
            log.error("Sign Signature verification failed！Header Sign : {}",headerSign);
            //If verification fails, return to the front end
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=utf-8");
            PrintWriter out = response.getWriter();
            Result<?> result = Result.error("SignSignature verification failed！");
            out.print(JSON.toJSON(result));
            return false;
        }
    }

}
