package org.jeecg.modules.warehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.warehouse.entity.Inventory;

import java.util.List;
import java.util.Map;

/**
 * 库存表 mapper 接口
 */
@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {

    /**
     * 获取库存价值报告
     * @return 库存价值报告
     */
    @Select("SELECT i.product_id, p.code as product_code, p.name as product_name, " +
            "i.quantity, p.price as unit_price, (i.quantity * p.price) as total_value, " +
            "i.min_stock_threshold, " +
            "CASE WHEN i.quantity = 0 THEN 'out_of_stock' " +
            "WHEN i.quantity <= i.min_stock_threshold THEN 'low_stock' " +
            "ELSE 'normal' END as status " +
            "FROM inventory i " +
            "LEFT JOIN product p ON i.product_id = p.id")
    List<Map<String, Object>> getValueReport();

    /**
     * 获取库存统计信息
     * @return 库存统计信息
     */
    @Select("SELECT " +
            "COUNT(DISTINCT i.product_id) as totalProducts, " +
            "COALESCE(SUM(i.quantity), 0) as totalQuantity, " +
            "COALESCE(SUM(i.reserved_quantity), 0) as totalReserved, " +
            "COALESCE(SUM(i.available_quantity), 0) as totalAvailable, " +
            "COALESCE(SUM(i.quantity * p.price), 0) as totalValue, " +
            "COUNT(CASE WHEN i.quantity <= i.min_stock_threshold AND i.quantity > 0 THEN 1 END) as lowStockCount, " +
            "COUNT(CASE WHEN i.quantity = 0 THEN 1 END) as outOfStockCount " +
            "FROM inventory i " +
            "LEFT JOIN product p ON i.product_id = p.id")
    Map<String, Object> getStatistics();

    /**
     * 获取今日入库数量
     * @return 今日入库数量
     */
    @Select("SELECT COALESCE(SUM(CASE WHEN transaction_type = 'IN' THEN quantity ELSE 0 END), 0) as todayInCount, " +
            "COALESCE(SUM(CASE WHEN transaction_type = 'OUT' THEN quantity ELSE 0 END), 0) as todayOutCount " +
            "FROM inventory_transactions " +
            "WHERE DATE(created_at) = CURDATE()")
    Map<String, Object> getTodayCounts();

    /**
     * 获取本周入库数量
     * @return 本周入库数量
     */
    @Select("SELECT COALESCE(SUM(CASE WHEN transaction_type = 'IN' THEN quantity ELSE 0 END), 0) as weekInCount, " +
            "COALESCE(SUM(CASE WHEN transaction_type = 'OUT' THEN quantity ELSE 0 END), 0) as weekOutCount " +
            "FROM inventory_transactions " +
            "WHERE YEARWEEK(created_at) = YEARWEEK(NOW())")
    Map<String, Object> getWeekCounts();

    /**
     * 获取本月入库数量
     * @return 本月入库数量
     */
    @Select("SELECT COALESCE(SUM(CASE WHEN transaction_type = 'IN' THEN quantity ELSE 0 END), 0) as monthInCount, " +
            "COALESCE(SUM(CASE WHEN transaction_type = 'OUT' THEN quantity ELSE 0 END), 0) as monthOutCount " +
            "FROM inventory_transactions " +
            "WHERE YEAR(created_at) = YEAR(NOW()) AND MONTH(created_at) = MONTH(NOW())")
    Map<String, Object> getMonthCounts();

    /**
     * 获取库存趋势数据
     * @param productId 产品ID
     * @param days 天数
     * @return 趋势数据
     */
    @Select("SELECT DATE(created_at) as date, " +
            "SUM(CASE WHEN transaction_type = 'IN' THEN quantity ELSE 0 END) as inQuantity, " +
            "SUM(CASE WHEN transaction_type = 'OUT' THEN quantity ELSE 0 END) as outQuantity " +
            "FROM inventory_transactions " +
            "WHERE product_id = #{productId} " +
            "AND created_at >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
            "GROUP BY DATE(created_at) " +
            "ORDER BY date ASC")
    List<Map<String, Object>> getInventoryTrends(@Param("productId") String productId, @Param("days") Integer days);
}