package org.jeecg.modules.warehouse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.warehouse.entity.CustomerBalance;
import org.jeecg.modules.warehouse.mapper.CustomerBalanceMapper;
import org.jeecg.modules.warehouse.service.ICustomerBalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * @Description: Customer Balance Service Implementation
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
@Slf4j
@Service
public class CustomerBalanceServiceImpl extends ServiceImpl<CustomerBalanceMapper, CustomerBalance> implements ICustomerBalanceService {

    @Autowired
    private CustomerBalanceMapper customerBalanceMapper;

    @Override
    public CustomerBalance getByCustomerId(String customerId) {
        return customerBalanceMapper.getByCustomerId(customerId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateBalance(String customerId, BigDecimal amount, String updatedBy) {
        try {
            int affectedRows = customerBalanceMapper.updateBalance(customerId, amount, updatedBy);
            if (affectedRows > 0) {
                log.info("Updated balance for customer {}: {} by {}", customerId, amount, updatedBy);
                return true;
            } else {
                log.warn("Failed to update balance for customer {}: no rows affected", customerId);
                return false;
            }
        } catch (Exception e) {
            log.error("Error updating balance for customer {}: {}", customerId, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setBalance(String customerId, BigDecimal balance, String updatedBy) {
        try {
            int affectedRows = customerBalanceMapper.setBalance(customerId, balance, updatedBy);
            if (affectedRows > 0) {
                log.info("Set balance for customer {} to {} by {}", customerId, balance, updatedBy);
                return true;
            } else {
                log.warn("Failed to set balance for customer {}: no rows affected", customerId);
                return false;
            }
        } catch (Exception e) {
            log.error("Error setting balance for customer {}: {}", customerId, e.getMessage());
            throw e;
        }
    }
}