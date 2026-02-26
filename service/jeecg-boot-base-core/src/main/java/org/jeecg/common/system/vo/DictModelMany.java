package org.jeecg.common.system.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Used when querying multiple dictionaries
 * @author: jeecg-boot
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DictModelMany extends DictModel {

    /**
     * dictionarycode，based on multiple fieldscodeUsed only when querying，用于区分不同的dictionary选项
     */
    private String dictCode;

}
