package org.jeecg.modules.demo.test.entity;

import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;

import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @Description: order customer
 * @Author: jeecg-boot
 * @Date:  2019-02-15
 * @Version: V1.0
 */
@Data
@TableName("jeecg_order_customer")
public class JeecgOrderCustomer implements Serializable {
    private static final long serialVersionUID = 1L;
    
	/**primary key*/
    @TableId(type = IdType.ASSIGN_ID)
	private java.lang.String id;
	/**Customer name*/
	@Excel(name="Customer name字",width=15)
	private java.lang.String name;
	/**gender*/
	private java.lang.String sex;
	/**ID number*/
	@Excel(name="ID number",width=15)
	private java.lang.String idcard;
	/**Scanned copy of ID card*/
	private java.lang.String idcardPic;
	/**Telephone1*/
	@Excel(name="Telephone",width=15)
	private java.lang.String telphone;
	/**foreign key*/
	private java.lang.String orderId;
	/**Creator*/
	private java.lang.String createBy;
	/**creation time*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private java.util.Date createTime;
	/**Modifier*/
	private java.lang.String updateBy;
	/**modification time*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private java.util.Date updateTime;
}
