package org.jeecg.common.api.dto;
import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecg.common.system.vo.LoginUser;
import java.io.Serializable;
import java.util.Date;

/**
 * log object
 * cloud api The interface transfer object used
 * @author: jeecg-boot
 */
@Data
public class LogDTO implements Serializable {

    private static final long serialVersionUID = 8482720462943906924L;

    /**content*/
    private String logContent;

    /**Log type(0:Operation log;1:Login log;2:scheduled tasks)  */
    private Integer logType;

    /**Operation type(1:Add to;2:Revise;3:delete;) */
    private Integer operateType;

    /**Login user */
    private LoginUser loginUser;

    private String id;
    private String createBy;
    private Date createTime;
    private Long costTime;
    private String ip;

    /**Request parameters */
    private String requestParam;

    /**Request type*/
    private String requestType;

    /**Request path*/
    private String requestUrl;

    /**Request method */
    private String method;

    /**Operator username*/
    private String username;

    /**Operator user account*/
    private String userid;

    /**
     * tenantID
     */
    private Integer tenantId;

    /**
     * Client terminal type pc:PC app:Mobile version h5:Mobile web client
     */
    private String clientType;
    
    public LogDTO(){

    }

    public LogDTO(String logContent, Integer logType, Integer operatetype){
        this.logContent = logContent;
        this.logType = logType;
        this.operateType = operatetype;
    }

    public LogDTO(String logContent, Integer logType, Integer operatetype, LoginUser loginUser){
        this.logContent = logContent;
        this.logType = logType;
        this.operateType = operatetype;
        this.loginUser = loginUser;
    }
}
