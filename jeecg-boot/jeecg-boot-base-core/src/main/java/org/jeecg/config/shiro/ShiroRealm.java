package org.jeecg.config.shiro;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.jeecg.common.api.CommonAPI;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.TokenUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Role;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;

/**
 * @Description: User login authentication and obtaining user authorization
 * @Author: Scott
 * @Date: 2019-4-23 8:13
 * @Version: 1.1
 */
@Component
@Slf4j
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class ShiroRealm extends AuthorizingRealm {
	@Lazy
    @Resource
    private CommonAPI commonApi;

    @Lazy
    @Resource
    private RedisUtil redisUtil;

    /**
     * This method must be overridden，otherwiseShiroWill report an error
     */
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }

    /**
     * Permission information authentication(Including roles and permissions)is user accesscontrollerVerification is only performed when(redisPermission information stored here)
     * This method will be called only when detecting user permissions is triggered.，For examplecheckRole,checkPermission
     *
     * @param principals Identity information
     * @return AuthorizationInfo Permission information
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        log.debug("===============ShiroPermission authentication starts============ [ roles、permissions]==========");
        String username = null;
        String userId = null;
        if (principals != null) {
            LoginUser sysUser = (LoginUser) principals.getPrimaryPrincipal();
            username = sysUser.getUsername();
            userId = sysUser.getId();
        }
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();

        // Set the role collection owned by the user，for example“admin,test”
        Set<String> roleSet = commonApi.queryUserRolesById(userId);
        //System.out.println(roleSet.toString());
        info.setRoles(roleSet);

        // Set the set of permissions owned by the user，for example“sys:role:add,sys:user:add”
        Set<String> permissionSet = commonApi.queryUserAuths(userId);
        info.addStringPermissions(permissionSet);
        //System.out.println(permissionSet);
        log.info("===============ShiroPermission authentication successful==============");
        return info;
    }

    /**
     * User information authentication is verified when the user logs in.(Does not existredis)
     * That is to say, verify whether the account number and password entered by the user are correct.，Error throws exception
     *
     * @param auth User login account and password information
     * @return Returns the user information encapsulated AuthenticationInfo Example
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken auth) throws AuthenticationException {
        log.debug("===============ShiroIdentity authentication starts============doGetAuthenticationInfo==========");
        String token = (String) auth.getCredentials();
        if (token == null) {
            HttpServletRequest req = SpringContextUtils.getHttpServletRequest();
            log.info("————————Authentication failed——————————IPaddress:  "+ oConvertUtils.getIpAddrByRequest(req) +"，URL:"+req.getRequestURI());
            throw new AuthenticationException("tokenis empty!");
        }
        // checktokeneffectiveness
        LoginUser loginUser = null;
        try {
            loginUser = this.checkUserTokenIsEffect(token);
        } catch (AuthenticationException e) {
            log.error("—————check check token fail——————————"+ e.getMessage(), e);
            // rethrow exception，letJwtFilterUnified processing，Avoid returning an error response twice
            throw e;
        }
        return new SimpleAuthenticationInfo(loginUser, token, getName());
    }

    /**
     * checktokenofeffectiveness
     *
     * @param token
     */
    public LoginUser checkUserTokenIsEffect(String token) throws AuthenticationException {
        // Decrypt to obtainusername，Used for comparison with database
        String username = JwtUtil.getUsername(token);
        if (username == null) {
            throw new AuthenticationException("TokenIllegal and invalid!");
        }

        // Query user information
        log.debug("———checktokenIs it valid?————checkUserTokenIsEffect——————— "+ token);
        LoginUser loginUser = TokenUtils.getLoginUser(username, commonApi, redisUtil);
        //LoginUser loginUser = commonApi.getUserByName(username);
        if (loginUser == null) {
            throw new AuthenticationException("用户Does not exist在!");
        }
        // Determine user status
        if (loginUser.getStatus() != 1) {
            throw new AuthenticationException("Account has been locked,Please contact the administrator!");
        }
        // checktokenWhether it expires after timeout & Or whether the account password is wrong?
        if (!jwtTokenRefresh(token, username, loginUser.getPassword())) {
            throw new AuthenticationException(CommonConstant.TOKEN_IS_INVALID_MSG);
        }
        //update-begin-author:taoyan date:20210609 for:check用户oftenant_idIs it consistent with what was passed from the front end?
        String userTenantIds = loginUser.getRelTenantIds();
        if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL && oConvertUtils.isNotEmpty(userTenantIds)){
            String contextTenantId = TenantContext.getTenant();
            log.debug("Log in to tenant：" + contextTenantId);
            log.debug("Which tenants does the user own?：" + userTenantIds);
             //Login user has no tenant，front endheadermedium tenantIDThe value is 0
            String str ="0";
            if(oConvertUtils.isNotEmpty(contextTenantId) && !str.equals(contextTenantId)){
                //update-begin-author:taoyan date:20211227 for: /issues/I4O14W User tenant information change judgment vulnerability
                String[] arr = userTenantIds.split(",");
                if(!oConvertUtils.isIn(contextTenantId, arr)){
                    boolean isAuthorization = false;
                    //========================================================================
                    // Query user information（If the tenant does not match, re-query the user information from the database.）
                    String loginUserKey = CacheConstant.SYS_USERS_CACHE + "::" + username;
                    redisUtil.del(loginUserKey);
                    LoginUser loginUserFromDb = commonApi.getUserByName(username);
                    if (oConvertUtils.isNotEmpty(loginUserFromDb.getRelTenantIds())) {
                        String[] newArray = loginUserFromDb.getRelTenantIds().split(",");
                        if (oConvertUtils.isIn(contextTenantId, newArray)) { 
                            isAuthorization = true;
                        }
                    }
                    //========================================================================

                    //*********************************************
                    if(!isAuthorization){
                        log.info("Tenant exception——Log in to tenant：" + contextTenantId);
                        log.info("Tenant exception——User owns tenant group：" + userTenantIds);
                        throw new AuthenticationException("Log in to tenant授权变更，Please log in again!");
                    }
                    //*********************************************
                }
                //update-end-author:taoyan date:20211227 for: /issues/I4O14W User tenant information change judgment vulnerability
            }
        }
        //update-end-author:taoyan date:20210609 for:check用户oftenant_idIs it consistent with what was passed from the front end?
        return loginUser;
    }

    /**
     * JWTTokenRefresh life cycle （accomplish： Function for users to operate online without being disconnected）
     * 1、After successful login, the user'sJWTgeneratedTokenask、vstore tocacheInside the cache(At this timek、vSame value)，The cache validity period is set toJwtvalid time2times
     * 2、when the user requests again，passJWTFilter层层check之后会进入到doGetAuthenticationInfoAuthenticate
     * 3、When the user requests this timejwtgeneratedtokenValue has timed out，But thetokencorrespondcacheinkStill exists，It means that the user has been operating onlyJWToftokenInvalid，The program will givetokencorrespondofk映射ofvvalue regenerationJWTTokenand overridevvalue，The cache lifetime is recalculated
     * 4、When the user requests this timejwt在generatedtokenValue has timed out，and incachemiddleDoes not exist在correspondofk，It means that the user account has been idle for timeout.，Returning user information has expired，Please log in again。
     * Notice： front end请求HeaderMedium settingsAuthorizationremain unchanged，checkeffectiveness以缓存intokenSubject to。
     *       User expiration time = JwtValid time * 2。
     *
     * @param userName
     * @param passWord
     * @return
     */
    public boolean jwtTokenRefresh(String token, String userName, String passWord) {
        String cacheToken = String.valueOf(redisUtil.get(CommonConstant.PREFIX_USER_TOKEN + token));
        if (oConvertUtils.isNotEmpty(cacheToken)) {
            // checktokeneffectiveness
            if (!JwtUtil.verify(cacheToken, userName, passWord)) {
                String newAuthorization = JwtUtil.sign(userName, passWord);
                // Set timeout
                redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + token, newAuthorization);
                redisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + token, JwtUtil.EXPIRE_TIME *2 / 1000);
                log.debug("——————————User online operation，renewtokenGuaranteed not to be dropped—————————jwtTokenRefresh——————— "+ token);
            }
            //update-begin--Author:scott  Date:20191005  for：Resolve every request，rewrite allredismiddle tokencaching problem
//			else {
//				// Set timeout
//				redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + token, cacheToken);
//				redisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + token, JwtUtil.EXPIRE_TIME / 1000);
//			}
            //update-end--Author:scott  Date:20191005   for：Resolve every request，rewrite allredismiddle tokencaching problem
            return true;
        }

        //redismiddleDoes not exist在此TOEKN，illustratetokenIllegal returnfalse
        return false;
    }

    /**
     * 清除当前用户of权限认证缓存
     *
     * @param principals Permission information
     */
    @Override
    public void clearCache(PrincipalCollection principals) {
        super.clearCache(principals);
        //update-begin---author:scott ---date::2024-06-18  for：【TV360X-1320】To assign permissions, you must log out and log in again to take effect.，Causes a lot of trouble for users---
        super.clearCachedAuthorizationInfo(principals);
        //update-end---author:scott ---date::2024-06-18  for：【TV360X-1320】To assign permissions, you must log out and log in again to take effect.，Causes a lot of trouble for users---
    }
}
