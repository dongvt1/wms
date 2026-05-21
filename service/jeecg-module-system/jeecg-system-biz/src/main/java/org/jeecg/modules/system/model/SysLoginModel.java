package org.jeecg.modules.system.model;


import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Login form
 *
 * @Author scott
 * @since  2019-01-18
 */
@Schema(description="Login object")
public class SysLoginModel {
	@Schema(description = "account")
    private String username;
	@Schema(description = "password")
    private String password;
	@Schema(description = "Verification code")
    private String captcha;
	@Schema(description = "Verification codekey")
    private String checkKey;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }

	public String getCheckKey() {
		return checkKey;
	}

	public void setCheckKey(String checkKey) {
		this.checkKey = checkKey;
	}
    
}