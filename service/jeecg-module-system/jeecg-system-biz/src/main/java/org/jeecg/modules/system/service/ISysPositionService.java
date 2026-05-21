package org.jeecg.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.system.entity.SysPosition;

import java.util.List;

/**
 * @Description: job list
 * @Author: jeecg-boot
 * @Date: 2019-09-19
 * @Version: V1.0
 */
public interface ISysPositionService extends IService<SysPosition> {

    /**
     * passcodeQuery
     * @param code Job code
     * @return SysPosition
     */
    SysPosition getByCode(String code);

    /**
     * pass用户idGet a list of job titles
     * @param userId
     * @return
     */
    List<SysPosition> getPositionList(String userId);

    /**
     * Get job title
     * @param postList
     * @return
     */
    String getPositionName(List<String> postList);
}
