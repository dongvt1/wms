package org.jeecg.modules.openapi.generator;

import lombok.experimental.UtilityClass;

import java.util.Random;

/**
 * @date 2024/12/10 10:00
 */
@UtilityClass
public class PathGenerator {

    // Base62character set
    private static final String BASE62 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    /**
     * Generate random paths
     * @return
     */
    public static String genPath() {
        StringBuilder result = new StringBuilder();
        Random random = new Random();
        for (int i=0; i<8; i++) {
            result.append(BASE62.charAt(random.nextInt(62)));
        }
        return result.toString();
    }
}
