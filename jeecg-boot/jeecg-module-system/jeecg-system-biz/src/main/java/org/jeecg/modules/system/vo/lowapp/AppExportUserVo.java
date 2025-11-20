package org.jeecg.modules.system.vo.lowapp;

import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.util.Date;

/**
* @Description: Application users and departments User export/Import entity class
*
* @author: wangshuai
* @date: 2023/6/14 16:42
*/
@Data
public class AppExportUserVo {

    /**User ID*/
    @Excel(name="User ID",width=30)
    private String id;

    /**Name*/
    @Excel(name="Name",width=30)
    private String realname;
    
    /**Position*/
    @Excel(name = "Position",width = 30)
    private String position;

    /**department*/
    @Excel(name = "department",width = 30)
    private String depart;

    /**Job number*/
    @Excel(name = "Job number",width = 30)
    private String workNo;
    
    /**Phone number*/
    @Excel(name = "Phone number",width = 30)
    private String phone;
    
    /**Mail*/
    @Excel(name = "Mail",width = 30)
    private String email;

    /**Join time*/
    @Excel(name = "Join time",width = 30, format = "yyyy-MM-dd")
    private Date createTime;
}
