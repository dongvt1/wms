package org.jeecg.common.api.dto.message;

import lombok.Data;
import java.io.Serializable;
import java.util.Map;

/**
 * template message
 * @author: jeecg-boot
 */
@Data
public class TemplateMessageDTO extends TemplateDTO implements Serializable {

    private static final long serialVersionUID = 411137565170647585L;


    /**
     * sender(User login account)
     */
    protected String fromUser;

    /**
     * send to(User login account)
     */
    protected String toUser;

    /**
     * Message topic
     */
    protected String title;


    public TemplateMessageDTO(){

    }

    /**
     * Constructor1 发template message用
     */
    public TemplateMessageDTO(String fromUser, String toUser,String title, Map<String, String> templateParam, String templateCode){
        super(templateCode, templateParam);
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.title = title;
    }



}
