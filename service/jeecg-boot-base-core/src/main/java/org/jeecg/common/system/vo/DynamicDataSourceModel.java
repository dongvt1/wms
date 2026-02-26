package org.jeecg.common.system.vo;

import lombok.Data;
import org.springframework.beans.BeanUtils;

/**
 * @Description: data source
 * @author: jeecg-boot
 */
@Data
public class DynamicDataSourceModel {

    public DynamicDataSourceModel() {

    }

    public DynamicDataSourceModel(Object dbSource) {
        if (dbSource != null) {
            BeanUtils.copyProperties(dbSource, this);
        }
    }

    /**
     * id
     */
    private java.lang.String id;
    /**
     * data source编码
     */
    private java.lang.String code;
    /**
     * Database type
     */
    private java.lang.String dbType;
    /**
     * Driver class
     */
    private java.lang.String dbDriver;
    /**
     * data source地址
     */
    private java.lang.String dbUrl;

//    /**
//     * Database name
//     */
//    private java.lang.String dbName;

    /**
     * username
     */
    private java.lang.String dbUsername;
    /**
     * password
     */
    private java.lang.String dbPassword;

}