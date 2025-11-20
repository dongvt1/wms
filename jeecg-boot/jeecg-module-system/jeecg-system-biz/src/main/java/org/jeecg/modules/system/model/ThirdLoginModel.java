package org.jeecg.modules.system.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Third party login information storage
 * @author: jeecg-boot
 */
@Data
public class ThirdLoginModel implements Serializable {
    private static final long serialVersionUID = 4098628709290780891L;

    /**
     * Third party login source
     */
    private String source;

    /**
     * Third party login uuid
     */
    private String uuid;

    /**
     * Third party login username
     */
    private String username;

    /**
     * Third party login avatar
     */
    private String avatar;

    /**
     * account 后缀Third party login 防止account重复
     */
    private String suffix;

    /**
     * opcode Prevent from being attacked
     */
    private String operateCode;

    public ThirdLoginModel(){

    }

    /**
     * Constructor
     * @param source
     * @param uuid
     * @param username
     * @param avatar
     */
    public ThirdLoginModel(String source,String uuid,String username,String avatar){
        this.source = source;
        this.uuid = uuid;
        this.username = username;
        this.avatar = avatar;
    }

    /**
     * 获取登录account名
     * @return
     */
    public String getUserLoginAccount(){
        if(suffix==null){
            return this.uuid;
        }
        return this.uuid + this.suffix;
    }
}
