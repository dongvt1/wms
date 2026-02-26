package org.jeecg.common.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.desensitization.annotation.SensitiveField;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * <p>
 * Online user information
 * </p>
 *
 * @Author scott
 * @since 2023-08-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class UserAccountInfo {

    /**
     * Login personid
     */
    private String id;

    /**
     * Login person账号
     */
    private String username;

    /**
     * Login person名字
     */
    private String realname;

    /**
     * e-mail
     */
    private String email;

    /**
     * avatar
     */
    @SensitiveField
    private String avatar;

    /**
     * Sync workflow engine1synchronous0不synchronous
     */
    private Integer activitiSync;

    /**
     * Telephone
     */
    @SensitiveField
    private String phone;
}
