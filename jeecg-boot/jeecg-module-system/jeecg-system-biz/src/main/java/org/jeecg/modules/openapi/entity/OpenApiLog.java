package org.jeecg.modules.openapi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * Call record table
 * @date 2024/12/10 9:41
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class OpenApiLog implements Serializable {
    private static final long serialVersionUID = -5870384488947863579L;

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * interfaceID
     */
    private String apiId;

    /**
     * callID
     */
    private String callAuthId;

    /**
     * call时间
     */
    private Date callTime;

    /**
     * time consuming
     */
    private Long usedTime;

    /**
     * response time
     */
    private Date responseTime;
}
