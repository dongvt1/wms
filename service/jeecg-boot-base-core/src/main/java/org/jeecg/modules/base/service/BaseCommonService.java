package org.jeecg.modules.base.service;

import org.jeecg.common.api.dto.LogDTO;
import org.jeecg.common.system.vo.LoginUser;

/**
 * commoninterface
 * @author: jeecg-boot
 */
public interface BaseCommonService {

    /**
     * save log
     * @param logDTO
     */
    void addLog(LogDTO logDTO);

    /**
     * save log
     * @param logContent
     * @param logType
     * @param operateType
     * @param user
     */
    void addLog(String logContent, Integer logType, Integer operateType, LoginUser user);

    /**
     * save log
     * @param logContent
     * @param logType
     * @param operateType
     */
    void addLog(String logContent, Integer logType, Integer operateType);

}
