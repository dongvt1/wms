package org.jeecg.modules.warehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.warehouse.entity.Inventory;
import org.jeecg.modules.warehouse.vo.InventoryReportVO;
import org.jeecg.modules.warehouse.vo.InventoryStatisticsVO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 库存服务接口
 */
public interface IInventoryService extends IService<Inventory> {

    /**
     * 调整库存
     * @param productId 产品ID
     * @param newQuantity 新数量
     * @param adjustmentReason 调整原因
     * @return 操作结果
     */
    String adjustInventory(String productId, Integer newQuantity, String adjustmentReason);

    /**
     * 获取库存报告
     * @param pageNo 页码
     * @param pageSize 页大小
     * @return 库存报告
     */
    InventoryReportVO getInventoryReport(Integer pageNo, Integer pageSize);

    /**
     * 获取库存价值报告
     * @return 库存价值报告
     */
    List<Map<String, Object>> getValueReport();

    /**
     * 导出库存报告
     * @param request HTTP请求
     * @param response HTTP响应
     * @param productId 产品ID
     * @param productName 产品名称
     * @param status 状态
     */
    void exportInventoryReport(HttpServletRequest request, HttpServletResponse response, 
                              String productId, String productName, String status);

    /**
     * 获取库存统计信息
     * @return 库存统计信息
     */
    InventoryStatisticsVO getStatistics();

    /**
     * 获取库存趋势数据
     * @param productId 产品ID
     * @param days 天数
     * @return 趋势数据
     */
    List<Map<String, Object>> getInventoryTrends(String productId, Integer days);
}