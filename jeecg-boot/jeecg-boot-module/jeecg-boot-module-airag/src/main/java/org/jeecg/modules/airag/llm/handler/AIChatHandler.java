package org.jeecg.modules.airag.llm.handler;

import com.alibaba.fastjson.JSONObject;
import dev.langchain4j.data.message.*;
import dev.langchain4j.rag.query.router.QueryRouter;
import dev.langchain4j.service.TokenStream;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.ai.handler.LLMHandler;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.AssertUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.airag.common.handler.AIChatParams;
import org.jeecg.modules.airag.common.handler.IAIChatHandler;
import org.jeecg.modules.airag.llm.consts.LLMConsts;
import org.jeecg.modules.airag.llm.entity.AiragModel;
import org.jeecg.modules.airag.llm.mapper.AiragModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;

/**
 * Large model chat tool class
 *
 * @Author: chenrui
 * @Date: 2025/2/18 14:31
 */
@Slf4j
@Component
public class AIChatHandler implements IAIChatHandler {

    @Autowired
    AiragModelMapper airagModelMapper;

    @Autowired
    EmbeddingHandler embeddingHandler;

    @Autowired
    LLMHandler llmHandler;


    @Value(value = "${jeecg.path.upload:}")
    private String uploadpath;

    /**
     * Q&A
     *
     * @param modelId
     * @param messages
     * @return
     * @author chenrui
     * @date 2025/2/18 21:03
     */
    @Override
    public String completions(String modelId, List<ChatMessage> messages) {
        AssertUtils.assertNotEmpty("Send at least one message", messages);
        AssertUtils.assertNotEmpty("Please select a model", modelId);
        // Organize messages
        return completions(modelId, messages, null);
    }

    /**
     * Q&A
     *
     * @param modelId
     * @param messages
     * @param params
     * @return
     * @author chenrui
     * @date 2025/2/18 21:03
     */
    @Override
    public String completions(String modelId, List<ChatMessage> messages, AIChatParams params) {
        AssertUtils.assertNotEmpty("Send at least one message", messages);
        AssertUtils.assertNotEmpty("Please select a model", modelId);

        AiragModel airagModel = airagModelMapper.getByIdIgnoreTenant(modelId);
        AssertUtils.assertSame("Model is not active,please first[AIModel configuration]middle[Test activation]Model", airagModel.getActivateFlag(), 1);
        return completions(airagModel, messages, params);
    }

    /**
     * Q&A
     *
     * @param airagModel
     * @param messages
     * @param params
     * @return
     * @author chenrui
     * @date 2025/2/24 17:30
     */
    public String completions(AiragModel airagModel, List<ChatMessage> messages, AIChatParams params) {
        params = mergeParams(airagModel, params);
        String resp;
        try {
            resp = llmHandler.completions(messages, params);
        } catch (Exception e) {
            // langchain4j Exceptionally friendly tips
            String errMsg = "调用大Model接口失败，Please check the background log for details。";
            if (oConvertUtils.isNotEmpty(e.getMessage())) {
                // Make detailed translations based on common abnormal keywords
                for (Map.Entry<String, String> entry : MODEL_ERROR_MAP.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    if (errMsg.contains(key)) {
                        errMsg = value;
                    }
                }
            }
            log.error("AIModel调用异常: {}", errMsg, e);
            throw new JeecgBootException(errMsg);
        }
        if (resp.contains("</think>")
                && (null == params.getNoThinking() || params.getNoThinking())) {
            String[] thinkSplit = resp.split("</think>");
            resp = thinkSplit[thinkSplit.length - 1];
        }
        return resp;
    }

    /**
     * 使用默认ModelQ&A
     *
     * @param messages
     * @param params
     * @return
     * @author chenrui
     * @date 2025/3/12 15:13
     */
    @Override
    public String completionsByDefaultModel(List<ChatMessage> messages, AIChatParams params) {
        return completions(new AiragModel(), messages, params);
    }

    /**
     * chat(streaming)
     *
     * @param modelId
     * @param messages
     * @return
     * @author chenrui
     * @date 2025/2/20 21:06
     */
    @Override
    public TokenStream chat(String modelId, List<ChatMessage> messages) {
        return chat(modelId, messages, null);
    }

    /**
     * chat(streaming)
     *
     * @param modelId
     * @param messages
     * @param params
     * @return
     * @author chenrui
     * @date 2025/2/18 21:03
     */
    @Override
    public TokenStream chat(String modelId, List<ChatMessage> messages, AIChatParams params) {
        AssertUtils.assertNotEmpty("Send at least one message", messages);
        AssertUtils.assertNotEmpty("Please select a model", modelId);

        AiragModel airagModel = airagModelMapper.getByIdIgnoreTenant(modelId);
        AssertUtils.assertSame("Model is not active,please first[AIModel configuration]middle[Test activation]Model", airagModel.getActivateFlag(), 1);
        return chat(airagModel, messages, params);
    }

    /**
     * chat(streaming)
     *
     * @param airagModel
     * @param messages
     * @param params
     * @return
     * @author chenrui
     * @date 2025/2/24 17:29
     */
    private TokenStream chat(AiragModel airagModel, List<ChatMessage> messages, AIChatParams params) {
        params = mergeParams(airagModel, params);
        return llmHandler.chat(messages, params);
    }

    /**
     * 使用默认Modelchat
     *
     * @param messages
     * @param params
     * @return
     * @author chenrui
     * @date 2025/3/12 15:13
     */
    @Override
    public TokenStream chatByDefaultModel(List<ChatMessage> messages, AIChatParams params) {
        return chat(new AiragModel(), messages, params);
    }

    /**
     * merge airagmodelandparams,paramsSubject to
     *
     * @param airagModel
     * @param params
     * @return
     * @author chenrui
     * @date 2025/3/11 17:45
     */
    private AIChatParams mergeParams(AiragModel airagModel, AIChatParams params) {
        if (null == airagModel) {
            return params;
        }
        if (params == null) {
            params = new AIChatParams();
        }

        params.setProvider(airagModel.getProvider());
        params.setModelName(airagModel.getModelName());
        params.setBaseUrl(airagModel.getBaseUrl());
        if (oConvertUtils.isObjectNotEmpty(airagModel.getCredential())) {
            JSONObject modelCredential = JSONObject.parseObject(airagModel.getCredential());
            params.setApiKey(oConvertUtils.getString(modelCredential.getString("apiKey"), null));
            params.setSecretKey(oConvertUtils.getString(modelCredential.getString("secretKey"), null));
        }
        if (oConvertUtils.isObjectNotEmpty(airagModel.getModelParams())) {
            JSONObject modelParams = JSONObject.parseObject(airagModel.getModelParams());
            if (oConvertUtils.isObjectEmpty(params.getTemperature())) {
                params.setTemperature(modelParams.getDouble("temperature"));
            }
            if (oConvertUtils.isObjectEmpty(params.getTopP())) {
                params.setTopP(modelParams.getDouble("topP"));
            }
            if (oConvertUtils.isObjectEmpty(params.getPresencePenalty())) {
                params.setPresencePenalty(modelParams.getDouble("presencePenalty"));
            }
            if (oConvertUtils.isObjectEmpty(params.getFrequencyPenalty())) {
                params.setFrequencyPenalty(modelParams.getDouble("frequencyPenalty"));
            }
            if (oConvertUtils.isObjectEmpty(params.getMaxTokens())) {
                params.setMaxTokens(modelParams.getInteger("maxTokens"));
            }
            if (oConvertUtils.isObjectEmpty(params.getTimeout())) {
                params.setTimeout(modelParams.getInteger("timeout"));
            }
        }

        // RAG
        List<String> knowIds = params.getKnowIds();
        if (oConvertUtils.isObjectNotEmpty(knowIds)) {
            QueryRouter queryRouter = embeddingHandler.getQueryRouter(knowIds, params.getTopNumber(), params.getSimilarity());
            params.setQueryRouter(queryRouter);
        }

        // Set suremaxTokensValue is correct
        if (oConvertUtils.isObjectNotEmpty(params.getMaxTokens()) && params.getMaxTokens() <= 0) {
            params.setMaxTokens(null);
        }

        // Default timeout
        if(oConvertUtils.isObjectEmpty(params.getTimeout())){
            params.setTimeout(60);
        }

        return params;
    }

    @Override
    public UserMessage buildUserMessage(String content, List<String> images) {
        AssertUtils.assertNotEmpty("Please enter message content", content);
        List<Content> contents = new ArrayList<>();
        contents.add(TextContent.from(content));
        if (oConvertUtils.isObjectNotEmpty(images)) {
            // Get all pictures,convert them toImageContent
            List<ImageContent> imageContents = buildImageContents(images);
            contents.addAll(imageContents);
        }
        return UserMessage.from(contents);
    }

    @Override
    public List<ImageContent> buildImageContents(List<String> images) {
        List<ImageContent> imageContents = new ArrayList<>();
        for (String imageUrl : images) {
            Matcher matcher = LLMConsts.WEB_PATTERN.matcher(imageUrl);
            if (matcher.matches()) {
                // From the Internet
                imageContents.add(ImageContent.from(imageUrl));
            } else {
                // local file
                String filePath = uploadpath + File.separator + imageUrl;
                // Read the file and convert to base64 encoded string
                try {
                    Path path = Paths.get(filePath);
                    byte[] fileContent = Files.readAllBytes(path);
                    String base64Data = Base64.getEncoder().encodeToString(fileContent);
                    // Get the file MIME type
                    String mimeType = Files.probeContentType(path);
                    // build ImageContent object
                    imageContents.add(ImageContent.from(base64Data, mimeType));
                } catch (IOException e) {
                    log.error("Failed to read file: " + filePath, e);
                    throw new RuntimeException("Failed to send message,Exception reading file:" + e.getMessage(), e);
                }
            }
        }
        return imageContents;
    }


}
