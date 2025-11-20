package org.jeecg.modules.airag.app.service.impl;

import com.alibaba.fastjson.JSONObject;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.data.message.*;
import dev.langchain4j.model.output.FinishReason;
import dev.langchain4j.service.TokenStream;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootBizTipException;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.util.*;
import org.jeecg.modules.airag.app.consts.AiAppConsts;
import org.jeecg.modules.airag.app.entity.AiragApp;
import org.jeecg.modules.airag.app.mapper.AiragAppMapper;
import org.jeecg.modules.airag.app.service.IAiragChatService;
import org.jeecg.modules.airag.app.vo.AppDebugParams;
import org.jeecg.modules.airag.app.vo.ChatConversation;
import org.jeecg.modules.airag.app.vo.ChatSendParams;
import org.jeecg.modules.airag.common.consts.AiragConsts;
import org.jeecg.modules.airag.common.handler.AIChatParams;
import org.jeecg.modules.airag.common.handler.IAIChatHandler;
import org.jeecg.modules.airag.common.utils.AiragLocalCache;
import org.jeecg.modules.airag.common.vo.MessageHistory;
import org.jeecg.modules.airag.common.vo.event.EventData;
import org.jeecg.modules.airag.common.vo.event.EventFlowData;
import org.jeecg.modules.airag.common.vo.event.EventMessageData;
import org.jeecg.modules.airag.flow.consts.FlowConsts;
import org.jeecg.modules.airag.flow.service.IAiragFlowService;
import org.jeecg.modules.airag.flow.vo.api.FlowRunParams;
import org.jeecg.modules.airag.llm.entity.AiragModel;
import org.jeecg.modules.airag.llm.handler.AIChatHandler;
import org.jeecg.modules.airag.llm.handler.JeecgToolsProvider;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;


/**
 * AIAssistant chatService
 *
 * @author chenrui
 * @date 2024/1/26 20:07
 */
@Service
@Slf4j
public class AiragChatServiceImpl implements IAiragChatService {

    @Autowired
    IAIChatHandler aiChatHandler;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    AiragAppMapper airagAppMapper;

    @Autowired
    IAiragFlowService airagFlowService;

    @Autowired
    private ISysBaseAPI sysBaseApi;
    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    JeecgToolsProvider jeecgToolsProvider;

    /**
     * Receive messages again
     */
    private static final ExecutorService SSE_THREAD_POOL = Executors.newFixedThreadPool(10); // maximum10threads

    @Override
    public SseEmitter send(ChatSendParams chatSendParams) {
        AssertUtils.assertNotEmpty("Parameter exception", chatSendParams);
        String userMessage = chatSendParams.getContent();
        AssertUtils.assertNotEmpty("Send at least one message", userMessage);

        // Get session information
        String conversationId = chatSendParams.getConversationId();
        String topicId = oConvertUtils.getString(chatSendParams.getTopicId(), UUIDGenerator.generate());
        // Getappinformation
        AiragApp app = null;
        if (oConvertUtils.isNotEmpty(chatSendParams.getAppId())) {
            app = airagAppMapper.getByIdIgnoreTenant(chatSendParams.getAppId());
        }
        ChatConversation chatConversation = getOrCreateChatConversation(app, conversationId);
        // Update title
        if (oConvertUtils.isEmpty(chatConversation.getTitle())) {
            chatConversation.setTitle(userMessage.length() > 5 ? userMessage.substring(0, 5) : userMessage);
        }
        // Send message
        return doChat(chatConversation, topicId, chatSendParams);
    }

    @Override
    public SseEmitter debugApp(AppDebugParams appDebugParams) {
        AssertUtils.assertNotEmpty("Parameter exception", appDebugParams);
        String userMessage = appDebugParams.getContent();
        AssertUtils.assertNotEmpty("Send at least one message", userMessage);
        AssertUtils.assertNotEmpty("applicationinformationcannot be empty", appDebugParams.getApp());
        // Get session information
        String topicId = oConvertUtils.getString(appDebugParams.getTopicId(), UUIDGenerator.generate());
        AiragApp app = appDebugParams.getApp();
        app.setId("__DEBUG_APP");
        ChatConversation chatConversation = getOrCreateChatConversation(app, topicId);
        // Send message
        SseEmitter emitter = doChat(chatConversation, topicId, appDebugParams);
        //save session
        saveChatConversation(chatConversation, true, null);
        return emitter;
    }


    @Override
    public Result<?> stop(String requestId) {
        AssertUtils.assertNotEmpty("requestIdcannot be empty", requestId);
        // 从cache中Get对应的SseEmitter
        SseEmitter emitter = AiragLocalCache.get(AiragConsts.CACHE_TYPE_SSE, requestId);
        if (emitter != null) {
            closeSSE(emitter, new EventData(requestId, null, EventData.EVENT_MESSAGE_END));
            return Result.ok("Session terminated successfully");
        } else {
            return Result.error("No corresponding session found");
        }
    }

    /**
     * closuresse
     *
     * @param emitter
     * @param eventData
     * @throws IOException
     * @author chenrui
     * @date 2025/2/27 15:56
     */
    private static void closeSSE(SseEmitter emitter, EventData eventData) {
        AssertUtils.assertNotEmpty("askidcannot be empty", eventData);
        if (null == emitter) {
            log.warn("会话已closure");
            return;
        }
        try {
            // Send completion event
            emitter.send(SseEmitter.event().data(eventData));
        } catch (Exception e) {
            if(!e.getMessage().contains("ResponseBodyEmitter has already completed")){
                log.error("An error occurred while terminating the session", e);
            }
            try {
                // Prevent exception bubbling
                emitter.completeWithError(e);
            } catch (Exception ignore) {}
        } finally {
            // Remove from cacheemitter
            AiragLocalCache.remove(AiragConsts.CACHE_TYPE_SSE, eventData.getRequestId());
            // closureemitter
            try {
                emitter.complete();
            } catch (Exception ignore) {}
        }
    }

    @Override
    public Result<?> getConversations(String appId) {
        if (oConvertUtils.isEmpty(appId)) {
            appId = AiAppConsts.DEFAULT_APP_ID;
        }
        String key = getConversationDirCacheKey(null);
        key = key + ":*";
        List<String> keys = redisUtil.scan(key);
        // If the key set is empty，Return empty list
        if (keys.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }

        // Iterate over the key collection，Get对应的 ChatConversation object
        List<ChatConversation> conversations = new ArrayList<>();
        for (Object k : keys) {
            ChatConversation conversation = (ChatConversation) redisTemplate.boundValueOps(k).get();

            if (conversation != null) {
                AiragApp app = conversation.getApp();
                if (null == app) {
                    continue;
                }
                String conversationAppId = app.getId();
                if (appId.equals(conversationAppId)) {
                    conversation.setApp(null);
                    conversation.setMessages(null);
                    conversations.add(conversation);
                }
            }
        }

        // Sort the session list in descending order of creation time
        conversations.sort((o1, o2) -> {
            Date date1 = o1.getCreateTime();
            Date date2 = o2.getCreateTime();
            if (date1 == null && date2 == null) {
                return 0;
            }
            if (date1 == null) {
                return 1;
            }
            if (date2 == null) {
                return -1;
            }
            return date2.compareTo(date1);
        });

        // Return results
        return Result.ok(conversations);
    }

    @Override
    public Result<?> getMessages(String conversationId) {
        AssertUtils.assertNotEmpty("Please select a session first", conversationId);
        String key = getConversationCacheKey(conversationId, null);
        if (oConvertUtils.isEmpty(key)) {
            return Result.ok(Collections.emptyList());
        }
        ChatConversation chatConversation = (ChatConversation) redisTemplate.boundValueOps(key).get();
        if (oConvertUtils.isObjectEmpty(chatConversation)) {
            return Result.ok(Collections.emptyList());
        }
        return Result.ok(chatConversation.getMessages());
    }

    @Override
    public Result<?> clearMessage(String conversationId) {
        AssertUtils.assertNotEmpty("Please select a session first", conversationId);
        String key = getConversationCacheKey(conversationId, null);
        if (oConvertUtils.isEmpty(key)) {
            return Result.ok(Collections.emptyList());
        }
        ChatConversation chatConversation = (ChatConversation) redisTemplate.boundValueOps(key).get();
        if (null != chatConversation && oConvertUtils.isObjectNotEmpty(chatConversation.getMessages())) {
            chatConversation.getMessages().clear();
            saveChatConversation(chatConversation);
        }
        return Result.ok();
    }

    @Override
    public Result<?> initChat(String appId) {
        AiragApp app = airagAppMapper.getByIdIgnoreTenant(appId);
        return Result.ok(app);
    }

    @Override
    public SseEmitter receiveByRequestId(String requestId) {
        AssertUtils.assertNotEmpty("Please select a session",requestId);
        if(AiragLocalCache.get(AiragConsts.CACHE_TYPE_SSE, requestId) == null){
            return null;
        }
        List<EventData> datas = AiragLocalCache.get(AiragConsts.CACHE_TYPE_SSE_HISTORY_MSG, requestId);
        if(null == datas){
            return null;
        }
        SseEmitter emitter = createSSE(requestId);
        // 120Second
        final long timeoutMillis = 120_000L;
        // Submit tasks using thread pool
        SSE_THREAD_POOL.submit(() -> {
            int lastIndex = 0;
            long lastActiveTime = System.currentTimeMillis();
            try {
                while (true) {
                    if(lastIndex < datas.size()) {
                        try {
                            EventData eventData = datas.get(lastIndex++);
                            String eventStr = JSONObject.toJSONString(eventData);
                            log.debug("[AIapplication]continue to receive-take overLLMreturn message:{}", eventStr);
                            emitter.send(SseEmitter.event().data(eventStr));
                            // There is new news，reset timer
                            lastActiveTime = System.currentTimeMillis();
                        } catch (IOException e) {
                            log.error("[AIapplication]continue to receive-Send messagefail");
                        }
                    } else {
                        // 没There is new news了
                        if (AiragLocalCache.get(AiragConsts.CACHE_TYPE_SSE, requestId) == null) {
                            // main threadssehas been removed,Exit thread.
                            log.info("[AIapplication]continue to receive-SSEMessage push completed: {}", requestId);
                            break;
                        } else if (System.currentTimeMillis() - lastActiveTime > timeoutMillis) {
                            // main thread未结束,Wait timeout,
                            log.warn("[AIapplication]continue to receive-Timeout waiting for message update，release thread: {}", requestId);
                            break;
                        } else {
                            // main thread未结束, Not timed out, Sleep for a while and check again
                            log.warn("[AIapplication]continue to receive-Wait for news update: {}", requestId);
                            Thread.sleep(500);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("SSEMessage push exception", e);
            } finally {
                try {
                    // Send completion event
                    emitter.send(SseEmitter.event().data(new EventData(requestId, null, EventData.EVENT_MESSAGE_END)));
                } catch (Exception e) {
                    log.error("An error occurred while terminating the session", e);
                    try {
                        // Prevent exception bubbling
                        emitter.completeWithError(e);
                    } catch (Exception ignore) {}
                } finally {
                    // closureemitter
                    try {
                        emitter.complete();
                    } catch (Exception ignore) {}
                }
            }
        });
        return emitter;
    }

    /**
     * createSSE
     * @param requestId
     * @return
     * @author chenrui
     * @date 2025/8/12 15:30
     */
    private static SseEmitter createSSE(String requestId) {
        SseEmitter emitter = new SseEmitter(-0L);
        emitter.onError(throwable -> {
            log.warn("SEE向客户端Send messagefail: {}", throwable.getMessage());
            AiragLocalCache.remove(AiragConsts.CACHE_TYPE_SSE, requestId);
            AiragLocalCache.remove(AiragConsts.CACHE_TYPE_SSE_SEND_TIME, requestId);
            try {
                emitter.complete();
            } catch (Exception ignore) {}
        });
        return emitter;
    }

    @Override
    public Result<?> deleteConversation(String conversationId) {
        AssertUtils.assertNotEmpty("Please select a conversation to delete", conversationId);
        String key = getConversationCacheKey(conversationId, null);
        if (oConvertUtils.isNotEmpty(key)) {
            Boolean delete = redisTemplate.delete(key);
            if (delete) {
                return Result.ok();
            } else {
                return Result.error("Failed to delete session");
            }
        }
        log.warn("[ai-chat]Delete session:Session not found:{}", conversationId);
        return Result.ok();
    }

    @Override
    public Result<?> updateConversationTitle(ChatConversation updateTitleParams) {
        AssertUtils.assertNotEmpty("Please select a session first", updateTitleParams);
        AssertUtils.assertNotEmpty("Please select a session first", updateTitleParams.getId());
        AssertUtils.assertNotEmpty("Please enter a session title", updateTitleParams.getTitle());
        String key = getConversationCacheKey(updateTitleParams.getId(), null);
        if (oConvertUtils.isEmpty(key)) {
            log.warn("[ai-chat]Delete session:Session not found:{}", updateTitleParams.getId());
            return Result.ok();
        }
        ChatConversation chatConversation = (ChatConversation) redisTemplate.boundValueOps(key).get();
        chatConversation.setTitle(updateTitleParams.getTitle());
        saveChatConversation(chatConversation);
        return Result.ok();
    }

    /**
     * Get会话cachekey
     *
     * @param conversationId
     * @param httpRequest
     * @return
     * @author chenrui
     * @date 2025/2/25 19:27
     */
    private String getConversationCacheKey(String conversationId, HttpServletRequest httpRequest) {
        if (oConvertUtils.isEmpty(conversationId)) {
            return null;
        }
        String key = getConversationDirCacheKey(httpRequest);
        key = key + ":" + conversationId;
        return key;
    }

    /**
     * Get当前用户会话的cache目录
     *
     * @param httpRequest
     * @return
     * @author chenrui
     * @date 2025/2/26 15:09
     */
    private String getConversationDirCacheKey(HttpServletRequest httpRequest) {
        String username = getUsername(httpRequest);
        // if user does not exist,Get当前ask的sessionid
        if (oConvertUtils.isEmpty(username)) {
            try {
                if (null == httpRequest) {
                    httpRequest = SpringContextUtils.getHttpServletRequest();
                }
                username = httpRequest.getSession().getId();
            } catch (Exception e) {
                log.error("Get当前ask的sessionidfail", e);
            }
        }
        AssertUtils.assertNotEmpty("Please log in first", username);
        return "airag:chat:" + username;
    }

    /**
     * Get会话
     *
     * @param app
     * @param conversationId
     * @return
     * @author chenrui
     * @date 2025/2/25 19:19
     */
    @NotNull
    private ChatConversation getOrCreateChatConversation(AiragApp app, String conversationId) {
        if (oConvertUtils.isObjectEmpty(app)) {
            app = new AiragApp();
            app.setId(AiAppConsts.DEFAULT_APP_ID);
        }
        ChatConversation chatConversation = null;
        String key = getConversationCacheKey(conversationId, null);
        if (oConvertUtils.isNotEmpty(key)) {
            chatConversation = (ChatConversation) redisTemplate.boundValueOps(key).get();
        }
        if (null == chatConversation) {
            chatConversation = createConversation(conversationId);
        }
        chatConversation.setApp(app);
        return chatConversation;
    }

    /**
     * create新的会话
     *
     * @param conversationId
     * @return
     * @author chenrui
     * @date 2025/2/26 15:53
     */
    @NotNull
    private ChatConversation createConversation(String conversationId) {
        // new session
        conversationId = oConvertUtils.getString(conversationId, UUIDGenerator.generate());
        ChatConversation chatConversation = new ChatConversation();
        chatConversation.setId(conversationId);
        chatConversation.setCreateTime(new Date());
        return chatConversation;
    }

    /**
     * save session
     *
     * @param chatConversation
     * @author chenrui
     * @date 2025/2/25 19:27
     */
    private void saveChatConversation(ChatConversation chatConversation) {
        saveChatConversation(chatConversation, false, null);
    }

    /**
     * save session
     *
     * @param chatConversation
     * @param temp             Is it a temporary session?
     * @author chenrui
     * @date 2025/2/25 19:27
     */
    private void saveChatConversation(ChatConversation chatConversation, boolean temp, HttpServletRequest httpRequest) {
        if (null == chatConversation) {
            return;
        }
        String key = getConversationCacheKey(chatConversation.getId(), httpRequest);
        if (oConvertUtils.isEmpty(key)) {
            return;
        }
        BoundValueOperations chatRedisCacheOp = redisTemplate.boundValueOps(key);
        chatRedisCacheOp.set(chatConversation);
        if (temp) {
            chatRedisCacheOp.expire(3, TimeUnit.HOURS);
        }
    }

    /**
     * Construct message
     *
     * @param conversation
     * @param topicId
     * @return
     * @author chenrui
     * @date 2025/2/25 15:26
     */
    private List<ChatMessage> collateMessage(ChatConversation conversation, String topicId) {
        List<MessageHistory> messagesHistory = conversation.getMessages();
        if (oConvertUtils.isObjectEmpty(messagesHistory)) {
            return new LinkedList<>();
        }
        LinkedList<ChatMessage> chatMessages = new LinkedList<>();
        for (int i = messagesHistory.size() - 1; i >= 0; i--) {
            MessageHistory history = messagesHistory.get(i);
            if (topicId.equals(history.getTopicId())) {
                ChatMessage chatMessage = null;
                switch (history.getRole()) {
                    case AiragConsts.MESSAGE_ROLE_USER:
                        List<Content> contents = new ArrayList<>();
                        List<MessageHistory.ImageHistory> images = history.getImages();
                        if (oConvertUtils.isObjectNotEmpty(images) && !images.isEmpty()) {
                            contents.addAll(images.stream().map(imageHistory -> {
                                if (oConvertUtils.isNotEmpty(imageHistory.getUrl())) {
                                    return ImageContent.from(imageHistory.getUrl());
                                } else {
                                    return ImageContent.from(imageHistory.getBase64Data(), imageHistory.getMimeType());
                                }
                            }).collect(Collectors.toList()));
                        }
                        contents.add(TextContent.from(history.getContent()));
                        chatMessage = UserMessage.from(contents);
                        break;
                    case AiragConsts.MESSAGE_ROLE_AI:
                        chatMessage = new AiMessage(history.getContent());
                        break;
                }
                if (null == chatMessage) {
                    continue;
                }
                chatMessages.addFirst(chatMessage);
            }
        }
        return chatMessages;
    }


    /**
     * add message
     *
     * @param messages
     * @param message
     * @param chatConversation
     * @param topicId
     * @return
     * @author chenrui
     * @date 2025/2/25 19:05
     */
    private void appendMessage(List<ChatMessage> messages, ChatMessage message, ChatConversation chatConversation, String topicId) {

        if (message.type().equals(ChatMessageType.SYSTEM)) {
            // System messages,Put it at the top of the message list,and does not record history
            messages.add(0, message);
            return;
        } else {
            messages.add(message);
        }
        List<MessageHistory> histories = chatConversation.getMessages();
        if (oConvertUtils.isObjectEmpty(histories)) {
            histories = new ArrayList<>();
        }
        // Message record
        MessageHistory historyMessage = MessageHistory.builder().conversationId(chatConversation.getId()).topicId(topicId).datetime(DateUtils.now()).build();
        if (message.type().equals(ChatMessageType.USER)) {
            historyMessage.setRole(AiragConsts.MESSAGE_ROLE_USER);
            StringBuilder textContent = new StringBuilder();
            List<MessageHistory.ImageHistory> images = new ArrayList<>();
            List<Content> contents = ((UserMessage) message).contents();
            contents.forEach(content -> {
                if (content.type().equals(ContentType.IMAGE)) {
                    ImageContent imageContent = (ImageContent) content;
                    Image image = imageContent.image();
                    MessageHistory.ImageHistory imageMessage = MessageHistory.ImageHistory.from(image.url(), image.base64Data(), image.mimeType());
                    images.add(imageMessage);
                } else if (content.type().equals(ContentType.TEXT)) {
                    textContent.append(((TextContent) content).text()).append("\n");
                }
            });
            historyMessage.setContent(textContent.toString());
            historyMessage.setImages(images);
        } else if (message.type().equals(ChatMessageType.AI)) {
            historyMessage.setRole(AiragConsts.MESSAGE_ROLE_AI);
            historyMessage.setContent(((AiMessage) message).text());
        }
        histories.add(historyMessage);
        chatConversation.setMessages(histories);
    }

    /**
     * Send chat message
     *
     * @param chatConversation
     * @param topicId
     * @param sendParams
     * @return
     * @author chenrui
     * @date 2025/2/28 11:04
     */
    @NotNull
    private SseEmitter doChat(ChatConversation chatConversation, String topicId, ChatSendParams sendParams) {
        // Assemble this message list from historical messages
        List<ChatMessage> messages = collateMessage(chatConversation, topicId);

        AiragApp aiApp = chatConversation.getApp();
        // Each session generates a new,used for cachingemitter
        String requestId = UUIDGenerator.generate();
        SseEmitter emitter = createSSE(requestId);
        // cacheemitter
        AiragLocalCache.put(AiragConsts.CACHE_TYPE_SSE, requestId, emitter);
        // cache开始send时间
        log.info("[AI-CHAT]开始Send message,requestId:{}", requestId);
        AiragLocalCache.put(AiragConsts.CACHE_TYPE_SSE_SEND_TIME, requestId, System.currentTimeMillis());
        // 初始化历史消息cache
        AiragLocalCache.put(AiragConsts.CACHE_TYPE_SSE_HISTORY_MSG, requestId, new CopyOnWriteArrayList<>());
        try {
            // Assemble user messages
            UserMessage userMessage = aiChatHandler.buildUserMessage(sendParams.getContent(), sendParams.getImages());
            // add message
            appendMessage(messages, userMessage, chatConversation, topicId);
            /* There should be several situations here:
             * 1. Noaiapplication:Get默认模型->Start chatting
             * 2. AIapplication-chat assistant(ChatAssistant):从applicationinformationAssemble model和prompt word->Start chatting
             * 3. AIapplication-Chat process(ChatFlow):从applicationinformationGet模型,process,Assemble input parameters->Call workflow
             */
            if (null != aiApp && !AiAppConsts.DEFAULT_APP_ID.equals(aiApp.getId())) {
                // aiapplication:查询applicationinformation(ChatAssistant,chatflow),模型information,Assemble model-prompt word,Knowledge base etc.
                if (AiAppConsts.APP_TYPE_CHAT_FLOW.equals(aiApp.getType())) {
                    // aiapplication:Chat process(ChatFlow)
                    sendWithFlow(requestId, aiApp.getFlowId(), chatConversation, topicId, messages, sendParams);
                } else {
                    // AIapplication-chat assistant(ChatAssistant):从applicationinformationAssemble model和prompt word
                    sendWithAppChat(requestId, messages, chatConversation, topicId);
                }
            } else {
                // send message
                sendWithDefault(requestId, chatConversation, topicId, null, messages, null);
            }
            // Send ready message
            EventData eventRequestId = new EventData(requestId, null, EventData.EVENT_INIT_REQUEST_ID, chatConversation.getId(), topicId);
            eventRequestId.setData(EventMessageData.builder().message("").build());
            sendMessage2Client(emitter, eventRequestId);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            EventData eventData = new EventData(requestId, null, EventData.EVENT_FLOW_ERROR, chatConversation.getId(), topicId);
            eventData.setData(EventFlowData.builder().success(false).message(e.getMessage()).build());
            closeSSE(emitter, eventData);
        }
        return emitter;
    }

    /**
     * 运行process
     *
     * @param requestId
     * @param flowId
     * @param chatConversation
     * @param topicId
     * @param messages
     * @param sendParams
     * @author chenrui
     * @date 2025/2/27 14:55
     */
    private void sendWithFlow(String requestId, String flowId, ChatConversation chatConversation, String topicId, List<ChatMessage> messages, ChatSendParams sendParams) {
        FlowRunParams flowRunParams = new FlowRunParams();
        flowRunParams.setRequestId(requestId);
        flowRunParams.setFlowId(flowId);
        flowRunParams.setConversationId(chatConversation.getId());
        flowRunParams.setTopicId(topicId);
        // Support streaming
        flowRunParams.setResponseMode(FlowConsts.FLOW_RESPONSE_MODE_STREAMING);
        Map<String, Object> flowInputParams = new HashMap<>();
        List<MessageHistory> histories = new ArrayList<>();
        if (oConvertUtils.isObjectNotEmpty(chatConversation.getMessages())) {
            // create历史消息的副本(Do not directly operate the originallist)
            histories.addAll(chatConversation.getMessages());
            // Remove last historical message(The last one is the message currently sent out.)
            histories.remove(histories.size() - 1);
        }
        flowInputParams.put(FlowConsts.FLOW_INPUT_PARAM_HISTORY, histories);
        flowInputParams.put(FlowConsts.FLOW_INPUT_PARAM_QUESTION, sendParams.getContent());
        flowInputParams.put(FlowConsts.FLOW_INPUT_PARAM_IMAGES, sendParams.getImages());
        flowRunParams.setInputParams(flowInputParams);
        HttpServletRequest httpRequest = SpringContextUtils.getHttpServletRequest();
        flowRunParams.setHttpRequest(httpRequest);
        // process结束后,Recordai返回并save session
        // sse
        SseEmitter emitter = AiragLocalCache.get(AiragConsts.CACHE_TYPE_SSE, requestId);
        flowRunParams.setEventCallback(eventData -> {
            if (EventData.EVENT_FLOW_FINISHED.equals(eventData.getEvent())) {
                // Print time-consuming log
                printChatDuration(requestId, "process执行完毕");
                // Already executed,删除时间cache
                AiragLocalCache.remove(AiragConsts.CACHE_TYPE_SSE_SEND_TIME, requestId);
                EventFlowData data = (EventFlowData) eventData.getData();
                if(data.isSuccess()) {
                    Object outputs = data.getOutputs();
                    if (oConvertUtils.isObjectNotEmpty(outputs)) {
                        AiMessage aiMessage;
                        if (outputs instanceof String) {
                            // Compatible inference model
                            String messageText = String.valueOf(outputs);
                            messageText = messageText.replaceAll("<think>([\\s\\S]*?)</think>", "> $1");
                            aiMessage = new AiMessage(messageText);
                        } else {
                            aiMessage = new AiMessage(JSONObject.toJSONString(outputs));
                        }
                        EventData msgEventData = new EventData(requestId, null, EventData.EVENT_MESSAGE, chatConversation.getId(), topicId);
                        EventMessageData messageEventData = EventMessageData.builder().message(aiMessage.text()).build();
                        msgEventData.setData(messageEventData);
                        msgEventData.setRequestId(requestId);
                        sendMessage2Client(emitter, msgEventData);
                        appendMessage(messages, aiMessage, chatConversation, topicId);
                        // save session
                        saveChatConversation(chatConversation, false, httpRequest);
                    }
                }else{
                    //update-begin---author:chenrui ---date:20250425  for：[QQYUN-12203]AI chat，Timeout or server error，Give a friendly reminder------------
                    // fail
                    String message = data.getMessage();
                    if (message != null && message.contains(FlowConsts.FLOW_ERROR_MSG_LLM_TIMEOUT)) {
                        message = "There are currently many users，Queuing，Please try again later！";
                        EventData errEventData = new EventData(requestId, null, EventData.EVENT_MESSAGE, chatConversation.getId(), topicId);
                        errEventData.setData(EventMessageData.builder().message("\n" + message).build());
                        sendMessage2Client(emitter, errEventData);
                        errEventData = new EventData(requestId, null, EventData.EVENT_MESSAGE_END, chatConversation.getId(), topicId);
                        // If it is timeout,主动closureSSE,防止process切面中返回异常消息导致前端不能正常展示上面的{General news}.
                        closeSSE(emitter, errEventData);
                    }
                    //update-end---author:chenrui ---date:20250425  for：[QQYUN-12203]AI chat，Timeout or server error，Give a friendly reminder------------
                }
            }
        });
        // 打印processtime consuming日志
        printChatDuration(requestId, "开始执行process");
        airagFlowService.runFlow(flowRunParams);
    }


    /**
     * sendappchat
     *
     * @param requestId
     * @param messages
     * @param chatConversation
     * @param topicId
     * @return
     * @author chenrui
     * @date 2025/2/28 10:41
     */
    private void sendWithAppChat(String requestId, List<ChatMessage> messages, ChatConversation chatConversation, String topicId) {
        AiragApp aiApp = chatConversation.getApp();
        String modelId = aiApp.getModelId();
        AssertUtils.assertNotEmpty("Please select a model first", modelId);
        // AIapplicationprompt word
        String prompt = aiApp.getPrompt();
        if (oConvertUtils.isNotEmpty(prompt)) {
            appendMessage(messages, new SystemMessage(prompt), chatConversation, topicId);
        }

        AIChatParams aiChatParams = new AIChatParams();
        // AIapplication自定义的模型参数
        String metadataStr = aiApp.getMetadata();
        if (oConvertUtils.isNotEmpty(metadataStr)) {
            JSONObject metadata = JSONObject.parseObject(metadataStr);
            if (oConvertUtils.isNotEmpty(metadata)) {
                if (metadata.containsKey("temperature")) {
                    aiChatParams.setTemperature(metadata.getDouble("temperature"));
                }
                if (metadata.containsKey("topP")) {
                    aiChatParams.setTopP(metadata.getDouble("topP"));
                }
                if (metadata.containsKey("presencePenalty")) {
                    aiChatParams.setPresencePenalty(metadata.getDouble("presencePenalty"));
                }
                if (metadata.containsKey("frequencyPenalty")) {
                    aiChatParams.setFrequencyPenalty(metadata.getDouble("frequencyPenalty"));
                }
                if (metadata.containsKey("maxTokens")) {
                    aiChatParams.setMaxTokens(metadata.getInteger("maxTokens"));
                }
            }
        }
        // 打印processtime consuming日志
        printChatDuration(requestId, "构造application自定义参数完成");
        // send message
        sendWithDefault(requestId, chatConversation, topicId, modelId, messages, aiChatParams);
    }

    /**
     * 处理chat
     * 向大模型Send message并接受响应
     *
     * @param chatConversation
     * @param topicId
     * @param modelId
     * @param messages
     * @return
     * @author chenrui
     * @date 2025/2/25 19:24
     */
    private void sendWithDefault(String requestId, ChatConversation chatConversation, String topicId, String modelId, List<ChatMessage> messages, AIChatParams aiChatParams) {
        // callaichat
        if (null == aiChatParams) {
            aiChatParams = new AIChatParams();
        }
        // If it is the defaultapp,Load system default tools
        if(chatConversation.getApp().getId().equals(AiAppConsts.DEFAULT_APP_ID)){
            aiChatParams.setTools(jeecgToolsProvider.getDefaultTools());
        }
        aiChatParams.setKnowIds(chatConversation.getApp().getKnowIds());
        aiChatParams.setMaxMsgNumber(oConvertUtils.getInt(chatConversation.getApp().getMsgNum(), 5));
        HttpServletRequest httpRequest = SpringContextUtils.getHttpServletRequest();
        TokenStream chatStream;
        try {
            // 打印processtime consuming日志
            printChatDuration(requestId, "start toLLMSend message");
            if (oConvertUtils.isNotEmpty(modelId)) {
                chatStream = aiChatHandler.chat(modelId, messages, aiChatParams);
            } else {
                chatStream = aiChatHandler.chatByDefaultModel(messages, aiChatParams);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            // sse
            SseEmitter emitter = AiragLocalCache.get(AiragConsts.CACHE_TYPE_SSE, requestId);
            if (null == emitter) {
                log.warn("[AIapplication]take overLLM返回会话已closure{}", requestId);
                return;
            }
            String errMsg = "call大模型接口fail，Please check the background log for details。";
            if(e instanceof JeecgBootException){
                errMsg = e.getMessage();
            }
            EventData eventData = new EventData(requestId, null, EventData.EVENT_FLOW_ERROR, chatConversation.getId(), topicId);
            eventData.setData(EventFlowData.builder().success(false).message(errMsg).build());
            closeSSE(emitter, eventData);
            throw new JeecgBootBizTipException("call大模型接口fail:" + e.getMessage());
        }
        /**
         * Are you thinking
         */
        AtomicBoolean isThinking = new AtomicBoolean(false);
        // aichat响应逻辑
        chatStream.onPartialResponse((String resMessage) -> {
            // Compatible inference model
            if ("<think>".equals(resMessage)) {
                isThinking.set(true);
                resMessage = "> ";
            }
            if ("</think>".equals(resMessage)) {
                isThinking.set(false);
                resMessage = "\n\n";
            }
            if (isThinking.get()) {
                if (null != resMessage && resMessage.contains("\n")) {
                    resMessage = "\n> ";
                }
            }
            EventData eventData = new EventData(requestId, null, EventData.EVENT_MESSAGE, chatConversation.getId(), topicId);
            EventMessageData messageEventData = EventMessageData.builder().message(resMessage).build();
            eventData.setData(messageEventData);
            eventData.setRequestId(requestId);
            // sse
            SseEmitter emitter = AiragLocalCache.get(AiragConsts.CACHE_TYPE_SSE, requestId);
            if (null == emitter) {
                log.warn("[AIapplication]take overLLM返回会话已closure");
                return;
            }
            sendMessage2Client(emitter, eventData);
        }).onCompleteResponse((responseMessage) -> {
            // 打印processtime consuming日志
            printChatDuration(requestId, "LLMOutput message completed");
            AiragLocalCache.remove(AiragConsts.CACHE_TYPE_SSE_SEND_TIME, requestId);
            // RecordaiReply
            AiMessage aiMessage = responseMessage.aiMessage();
            FinishReason finishReason = responseMessage.finishReason();
            String respText = aiMessage.text();
            // sse
            SseEmitter emitter = AiragLocalCache.get(AiragConsts.CACHE_TYPE_SSE, requestId);
            if (null == emitter) {
                log.warn("[AIapplication]take overLLM返回会话已closure");
                return;
            }
            if (FinishReason.STOP.equals(finishReason) || null == finishReason) {
                // End normally
                EventData eventData = new EventData(requestId, null, EventData.EVENT_MESSAGE_END, chatConversation.getId(), topicId);
                appendMessage(messages, aiMessage, chatConversation, topicId);
                // save session
                saveChatConversation(chatConversation, false, httpRequest);
                closeSSE(emitter, eventData);
            } else if (FinishReason.TOOL_EXECUTION.equals(finishReason)) {
                // Requires execution tools
                // TODO author: chenrui for: date:2025/3/7
            } else if (FinishReason.LENGTH.equals(finishReason)) {
                // Context length exceeds limit
                log.error("call模型异常:Context length exceeds limit:{}", responseMessage.tokenUsage());
                EventData eventData = new EventData(requestId, null, EventData.EVENT_MESSAGE, chatConversation.getId(), topicId);
                eventData.setData(EventMessageData.builder().message("\nContext length exceeds limit，请调整模型maximumTokens").build());
                sendMessage2Client(emitter, eventData);
                eventData = new EventData(requestId, null, EventData.EVENT_MESSAGE_END, chatConversation.getId(), topicId);
                closeSSE(emitter, eventData);
            } else {
                // abnormal end
                log.error("call模型异常:" + respText);
                if (respText.contains("insufficient Balance")) {
                    respText = "Insufficient balance of large language model account!";
                }
                EventData eventData = new EventData(requestId, null, EventData.EVENT_FLOW_ERROR, chatConversation.getId(), topicId);
                eventData.setData(EventFlowData.builder().success(false).message(respText).build());
                closeSSE(emitter, eventData);
            }
        }).onError((Throwable error) -> {
            // 打印processtime consuming日志
            printChatDuration(requestId, "LLMOutput message exception");
            AiragLocalCache.remove(AiragConsts.CACHE_TYPE_SSE_SEND_TIME, requestId);
            // sse
            SseEmitter emitter = AiragLocalCache.get(AiragConsts.CACHE_TYPE_SSE, requestId);
            if (null == emitter) {
                log.warn("[AIapplication]take overLLM返回会话已closure{}", requestId);
                return;
            }
            log.error(error.getMessage(), error);
            String errMsg = error.getMessage();
            if (errMsg != null && errMsg.contains("timeout")) {
                //update-begin---author:chenrui ---date:20250425  for：[QQYUN-12203]AI chat，Timeout or server error，Give a friendly reminder------------
                errMsg = "There are currently many users，Queuing，Please try again later！";
                EventData eventData = new EventData(requestId, null, EventData.EVENT_MESSAGE, chatConversation.getId(), topicId);
                eventData.setData(EventMessageData.builder().message("\n" + errMsg).build());
                sendMessage2Client(emitter, eventData);
                eventData = new EventData(requestId, null, EventData.EVENT_MESSAGE_END, chatConversation.getId(), topicId);
                closeSSE(emitter, eventData);
                //update-end---author:chenrui ---date:20250425  for：[QQYUN-12203]AI chat，Timeout or server error，Give a friendly reminder------------
            } else {
                errMsg = "call大模型接口fail，Please check the background log for details。";
                // Make detailed translations based on common abnormal keywords
                for (Map.Entry<String, String> entry : AIChatHandler.MODEL_ERROR_MAP.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    if (error.getMessage().contains(key)) {
                        errMsg = value;
                    }
                }
                EventData eventData = new EventData(requestId, null, EventData.EVENT_FLOW_ERROR, chatConversation.getId(), topicId);
                eventData.setData(EventFlowData.builder().success(false).message(errMsg).build());
                closeSSE(emitter, eventData);
            }
        }).start();
    }

    /**
     * Send message到客户端
     *
     * @param emitter
     * @param eventData
     * @author chenrui
     * @date 2025/4/22 19:58
     */
    private static void sendMessage2Client(SseEmitter emitter, EventData eventData) {
        try {
            log.info("Send message:{}", eventData.getRequestId());
            String eventStr = JSONObject.toJSONString(eventData);
            log.debug("[AIapplication]take overLLMreturn message:{}", eventStr);
            emitter.send(SseEmitter.event().data(eventStr));
            List<EventData> historyMsg = AiragLocalCache.get(AiragConsts.CACHE_TYPE_SSE_HISTORY_MSG, eventData.getRequestId());
            if (null == historyMsg) {
                historyMsg = new CopyOnWriteArrayList<>();
                AiragLocalCache.put(AiragConsts.CACHE_TYPE_SSE_HISTORY_MSG, eventData.getRequestId(), historyMsg);
            }
            historyMsg.add(eventData);
        } catch (IOException e) {
            log.error("Send messagefail", e);
        }
    }

    /**
     * sendchatReturn results
     *
     * @author chenrui
     * @date 2025/2/28 11:05
     */
    private static class ChatResult {
        public final SseEmitter emitter;
        public final AiragModel chatModel;

        public ChatResult(SseEmitter emitter, AiragModel chatModel) {
            this.emitter = emitter;
            this.chatModel = chatModel;
        }
    }


    /**
     * Summary session title
     * few questions: <br/>
     * 1. 如果在send message时同步Summary session title,This will cause the interface to be slow or even time out..
     * 2. 但如果异步更new session标题会导致Message record丢失(Incomplete)Or the title is missing,A lot of logic needs to be written to ensure eventual consistency.
     * so Don’t use it for nowAI更new session标题. If you need to add a separate interface later,,由前端call或者在第一次消息take over完成后再异步更新
     *
     * @param chatConversation
     * @param question
     * @param modelId
     * @return
     * @author chenrui
     * @date 2025/2/25 17:12
     */
    protected void summaryConversationTitle(ChatConversation chatConversation, String question, String modelId) {
        if (oConvertUtils.isEmpty(chatConversation.getId())) {
            return;
        }
        String key = getConversationCacheKey(chatConversation.getId(), null);
        if (oConvertUtils.isEmpty(key)) {
            return;
        }
        CompletableFuture.runAsync(() -> {
            List<ChatMessage> messages = new LinkedList<>();
            String systemMsgStr = "Based on user questions,Summary session title.\n" + "The requirements are as follows:\n" + "1. Answer in Chinese.\n" + "2. The title length is controlled within5Chinese characters10Within English characters\n" + "3. Reply directly to conversation title,No other irrelevant descriptions\n" + "4. If it cannot be summarized,Reply I don’t know\n";
            messages.add(new SystemMessage(systemMsgStr));
            messages.add(new UserMessage(question));
            String summaryTitle;
            try {
                summaryTitle = aiChatHandler.completions(modelId, messages, null);
                log.info("Summary session complete{}", summaryTitle);
                if (summaryTitle.equalsIgnoreCase("have no idea")) {
                    summaryTitle = "";
                }
            } catch (Exception e) {
                log.warn("AI总结会话fail" + e.getMessage(), e);
                summaryTitle = "";
            }
            // 更new session标题
            ChatConversation cachedConversation = (ChatConversation) redisTemplate.boundValueOps(key).get();
            if (null == cachedConversation) {
                cachedConversation = chatConversation;
            }
            if (oConvertUtils.isEmpty(chatConversation.getTitle())) {
                // Check again whether the title is empty,Update only if the title is empty
                if (oConvertUtils.isNotEmpty(summaryTitle)) {
                    cachedConversation.setTitle(summaryTitle);
                } else {
                    cachedConversation.setTitle(question.length() > 5 ? question.substring(0, 5) : question);
                }
                //save session
                saveChatConversation(cachedConversation);
            }
        });
    }

    /**
     * Get用户名
     *
     * @param httpRequest
     * @return
     * @author chenrui
     * @date 2025/3/27 15:05
     */
    private String getUsername(HttpServletRequest httpRequest) {
        try {
            TokenUtils.getTokenByRequest();
            String token;
            if (null != httpRequest) {
                token = TokenUtils.getTokenByRequest(httpRequest);
            } else {
                token = TokenUtils.getTokenByRequest();
            }
            if (TokenUtils.verifyToken(token, sysBaseApi, redisUtil)) {
                return JwtUtil.getUsername(token);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }


    /**
     * Printing takes time
     * @param requestId
     * @param message
     * @author chenrui
     * @date 2025/4/28 15:15
     */
    private static void printChatDuration(String requestId,String message) {
        Long beginTime = AiragLocalCache.get(AiragConsts.CACHE_TYPE_SSE_SEND_TIME, requestId);
        if (null != beginTime) {
            log.info("[AI-CHAT]{},requestId:{},time consuming:{}s", message, requestId, (System.currentTimeMillis() - beginTime) / 1000);
        }
    }
}