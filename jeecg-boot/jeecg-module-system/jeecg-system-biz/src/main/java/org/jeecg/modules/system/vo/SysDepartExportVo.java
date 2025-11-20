package org.jeecg.modules.system.vo;

import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;

@Data
public class SysDepartExportVo {
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
    /**English name*/
    @Excel(name="English name",width=15)
    private String departNameEn;
    /**sort*/
    @Excel(name="sort",width=15)
    private Integer departOrder;
    /**describe*/
    @Excel(name="describe",width=15)
    private String description;
    /**mechanism类别 1=company，2=组织mechanism，3=post*/
    @Excel(name="mechanism类别",width=15,dicCode="org_category")
    private String orgCategory;
    /** Rankid */
    @Excel(name="Rank",width=15,dictTable = "sys_position", dicCode = "id", dicText = "name")
    private String positionId;
    /**mechanism编码*/
    @Excel(name="mechanism编码",width=15)
    private String orgCode;
    /**Phone number*/
    @Excel(name="Phone number",width=15)
    private String mobile;
    /**fax*/
    @Excel(name="fax",width=15)
    private String fax;
    /**address*/
    @Excel(name="address",width=15)
    private String address;
    /**Remark*/
    @Excel(name="Remark",width=15)
    private String memo;
}
