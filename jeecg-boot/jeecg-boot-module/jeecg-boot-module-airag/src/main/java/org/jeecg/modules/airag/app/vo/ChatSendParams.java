package org.jeecg.modules.airag.app.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description: Input parameters for sending messages
 * @Author: chenrui
 * @Date: 2025/2/25 11:47
 */
@NoArgsConstructor
@Data
public class ChatSendParams {

    public ChatSendParams(String content, String conversationId, String topicId, String appId) {
        this.content = content;
        this.conversationId = conversationId;
        this.topicId = topicId;
        this.appId = appId;
    }

    /**
     * Chat content entered by the user
     */
    private String content;

    /**
     * dialogue sessionID
     */
    private String conversationId;

    /**
     * conversation topicID（Used to associate history）
     */
    private String topicId;

    /**
     * applicationid
     */
    private String appId;

    /**
     * Picture list
     */
    private List<String> images;

}
