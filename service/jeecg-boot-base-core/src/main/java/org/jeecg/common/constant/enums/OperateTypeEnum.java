package org.jeecg.common.constant.enums;

import org.jeecg.common.constant.CommonConstant;

/**
 * @Description: Operation type
 * @author: jeecg-boot
 * @date: 2022/3/31 10:05
 */
public enum OperateTypeEnum {

    /**
     * list
     */
    LIST(CommonConstant.OPERATE_TYPE_1, "list"),

    /**
     * New
     */
    ADD(CommonConstant.OPERATE_TYPE_2, "add"),

    /**
     * edit
     */
    EDIT(CommonConstant.OPERATE_TYPE_3, "edit"),

    /**
     * delete
     */
    DELETE(CommonConstant.OPERATE_TYPE_4, "delete"),

    /**
     * import
     */
    IMPORT(CommonConstant.OPERATE_TYPE_5, "import"),

    /**
     * Export
     */
    EXPORT(CommonConstant.OPERATE_TYPE_6, "export");

    /**
     * type 1list,2New,3edit,4delete,5import,6Export
     */
    int type;

    /**
     * coding(Request method)
     */
    String code;


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Constructor
     *
     * @param type type
     * @param code coding(Request method)
     */
    OperateTypeEnum(int type, String code) {
        this.type = type;
        this.code = code;
    }


    /**
     * Match based on request name
     *
     * @param methodName Request name
     * @return Integer type
     */
    public static Integer getTypeByMethodName(String methodName) {
        for (OperateTypeEnum e : OperateTypeEnum.values()) {
            if (methodName.startsWith(e.getCode())) {
                return e.getType();
            }
        }
        return CommonConstant.OPERATE_TYPE_1;
    }
}
