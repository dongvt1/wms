package org.jeecg.modules.demo.test.service;

import java.util.List;

import org.jeecg.modules.demo.test.entity.JeecgOrderTicket;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: Order air ticket
 * @Author: jeecg-boot
 * @Date:  2019-02-15
 * @Version: V1.0
 */
public interface IJeecgOrderTicketService extends IService<JeecgOrderTicket> {

    /**
     * by orderid查询Order air ticket
     * @param mainId Orderid
     * @return Order air ticket集合
     */
	public List<JeecgOrderTicket> selectTicketsByMainId(String mainId);
}
