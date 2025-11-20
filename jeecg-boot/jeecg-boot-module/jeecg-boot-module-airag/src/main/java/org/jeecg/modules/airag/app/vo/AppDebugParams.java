package org.jeecg.modules.airag.app.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.jeecg.modules.airag.app.entity.AiragApp;

/**
 * @Description: Application debugging input parameters
 * @Author: chenrui
 * @Date: 2025/2/25 11:47
 */
@Data
public class AppDebugParams extends ChatSendParams {

    /**
     * Application information
     */
    AiragApp app;

}
