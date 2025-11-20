package org.jeecg.modules.system.model;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Title: DuplicateCheckVo
 * @Description: Repeat checkVO
 * @Author Zhang Daihao
 * @Date 2019-03-25
 * @Version V1.0
 */
@Data
@Schema(description="Repeat checkdata模型")
public class DuplicateCheckVo implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * table name
	 */
	@Schema(description="table name",name="tableName",example="sys_log")
	private String tableName;
	
	/**
	 * Field name
	 */
	@Schema(description="Field name",name="fieldName",example="id")
	private String fieldName;
	
	/**
	 * field value
	 */
	@Schema(description="field value",name="fieldVal",example="1000")
	private String fieldVal;
	
	/**
	 * dataID
	*/
	@Schema(description="dataID",name="dataId",example="2000")
	private String dataId;

}