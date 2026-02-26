package org.jeecg.modules.message.handle.enums;

/**
 * Push status enum
 * @author: jeecg-boot
 */
public enum SendMsgStatusEnum {

//push status 0Not pushed 1Push successful 2Push failed
	WAIT("0"), SUCCESS("1"), FAIL("2");

	private String code;

	private SendMsgStatusEnum(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public void setStatusCode(String code) {
		this.code = code;
	}

}