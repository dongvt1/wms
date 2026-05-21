package com.cy.modules.warehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.cy.modules.warehouse.entity.Customer;

/**
 * @Description: Mapper interface khách hàng
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
public interface CustomerMapper extends BaseMapper<Customer> {

    /**
     * Kiểm tra mã khách hàng có duy nhất không
     * @param customerCode Mã khách hàng
     * @param excludeId ID loại trừ (khi cập nhật)
     * @return Số bản ghi trùng mã
     */
    @Select("SELECT COUNT(*) FROM customers WHERE customer_code = #{customerCode} AND (#{excludeId} IS NULL OR id != #{excludeId})")
    int checkCodeUnique(@Param("customerCode") String customerCode, @Param("excludeId") String excludeId);

    /**
     * Lấy khách hàng theo mã
     * @param customerCode Mã khách hàng
     * @return Đối tượng khách hàng
     */
    @Select("SELECT * FROM customers WHERE customer_code = #{customerCode}")
    Customer getByCode(@Param("customerCode") String customerCode);

    /**
     * Lấy danh sách khách hàng đang hoạt động
     * @return Danh sách khách hàng hoạt động
     */
    @Select("SELECT * FROM customers WHERE status = 1 ORDER BY create_time DESC")
    java.util.List<Customer> getActiveCustomers();

    /**
     * Tìm kiếm khách hàng theo tên hoặc mã
     * @param keyword Từ khóa tìm kiếm
     * @return Danh sách khách hàng phù hợp
     */
    @Select("SELECT * FROM customers WHERE (customer_name LIKE CONCAT('%', #{keyword}, '%') OR customer_code LIKE CONCAT('%', #{keyword}, '%')) AND status = 1 ORDER BY customer_name")
    java.util.List<Customer> searchCustomers(@Param("keyword") String keyword);
}