package org.jeecg.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.config.mqtoken.UserTokenContext;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: System data log
 * @author: jeecg-boot
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Slf4j
public class SysDataLog implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@TableId(type = IdType.ASSIGN_ID)
    /**
     * id
     */
	private String id;

    /**
     * Creator login name
     */
	private String createBy;

    /**
     * Creator’s real name
     */
	private String createName;

    /**
     * Creation date
     */
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date createTime;

    /**
     * Updater login name
     */
	private String updateBy;
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")

    /**
     * Update date
     */
    private Date updateTime;

    /**
     * table name
     */
    private String dataTable;

    /**
     * dataID
     */
    private String dataId;

    /**
     * data内容
     */
    private String dataContent;

    /**
     * version number
     */
    private String dataVersion;


    //update-begin-author:taoyan date:2022-7-26 for: Used to log form comments 区分data
    /**
     * type
     */
    private String type;
    //update-end-author:taoyan date:2022-7-26 for: Used to log form comments 区分data

    /**
     * pass loginUser set up createName
     */
    public void autoSetCreateName() {
        try {
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            this.setCreateName(sysUser.getRealname());
        } catch (Exception e) {
            // QQYUN-13669 Further optimization：Solve the problem of obtaining empty user information in some asynchronous scenarios
            String token = UserTokenContext.getToken();
            if (StringUtils.hasText(token)) {
                this.setCreateName(JwtUtil.getUsername(token));
            } else {
                log.warn("SecurityUtils.getSubject() Exception in obtaining user information：" + e.getMessage());
            }
        }
    }

}
