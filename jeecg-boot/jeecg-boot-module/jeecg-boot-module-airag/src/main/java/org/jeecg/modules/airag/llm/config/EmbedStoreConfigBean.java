package org.jeecg.modules.airag.llm.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Vector repository configuration
 *
 * @Author: chenrui
 * @Date: 2025/2/18 14:24
 */
@NoArgsConstructor
@Data
@Component
@ConfigurationProperties(prefix = EmbedStoreConfigBean.PREFIX)
public class EmbedStoreConfigBean {
    public static final String PREFIX = "jeecg.airag.embed-store";

    /**
     * host
     */
    private String host = "127.0.0.1";
    /**
     * port
     */
    private int port = 5432;
    /**
     * database
     */
    private String database = "postgres";
    /**
     * username
     */
    private String user = "postgres";
    /**
     * password
     */
    private String password = "postgres";

    /**
     * table to store vectors
     */
    private String table = "embeddings";

}
