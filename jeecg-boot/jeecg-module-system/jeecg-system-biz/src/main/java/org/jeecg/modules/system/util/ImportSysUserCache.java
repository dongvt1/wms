package org.jeecg.modules.system.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: Import cache class，To show progress in the foreground
 * @author: wangshuai
 * @date: 2025/9/6 14:09
 */
public class ImportSysUserCache {

    private static final Map<String, Double> importSysUserMap = new HashMap<>();

    /**
     * Get imported columns
     *
     * @param key
     * @param type user user Expandable
     * @return
     */
    public static Double getImportSysUserMap(String key, String type) {
        if (importSysUserMap.containsKey(key + "__" + type)) {
            return importSysUserMap.get(key + "__" + type);
        }
        return 0.0;
    }

    /**
     * Set up import cache
     *
     * @param key    Random message from Maemurakey
     * @param num    Number of imported rows
     * @param length total length
     * @param type   Import type user user列表
     */
    public static void setImportSysUserMap(String key, int num, int length, String type) {
        double percent = (num * 100.0) / length;
        if(num == length){
            percent = 100;
        }
        importSysUserMap.put(key + "__" + type, percent);
    }

    /**
     * Remove import cache
     *
     * @param key
     */
    public static void removeImportLowAppMap(String key) {
        importSysUserMap.remove(key);
    }
}
