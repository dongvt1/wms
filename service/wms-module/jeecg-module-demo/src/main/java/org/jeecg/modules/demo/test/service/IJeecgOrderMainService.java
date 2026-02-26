package org.jeecg.modules.demo.test.service;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.jeecg.modules.demo.test.entity.JeecgOrderCustomer;
import org.jeecg.modules.demo.test.entity.JeecgOrderMain;
import org.jeecg.modules.demo.test.entity.JeecgOrderTicket;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: Order
 * @Author: jeecg-boot
 * @Date:  2019-02-15
 * @Version: V1.0
 */
public interface IJeecgOrderMainService extends IService<JeecgOrderMain> {

	/**
	 * Add one to many
	 * @param jeecgOrderMain Order实体类
     * @param jeecgOrderCustomerList Order客户gather
     * @param jeecgOrderTicketList Order机票gather
	 */
	public void saveMain(JeecgOrderMain jeecgOrderMain,List<JeecgOrderCustomer> jeecgOrderCustomerList,List<JeecgOrderTicket> jeecgOrderTicketList) ;
	
	/**
	 * Modify one-to-many
     * @param jeecgOrderMain Order实体类
     * @param jeecgOrderCustomerList Order客户gather
     * @param jeecgOrderTicketList Order机票gather
	 */
	public void updateMain(JeecgOrderMain jeecgOrderMain,List<JeecgOrderCustomer> jeecgOrderCustomerList,List<JeecgOrderTicket> jeecgOrderTicketList);
	
	/**
	 * Delete one to many
	 * @param id Orderid
	 */
	public void delMain (String id);
	
	/**
	 * 批量Delete one to many
	 * @param idList Orderidgather
	 */
	public void delBatchMain (Collection<? extends Serializable> idList);

    /**
     * Modify one-to-many
     * @param jeecgOrderMain Order实体类
     * @param jeecgOrderCustomerList Order客户gather
     * @param jeecgOrderTicketList Order机票gather
     */
	public void updateCopyMain(JeecgOrderMain jeecgOrderMain, List<JeecgOrderCustomer> jeecgOrderCustomerList, List<JeecgOrderTicket> jeecgOrderTicketList);
}
