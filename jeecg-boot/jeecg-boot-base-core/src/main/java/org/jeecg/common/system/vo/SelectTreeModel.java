package org.jeecg.common.system.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Drop down tree model
 *
 * @author jeecg-boot
 */
@Data
public class SelectTreeModel implements Serializable {

    private String key;
    private String title;
    private String value;
    /**
     * fatherId
     */
    private String parentId;
    /**
     * Is it a leaf node?
     */
    private boolean isLeaf;
    /**
     * child node
     */
    private List<SelectTreeModel> children;

}
