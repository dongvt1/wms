package org.jeecg.modules.demo.test.service;

import java.util.List;

import org.jeecg.modules.demo.test.entity.JeecgOrderCustomer;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: order customer
 * @Author: jeecg-boot
 * @Date:  2019-02-15
 * @Version: V1.0
 */
public interface IJeecgOrderCustomerService extends IService<JeecgOrderCustomer> {

    /**
     * According to orderid获取order customer数据
     * @param mainId Orderid
     * @return Order顾客集合
     */
	public List<JeecgOrderCustomer> selectCustomersByMainId(String mainId);
}
