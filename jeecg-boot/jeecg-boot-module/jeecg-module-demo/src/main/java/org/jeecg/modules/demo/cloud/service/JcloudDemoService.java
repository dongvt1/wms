package org.jeecg.modules.demo.cloud.service;

import org.jeecg.common.api.vo.Result;

/**
 * @Description: JcloudDemoServiceinterface
 * @author: jeecg-boot
 */
public interface JcloudDemoService {

    /**
     * Get information（test）
     * @param name Name
     * @return "Hello，" + name
     */
    String getMessage(String name);
}
