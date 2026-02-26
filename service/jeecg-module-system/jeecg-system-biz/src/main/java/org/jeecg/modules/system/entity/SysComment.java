package org.jeecg.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: System comment reply form
 * @Author: jeecg-boot
 * @Date:   2022-07-19
 * @Version: V1.0
 */
@Data
@TableName("sys_comment")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="System comment reply form")
public class SysComment implements Serializable {
    private static final long serialVersionUID = 1L;

	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private String id;
	/**table name*/
	@Excel(name = "table name", width = 15)
    @Schema(description = "table name")
    private String tableName;
	/**dataid*/
	@Excel(name = "dataid", width = 15)
    @Schema(description = "dataid")
    private String tableDataId;
	/**Source userid*/
	@Excel(name = "Source userid", width = 15)
    @Schema(description = "Source userid")
    @Dict(dictTable = "sys_user", dicCode = "id", dicText = "realname")
    private String fromUserId;
	/**Send to userid(allowed to be empty)*/
	@Excel(name = "Send to userid(allowed to be empty)", width = 15)
    @Schema(description = "Send to userid(allowed to be empty)")
    @Dict(dictTable = "sys_user", dicCode = "id", dicText = "realname")
    private String toUserId;
	/**Commentid(allowed to be empty，When not empty，is the reply)*/
	@Excel(name = "Commentid(allowed to be empty，When not empty，is the reply)", width = 15)
    @Schema(description = "Commentid(allowed to be empty，When not empty，is the reply)")
    @Dict(dictTable = "sys_comment", dicCode = "id", dicText = "comment_content")
    private String commentId;
	/**Reply content*/
	@Excel(name = "Reply content", width = 15)
    @Schema(description = "Reply content")
    private String commentContent;
	/**Creator*/
    @Schema(description = "Creator")
    private String createBy;
	/**Creation date*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Creation date")
    private Date createTime;
	/**Updater*/
    @Schema(description = "Updater")
    private String updateBy;
	/**Update date*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Update date")
    private Date updateTime;

    /**
     * 不是data库字段，用于Comment跳转
     */
    @TableField(exist = false)
    private String tableId;

}
