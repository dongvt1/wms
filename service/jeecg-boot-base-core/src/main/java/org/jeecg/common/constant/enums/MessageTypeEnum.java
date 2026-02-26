package org.jeecg.common.constant.enums;

import org.jeecg.common.system.annotation.EnumDict;
import org.jeecg.common.system.vo.DictModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Message type
 *
 * @author: jeecg-boot
 */
@EnumDict("messageType")
public enum MessageTypeEnum {

    /**
     * System messages
     */
    XT("system", "System messages"),
    /**
     * Email message
     */
    YJ("email", "Email message"),
    /**
     * DingTalk news
     */
    DD("dingtalk", "DingTalk news"),
    /**
     * Enterprise WeChat
     */
    QYWX("wechat_enterprise", "Enterprise WeChat");

    MessageTypeEnum(String type, String note) {
        this.type = type;
        this.note = note;
    }

    /**
     * Message type
     */
    String type;

    /**
     * Type description
     */
    String note;

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    /**
     * Get dictionary data
     *
     * @return
     */
    public static List<DictModel> getDictList() {
        List<DictModel> list = new ArrayList<>();
        DictModel dictModel = null;
        for (MessageTypeEnum e : MessageTypeEnum.values()) {
            dictModel = new DictModel();
            dictModel.setValue(e.getType());
            dictModel.setText(e.getNote());
            list.add(dictModel);
        }
        return list;
    }

    /**
     * according totypeGet enumeration
     *
     * @param type
     * @return
     */
    public static MessageTypeEnum valueOfType(String type) {
        for (MessageTypeEnum e : MessageTypeEnum.values()) {
            if (e.getType().equals(type)) {
                return e;
            }
        }
        return null;
    }

}
