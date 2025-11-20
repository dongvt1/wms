package org.jeecg.common.constant.enums;

import org.jeecg.common.util.oConvertUtils;

/**
 * System announcement custom jump method
 * @author: jeecg-boot
 */
public enum SysAnnmentTypeEnum {
    /**
     * Email jump component
     */
    EMAIL("email", "component", "modules/eoa/email/modals/EoaEmailInForm"),
    /**
     * The process jumps to my task
     */
    BPM("bpm", "url", "/bpm/task/MyTaskList"),
    
    /**
     * process copy task
     */
    BPM_VIEW("bpm_cc", "url", "/bpm/task/MyTaskList"),
    /**
     * Invite users to jump to personal settings
     */
    TENANT_INVITE("tenant_invite", "url", "/system/usersetting"),
    /**
     * collaborative work-To-do notification
     * for [JHHB-136]【vue3】collaborative work系统消息需要添加一个类型
     */
    EOA_CO_NOTIFY("eoa_co_notify", "url", "/collaboration/pending"),
    /**
     * collaborative work-reminder notice
     * for [JHHB-136]【vue3】collaborative work系统消息需要添加一个类型
     */
    EOA_CO_REMIND("eoa_co_remind", "url", "/collaboration/pending"),
    /**
     * Supervision and management-urge
     */
    EOA_SUP_REMIND("eoa_sup_remind", "url", "/superivse/list"),
    /**
     * Supervision and management-notify
     */
    EOA_SUP_NOTIFY("eoa_sup_notify", "url", "/superivse/list");

    /**
     * Business type(email:mail bpm:process)
     */
    private String type;
    /**
     * Open method components：component routing：url
     */
    private String openType;
    /**
     * components/routing address
     */
    private String openPage;

    SysAnnmentTypeEnum(String type, String openType, String openPage) {
        this.type = type;
        this.openType = openType;
        this.openPage = openPage;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOpenType() {
        return openType;
    }

    public void setOpenType(String openType) {
        this.openType = openType;
    }

    public String getOpenPage() {
        return openPage;
    }

    public void setOpenPage(String openPage) {
        this.openPage = openPage;
    }

    public static SysAnnmentTypeEnum getByType(String type) {
        if (oConvertUtils.isEmpty(type)) {
            return null;
        }
        for (SysAnnmentTypeEnum val : values()) {
            if (val.getType().equals(type)) {
                return val;
            }
        }
        return null;
    }
}
