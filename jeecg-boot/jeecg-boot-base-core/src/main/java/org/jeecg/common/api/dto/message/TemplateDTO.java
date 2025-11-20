package org.jeecg.common.api.dto.message;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * Message templatedto
 * @author: jeecg-boot
 */
@Data
public class TemplateDTO implements Serializable {

    private static final long serialVersionUID = 5848247133907528650L;

    /**
     * template encoding
     */
    protected String templateCode;

    /**
     * template parameters
     */
    protected Map<String, String> templateParam;

    /**
     * Constructor 通过设置template parameters和template encoding Get message content as parameter
     */
    public TemplateDTO(String templateCode, Map<String, String> templateParam){
        this.templateCode = templateCode;
        this.templateParam = templateParam;
    }

    public TemplateDTO(){

    }
}
