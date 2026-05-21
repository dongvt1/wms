package org.jeecg.modules.message.entity;

import java.io.Serializable;

import lombok.Data;

/**
 * Send message entity
 * @author: jeecg-boot
 */
@Data
public class MsgParams implements Serializable  {
	
	private static final long serialVersionUID = 1L;
	/**
     * Message type
     */
	private String msgType;

    /**
     * message receiver
     */
	private String receiver;

    /**
     * Message template code
     */
	private String templateCode;

    /**
     * test data
     */
	private String testData;
	
}
