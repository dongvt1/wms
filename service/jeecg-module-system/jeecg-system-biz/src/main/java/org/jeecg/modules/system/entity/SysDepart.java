package org.jeecg.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * <p>
 * Department table
 * <p>
 * 
 * @Author Steve
 * @Since  2019-01-22
 */
@Data
@TableName("sys_depart")
public class SysDepart implements Serializable {
    private static final long serialVersionUID = 1L;
    
	/**ID*/
	@TableId(type = IdType.ASSIGN_ID)
	private String id;
	/**parent organizationID*/
	private String parentId;
	/**mechanism/Department name*/
	@Excel(name="mechanism/Department name",width=15)
	private String departName;
	/**mechanism/Department path name（non-persistent fields）*/
	@TableField(exist = false)
	private String departPathName;
	/**English name*/
	@Excel(name="English name",width=15)
	private String departNameEn;
	/**abbreviation*/
	private String departNameAbbr;
	/**sort*/
	@Excel(name="sort",width=15)
	private Integer departOrder;
	/**describe*/
	@Excel(name="describe",width=15)
	private String description;
	/**mechanism类别 1=company，2=组织mechanism，3=post 4=子company*/
	@Excel(name="mechanism类别",width=15,dicCode="org_category")
	private String orgCategory;
	/**mechanism类型*/
	private String orgType;
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
	/**state（1enable，0不enable）*/
	@Dict(dicCode = "depart_status")
	private String status;
	/**删除state（0，normal，1Deleted）*/
	@Dict(dicCode = "del_flag")
	private String delFlag;
	/**Connecting with enterprise WeChatID*/
	private String qywxIdentifier;
	/**Departments that connect with DingTalkID*/
	private String dingIdentifier;
	/**Creator*/
	private String createBy;
	/**Creation date*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date createTime;
	/**Updater*/
	private String updateBy;
	/**Update date*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date updateTime;
	/**tenantID*/
	private java.lang.Integer tenantId;

	/**Whether there are leaf nodes: 1yes0no*/
	private Integer izLeaf;

    //update-begin---author:wangshuai ---date:20200308  for：[JTC-119]Set department heads under the department management menu，Add field manageridsand the old person in chargeids
    /**of department headids*/
	@TableField(exist = false)
	private String directorUserIds;
    /**旧的of department headids(Used to compare deletions and additions)*/
	@TableField(exist = false)
    private String oldDirectorUserIds;
    //update-end---author:wangshuai ---date:20200308  for：[JTC-119]Add field manageridsand the old person in chargeids

    /**
     * Rankid
     */
    @Excel(name="Rank",width=15,dictTable = "sys_position", dicCode = "id", dicText = "name")
    @Dict(dictTable = "sys_position", dicCode = "id", dicText = "name")
    private String positionId;

    /**
     * 部门postid
     */
    @Excel(name="上级post",width=15,dictTable = "sys_depart", dicCode = "id", dicText = "depart_name")
    @Dict(dictTable = "sys_depart", dicCode = "id", dicText = "depart_name")
    private String depPostParentId;
    
	/**
	 * rewriteequalsmethod
	 */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
			return true;
		}
        if (o == null || getClass() != o.getClass()) {
			return false;
		}
        if (!super.equals(o)) {
			return false;
		}
        SysDepart depart = (SysDepart) o;
        return Objects.equals(id, depart.id) &&
                Objects.equals(parentId, depart.parentId) &&
                Objects.equals(departName, depart.departName) &&
                Objects.equals(departNameEn, depart.departNameEn) &&
                Objects.equals(departNameAbbr, depart.departNameAbbr) &&
                Objects.equals(departOrder, depart.departOrder) &&
                Objects.equals(description, depart.description) &&
                Objects.equals(orgCategory, depart.orgCategory) &&
                Objects.equals(orgType, depart.orgType) &&
                Objects.equals(orgCode, depart.orgCode) &&
                Objects.equals(mobile, depart.mobile) &&
                Objects.equals(fax, depart.fax) &&
                Objects.equals(address, depart.address) &&
                Objects.equals(memo, depart.memo) &&
                Objects.equals(status, depart.status) &&
                Objects.equals(delFlag, depart.delFlag) &&
                Objects.equals(createBy, depart.createBy) &&
                Objects.equals(createTime, depart.createTime) &&
                Objects.equals(updateBy, depart.updateBy) &&
                Objects.equals(tenantId, depart.tenantId) &&
                Objects.equals(updateTime, depart.updateTime);
    }

    /**
     * rewritehashCodemethod
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, parentId, departName, 
        		departNameEn, departNameAbbr, departOrder, description,orgCategory, 
        		orgType, orgCode, mobile, fax, address, memo, status, 
        		delFlag, createBy, createTime, updateBy, updateTime, tenantId);
    }
}
