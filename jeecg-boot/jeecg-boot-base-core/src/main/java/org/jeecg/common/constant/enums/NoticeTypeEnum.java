package org.jeecg.common.constant.enums;

/**
* @Description: File type enumeration class
*
* @author: wangshuai
* @date: 2025/6/26 17:29
*/
public enum NoticeTypeEnum {
    
    //VUE3dedicated
    NOTICE_TYPE_FILE("Knowledge base news","file"),
    NOTICE_TYPE_FLOW("Workflow messages","flow"),
    NOTICE_TYPE_PLAN("schedule news","plan"),
    //Not used yet
    NOTICE_TYPE_MEETING("Meeting news","meeting"),
    NOTICE_TYPE_SYSTEM("System messages","system"),
    /**
     * collaborative work
     * for [JHHB-136]【vue3】collaborative workSystem messages需要添加一个类型
     */
    NOTICE_TYPE_COLLABORATION("collaborative work", "collab"),
    /**
     * supervise
     */
    NOTICE_TYPE_SUPERVISE("supervise管理", "supe");

    /**
     * File type name
     */
    private String name;

    /**
     * file type value
     */
    private String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    NoticeTypeEnum(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Get chat notification type
     * 
     * @param value
     * @return
     */
    public static String getChatNoticeType(String value){
        return value + "Notice";
    }

    /**
     * Get notification name
     *
     * @param value
     * @return
     */
    public static String getNoticeNameByValue(String value){
        value = value.replace("Notice","");
        for (NoticeTypeEnum e : NoticeTypeEnum.values()) {
            if (e.getValue().equals(value)) {
                return e.getName();
            }
        }
        return "System messages";
    }
}
