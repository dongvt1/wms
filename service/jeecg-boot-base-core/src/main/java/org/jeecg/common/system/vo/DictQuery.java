package org.jeecg.common.system.vo;

import lombok.Data;

/**
 * Dictionary query parameter entity
 * @author: jeecg-boot
 */
@Data
public class DictQuery {
    /**
     * table name
     */
    private String table;
    /**
     * storage column
     */
    private String code;

    /**
     * show columns
     */
    private String text;

    /**
     * Keyword query
     */
    private String keyword;

    /**
     * storage column的值 Used to echo queries
     */
    private String codeValue;

}
