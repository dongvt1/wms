package org.jeecg.common.constant.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * @Description: SMS enumeration class
 * @author: jeecg-boot
 */
public enum DySmsEnum {

    /**Login SMS template encoding*/
    LOGIN_TEMPLATE_CODE("SMS_175435174","Knock knock cloud","code"),
    /**Forgot password SMS template encoding*/
    FORGET_PASSWORD_TEMPLATE_CODE("SMS_175435174","Knock knock cloud","code"),
    /**Change password SMS template encoding*/
    CHANGE_PASSWORD_TEMPLATE_CODE("SMS_465391221","Knock knock cloud","code"),
    /**Register account SMS template coding*/
    REGISTER_TEMPLATE_CODE("SMS_175430166","Knock knock cloud","code");
	
	/**
	 * SMS template encoding
	 */
	private String templateCode;
	/**
	 * sign
	 */
	private String signName;
	/**
	 * Required data name for SMS template，Multiplekeyseparated by commas，Configure here as verification
	 */
	private String keys;
	
	private DySmsEnum(String templateCode,String signName,String keys) {
		this.templateCode = templateCode;
		this.signName = signName;
		this.keys = keys;
	}
	
	public String getTemplateCode() {
		return templateCode;
	}
	
	public void setTemplateCode(String templateCode) {
		this.templateCode = templateCode;
	}
	
	public String getSignName() {
		return signName;
	}
	
	public void setSignName(String signName) {
		this.signName = signName;
	}
	
	public String getKeys() {
		return keys;
	}

	public void setKeys(String keys) {
		this.keys = keys;
	}

	public static DySmsEnum toEnum(String templateCode) {
		if(StringUtils.isEmpty(templateCode)){
			return null;
		}
		for(DySmsEnum item : DySmsEnum.values()) {
			if(item.getTemplateCode().equals(templateCode)) {
				return item;
			}
		}
		return null;
	}
}

