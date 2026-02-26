package org.jeecg.common.api.dto.message;

import lombok.Data;
import org.jeecg.common.constant.CommonConstant;

import java.io.Serializable;
import java.util.*;

/**
 * General news
 * @author: jeecg-boot
 */
@Data
public class MessageDTO implements Serializable {
    private static final long serialVersionUID = -5690444483968058442L;
    
    /**
     * sender(User login account)
     */
    protected String fromUser;

    /**
     * send to(User login account)
     */
    protected String toUser;

    /**
     * send to所有人
     */
    protected Boolean toAll;

    /**
     * Message topic
     */
    protected String title;

    /**
     * Message content
     */
    protected String content;

    /**
     * Message type 1:information  2:系统information
     */
    protected String category;
    
    /**
     * Message type：org.jeecg.common.constant.enums.MessageTypeEnum
     *  XT("system",  "系统information")
     *  YJ("email",  "邮件information")
     *  DD("dingtalk", "钉钉information")
     *  QYWX("wechat_enterprise", "Enterprise WeChat")
     */
    protected String type;
    

    //---【Push template related parameters】-------------------------------------------------------------
    /**
     * Whether to sendMarkdown格式的information
     */
    protected boolean isMarkdown;
    /**
     * 模板information对应的模板编码
     */
    protected String templateCode;
    /**
     * Parse template content Corresponding data
     */
    protected Map<String, Object> data;
    //---【Push template related parameters】-------------------------------------------------------------

    //---【Email related parameters】-------------------------------------------------------------
    /**
     * Email copy
     */
    private String copyToUser;

    /**
     * Email push address
     */
    protected Set<String> toEmailList;

    /**
     * Email CC address
     */
    protected Set<String> ccEmailList;

    /**
     * Whether to push for scheduled tasksemail
     */
    private Boolean isTimeJob = false;
    
    //---【Email related parameters】-------------------------------------------------------------

    /**
     * enumerate：org.jeecg.common.constant.enums.NoticeTypeEnum
     * notification type(system:系统information、file:knowledge base、flow:process、plan:schedule、meeting:Meeting)
     */
    private String noticeType;
    
    public MessageDTO(){
    }

    /**
     * Constructor1 系统information
     */
    public MessageDTO(String fromUser,String toUser,String title, String content){
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.title = title;
        this.content = content;
        //default All2系统information
        this.category = CommonConstant.MSG_CATEGORY_2;
    }

    /**
     * Constructor2 Support settingscategory 1:information  2:系统information
     */
    public MessageDTO(String fromUser,String toUser,String title, String content, String category){
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.title = title;
        this.content = content;
        this.category = category;
    }

    public boolean isMarkdown() {
        return this.isMarkdown;
    }

    public void setIsMarkdown(boolean isMarkdown) {
        this.isMarkdown = isMarkdown;
    }
}
