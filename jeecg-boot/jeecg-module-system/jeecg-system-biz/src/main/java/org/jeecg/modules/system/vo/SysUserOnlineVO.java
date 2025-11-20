package org.jeecg.modules.system.vo;

import java.util.Date;

import org.jeecg.common.aspect.annotation.Dict;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 *
 * @Author: chenli
 * @Date: 2020-06-07
 * @Version: V1.0
 */
@Data
public class SysUserOnlineVO {
    /**
     * sessionid
     */
    private String id;

    /**
     * session编号
     */
    private String token;

    /**
     * username
     */
    private String username;

    /**
     * username
     */
    private String realname;

    /**
     * avatar
     */
    private String avatar;

    /**
     * Birthday
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date birthday;

    /**
     * gender（1：male 2：female）
     */
    @Dict(dicCode = "sex")
    private Integer sex;

    /**
     * Phone number
     */
    private String phone;
}
