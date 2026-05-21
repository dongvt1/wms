package org.jeecg.modules.system.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * <p>
 *
 * </p>
 *
 * @Author zhangweijian
 * @since 2018-12-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SysDictItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * dictionaryid
     */
    private String dictId;

    /**
     * dictionary项文本
     */
    @Excel(name = "dictionary项文本", width = 20)
    private String itemText;

    /**
     * dictionary项值
     */
    @Excel(name = "dictionary项值", width = 30)
    private String itemValue;

    /**
     * describe
     */
    @Excel(name = "describe", width = 40)
    private String description;

    /**
     * sort
     */
    @Excel(name = "sort", width = 15,type=4)
    private Integer sortOrder;


    /**
     * state（1enable 0不enable）
     */
    @Dict(dicCode = "dict_item_status")
    private Integer status;

    private String createBy;

    private Date createTime;

    private String updateBy;

    private Date updateTime;

    /**
     * dictionary项颜色 
     */
    private String itemColor;

}
