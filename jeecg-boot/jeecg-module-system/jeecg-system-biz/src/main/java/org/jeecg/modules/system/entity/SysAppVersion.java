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

import java.io.Serializable;

/**
 * @Description: appSystem configuration
 * @Author: jeecg-boot
 * @Date:   2021-07-07
 * @Version: V1.0
 * 
 * e3e3NcxzbUiGa53YYVXxWc8ADo5ISgQGx/gaZwERF91oAryDlivjqBv3wqRArgChupi+Y/Gg/swwGEyL0PuVFg==
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="appSystem configuration")
public class SysAppVersion implements Serializable {
    private static final long serialVersionUID = 1L;

	/**primary key*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "primary key")
    private String id;
	/**Creator*/
    @Schema(description = "Creator")
    private String createBy;
	/**Creation date*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Creation date")
    private java.util.Date createTime;
	/**Updater*/
    @Schema(description = "Updater")
    private String updateBy;
	/**Update date*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Update date")
    private java.util.Date updateTime;
	/**Department*/
    @Schema(description = "Department")
    private String sysOrgCode;
	/**title*/
	@Excel(name = "title", width = 15)
    @Schema(description = "title")
    private String appTitle;
	/**logo*/
	@Excel(name = "logo", width = 15)
    @Schema(description = "logo")
    private String appLogo;
	/**Home page carousel*/
	@Excel(name = "Home page carousel", width = 15)
    @Schema(description = "Home page carousel")
    private String carouselImgJson;
	/**Home menu map*/
	@Excel(name = "Home menu map", width = 15)
    @Schema(description = "Home menu map")
    private String routeImgJson;
    /**appVersion*/
    @Schema(description = "Version")
    private String appVersion;
    /**Version编码*/
    @Schema(description = "Version编码")
    private Integer versionNum;
    /**appDownload path*/
    @Schema(description = "appDownload path")
    private String downloadUrl;
    /**Hot update path*/
    @Schema(description = "Hot update path")
    private String wgtUrl;
    /**Update content*/
    @Schema(description = "Update content")
    private String updateNote;
}
