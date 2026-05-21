package org.jeecg.modules.system.entity;

import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @Description: User position relationship table
 * @Author: jeecg-boot
 * @Date:   2023-02-14
 * @Version: V1.0
 */
@Schema(description="User position relationship table")
@Data
@TableName("sys_user_position")
public class SysUserPosition implements Serializable {
    private static final long serialVersionUID = 1L;

	/**primary key*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "primary key")
    private String id;
	/**userid*/
	@Excel(name = "userid", width = 15)
    @Schema(description = "userid")
    private String userId;
	/**Positionid*/
    @Schema(description = "Positionid")
    private String positionId;
	/**Creator*/
    @Schema(description = "Creator")
    private String createBy;
	/**creation time*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "creation time")
    private Date createTime;
	/**Modifier*/
    @Schema(description = "Modifier")
    private String updateBy;
	/**modification time*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "modification time")
    private Date updateTime;
}
