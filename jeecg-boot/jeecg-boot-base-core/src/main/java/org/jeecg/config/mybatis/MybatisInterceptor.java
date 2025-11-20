package org.jeecg.config.mybatis;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.binding.MapperMethod.ParamMap;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.TenantConstant;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.TokenUtils;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Properties;

/**
 * mybatisInterceptor，Automatically inject the creator、creation time、Modifier、modification time
 * @Author scott
 * @Date  2019-01-19
 *
 */
@Slf4j
@Component
@Intercepts({ @Signature(type = Executor.class, method = "update", args = { MappedStatement.class, Object.class }) })
public class MybatisInterceptor implements Interceptor {

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
		String sqlId = mappedStatement.getId();
		log.debug("------sqlId------" + sqlId);
		SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
		Object parameter = invocation.getArgs()[1];
		log.debug("------sqlCommandType------" + sqlCommandType);

		if (parameter == null) {
			return invocation.proceed();
		}
		if (SqlCommandType.INSERT == sqlCommandType) {
			LoginUser sysUser = this.getLoginUser();
			Field[] fields = oConvertUtils.getAllFields(parameter);
			for (Field field : fields) {
				log.debug("------field.name------" + field.getName());
				try {
					if ("createBy".equals(field.getName())) {
						field.setAccessible(true);
						Object localCreateBy = field.get(parameter);
						field.setAccessible(false);
						if (localCreateBy == null || "".equals(localCreateBy)) {
							if (sysUser != null) {
								// Login user account
								field.setAccessible(true);
								field.set(parameter, sysUser.getUsername());
								field.setAccessible(false);
							}
						}
					}
					// 注入creation time
					if ("createTime".equals(field.getName())) {
						field.setAccessible(true);
						Object localCreateDate = field.get(parameter);
						field.setAccessible(false);
						if (localCreateDate == null || "".equals(localCreateDate)) {
							field.setAccessible(true);
							field.set(parameter, new Date());
							field.setAccessible(false);
						}
					}
					//Inject department code
					if ("sysOrgCode".equals(field.getName())) {
						field.setAccessible(true);
						Object localSysOrgCode = field.get(parameter);
						field.setAccessible(false);
						if (localSysOrgCode == null || "".equals(localSysOrgCode)) {
							// Get logged in user information
							if (sysUser != null) {
								field.setAccessible(true);
								field.set(parameter, sysUser.getOrgCode());
								field.setAccessible(false);
							}
						}
					}

					//------------------------------------------------------------------------------------------------
					//Inject tenantID（Whether to enable multi-tenant data isolation of the system management module【SAASMulti-tenant model】）
					if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
						if (TenantConstant.TENANT_ID.equals(field.getName())) {
							field.setAccessible(true);
							Object localTenantId = field.get(parameter);
							field.setAccessible(false);
							if (localTenantId == null) {
								field.setAccessible(true);

								String tenantId = TenantContext.getTenant();
								//If you get the tenant through a threadIDis empty，then through the current requestrequestGet tenants（shiro排除Interceptor的请求会获取不到租户ID）
								if(oConvertUtils.isEmpty(tenantId) && MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
									try {
										tenantId = TokenUtils.getTenantIdByRequest(SpringContextUtils.getHttpServletRequest());
									} catch (Exception e) {
										//e.printStackTrace();
									}
								}

								if (field.getType().equals(String.class)) {
									// The field type isString
									field.set(parameter, tenantId);
								} else {
									// The field type is notString
									field.set(parameter, oConvertUtils.getInt(tenantId, 0));
								}
								field.setAccessible(false);
							}
						}
					}
					//------------------------------------------------------------------------------------------------
					
				} catch (Exception e) {
				}
			}
		}
		if (SqlCommandType.UPDATE == sqlCommandType) {
			LoginUser sysUser = this.getLoginUser();
			Field[] fields = null;
			if (parameter instanceof ParamMap) {
				ParamMap<?> p = (ParamMap<?>) parameter;
				//update-begin-author:scott date:20190729 for:Batch update errorissues/IZA3Q--
                String et = "et";
				if (p.containsKey(et)) {
					parameter = p.get(et);
				} else {
					parameter = p.get("param1");
				}
				//update-end-author:scott date:20190729 for:Batch update errorissues/IZA3Q-

				//update-begin-author:scott date:20190729 for:An error occurs when updating the specified field issues/#516-
				if (parameter == null) {
					return invocation.proceed();
				}
				//update-end-author:scott date:20190729 for:An error occurs when updating the specified field issues/#516-

				fields = oConvertUtils.getAllFields(parameter);
			} else {
				fields = oConvertUtils.getAllFields(parameter);
			}

			for (Field field : fields) {
				log.debug("------field.name------" + field.getName());
				try {
					if ("updateBy".equals(field.getName())) {
						//Get logged in user information
						if (sysUser != null) {
							// Login account
							field.setAccessible(true);
							field.set(parameter, sysUser.getUsername());
							field.setAccessible(false);
						}
					}
					if ("updateTime".equals(field.getName())) {
						field.setAccessible(true);
						field.set(parameter, new Date());
						field.setAccessible(false);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return invocation.proceed();
	}

	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	@Override
	public void setProperties(Properties properties) {
		// TODO Auto-generated method stub
	}

	//update-begin--Author:scott  Date:20191213 for：About useQuzrtz Start thread task， #465
    /**
     * Get logged in user
     * @return
     */
	private LoginUser getLoginUser() {
		LoginUser sysUser = null;
		try {
			sysUser = SecurityUtils.getSubject().getPrincipal() != null ? (LoginUser) SecurityUtils.getSubject().getPrincipal() : null;
		} catch (Exception e) {
			//e.printStackTrace();
			sysUser = null;
		}
		return sysUser;
	}
	//update-end--Author:scott  Date:20191213 for：About useQuzrtz Start thread task， #465

}
