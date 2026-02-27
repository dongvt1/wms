package com.cy.modules.warehouse.mapper;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cy.modules.warehouse.entity.StockTransaction;

/**
 * @Description: Mapper phiếu xuất nhập kho
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
public interface StockTransactionMapper extends BaseMapper<StockTransaction> {

    /**
     * Lấy danh sách phiếu theo khoảng thời gian
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return Danh sách phiếu
     */
    @Select("SELECT * FROM stock_transactions WHERE transaction_date BETWEEN #{startDate} AND #{endDate} ORDER BY transaction_date DESC")
    List<StockTransaction> getByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * Lấy danh sách phiếu theo trạng thái
     * @param status Trạng thái
     * @return Danh sách phiếu
     */
    @Select("SELECT * FROM stock_transactions WHERE status = #{status} ORDER BY transaction_date DESC")
    List<StockTransaction> getByStatus(@Param("status") String status);

    /**
     * Lấy danh sách phiếu theo loại
     * @param transactionType Loại phiếu
     * @return Danh sách phiếu
     */
    @Select("SELECT * FROM stock_transactions WHERE transaction_type = #{transactionType} ORDER BY transaction_date DESC")
    List<StockTransaction> getByTransactionType(@Param("transactionType") String transactionType);

    /**
     * Lấy báo cáo tổng hợp phiếu
     * @return Danh sách tổng hợp phiếu
     */
    @Select("SELECT st.*, " +
            "(SELECT COUNT(*) FROM stock_transaction_items WHERE transaction_id = st.id) as item_count " +
            "FROM stock_transactions st " +
            "ORDER BY st.transaction_date DESC")
    List<StockTransaction> getTransactionSummary();

    /**
     * Lấy phiếu theo mã
     * @param transactionCode Mã phiếu
     * @return Thông tin phiếu
     */
    @Select("SELECT * FROM stock_transactions WHERE transaction_code = #{transactionCode}")
    StockTransaction getByCode(@Param("transactionCode") String transactionCode);
}