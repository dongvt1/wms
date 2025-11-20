package org.jeecg.config.mybatis;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @Description: Local thread variable storage tool class
 * @author: lsq
 * @date: 2022Year03moon25day 11:42
 */
public class ThreadLocalDataHelper {
    /**
     * Thread local variables
     */
    private static final ThreadLocal<ConcurrentHashMap> REQUEST_DATA = new ThreadLocal<>();

    /**
     * Store local parameters
     */
    private static final ConcurrentHashMap DATA_MAP = new ConcurrentHashMap<>();

    /**
     * Set request parameters
     *
     * @param key  parameterkey
     * @param value parameter值
     */
    public static void put(String key, Object value) {
        if(ObjectUtil.isNotEmpty(value)) {
            DATA_MAP.put(key, value);
            REQUEST_DATA.set(DATA_MAP);
        }
    }

    /**
     * 获取请求parameter值
     *
     * @param key 请求parameter
     * @return
     */
    public static <T> T get(String key) {
        ConcurrentHashMap dataMap = REQUEST_DATA.get();
        if (CollectionUtils.isNotEmpty(dataMap)) {
            return (T) dataMap.get(key);
        }
        return null;
    }

    /**
     * 获取请求parameter
     *
     * @return 请求parameter MAP object
     */
    public static void clear() {
        DATA_MAP.clear();
        REQUEST_DATA.remove();
    }

}

