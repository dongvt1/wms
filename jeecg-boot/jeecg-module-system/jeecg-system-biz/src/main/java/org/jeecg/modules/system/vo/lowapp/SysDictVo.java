package org.jeecg.modules.system.vo.lowapp;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import org.jeecg.modules.system.entity.SysDict;
import org.jeecg.modules.system.entity.SysDictItem;

import java.util.List;

@Data
public class SysDictVo {
    /**
     * dictionaryid
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * dictionary名称
     */
    private String dictName;

    /**
     * dictionary编码
     */
    private String dictCode;

    /**
     * applicationid
     */
    private String lowAppId;
    
    /**
     * tenantID
     */
    private Integer tenantId;

    /**
     * dictionary子项
     */
    private List<SysDictItem> dictItemsList;
}
