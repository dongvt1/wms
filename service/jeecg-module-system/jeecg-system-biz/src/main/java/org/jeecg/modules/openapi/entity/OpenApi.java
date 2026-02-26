package org.jeecg.modules.openapi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * Interface table
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class OpenApi  implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * Interface name
     */
    private String name;

    /**
     * Request method，likePOST、GET
     */
    private String requestMethod;

    /**
     * Relative interface path open to the outside world
     */
    private String requestUrl;

    /**
     * IP blacklist
     */
    private String blackList;
    /**
     * Request headerjson
     */
    private String headersJson;
    /**
     * Request parametersjson
     */
    private String paramsJson;


    /**
     * Currently only supportsjson
     */
    private String body;

    /**
     * Original interface path
     */
    private String originUrl;

    /**
     * state(1：normal  2：abandoned ）
     */
    private Integer status;

    /**
     * 删除state（0，normal，1Deleted）
     */
    @TableLogic
    private Integer delFlag;

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
    /**
     * Historical selected interfaces
     */
    @TableField(exist = false)
    private String ifCheckBox = "0";
}
