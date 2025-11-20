package org.jeecg.common.constant.enums;

/**
 * Client terminal type
 */
public enum ClientTerminalTypeEnum {

    PC("pc", "computer terminal"),
    H5("h5", "Mobile web client"),
    APP("app", "cell phoneappend");

    private String key;
    private String text;

    ClientTerminalTypeEnum(String value, String text) {
        this.key = value;
        this.text = text;
    }

    public String getKey() {
        return this.key;
    }
}
