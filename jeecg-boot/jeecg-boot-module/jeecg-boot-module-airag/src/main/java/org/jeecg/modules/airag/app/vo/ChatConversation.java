package org.jeecg.modules.airag.app.vo;

import lombok.Data;
import org.jeecg.modules.airag.app.entity.AiragApp;
import org.jeecg.modules.airag.common.vo.MessageHistory;

import java.util.Date;
import java.util.List;

/**
 * @Description: chat session
 * @Author: chenrui
 * @Date: 2025/2/25 14:56
 */
@Data
public class ChatConversation {

    /**
     * sessionid
     */
    private String id;

    /**
     * session标题
     */
    private String title;

    /**
     * Message record
     */
    private List<MessageHistory> messages;

    /**
     * app
     */
    private AiragApp app;

    /**
     * creation time
     */
    private Date createTime;
}
