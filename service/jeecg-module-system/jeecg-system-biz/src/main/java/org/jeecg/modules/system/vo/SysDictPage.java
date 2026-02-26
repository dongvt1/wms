package org.jeecg.modules.system.vo;

import lombok.Data;
import org.jeecg.modules.system.entity.SysDictItem;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecgframework.poi.excel.annotation.ExcelCollection;

import java.util.List;

/**
 * @Description: System dictionary pagination
 * @author: jeecg-boot
 */
@Data
public class SysDictPage {

    /**
     * primary key
     */
    private String id;
    /**
     * Dictionary name
     */
    @Excel(name = "Dictionary name", width = 20)
    private String dictName;

    /**
     * dictionary encoding
     */
    @Excel(name = "dictionary encoding", width = 30)
    private String dictCode;
    /**
     * delete status
     */
    private Integer delFlag;
    /**
     * describe
     */
    @Excel(name = "describe", width = 30)
    private String description;

    @ExcelCollection(name = "dictionary list")
    private List<SysDictItem> sysDictItemList;

}
