package org.jeecg.config.init;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * ShiroCache cleaning
 * Clear all when app startsShiroAuthorization cache
 * Mainly used to solve restart projects，User did not log in again，Problem with button permissions not taking effect
 */
@Slf4j
@Component
public class ShiroCacheClearRunner implements ApplicationRunner {

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public void run(ApplicationArguments args) {
        // Clear all authorizationsrediscache
        log.info("——— Service restart, clearing all user shiro authorization cache ——— ");
        redisUtil.removeAll(CommonConstant.PREFIX_USER_SHIRO_CACHE);

    }
}