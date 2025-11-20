package org.jeecg.modules.openapi.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * Request header table
 * @date 2024/12/10 14:37
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class OpenApiHeader implements Serializable {
    private static final long serialVersionUID = 5032708503120184683L;


    /**
     * key
     */
    private String headerKey;

    /**
     * Is it required?(0:no，1：yes)
     */
    private Integer required;

    /**
     * default value
     */
    private String defaultValue;

    /**
     * illustrate
     */
    private String note;
}
