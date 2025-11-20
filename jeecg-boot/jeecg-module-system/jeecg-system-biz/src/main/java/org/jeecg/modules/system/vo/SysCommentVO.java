package org.jeecg.modules.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Description: VO Comment information+File information
 * @Author: jeecg-boot
 * @Date: 2022-07-19
 * @Version: V1.0
 */
@Data
public class SysCommentVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private String id;
    /**
     * table name
     */
    private String tableName;
    /**
     * dataid
     */
    private String tableDataId;
    /**
     * Source userid
     */
    private String fromUserId;
    /**
     * Reply content
     */
    private String commentContent;
    /**
     * Creation date
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Creation date")
    private Date createTime;

    /**
     * File information
     */
    private List<SysCommentFileVo> fileList;

    /**
     * Send to userid(allowed to be empty)
     */
    @Dict(dictTable = "sys_user", dicCode = "id", dicText = "realname")
    private String toUserId;
    
    /**
     * Commentid(allowed to be empty，When not empty，is the reply)
     */
    private String commentId;

    /**
     * of the message senderrealname
     */
    private String fromUserId_dictText;

    /**
     * of the person being replied torealname
     */
    private String toUserId_dictText;

    /**
     * of the message sender头像
     */
    private String fromUserAvatar;

    /**
     * of the person being replied to头像
     */
    private String toUserAvatar;
    
    public SysCommentVO() {

    }

}
