package org.jeecg.common.system.base.entity;

import java.io.Serializable;

import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @Description: Entitybase class
 * @Author: dangzhenghui@163.com
 * @Date: 2019-4-28
 * @Version: 1.1
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "ID")
    private java.lang.String id;

    /**
     * Creator
     */
    @Schema(description = "Creator")
    @Excel(name = "Creator", width = 15)
    private java.lang.String createBy;

    /**
     * creation time
     */
    @Schema(description = "creation time")
    @Excel(name = "creation time", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date createTime;

    /**
     * Updater
     */
    @Schema(description = "Updater")
    @Excel(name = "Updater", width = 15)
    private java.lang.String updateBy;

    /**
     * Update time
     */
    @Schema(description = "Update time")
    @Excel(name = "Update time", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date updateTime;

}
