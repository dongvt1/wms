package org.jeecg.modules.message.test;

import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.exceptions.ClientException;
import org.jeecg.JeecgSystemApplication;
import org.jeecg.common.api.dto.message.BusMessageDTO;
import org.jeecg.common.api.dto.message.BusTemplateMessageDTO;
import org.jeecg.common.api.dto.message.MessageDTO;
import org.jeecg.common.api.dto.message.TemplateMessageDTO;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.enums.DySmsEnum;
import org.jeecg.common.constant.enums.EmailTemplateEnum;
import org.jeecg.common.constant.enums.MessageTypeEnum;
import org.jeecg.common.constant.enums.SysAnnmentTypeEnum;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.util.DySmsHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: Message push test
 * @Author: lsq
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = JeecgSystemApplication.class)
public class SendMessageTest {

    @Autowired
    ISysBaseAPI sysBaseAPI;

    /**
     * Send system message
     */
    @Test
    public void sendSysAnnouncement() {
        //sender
        String fromUser = "admin";
        //recipient
        String toUser = "jeecg";
        //title
        String title = "System messages";
        //content
        String msgContent = "TEST:Today's schedule has been delivered！";
        //Send system message
        sysBaseAPI.sendSysAnnouncement(new MessageDTO(fromUser, toUser, title, msgContent));
        //Message type
        String msgCategory = CommonConstant.MSG_CATEGORY_1;
        //Business type
        String busType = SysAnnmentTypeEnum.EMAIL.getType();
        //businessID
        String busId = "11111";
        //send带business参数的System messages
        BusMessageDTO busMessageDTO = new BusMessageDTO(fromUser, toUser, title, msgContent, msgCategory, busType,busId);
        sysBaseAPI.sendBusAnnouncement(busMessageDTO);
    }

    /**
     * Send template message
     */
    @Test
    public void sendTemplateAnnouncement() {
        //sender
        String fromUser = "admin";
        //recipient
        String toUser = "jeecg";
        //title
        String title = "Notices and Announcements";
        //Template coding
        String templateCode = "412358";
        //Template parameters
        Map templateParam = new HashMap<>();
        templateParam.put("realname","JEECGuser");
        sysBaseAPI.sendTemplateAnnouncement(new TemplateMessageDTO(fromUser,toUser,title,templateParam,templateCode));
        //Business type
        String busType = SysAnnmentTypeEnum.EMAIL.getType();
        //businessID
        String busId = "11111";
        //send带business参数的模版消息
        BusTemplateMessageDTO busMessageDTO = new BusTemplateMessageDTO(fromUser, toUser, title, templateParam ,templateCode, busType,busId);
        sysBaseAPI.sendBusTemplateAnnouncement(busMessageDTO);
        //新Send template message
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setType(MessageTypeEnum.XT.getType());
        messageDTO.setToAll(false);
        messageDTO.setToUser(toUser);
        messageDTO.setTitle("【process error】");
        messageDTO.setFromUser("admin");
        HashMap data = new HashMap<>();
        data.put(CommonConstant.NOTICE_MSG_BUS_TYPE, "msg_node");
        messageDTO.setData(data);
        messageDTO.setContent("TEST:Process execution failed！Task node not found");
        sysBaseAPI.sendTemplateMessage(messageDTO);
    }
    /**
     * Send email
     */
    @Test
    public void sendEmailMsg() {
        String title = "【Schedule reminder】Your schedule task is about to start";
        String content = "TEST:Dear Mr. Wang，The concert you purchased will be this Sunday10：08The event will be held as scheduled at the National Center for the Performing Arts，Please bring your tickets and ID cards with you when you arrive.";
        String email = "250678106@qq.com";
        sysBaseAPI.sendEmailMsg(email,title,content);
    }
    /**
     * sendhtmlTemplate email
     */
    @Test
    public void sendTemplateEmailMsg() {
        String title = "Received a reminder";
        String email = "250678106@qq.com";
        JSONObject params = new JSONObject();
        params.put("bpm_name","Advanced settings");
        params.put("bpm_task","approver");
        params.put("datetime","2023-10-07 18:00:49");
        params.put("url","http://boot3.jeecg.com/message/template");
        params.put("remark","Come on, come on, come on, come on, come on, come on, come on, come on, come on, come on, come on, come on, come on, come on, come on, come on, come on, come on!");
        sysBaseAPI.sendHtmlTemplateEmail(email,title, EmailTemplateEnum.BPM_CUIBAN_EMAIL,params);
    }
    /**
     * send短信
     */
    @Test
    public void sendSms() throws ClientException {
        //Phone number
        String mobile = "159***";
        //Message template
        DySmsEnum templateCode = DySmsEnum.LOGIN_TEMPLATE_CODE;
        //Template required parameters
        JSONObject obj = new JSONObject();
        obj.put("code", "4XDP");
        DySmsHelper.sendSms(mobile, obj, templateCode);
    }
}
