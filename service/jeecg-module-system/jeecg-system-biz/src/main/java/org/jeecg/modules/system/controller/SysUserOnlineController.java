package org.jeecg.modules.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecg.modules.system.service.impl.SysBaseApiImpl;
import org.jeecg.modules.system.vo.SysUserOnlineVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @Description: Online users
 * @Author: chenli
 * @Date: 2020-06-07
 * @Version: V1.0
 */
@RestController
@RequestMapping("/sys/online")
@Slf4j
public class SysUserOnlineController {

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    public RedisTemplate redisTemplate;
    @Autowired
    public ISysUserService userService;
    @Autowired
    private SysBaseApiImpl sysBaseApi;
    @Resource
    private BaseCommonService baseCommonService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Result<Page<SysUserOnlineVO>> list(@RequestParam(name="username", required=false) String username,
                                              @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,@RequestParam(name="pageSize", defaultValue="10") Integer pageSize) {
        Collection<String> keys = redisUtil.scan(CommonConstant.PREFIX_USER_TOKEN + "*");
        List<SysUserOnlineVO> onlineList = new ArrayList<SysUserOnlineVO>();
        for (String key : keys) {
            String token = (String)redisUtil.get(key);
            if (StringUtils.isNotEmpty(token)) {
                SysUserOnlineVO online = new SysUserOnlineVO();
                online.setToken(token);
                //TODO Change to one-time query
                LoginUser loginUser = sysBaseApi.getUserByName(JwtUtil.getUsername(token));
                if (loginUser != null && !"_reserve_user_external".equals(loginUser.getUsername())) {
                    //update-begin---author:wangshuai ---date:20220104  for：[JTC-382]Online users查询invalid------------
                    //Verify that the username is the same as the username passed in
                    boolean isMatchUsername=true;
                    //Determine whether the username is empty，And the current loop of users does not contain the passed user name.，Then set it tofalse
                    if(oConvertUtils.isNotEmpty(username) && !loginUser.getUsername().contains(username)){
                        isMatchUsername = false;
                    }
                    if(isMatchUsername){
                        BeanUtils.copyProperties(loginUser, online);
                        onlineList.add(online);
                    }
                    //update-end---author:wangshuai ---date:20220104  for：[JTC-382]Online users查询invalid------------
                }
            }
        }
        Collections.reverse(onlineList);

        Page<SysUserOnlineVO> page = new Page<SysUserOnlineVO>(pageNo, pageSize);
        int count = onlineList.size();
        List<SysUserOnlineVO> pages = new ArrayList<>();
        // Calculate the subscript of the first data on the current page
        int currId = pageNo > 1 ? (pageNo - 1) * pageSize : 0;
        for (int i = 0; i < pageSize && i < count - currId; i++) {
            pages.add(onlineList.get(currId + i));
        }
        page.setSize(pageSize);
        page.setCurrent(pageNo);
        page.setTotal(count);
        // Calculate the total number of pagination pages
        page.setPages(count % 10 == 0 ? count / 10 : count / 10 + 1);
        page.setRecords(pages);

        Result<Page<SysUserOnlineVO>> result = new Result<Page<SysUserOnlineVO>>();
        result.setSuccess(true);
        result.setResult(page);
        return result;
    }

    /**
     * Forced user withdrawal
     */
    @RequestMapping(value = "/forceLogout",method = RequestMethod.POST)
    public Result<Object> forceLogout(@RequestBody SysUserOnlineVO online) {
        //User exit logic
        if(oConvertUtils.isEmpty(online.getToken())) {
            return Result.error("Logout failed！");
        }
        String username = JwtUtil.getUsername(online.getToken());
        LoginUser sysUser = sysBaseApi.getUserByName(username);
        if(sysUser!=null) {
            baseCommonService.addLog("force: "+sysUser.getRealname()+"Exit successfully！", CommonConstant.LOG_TYPE_1, null,sysUser);
            log.info(" force  "+sysUser.getRealname()+"Exit successfully！ ");
            //Clear user loginTokencache
            redisUtil.del(CommonConstant.PREFIX_USER_TOKEN + online.getToken());
            //Clear user loginShiro权限cache
            redisUtil.del(CommonConstant.PREFIX_USER_SHIRO_CACHE + sysUser.getId());
            //清空用户ofcache信息（Include department information），For examplesys:cache:user::<username>
            redisUtil.del(String.format("%s::%s", CacheConstant.SYS_USERS_CACHE, sysUser.getUsername()));
            //callshirooflogout
            SecurityUtils.getSubject().logout();
            return Result.ok("Log out successfully！");
        }else {
            return Result.error("Tokeninvalid!");
        }
    }
}
