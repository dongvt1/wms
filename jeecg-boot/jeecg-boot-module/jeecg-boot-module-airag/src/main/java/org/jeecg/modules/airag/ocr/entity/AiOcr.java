package org.jeecg.modules.airag.ocr.entity;

import lombok.Data;

/**
* @Description: OCRIdentify entity classes
*
* @author: wangshuai
* @date: 2025/4/16 17:01
*/
@Data
public class AiOcr {

    /**
     * serial number
     */
    private String id;
    
    /**
     * title
     */
    private String title;
    
    /**
     * prompt word
     */
    private String prompt;
    
}
