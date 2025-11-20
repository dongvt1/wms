package org.jeecg.modules.message.service;

import java.util.List;

import org.jeecg.common.system.base.service.JeecgService;
import org.jeecg.modules.message.entity.SysMessageTemplate;

/**
 * @Description: Message template
 * @Author: jeecg-boot
 * @Date:  2019-04-09
 * @Version: V1.0
 */
public interface ISysMessageTemplateService extends JeecgService<SysMessageTemplate> {

    /**
     * via templateCODE查询Message template
     * @param code templateCODE
     * @return
     */
    List<SysMessageTemplate> selectByCode(String code);
}
