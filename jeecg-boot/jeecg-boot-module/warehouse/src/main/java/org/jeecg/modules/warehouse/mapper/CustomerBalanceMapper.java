package org.jeecg.modules.warehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.jeecg.modules.warehouse.entity.CustomerBalance;

import java.math.BigDecimal;

/**
 * @Description: Customer Balance Mapper Interface
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
public interface CustomerBalanceMapper extends BaseMapper<CustomerBalance> {

    /**
     * Get customer balance by customer ID
     * @param customerId Customer ID
     * @return Customer balance entity
     */
    @Select("SELECT * FROM customer_balances WHERE customer_id = #{customerId}")
    CustomerBalance getByCustomerId(@Param("customerId") String customerId);

    /**
     * Update customer balance
     * @param customerId Customer ID
     * @param amount Amount to add (positive) or subtract (negative)
     * @param updatedBy User who updated
     * @return Number of affected rows
     */
    @Update("UPDATE customer_balances SET balance = balance + #{amount}, last_updated = NOW(), updated_by = #{updatedBy} WHERE customer_id = #{customerId}")
    int updateBalance(@Param("customerId") String customerId, @Param("amount") BigDecimal amount, @Param("updatedBy") String updatedBy);

    /**
     * Set customer balance to specific amount
     * @param customerId Customer ID
     * @param balance New balance amount
     * @param updatedBy User who updated
     * @return Number of affected rows
     */
    @Update("UPDATE customer_balances SET balance = #{balance}, last_updated = NOW(), updated_by = #{updatedBy} WHERE customer_id = #{customerId}")
    int setBalance(@Param("customerId") String customerId, @Param("balance") BigDecimal balance, @Param("updatedBy") String updatedBy);
}