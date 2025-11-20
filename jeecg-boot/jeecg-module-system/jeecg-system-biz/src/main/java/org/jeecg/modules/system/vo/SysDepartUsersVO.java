package org.jeecg.modules.system.vo;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * @Description: System departmentVO
 * @author: jeecg-boot
 */
@Data
public class SysDepartUsersVO implements Serializable{
	private static final long serialVersionUID = 1L;
	
	/**departmentid*/
	private String depId;
	/**Corresponding useridgather*/
	private List<String> userIdList;
	public SysDepartUsersVO(String depId, List<String> userIdList) {
		super();
		this.depId = depId;
		this.userIdList = userIdList;
	}
    //update-begin--Author:kangxiaolin  Date:20190908 for：[512][department管理]Click to add existing user failed to fix--------------------

	public SysDepartUsersVO(){

	}
    //update-begin--Author:kangxiaolin  Date:20190908 for：[512][department管理]Click to add existing user failed to fix--------------------

}
