package org.jeecg.common.exception;

import cn.hutool.core.util.ObjectUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthorizedException;
import org.jeecg.common.api.dto.LogDTO;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.enums.ClientTerminalTypeEnum;
import org.jeecg.common.enums.SentinelErrorInfoEnum;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.BrowserUtils;
import org.jeecg.common.util.IpUtils;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.base.service.BaseCommonService;
import org.springframework.beans.BeansException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.connection.PoolException;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * exception handler
 * 
 * @Author scott
 * @Date 2019
 */
@RestControllerAdvice
@Slf4j
public class JeecgBootExceptionHandler {

	@Resource
	BaseCommonService baseCommonService;

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public Result<?> handleValidationExceptions(MethodArgumentNotValidException e) {
		log.error(e.getMessage(), e);
		addSysLog(e);
		return Result.error("Verification failed！" + e.getBindingResult().getAllErrors().stream().map(ObjectError::getDefaultMessage).collect(Collectors.joining(",")));
	}
	
	/**
	 * Handle custom exceptions
	 */
	@ExceptionHandler(JeecgBootException.class)
	public Result<?> handleJeecgBootException(JeecgBootException e){
		log.error(e.getMessage(), e);
		addSysLog(e);
		return Result.error(e.getErrCode(), e.getMessage());
	}
	
	/**
	 * Handle custom exceptions
	 */
	@ExceptionHandler(JeecgBootBizTipException.class)
	public Result<?> handleJeecgBootBizTipException(JeecgBootBizTipException e){
		log.error(e.getMessage());
		return Result.error(e.getErrCode(), e.getMessage());
	}

	/**
	 * Handle custom microservice exceptions
	 */
	@ExceptionHandler(JeecgCloudException.class)
	public Result<?> handleJeecgCloudException(JeecgCloudException e){
		log.error(e.getMessage(), e);
		addSysLog(e);
		return Result.error(e.getMessage());
	}

	/**
	 * Handle custom exceptions
	 */
	@ExceptionHandler(JeecgBoot401Exception.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public Result<?> handleJeecgBoot401Exception(JeecgBoot401Exception e){
		log.error(e.getMessage(), e);
		addSysLog(e);
		return new Result(401,e.getMessage());
	}

	@ExceptionHandler(NoHandlerFoundException.class)
	public Result<?> handlerNoFoundException(Exception e) {
		log.error(e.getMessage(), e);
		addSysLog(e);
		return Result.error(404, "path does not exist，Please check if the path is correct");
	}

	@ExceptionHandler(DuplicateKeyException.class)
	public Result<?> handleDuplicateKeyException(DuplicateKeyException e){
		log.error(e.getMessage(), e);
		addSysLog(e);
		return Result.error("The record already exists in the database");
	}

	@ExceptionHandler({UnauthorizedException.class, AuthorizationException.class})
	public Result<?> handleAuthorizationException(AuthorizationException e){
		log.error(e.getMessage(), e);
		return Result.noauth("permission denied，Please contact the administrator to assign permissions！");
	}

	@ExceptionHandler(Exception.class)
	public Result<?> handleException(Exception e){
		log.error(e.getMessage(), e);
		//update-begin---author:zyf ---date:20220411  for：deal withSentinelCurrent limiting custom exception
		Throwable throwable = e.getCause();
		SentinelErrorInfoEnum errorInfoEnum = SentinelErrorInfoEnum.getErrorByException(throwable);
		if (ObjectUtil.isNotEmpty(errorInfoEnum)) {
			return Result.error(errorInfoEnum.getError());
		}
		//update-end---author:zyf ---date:20220411  for：deal withSentinelCurrent limiting custom exception
		addSysLog(e);
		return Result.error("Operation failed，"+e.getMessage());
	}
	
	/**
	 * @Author Zhenghui
	 * @param e
	 * @return
	 */
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public Result<?> httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e){
		StringBuffer sb = new StringBuffer();
		sb.append("Not supported");
		sb.append(e.getMethod());
		sb.append("Request method，");
		sb.append("Support the following");
		String [] methods = e.getSupportedMethods();
		if(methods!=null){
			for(String str:methods){
				sb.append(str);
				sb.append("、");
			}
		}
		log.error(sb.toString(), e);
		//return Result.error("permission denied，Please contact the administrator for authorization");
		addSysLog(e);
		return Result.error(405,sb.toString());
	}
	
	 /** 
	  * springDefault upload size100MB Catch exception if size exceededMaxUploadSizeExceededException 
	  */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<?> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
    	log.error(e.getMessage(), e);
		addSysLog(e);
        return Result.error("File size exceeded10MBlimit, Please compress or reduce file quality! ");
    }

	/**
	 * deal with文件过大abnormal.
	 * jdk17inMultipartExceptionThe exception class has been split intoMultipartExceptionandMaxUploadSizeExceededException
	 * for [QQYUN-11716]There is no precise prompt when uploading large images fails.
	 * @param e
	 * @return
	 * @author chenrui
	 * @date 2025/4/8 16:13
	 */
	@ExceptionHandler(MultipartException.class)
	public Result<?> handleMaxUploadSizeExceededException(MultipartException e) {
		Throwable cause = e.getCause();
		if (cause instanceof IllegalStateException) {
			log.error("File size exceededlimit: {}", cause.getMessage(), e);
			addSysLog(e);
			return Result.error("File size exceededlimit, Please compress or reduce file quality!");
		} else {
			return handleException(e);
		}
	}

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<?> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
    	log.error(e.getMessage(), e);
		addSysLog(e);
    	//【issues/3624】Database execution exceptionhandleDataIntegrityViolationExceptionThe prompt is wrong #3624
        return Result.error("Execution database exception,Violation of integrity e.g.：Violation of unique constraint、违反非空limit、Field content exceeds length, etc.");
    }

    @ExceptionHandler(PoolException.class)
    public Result<?> handlePoolException(PoolException e) {
    	log.error(e.getMessage(), e);
		addSysLog(e);
        return Result.error("Redis Connection abnormality!");
    }


	/**
	 * SQLInject risk，全局abnormaldeal with
	 *
	 * @param exception
	 * @return
	 */
	@ExceptionHandler(JeecgSqlInjectionException.class)
	public Result<?> handleSQLException(Exception exception) {
		String msg = exception.getMessage().toLowerCase();
		final String extractvalue = "extractvalue";
		final String updatexml = "updatexml";
		boolean hasSensitiveInformation = msg.indexOf(extractvalue) >= 0 || msg.indexOf(updatexml) >= 0;
		if (msg != null && hasSensitiveInformation) {
			log.error("Verification failed，existSQLInject risk！{}", msg);
			return Result.error("Verification failed，existSQLInject risk！");
		}
		addSysLog(exception);
		return Result.error("Verification failed，existSQLInject risk！" + msg);
	}

	//update-begin---author:chenrui ---date:20240423  for：[QQYUN-8732]Captured all error logs 方便后续deal with，Get a separate log type------------
	/**
	 * Add exception new system log
	 * @param e abnormal
	 * @author chenrui
	 * @date 2024/4/22 17:16
	 */
    private void addSysLog(Throwable e) {
        LogDTO log = new LogDTO();
        log.setLogType(CommonConstant.LOG_TYPE_4);
        log.setLogContent(e.getClass().getName()+":"+e.getMessage());
		log.setRequestParam(ExceptionUtils.getStackTrace(e));
        //Getrequest
        HttpServletRequest request = null;
        try {
            request = SpringContextUtils.getHttpServletRequest();
        } catch (NullPointerException | BeansException ignored) {
        }
        if (null != request) {
			//update-begin---author:chenrui ---date:20250408  for：[QQYUN-11716]There is no precise prompt when uploading large images fails.------------
			//Request parameters
			if (!isTooBigException(e)) {
				// 文件上传过大abnormal时不能Get参数,Otherwise an error will be reported
				Map<String, String[]> parameterMap = request.getParameterMap();
				if(!CollectionUtils.isEmpty(parameterMap)) {
					log.setMethod(oConvertUtils.mapToString(request.getParameterMap()));
				}
			}
			//update-end---author:chenrui ---date:20250408  for：[QQYUN-11716]There is no precise prompt when uploading large images fails.------------
            // Request address
            log.setRequestUrl(request.getRequestURI());
            //set upIPaddress
            log.setIp(IpUtils.getIpAddr(request));
            //set up客户端
			if(BrowserUtils.isDesktop(request)){
				log.setClientType(ClientTerminalTypeEnum.PC.getKey());
			}else{
				log.setClientType(ClientTerminalTypeEnum.APP.getKey());
			}
        }

       
		//Get登录用户信息
		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		if(sysUser!=null){
			log.setUserid(sysUser.getUsername());
			log.setUsername(sysUser.getRealname());

		}

        baseCommonService.addLog(log);
    }
	//update-end---author:chenrui ---date:20240423  for：[QQYUN-8732]Captured all error logs 方便后续deal with，Get a separate log type------------

	/**
	 * 是否文件过大abnormal
	 * for [QQYUN-11716]There is no precise prompt when uploading large images fails.
	 * @param e
	 * @return
	 * @author chenrui
	 * @date 2025/4/8 20:21
	 */
	private static boolean isTooBigException(Throwable e) {
		boolean isTooBigException = false;
		if(e instanceof MultipartException){
			Throwable cause = e.getCause();
			if (cause instanceof IllegalStateException){
				isTooBigException = true;
			}
		}
		if(e instanceof MaxUploadSizeExceededException){
			isTooBigException = true;
		}
		return isTooBigException;
	}

}
