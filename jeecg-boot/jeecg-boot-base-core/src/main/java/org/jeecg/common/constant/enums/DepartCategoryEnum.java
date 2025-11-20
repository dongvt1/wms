package org.jeecg.common.constant.enums;

import org.jeecg.common.util.oConvertUtils;

/**
* @Description: Department type enumeration class
*
* @author: wangshuai
* @date: 2025/8/19 21:37
*/
public enum DepartCategoryEnum {
    
    DEPART_CATEGORY_COMPANY("Department type：company","company","1"),
    DEPART_CATEGORY_DEPART("Department type：department","department","2"),
    DEPART_CATEGORY_POST("Department type：post","post","3"),
    DEPART_CATEGORY_SUB_COMPANY("Department type：子company","子company","4");

    DepartCategoryEnum(String described, String name, String value) {
        this.value = value;
        this.name = name;
        this.described = described;
    }

    /**
     * describe
     */
    private String described;
    /**
     * value
     */
    private String value;

    /**
     * name
     */
    private String name;

    public String getDescribed() {
        return described;
    }

    public void setDescribed(String described) {
        this.described = described;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * 根据value获取name
     * 
     * @param value
     * @return
     */
    public static String getNameByValue(String value){
        if (oConvertUtils.isEmpty(value)) {
            return null;
        }
        for (DepartCategoryEnum val : values()) {
            if (val.getValue().equals(value)) {
                return val.getName();
            }
        }
        return value;
    }
    
    /**
     * 根据name获取value
     * 
     * @param name
     * @return
     */
    public static String getValueByName(String name){
        if (oConvertUtils.isEmpty(name)) {
            return null;
        }
        for (DepartCategoryEnum val : values()) {
            if (val.getName().equals(name)) {
                return val.getValue();
            }
        }
        return name;
    }
}
