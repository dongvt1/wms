package org.jeecg.modules.warehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.warehouse.entity.Customer;

/**
 * @Description: Customer Mapper Interface
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
public interface CustomerMapper extends BaseMapper<Customer> {

    /**
     * Check if customer code is unique
     * @param customerCode Customer code
     * @param excludeId Exclude ID (for update)
     * @return Count of records with the same code
     */
    @Select("SELECT COUNT(*) FROM customers WHERE customer_code = #{customerCode} AND (#{excludeId} IS NULL OR id != #{excludeId})")
    int checkCodeUnique(@Param("customerCode") String customerCode, @Param("excludeId") String excludeId);

    /**
     * Get customer by code
     * @param customerCode Customer code
     * @return Customer entity
     */
    @Select("SELECT * FROM customers WHERE customer_code = #{customerCode}")
    Customer getByCode(@Param("customerCode") String customerCode);

    /**
     * Get active customers
     * @return List of active customers
     */
    @Select("SELECT * FROM customers WHERE status = 1 ORDER BY create_time DESC")
    java.util.List<Customer> getActiveCustomers();

    /**
     * Search customers by name or code
     * @param keyword Search keyword
     * @return List of matching customers
     */
    @Select("SELECT * FROM customers WHERE (customer_name LIKE CONCAT('%', #{keyword}, '%') OR customer_code LIKE CONCAT('%', #{keyword}, '%')) AND status = 1 ORDER BY customer_name")
    java.util.List<Customer> searchCustomers(@Param("keyword") String keyword);
}