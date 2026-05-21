package org.jeecg.modules.system.vo;

import lombok.Data;

/**
* @Description: Department positions
*
* @author: wangshuai
* @date: 2025/8/18 10:11
*/
@Data
public class SysDepartPositionVo {

    /**
     * departmentid
     */
    private String id;

    /**
     * Whether it is a leaf node(Data return)
     */
    private Integer izLeaf;
    
    /**
     * department名称
     */
    private String departName;

    /**
     * Job title
     */
    private String positionName;

    /**
     * parentid
     */
    private String parentId;

    /**
     * department编码
     */
    private String orgCode;

    /**
     * Institution type
     */
    private String orgCategory;

    /**
     * Superior positionid
     */
    private String depPostParentId;
}
