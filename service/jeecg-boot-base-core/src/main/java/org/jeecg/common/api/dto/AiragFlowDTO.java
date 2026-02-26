package org.jeecg.common.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * callAIProcess input parameters
 * for [QQYUN-13634]existbaseapiEncapsulation method inside，方便其他模块call
 * @author chenrui
 * @date 2025/9/2 14:11
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AiragFlowDTO implements Serializable {


    private static final long serialVersionUID = 7431775881170684867L;

    /**
     * processid
     */
    private String flowId;


    /**
     * input parameters
     */
    private Map<String, Object> inputParams;
}
