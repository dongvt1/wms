package org.jeecg.modules.demo.test.service.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.jeecg.modules.demo.test.entity.JeecgOrderCustomer;
import org.jeecg.modules.demo.test.entity.JeecgOrderMain;
import org.jeecg.modules.demo.test.entity.JeecgOrderTicket;
import org.jeecg.modules.demo.test.mapper.JeecgOrderCustomerMapper;
import org.jeecg.modules.demo.test.mapper.JeecgOrderMainMapper;
import org.jeecg.modules.demo.test.mapper.JeecgOrderTicketMapper;
import org.jeecg.modules.demo.test.service.IJeecgOrderMainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: Order
 * @Author: jeecg-boot
 * @Date:  2019-02-15
 * @Version: V1.0
 */
@Service
public class JeecgOrderMainServiceImpl extends ServiceImpl<JeecgOrderMainMapper, JeecgOrderMain> implements IJeecgOrderMainService {

    @Autowired
    private JeecgOrderMainMapper jeecgOrderMainMapper;
    @Autowired
    private JeecgOrderCustomerMapper jeecgOrderCustomerMapper;
    @Autowired
    private JeecgOrderTicketMapper jeecgOrderTicketMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMain(JeecgOrderMain jeecgOrderMain, List<JeecgOrderCustomer> jeecgOrderCustomerList, List<JeecgOrderTicket> jeecgOrderTicketList) {
        jeecgOrderMainMapper.insert(jeecgOrderMain);
        if (jeecgOrderCustomerList != null) {
            for (JeecgOrderCustomer entity : jeecgOrderCustomerList) {
                entity.setOrderId(jeecgOrderMain.getId());
                jeecgOrderCustomerMapper.insert(entity);
            }
        }
        if (jeecgOrderTicketList != null) {
            for (JeecgOrderTicket entity : jeecgOrderTicketList) {
                entity.setOrderId(jeecgOrderMain.getId());
                jeecgOrderTicketMapper.insert(entity);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMain(JeecgOrderMain jeecgOrderMain, List<JeecgOrderCustomer> jeecgOrderCustomerList, List<JeecgOrderTicket> jeecgOrderTicketList) {
        jeecgOrderMainMapper.updateById(jeecgOrderMain);

        //1.Delete subtable data first
        jeecgOrderTicketMapper.deleteTicketsByMainId(jeecgOrderMain.getId());
        jeecgOrderCustomerMapper.deleteCustomersByMainId(jeecgOrderMain.getId());

        //2.Subtable data reinsertion
        if (jeecgOrderCustomerList != null) {
            for (JeecgOrderCustomer entity : jeecgOrderCustomerList) {
                entity.setOrderId(jeecgOrderMain.getId());
                jeecgOrderCustomerMapper.insert(entity);
            }
        }
        if (jeecgOrderTicketList != null) {
            for (JeecgOrderTicket entity : jeecgOrderTicketList) {
                entity.setOrderId(jeecgOrderMain.getId());
                jeecgOrderTicketMapper.insert(entity);
            }
        }
    }

    /**
     * One-to-many maintenance logic transformation  LOWCOD-315
     * @param jeecgOrderMain
     * @param jeecgOrderCustomerList
     * @param jeecgOrderTicketList
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCopyMain(JeecgOrderMain jeecgOrderMain, List<JeecgOrderCustomer> jeecgOrderCustomerList, List<JeecgOrderTicket> jeecgOrderTicketList) {
        jeecgOrderMainMapper.updateById(jeecgOrderMain);

        // Loop the data passed from the front desk
        for (JeecgOrderTicket ticket:jeecgOrderTicketList){
            // Query the subtable database first
            JeecgOrderTicket orderTicket = jeecgOrderTicketMapper.selectById(ticket.getId());
            if(orderTicket == null){
                // When it was passedidWhen the database does not exist，Description database does not exist，Use new logic
                ticket.setOrderId(jeecgOrderMain.getId());
                jeecgOrderTicketMapper.insert(ticket);
                continue;
            }
            if(orderTicket.getId().equals(ticket.getId())){
                // Passed overidand databaseidWhen the time comes，Indicates that the data exists in the database，Use update logic
                jeecgOrderTicketMapper.updateById(ticket);
            }
        }
        for (JeecgOrderCustomer customer:jeecgOrderCustomerList){
            // Query the subtable database first
            JeecgOrderCustomer customers = jeecgOrderCustomerMapper.selectById(customer.getId());
            if(customers == null){
                // When it was passedidWhen the database does not exist，Description database does not exist，Use new logic
                customer.setOrderId(jeecgOrderMain.getId());
                jeecgOrderCustomerMapper.insert(customer);
                continue;
            }
            if(customers.getId().equals(customer.getId())){
                //TODO Passed overidand databaseidWhen the time comes，Indicates that the data exists in the database，Use update logic
                jeecgOrderCustomerMapper.updateById(customer);
            }
        }
        // Take the difference set after adding new and deleted， When it was passediddoes not exist，while the database exists，Description deleted，Use deletion logic
        List<JeecgOrderTicket> jeecgOrderTickets = jeecgOrderTicketMapper.selectTicketsByMainId(jeecgOrderMain.getId());
        List<JeecgOrderTicket> collect = jeecgOrderTickets.stream()
                .filter(item -> !jeecgOrderTicketList.stream()
                .map(e -> e.getId())
                .collect(Collectors.toList())
                .contains(item.getId()))
                .collect(Collectors.toList());
        // forLoop deleteid
        for (JeecgOrderTicket ticket:collect){
            jeecgOrderTicketMapper.deleteById(ticket.getId());
        }

        List<JeecgOrderCustomer> jeecgOrderCustomers = jeecgOrderCustomerMapper.selectCustomersByMainId(jeecgOrderMain.getId());
        List<JeecgOrderCustomer> customersCollect = jeecgOrderCustomers.stream()
                .filter(item -> !jeecgOrderCustomerList.stream()
                        .map(e -> e.getId())
                        .collect(Collectors.toList())
                        .contains(item.getId()))
                .collect(Collectors.toList());
        //TODO forLoop deleteid
        for (JeecgOrderCustomer c:customersCollect){
            jeecgOrderCustomerMapper.deleteById(c.getId());
        }
    }
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void delMain(String id) {
		jeecgOrderMainMapper.deleteById(id);
		jeecgOrderTicketMapper.deleteTicketsByMainId(id);
		jeecgOrderCustomerMapper.deleteCustomersByMainId(id);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void delBatchMain(Collection<? extends Serializable> idList) {
		for(Serializable id:idList) {
			jeecgOrderMainMapper.deleteById(id);
			jeecgOrderTicketMapper.deleteTicketsByMainId(id.toString());
			jeecgOrderCustomerMapper.deleteCustomersByMainId(id.toString());
		}
	}

}
