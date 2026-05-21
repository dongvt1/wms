package org.jeecg.modules.system.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: user rolevo
 * @author: jeecg-boot
 */
@Data
public class SysUserRoleVO implements Serializable{
	private static final long serialVersionUID = 1L;

	/**departmentid*/
	private String roleId;
	/**Corresponding useridgather*/
	private List<String> userIdList;

	public SysUserRoleVO() {
		super();
	}

	public SysUserRoleVO(String roleId, List<String> userIdList) {
		super();
		this.roleId = roleId;
		this.userIdList = userIdList;
	}

}
