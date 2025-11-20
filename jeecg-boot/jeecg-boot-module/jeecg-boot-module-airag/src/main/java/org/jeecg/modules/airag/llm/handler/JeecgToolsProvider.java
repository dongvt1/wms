package org.jeecg.modules.airag.llm.handler;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.service.tool.ToolExecutor;
import lombok.Getter;

import java.util.Map;

/**
 * for [QQYUN-13565]【AIassistant】Added tool extensions for creating users and querying users
 * @Description: jeecg llmtool provider
 * @Author: chenrui
 * @Date: 2025/8/26 18:06
 */
public interface JeecgToolsProvider {

    /**
     * Get the default tool list
     * @return
     * @author chenrui
     * @date 2025/8/27 09:49
     */
    public Map<ToolSpecification, ToolExecutor> getDefaultTools();

    /**
     * jeecgLlmTools
     * @author chenrui
     * @date 2025/8/27 09:49
     */
    @Getter
    class JeecgLlmTools{
        ToolSpecification toolSpecification;
        ToolExecutor toolExecutor;

        public JeecgLlmTools(ToolSpecification toolSpecification, ToolExecutor toolExecutor) {
            this.toolSpecification = toolSpecification;
            this.toolExecutor = toolExecutor;
        }


    }
}
