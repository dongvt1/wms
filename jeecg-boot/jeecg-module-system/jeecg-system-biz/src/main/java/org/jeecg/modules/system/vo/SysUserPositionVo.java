package org.jeecg.modules.system.vo;

import lombok.Data;

/**
* @Description: User position entity class
*
* @author: wangshuai
* @date: 2023/6/14 16:41
*/
@Data
public class SysUserPositionVo {
    
    /**Positionid*/
    private String id;

    /**Job title*/
    private String name;
    
    /**userid*/
    private String userId;
}
