package org.jeecg.modules.demo.test.vo;

import java.util.List;

import org.jeecg.modules.demo.test.entity.JeecgOrderCustomer;
import org.jeecg.modules.demo.test.entity.JeecgOrderTicket;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecgframework.poi.excel.annotation.ExcelCollection;

import lombok.Data;

/**
 * @Description: One-to-many example
 * @author: jeecg-boot
 */
@Data
public class JeecgOrderMainPage {
	
	/**primary key*/
	private java.lang.String id;
	/**Order number*/
	@Excel(name="Order number",width=15)
	private java.lang.String orderCode;
	/**Order type*/
	private java.lang.String ctype;
	/**order date*/
	@Excel(name="order date",width=15,format = "yyyy-MM-dd")
	private java.util.Date orderDate;
	/**Order amount*/
	@Excel(name="Order amount",width=15)
	private java.lang.Double orderMoney;
	/**Order notes*/
	private java.lang.String content;
	/**Creator*/
	private java.lang.String createBy;
	/**creation time*/
	private java.util.Date createTime;
	/**Modifier*/
	private java.lang.String updateBy;
	/**modification time*/
	private java.util.Date updateTime;
	
	@ExcelCollection(name="client")
	private List<JeecgOrderCustomer> jeecgOrderCustomerList;
	@ExcelCollection(name="air tickets")
	private List<JeecgOrderTicket> jeecgOrderTicketList;

	private String bpmStatus;
	
}
