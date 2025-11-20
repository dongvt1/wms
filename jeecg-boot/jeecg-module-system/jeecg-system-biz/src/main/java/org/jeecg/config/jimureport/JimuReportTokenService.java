package org.jeecg.config.jimureport;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.SysUserCacheInfo;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.TokenUtils;
import org.jeecg.modules.jmreport.api.JmReportTokenServiceI;
import org.jeecg.modules.system.service.impl.SysBaseApiImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Customized building block report authentication(If you don't customize，Then all requests will not be subject to permission control.)
 *  * 1.Customize logintoken
 *  * 2.Customize login用户
 * @author: jeecg-boot
 */


@Slf4j
@Component
public class JimuReportTokenService implements JmReportTokenServiceI {
    @Autowired
    private SysBaseApiImpl sysBaseApi;
    @Autowired
    @Lazy
    private RedisUtil redisUtil;

    @Override
    public String getToken(HttpServletRequest request) {
        return TokenUtils.getTokenByRequest(request);
    }

    @Override
    public String getUsername(String token) {
        return JwtUtil.getUsername(token);
    }

    @Override
    public String[] getRoles(String token) {
        String username = JwtUtil.getUsername(token);
        Set roles = sysBaseApi.getUserRoleSet(username);
        if(CollectionUtils.isEmpty(roles)){
            return null;
        }
        return (String[]) roles.toArray(new String[roles.size()]);
    }

    @Override
    public Boolean verifyToken(String token) {
        return TokenUtils.verifyToken(token, sysBaseApi, redisUtil);
    }

    @Override
    public Map<String, Object> getUserInfo(String token) {
        Map<String, Object> map = new HashMap(5);
        String username = JwtUtil.getUsername(token);
        //passed heretokenCan only get one piece of information User account  What follows is to obtain other information based on the account number Query data or goredis Users can customize according to their own business
        SysUserCacheInfo userInfo = null;
        try {
            userInfo = sysBaseApi.getCacheUser(username);
        } catch (Exception e) {
            log.error("Exception in obtaining user information:"+ e.getMessage());
            return map;
        }
        //Set account name
        map.put(SYS_USER_CODE, userInfo.getSysUserCode());
        //Set department code
        map.put(SYS_ORG_CODE, userInfo.getSysOrgCode());
        // Save all information tomap parsesql/apiwill be based onmap的键值parse
        return map;
    }

    /**
     * WilljeecgbootPlatform permissions are passed to the building block report
     * @param token
     * @return
     */
    @Override
    public String[] getPermissions(String token) {
        // Get user information
        String username = JwtUtil.getUsername(token);
        SysUserCacheInfo userInfo = null;
        try {
            userInfo = sysBaseApi.getCacheUser(username);
        } catch (Exception e) {
            log.error("Exception in obtaining user information:"+ e.getMessage());
        }
        if(userInfo == null){
            return null;
        }
        // Query permissions
        Set<String> userPermissions = sysBaseApi.getUserPermissionSet(userInfo.getSysUserId());
        if(CollectionUtils.isEmpty(userPermissions)){
            return null;
        }
        return userPermissions.toArray(new String[0]);
    }
}
