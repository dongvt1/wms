package org.jeecg.modules.system.vo;

import lombok.Data;
import org.jeecg.common.constant.enums.DepartCategoryEnum;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysDepart;

import java.util.ArrayList;
import java.util.List;

/**
* @Description: Position drop-down selection tree
*
* @author: wangshuai
* @date: 2025/8/18 9:40
*/
@Data
public class SysPositionSelectTreeVo {
    /** correspondSysDepartinidField,前端数据树invalue*/
    private String value;

    /** corresponddepart_nameField,前端数据树intitle*/
    private String title;
    private boolean isLeaf;
    /** Whether to show checkbox */
    private boolean checkable;
    /** Whether to disable */
    private boolean disabled;
    // 以下所有Field均与SysDepartsame
    private String id;
    /**parentid*/
    private String parentId;
    /**Department Category*/
    private String orgCategory;
    /**Department code*/
    private String orgCode;
    
    private List<SysPositionSelectTreeVo> children = new ArrayList<>();

    /**
     * WillSysDepartobject converted toSysDepartTreeModelobject
     * @param sysDepart
     */
    public SysPositionSelectTreeVo(SysDepart sysDepart) {
        this.value = sysDepart.getId();
        this.title = sysDepart.getDepartName();
        this.id = sysDepart.getId();
        this.parentId = sysDepart.getParentId();
        this.orgCategory = sysDepart.getOrgCategory();
        this.orgCode = sysDepart.getOrgCode();
        if(0 == sysDepart.getIzLeaf()){
            this.isLeaf = false;
        }else{
            this.isLeaf = true;
        }
        if(DepartCategoryEnum.DEPART_CATEGORY_POST.getValue().equals(sysDepart.getOrgCategory())){
            this.checkable = true;
            this.disabled = false;
        }else{
            this.checkable = false;
            this.disabled = true;
        }
    }

    public SysPositionSelectTreeVo(SysDepartPositionVo position) {
        this.value = position.getId();
        if(oConvertUtils.isNotEmpty(position.getDepartName())){
            this.title = position.getPositionName() + "("+position.getDepartName()+")";
        }else{
            this.title = position.getPositionName();
        }
        this.id = position.getId();
        this.parentId = position.getDepPostParentId();
        this.orgCategory = "3";
        if(0 == position.getIzLeaf()){
            this.isLeaf = false;
        }else{
            this.isLeaf = true;
        }
        this.checkable = true;
        this.disabled = false;
    }
}
