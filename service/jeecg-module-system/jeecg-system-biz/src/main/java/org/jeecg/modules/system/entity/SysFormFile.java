package org.jeecg.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;
import java.util.Date;

/**
 * @Description: form comment file
 * @Author: jeecg-boot
 * @Date:   2022-07-21
 * @Version: V1.0
 */
@Data
@TableName("sys_form_file")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="form comment file")
public class SysFormFile {
    
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
	/**associated filesid*/
	@Excel(name = "associated filesid", width = 15)
    @Schema(description = "associated filesid")
	private String fileId;
	/**Document type（folder:folder excel:excel doc:word pp:ppt image:picture  archive:Other documents video:video）*/
	@Excel(name = "Document type（folder:folder excel:excel doc:word pp:ppt image:picture  archive:Other documents video:video）", width = 15)
	@Schema(description = "Document type（folder:folder excel:excel doc:word pp:ppt image:picture  archive:Other documents video:video）")
	private String fileType;
	/**Creator login name*/
	@Excel(name = "Creator login name", width = 15)
    @Schema(description = "Creator login name")
	private String createBy;
	/**Creation date*/
	@Excel(name = "Creation date", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Creation date")
	private Date createTime;
}
