package org.jeecg.modules.system.vo.lowapp;

import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;

@Data
public class ExportDepartVo {
    /**Department path*/
    @Excel(name="Department path",width=50)
    private String departNameUrl;
    /**mechanism/Department name*/
    @Excel(name="Department name",width=50)
    private String departName;
    /**id*/
    private String id;
    /**parentid*/
    private String parentId;
}
