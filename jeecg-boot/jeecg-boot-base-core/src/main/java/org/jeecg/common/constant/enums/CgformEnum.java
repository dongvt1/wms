package org.jeecg.common.constant.enums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * onlineform enum Code generator used
 * @author: jeecg-boot
 */
public enum CgformEnum {

    /**
     * Single table
     */
    ONE(1, "one", "/jeecg/code-template-online", "default.one", "classic style", new String[]{"vue3","vue","vue3Native"}),

    /**
     * Multiple tables
     */
    MANY(2, "many", "/jeecg/code-template-online", "default.onetomany", "classic style" ,new String[]{"vue"}),
    /**
     * Multiple tables（jvxestyle）
     *  */
    JVXE_TABLE(2, "jvxe", "/jeecg/code-template-online", "jvxe.onetomany", "默认style" ,new String[]{"vue3","vue","vue3Native"}),

    /**
     * Multiple tables (erpstyle)
     */
    ERP(2, "erp", "/jeecg/code-template-online", "erp.onetomany", "ERPstyle" ,new String[]{"vue3","vue","vue3Native"}),
    /**
     * Multiple tables（内嵌子表style）
     */
    INNER_TABLE(2, "innerTable", "/jeecg/code-template-online", "inner-table.onetomany", "内嵌子表style" ,new String[]{"vue3","vue"}),
    /**
     * Multiple tables（tabstyle）
     *  */
    TAB(2, "tab", "/jeecg/code-template-online", "tab.onetomany", "Tabstyle" ,new String[]{"vue3","vue"}),
    /**
     * tree list
     */
    TREE(3, "tree", "/jeecg/code-template-online", "default.tree", "tree list" ,new String[]{"vue3","vue","vue3Native"});

    /**
     * type 1/Single table 2/one to many 3/Tree
     */
    int type;
    /**
     * Coding identification
     */
    String code;
    /**
     * Code generator template path
     */
    String templatePath;
    /**
     * Code generator template path
     */
    String stylePath;
    /**
     * 模板style名称
     */
    String note;
    /**
     * 支持codestyle vue3:vue3wrapper code vue3Native:vue3native code vue:vue2code
     */
    String[] vueStyle;

    /**
     * Constructor
     *
     * @param type type 1/Single table 2/one to many 3/Tree
     * @param code template encoding
     * @param templatePath  template path
     * @param stylePath  template subpath
     * @param note
     * @param vueStyle 支持codestyle
     */
    CgformEnum(int type, String code, String templatePath, String stylePath, String note, String[] vueStyle) {
        this.type = type;
        this.code = code;
        this.templatePath = templatePath;
        this.stylePath = stylePath;
        this.note = note;
        this.vueStyle = vueStyle;
    }

    /**
     * according tocode获取template path
     *
     * @param code
     * @return
     */
    public static String getTemplatePathByConfig(String code) {
        return getCgformEnumByConfig(code).templatePath;
    }


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTemplatePath() {
        return templatePath;
    }

    public void setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
    }

    public String getStylePath() {
        return stylePath;
    }

    public void setStylePath(String stylePath) {
        this.stylePath = stylePath;
    }

    public String[] getVueStyle() {
        return vueStyle;
    }

    public void setVueStyle(String[] vueStyle) {
        this.vueStyle = vueStyle;
    }

    /**
     * according tocodeFind enumeration
     *
     * @param code
     * @return
     */
    public static CgformEnum getCgformEnumByConfig(String code) {
        for (CgformEnum e : CgformEnum.values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        return null;
    }

    /**
     * according totype找所有
     *
     * @param type
     * @return
     */
    public static List<Map<String, Object>> getJspModelList(int type) {
        List<Map<String, Object>> ls = new ArrayList<Map<String, Object>>();
        for (CgformEnum e : CgformEnum.values()) {
            if (e.type == type) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("code", e.code);
                map.put("note", e.note);
                ls.add(map);
            }
        }
        return ls;
    }


}
