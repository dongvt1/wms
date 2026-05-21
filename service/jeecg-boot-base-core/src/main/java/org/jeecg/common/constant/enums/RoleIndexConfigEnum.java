package org.jeecg.common.constant.enums;

import org.jeecg.common.util.oConvertUtils;

import java.util.List;

/**
 * Home page customization
 * Configuration through role coding and homepage component path
 * The order of enumeration is affected by the high and low weight of permissions.（That is to configure multiple roles，In the previous role homepage，Will take effect first）
 * @author: jeecg-boot
 */
public enum RoleIndexConfigEnum {

    /**Home page customization admin*/
//    ADMIN("admin", "dashboard/Analysis"),
    //TEST("test",  "dashboard/IndexChart"),
    /**Home page customization hr*/
//    HR("hr", "dashboard/IndexBdc");
  
    //DM("dm", "dashboard/IndexTask"),

    // Note：This value is only to prevent errors，without any practical significance
    ROLE_INDEX_CONFIG_ENUM("RoleIndexConfigEnumDefault", "dashboard/Analysis");

    /**
     * role coding
     */
    String roleCode;
    /**
     * routingindex
     */
    String componentUrl;

    /**
     * Constructor
     *
     * @param roleCode role coding
     * @param componentUrl Home page component path（The rules are the same as the menu configuration）
     */
    RoleIndexConfigEnum(String roleCode, String componentUrl) {
        this.roleCode = roleCode;
        this.componentUrl = componentUrl;
    }
    /**
     * according tocodeFind enumeration
     * @param roleCode role coding
     * @return
     */
    private static RoleIndexConfigEnum getEnumByCode(String roleCode) {
        for (RoleIndexConfigEnum e : RoleIndexConfigEnum.values()) {
            if (e.roleCode.equals(roleCode)) {
                return e;
            }
        }
        return null;
    }
    /**
     * according tocodetry to findindex
     * @param roleCode role coding
     * @return
     */
    private static String getIndexByCode(String roleCode) {
        for (RoleIndexConfigEnum e : RoleIndexConfigEnum.values()) {
            if (e.roleCode.equals(roleCode)) {
                return e.componentUrl;
            }
        }
        return null;
    }

    public static String getIndexByRoles(List<String> roles) {
        String[] rolesArray = roles.toArray(new String[roles.size()]);
        for (RoleIndexConfigEnum e : RoleIndexConfigEnum.values()) {
            if (oConvertUtils.isIn(e.roleCode,rolesArray)){
                return e.componentUrl;
            }
        }
        return null;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public String getComponentUrl() {
        return componentUrl;
    }

    public void setComponentUrl(String componentUrl) {
        this.componentUrl = componentUrl;
    }
}
