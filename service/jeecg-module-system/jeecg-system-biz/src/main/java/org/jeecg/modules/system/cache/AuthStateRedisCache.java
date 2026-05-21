package org.jeecg.modules.system.cache;

import jakarta.annotation.PostConstruct;
import me.zhyd.oauth.cache.AuthCacheConfig;
import me.zhyd.oauth.cache.AuthStateCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;


public class AuthStateRedisCache implements AuthStateCache {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private ValueOperations<String, String> valueOperations;

    @PostConstruct
    public void init() {
        valueOperations = redisTemplate.opsForValue();
    }

    /**
     * cache，default3minute
     *
     * @param key   cachekey
     * @param value cache内容
     */
    @Override
    public void cache(String key, String value) {
        valueOperations.set(key, value, AuthCacheConfig.timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * cache
     *
     * @param key     cachekey
     * @param value   cache内容
     * @param timeout 指定cache过期时间（millisecond）
     */
    @Override
    public void cache(String key, String value, long timeout) {
        valueOperations.set(key, value, timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * 获取cache内容
     *
     * @param key cachekey
     * @return cache内容
     */
    @Override
    public String get(String key) {
        return valueOperations.get(key);
    }

    /**
     * existskey，If correspondingkeyofvalueValue has expired，also returnfalse
     *
     * @param key cachekey
     * @return true：existkey，andvalueNot expired；false：key不exist或者已过期
     */
    @Override
    public boolean containsKey(String key) {
        return redisTemplate.hasKey(key);
    }
}
