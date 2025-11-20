package org.jeecg.modules.airag.app.service;

import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.airag.app.vo.AppDebugParams;
import org.jeecg.modules.airag.app.vo.ChatConversation;
import org.jeecg.modules.airag.app.vo.ChatSendParams;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * aichat
 *
 * @author chenrui
 * @date 2025/2/25 13:36
 */
public interface IAiragChatService {

    /**
     * Send message
     *
     * @param chatSendParams
     * @return
     * @author chenrui
     * @date 2025/2/25 13:39
     */
    SseEmitter send(ChatSendParams chatSendParams);


    /**
     * Debugging application
     *
     * @param appDebugParams
     * @return
     * @author chenrui
     * @date 2025/2/28 10:49
     */
    SseEmitter debugApp(AppDebugParams appDebugParams);

    /**
     * stopped responding
     *
     * @param requestId
     * @return
     * @author chenrui
     * @date 2025/2/25 17:17
     */
    Result<?> stop(String requestId);

    /**
     * Get all conversations
     *
     * @param appId
     * @return
     * @author chenrui
     * @date 2025/2/26 14:48
     */
    Result<?> getConversations(String appId);

    /**
     * 获取对话chat记录
     *
     * @param conversationId
     * @return
     * @author chenrui
     * @date 2025/2/26 15:16
     */
    Result<?> getMessages(String conversationId);

    /**
     * Delete session
     *
     * @param conversationId
     * @return
     * @author chenrui
     * @date 2025/3/3 16:55
     */
    Result<?> deleteConversation(String conversationId);

    /**
     * Update session title
     * @param updateTitleParams
     * @return
     * @author chenrui
     * @date 2025/3/3 17:02
     */
    Result<?> updateConversationTitle(ChatConversation updateTitleParams);

    /**
     * Clear messages
     * @param conversationId
     * @return
     * @author chenrui
     * @date 2025/3/3 19:49
     */
    Result<?> clearMessage(String conversationId);

    /**
     * 初始化chat(Ignore tenant)
     * [QQYUN-12113]分享之后的chat，application、Model、Knowledge base is not queried based on tenant
     * @param appId
     * @return
     * @author chenrui
     * @date 2025/4/21 14:17
     */
    Result<?> initChat(String appId);

    /**
     * Continue to receive messages
     * @param requestId
     * @return
     * @author chenrui
     * @date 2025/8/11 17:39
     */
    SseEmitter receiveByRequestId(String requestId);
}
