//package org.jeecg.modules.ngalain.aop;
//
//import jakarta.servlet.http.HttpServletRequest;
//
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Pointcut;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.context.request.RequestAttributes;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;;
//
//
//// Comment out for now，Improve system performance
////@Aspect   //define an aspect
////@Configuration
//public class LogRecordAspect {
//private static final Logger logger = LoggerFactory.getLogger(LogRecordAspect.class);
//
//    // Define cut pointsPointcut
//    @Pointcut("execution(public * org.jeecg.modules.*.*.*Controller.*(..))")
//    public void excudeService() {
//    }
//
//    @Around("excudeService()")
//    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
//        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
//        ServletRequestAttributes sra = (ServletRequestAttributes) ra;
//        HttpServletRequest request = sra.getRequest();
//
//        String url = request.getRequestURL().toString();
//        String method = request.getMethod();
//        String uri = request.getRequestURI();
//        String queryString = request.getQueryString();
//        logger.info("Request to start, Various parameters, url: {}, method: {}, uri: {}, params: {}", url, method, uri, queryString);
//
//        // resultThe value is the return value of the intercepted method
//        Object result = pjp.proceed();
//
//        logger.info("Request end，controllerThe return value is " + result);
//        return result;
//    }
//}