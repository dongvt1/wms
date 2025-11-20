package org.jeecg.modules.warehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.warehouse.entity.CustomerBalance;

import java.math.BigDecimal;

/**
 * @Description: Customer Balance Service Interface
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
public interface ICustomerBalanceService extends IService<CustomerBalance> {

    /**
     * Get customer balance by customer ID
     * @param customerId Customer ID
     * @return Customer balance entity
     */
    CustomerBalance getByCustomerId(String customerId);

    /**
     * Update customer balance
     * @param customerId Customer ID
     * @param amount Amount to add (positive) or subtract (negative)
     * @param updatedBy User who updated
     * @return True if successful, false otherwise
     */
    boolean updateBalance(String customerId, BigDecimal amount, String updatedBy);

    /**
     * Set customer balance to specific amount
     * @param customerId Customer ID
     * @param balance New balance amount
     * @param updatedBy User who updated
     * @return True if successful, false otherwise
     */
    boolean setBalance(String customerId, BigDecimal balance, String updatedBy);
}