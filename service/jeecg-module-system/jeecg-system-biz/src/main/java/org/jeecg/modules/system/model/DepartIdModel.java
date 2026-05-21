package org.jeecg.modules.system.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jeecg.modules.system.entity.SysDepart;

/**
 * <p>
 * Department table Entity class that encapsulates the name of the department in the tree structure
 * <p>
 * 
 * @Author Steve
 * @Since 2019-01-22 
 *
 */
public class DepartIdModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * primary keyID
     */
    private String key;

    /**
     * primary keyID
     */
    private String value;
    /**
     * Department code
     */
    private String code;

    /**
     * Department name
     */
    private String title;
    
    List<DepartIdModel> children = new ArrayList<>();
    
    /**
     * WillSysDepartTreeModelPart of the data is placed in the object
     * @param treeModel
     * @return
     */
    public DepartIdModel convert(SysDepartTreeModel treeModel) {
        this.key = treeModel.getId();
        this.value = treeModel.getId();
        this.title = treeModel.getDepartName();
        return this;
    }
    
    /**
     * This method is used by the implementation class of the user department
     * @param sysDepart
     * @return
     */
    public DepartIdModel convertByUserDepart(SysDepart sysDepart) {
        this.key = sysDepart.getId();
        this.value = sysDepart.getId();
        this.code = sysDepart.getOrgCode();
        this.title = sysDepart.getDepartName();
        return this;
    } 

    public List<DepartIdModel> getChildren() {
        return children;
    }

    public void setChildren(List<DepartIdModel> children) {
        this.children = children;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
