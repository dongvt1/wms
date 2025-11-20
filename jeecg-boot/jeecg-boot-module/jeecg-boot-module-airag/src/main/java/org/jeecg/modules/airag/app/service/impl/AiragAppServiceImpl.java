package org.jeecg.modules.airag.app.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.output.FinishReason;
import dev.langchain4j.service.TokenStream;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.util.AssertUtils;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.modules.airag.app.consts.Prompts;
import org.jeecg.modules.airag.app.entity.AiragApp;
import org.jeecg.modules.airag.app.mapper.AiragAppMapper;
import org.jeecg.modules.airag.app.service.IAiragAppService;
import org.jeecg.modules.airag.common.consts.AiragConsts;
import org.jeecg.modules.airag.common.handler.AIChatParams;
import org.jeecg.modules.airag.common.handler.IAIChatHandler;
import org.jeecg.modules.airag.common.utils.AiragLocalCache;
import org.jeecg.modules.airag.common.vo.event.EventData;
import org.jeecg.modules.airag.common.vo.event.EventFlowData;
import org.jeecg.modules.airag.common.vo.event.EventMessageData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Description: AIapplication
 * @Author: jeecg-boot
 * @Date: 2025-02-26
 * @Version: V1.0
 */
@Slf4j
@Service
public class AiragAppServiceImpl extends ServiceImpl<AiragAppMapper, AiragApp> implements IAiragAppService {

    @Autowired
    IAIChatHandler aiChatHandler;

    @Override
    public Object generatePrompt(String prompt, boolean blocking) {
        AssertUtils.assertNotEmpty("Please enter the prompt word", prompt);
        List<ChatMessage> messages = Arrays.asList(new SystemMessage(Prompts.GENERATE_LLM_PROMPT), new UserMessage(prompt));

        AIChatParams params = new AIChatParams();
        params.setTemperature(0.8);
        params.setTopP(0.9);
        params.setPresencePenalty(0.1);
        params.setFrequencyPenalty(0.1);
        if(blocking){
            String promptValue = aiChatHandler.completionsByDefaultModel(messages, params);
            if (promptValue == null || promptValue.isEmpty()) {
                return Result.error("Build failed");
            }
            return Result.OK("success", promptValue);
        }else{
            SseEmitter emitter = new SseEmitter(-0L);
            // Run asynchronously(streaming)
            TokenStream tokenStream = aiChatHandler.chatByDefaultModel(messages, params);
            /**
             * Are you thinking
             */
            AtomicBoolean isThinking = new AtomicBoolean(false);
            String requestId = UUIDGenerator.generate();
            // aiChat response logic
            tokenStream.onPartialResponse((String resMessage) -> {
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
                        EventData eventData = new EventData(requestId, null, EventData.EVENT_MESSAGE);
                        EventMessageData messageEventData = EventMessageData.builder()
                                .message(resMessage)
                                .build();
                        eventData.setData(messageEventData);
                        try {
                            String eventStr = JSONObject.toJSONString(eventData);
                            log.debug("[AIapplication]take overLLMreturn message:{}", eventStr);
                            emitter.send(SseEmitter.event().data(eventStr));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .onCompleteResponse((responseMessage) -> {
                        // RecordaiReply
                        AiMessage aiMessage = responseMessage.aiMessage();
                        FinishReason finishReason = responseMessage.finishReason();
                        String respText = aiMessage.text();
                        if (FinishReason.STOP.equals(finishReason) || null == finishReason) {
                            // End normally
                            EventData eventData = new EventData(requestId, null, EventData.EVENT_MESSAGE_END);
                            try {
                                log.debug("[AIapplication]take overLLMreturn message完成:{}", respText);
                                emitter.send(SseEmitter.event().data(eventData));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            closeSSE(emitter, eventData);
                        } else {
                            // abnormal end
                            log.error("Exception when calling model:" + respText);
                            if (respText.contains("insufficient Balance")) {
                                respText = "Big prediction model account balance is insufficient!";
                            }
                            EventData eventData = new EventData(requestId, null, EventData.EVENT_FLOW_ERROR);
                            eventData.setData(EventFlowData.builder().success(false).message(respText).build());
                            closeSSE(emitter, eventData);
                        }
                    })
                    .onError((Throwable error) -> {
                        // sse
                        String errMsg = "Failed to call large model interface:" + error.getMessage();
                        log.error(errMsg, error);
                        EventData eventData = new EventData(requestId, null, EventData.EVENT_FLOW_ERROR);
                        eventData.setData(EventFlowData.builder().success(false).message(errMsg).build());
                        closeSSE(emitter, eventData);
                    })
                    .start();
            return emitter;
        }
    }

    private static void closeSSE(SseEmitter emitter, EventData eventData) {
        try {
            // Send completion event
            emitter.send(SseEmitter.event().data(eventData));
        } catch (IOException e) {
            log.error("An error occurred while terminating the session", e);
        } finally {
            // Remove from cacheemitter
            AiragLocalCache.remove(AiragConsts.CACHE_TYPE_SSE, eventData.getRequestId());
            // closureemitter
            emitter.complete();
        }
    }
}
