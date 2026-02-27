package com.cy.modules.warehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import com.cy.modules.warehouse.entity.CustomerBalance;

import java.math.BigDecimal;

/**
 * @Description: Mapper interface số dư khách hàng
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
public interface CustomerBalanceMapper extends BaseMapper<CustomerBalance> {

    /**
     * Lấy số dư theo ID khách hàng
     * @param customerId ID khách hàng
     * @return Đối tượng số dư khách hàng
     */
    @Select("SELECT * FROM customer_balances WHERE customer_id = #{customerId}")
    CustomerBalance getByCustomerId(@Param("customerId") String customerId);

    /**
     * Cập nhật số dư khách hàng
     * @param customerId ID khách hàng
     * @param amount Số tiền cộng (dương) hoặc trừ (âm)
     * @param updatedBy Người cập nhật
     * @return Số dòng bị ảnh hưởng
     */
    @Update("UPDATE customer_balances SET balance = balance + #{amount}, last_updated = NOW(), updated_by = #{updatedBy} WHERE customer_id = #{customerId}")
    int updateBalance(@Param("customerId") String customerId, @Param("amount") BigDecimal amount, @Param("updatedBy") String updatedBy);

    /**
     * Thiết lập số dư khách hàng về một giá trị cụ thể
     * @param customerId ID khách hàng
     * @param balance Số dư mới
     * @param updatedBy Người cập nhật
     * @return Số dòng bị ảnh hưởng
     */
    @Update("UPDATE customer_balances SET balance = #{balance}, last_updated = NOW(), updated_by = #{updatedBy} WHERE customer_id = #{customerId}")
    int setBalance(@Param("customerId") String customerId, @Param("balance") BigDecimal balance, @Param("updatedBy") String updatedBy);
}