package org.jeecg.modules.openapi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;

import java.io.Serializable;
import java.util.Date;

/**
 * permission table
 * @date 2024/12/10 9:38
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class OpenApiAuth implements Serializable {

    private static final long serialVersionUID = -5933153354153738498L;

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * Authorized name
     */
    private String name;

    /**
     * access key
     */
    private String ak;

    /**
     * secret key
     */
    private String sk;

    /**
     * system userID
     */
    @Dict(dictTable = "sys_user",dicCode = "id",dicText = "username")
    private String systemUserId;

    /**
     * Creator
     */
    private String createBy;

    /**
     * creation time
     */
    private Date createTime;

    /**
     * Updater
     */
    private String updateBy;

    /**
     * Update time
     */
    private Date updateTime;
}
