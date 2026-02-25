package org.jeecg.modules.warehouse.mapper;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.warehouse.entity.StockTransaction;

/**
 * @Description: StockTransaction Mapper
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
public interface StockTransactionMapper extends BaseMapper<StockTransaction> {

    /**
     * Get transactions by date range
     * @param startDate Start date
     * @param endDate End date
     * @return List of transactions
     */
    @Select("SELECT * FROM stock_transactions WHERE transaction_date BETWEEN #{startDate} AND #{endDate} ORDER BY transaction_date DESC")
    List<StockTransaction> getByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * Get transactions by status
     * @param status Status
     * @return List of transactions
     */
    @Select("SELECT * FROM stock_transactions WHERE status = #{status} ORDER BY transaction_date DESC")
    List<StockTransaction> getByStatus(@Param("status") String status);

    /**
     * Get transactions by type
     * @param transactionType Transaction type
     * @return List of transactions
     */
    @Select("SELECT * FROM stock_transactions WHERE transaction_type = #{transactionType} ORDER BY transaction_date DESC")
    List<StockTransaction> getByTransactionType(@Param("transactionType") String transactionType);

    /**
     * Get transaction summary report
     * @return List of transaction summary
     */
    @Select("SELECT st.*, " +
            "(SELECT COUNT(*) FROM stock_transaction_items WHERE transaction_id = st.id) as item_count " +
            "FROM stock_transactions st " +
            "ORDER BY st.transaction_date DESC")
    List<StockTransaction> getTransactionSummary();

    /**
     * Get transaction by code
     * @param transactionCode Transaction code
     * @return Transaction
     */
    @Select("SELECT * FROM stock_transactions WHERE transaction_code = #{transactionCode}")
    StockTransaction getByCode(@Param("transactionCode") String transactionCode);
}