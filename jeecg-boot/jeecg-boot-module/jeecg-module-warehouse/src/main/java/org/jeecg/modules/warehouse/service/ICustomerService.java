package org.jeecg.modules.warehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.warehouse.entity.Customer;

import java.util.List;

/**
 * @Description: Customer Service Interface
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
public interface ICustomerService extends IService<Customer> {

    /**
     * Check if customer code is unique
     * @param customerCode Customer code
     * @param excludeId Exclude ID (for update)
     * @return True if unique, false otherwise
     */
    boolean isCodeUnique(String customerCode, String excludeId);

    /**
     * Get customer by code
     * @param customerCode Customer code
     * @return Customer entity
     */
    Customer getByCode(String customerCode);

    /**
     * Get active customers
     * @return List of active customers
     */
    List<Customer> getActiveCustomers();

    /**
     * Search customers by name or code
     * @param keyword Search keyword
     * @return List of matching customers
     */
    List<Customer> searchCustomers(String keyword);

    /**
     * Get customer order history
     * @param customerId Customer ID
     * @return List of customer orders
     */
    List<Object> getCustomerOrderHistory(String customerId);

    /**
     * Get customer statistics
     * @param customerId Customer ID
     * @return Customer statistics object
     */
    Object getCustomerStatistics(String customerId);
}