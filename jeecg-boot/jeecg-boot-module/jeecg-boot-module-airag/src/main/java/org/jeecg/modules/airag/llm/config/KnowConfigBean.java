package org.jeecg.modules.airag.llm.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Knowledge base configuration
 *
 * @Author: chenrui
 * @Date: 2025-04-01 14:19
 */
@NoArgsConstructor
@Data
@Component
@ConfigurationProperties(prefix = KnowConfigBean.PREFIX)
public class KnowConfigBean {
    public static final String PREFIX = "jeecg.airag.know";

    /**
     * turn onMinerUparse
     */
    private boolean enableMinerU = false;

    /**
     * condaenvironment(Not used by defaultconda)
     */
    private String condaEnv = null;

}
