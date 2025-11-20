package org.jeecg.modules.airag.app.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.util.CommonUtils;
import org.jeecg.config.shiro.IgnoreAuth;
import org.jeecg.modules.airag.app.service.IAiragChatService;
import org.jeecg.modules.airag.app.vo.ChatConversation;
import org.jeecg.modules.airag.app.vo.ChatSendParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;



/**
 * airagapplication-chat
 *
 * @Author: chenrui
 * @Date: 2025-02-25 11:40
 */
@Slf4j
@RestController
@RequestMapping("/airag/chat")
public class AiragChatController {

    @Autowired
    IAiragChatService chatService;

    @Value(value = "${jeecg.path.upload}")
    private String uploadpath;

    /**
     * local：local minio：minio Ali：alioss
     */
    @Value(value="${jeecg.uploadType}")
    private String uploadType;


    /**
     * Send message
     *
     * @return Return aResultobject，表示Send message的结果
     * @author chenrui
     * @date 2025/2/25 11:42
     */
    @IgnoreAuth
    @PostMapping(value = "/send")
    public SseEmitter send(@RequestBody ChatSendParams chatSendParams) {
        return chatService.send(chatSendParams);
    }

    /**
     * Send message <br/>
     * Compatible with older browsers
     * @param content
     * @param conversationId
     * @param topicId
     * @param appId
     * @return
     * @author chenrui
     * @date 2025/2/25 18:13
     */
    @GetMapping(value = "/send")
    public SseEmitter sendByGet(@RequestParam("content") String content,
                                @RequestParam(value = "conversationId", required = false) String conversationId,
                                @RequestParam(value = "topicId", required = false) String topicId,
                                @RequestParam(value = "appId", required = false) String appId) {
        ChatSendParams chatSendParams = new ChatSendParams(content, conversationId, topicId, appId);
        return chatService.send(chatSendParams);
    }

    /**
     * Get all conversations
     *
     * @return Return aResultobject，Contains information about all conversations
     * @author chenrui
     * @date 2025/2/25 11:42
     */
    @IgnoreAuth
    @GetMapping(value = "/init")
    public Result<?> initChat(@RequestParam(name = "id", required = true) String id) {
        return chatService.initChat(id);
    }

    /**
     * Get all conversations
     *
     * @return Return aResultobject，Contains information about all conversations
     * @author chenrui
     * @date 2025/2/25 11:42
     */
    @IgnoreAuth
    @GetMapping(value = "/conversations")
    public Result<?> getConversations(@RequestParam(value = "appId", required = false) String appId) {
        return chatService.getConversations(appId);
    }

    /**
     * Delete session
     *
     * @param id
     * @return
     * @author chenrui
     * @date 2025/3/3 16:55
     */
    @IgnoreAuth
    @DeleteMapping(value = "/conversation/{id}")
    public Result<?> deleteConversation(@PathVariable("id") String id) {
        return chatService.deleteConversation(id);
    }

    /**
     * Update session title
     *
     * @param updateTitleParams
     * @return
     * @author chenrui
     * @date 2025/3/3 16:55
     */
    @IgnoreAuth
    @PutMapping(value = "/conversation/update/title")
    public Result<?> updateConversationTitle(@RequestBody ChatConversation updateTitleParams) {
        return chatService.updateConversationTitle(updateTitleParams);
    }

    /**
     * Get news
     *
     * @return Return aResultobject，Contains information about the message
     * @author chenrui
     * @date 2025/2/25 11:42
     */
    @IgnoreAuth
    @GetMapping(value = "/messages")
    public Result<?> getMessages(@RequestParam(value = "conversationId", required = true) String conversationId) {
        return chatService.getMessages(conversationId);
    }

    /**
     * Clear messages
     *
     * @return
     * @author chenrui
     * @date 2025/2/25 11:42
     */
    @IgnoreAuth
    @GetMapping(value = "/messages/clear/{conversationId}")
    public Result<?> clearMessage(@PathVariable(value = "conversationId") String conversationId) {
        return chatService.clearMessage(conversationId);
    }

    /**
     * Continue to receive messages
     *
     * @param requestId
     * @return
     * @author chenrui
     * @date 2025/8/11 17:49
     */
    @IgnoreAuth
    @GetMapping(value = "/receive/{requestId}")
    public SseEmitter receiveByRequestId(@PathVariable(name = "requestId", required = true) String requestId) {
        return chatService.receiveByRequestId(requestId);
    }


    /**
     * upon requestIDStop processing a request
     *
     * @param requestId The unique identifier of the request，Used to identify and stop specific requests
     * @return Return aResultobject，Indicates the result of a stop request
     * @author chenrui
     * @date 2025/2/25 11:42
     */
    @IgnoreAuth
    @GetMapping(value = "/stop/{requestId}")
    public Result<?> stop(@PathVariable(name = "requestId", required = true) String requestId) {
        return chatService.stop(requestId);
    }


    /**
     * Upload files
     * for [QQYUN-12135]AIchat，Uploading pictures prompts illegaltoken
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     * @author chenrui
     * @date 2025/4/25 11:04
     */
    @IgnoreAuth
    @PostMapping(value = "/upload")
    public Result<?> upload(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String bizPath = "airag";

        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        // 获取Upload filesobject
        MultipartFile file = multipartRequest.getFile("file");
        String savePath;
        if (CommonConstant.UPLOAD_TYPE_LOCAL.equals(uploadType)) {
            savePath = CommonUtils.uploadLocal(file, bizPath, uploadpath);
        } else {
            savePath = CommonUtils.upload(file, bizPath, uploadType);
        }
        Result<?> result = new Result<>();
        result.setMessage(savePath);
        result.setSuccess(true);
        return result;
    }

}
