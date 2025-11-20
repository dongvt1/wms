package org.jeecg.modules.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.warehouse.entity.Inventory;
import org.jeecg.modules.warehouse.entity.InventoryAdjustment;
import org.jeecg.modules.warehouse.entity.InventoryTransaction;
import org.jeecg.modules.warehouse.mapper.InventoryMapper;
import org.jeecg.modules.warehouse.service.IInventoryAdjustmentService;
import org.jeecg.modules.warehouse.service.IInventoryService;
import org.jeecg.modules.warehouse.service.IInventoryTransactionService;
import org.jeecg.modules.warehouse.vo.InventoryReportVO;
import org.jeecg.modules.warehouse.vo.InventoryStatisticsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * 库存服务实现类
 */
@Service
@Slf4j
public class InventoryServiceImpl extends ServiceImpl<InventoryMapper, Inventory> implements IInventoryService {

    @Autowired
    private IInventoryTransactionService inventoryTransactionService;
    
    @Autowired
    private IInventoryAdjustmentService inventoryAdjustmentService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String adjustInventory(String productId, Integer newQuantity, String adjustmentReason) {
        try {
            // 获取当前库存
            QueryWrapper<Inventory> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("product_id", productId);
            Inventory currentInventory = this.getOne(queryWrapper);
            
            if (currentInventory == null) {
                return "未找到对应产品的库存记录";
            }
            
            Integer oldQuantity = currentInventory.getQuantity();
            
            // 创建库存调整记录
            InventoryAdjustment adjustment = new InventoryAdjustment();
            adjustment.setProductId(productId);
            adjustment.setOldQuantity(oldQuantity);
            adjustment.setNewQuantity(newQuantity);
            adjustment.setAdjustmentReason(adjustmentReason);
            inventoryAdjustmentService.save(adjustment);
            
            // 创建库存交易记录
            InventoryTransaction transaction = new InventoryTransaction();
            transaction.setProductId(productId);
            transaction.setTransactionType("ADJUST");
            transaction.setQuantity(newQuantity);
            transaction.setReason(adjustmentReason);
            inventoryTransactionService.save(transaction);
            
            // 更新库存
            currentInventory.setQuantity(newQuantity);
            currentInventory.setAvailableQuantity(newQuantity);
            currentInventory.setLastUpdated(new Date());
            this.updateById(currentInventory);
            
            return "库存调整成功";
        } catch (Exception e) {
            log.error("调整库存失败", e);
            return "调整库存失败: " + e.getMessage();
        }
    }

    @Override
    public InventoryReportVO getInventoryReport(Integer pageNo, Integer pageSize) {
        InventoryReportVO report = new InventoryReportVO();
        
        // 获取所有库存数据
        List<Map<String, Object>> allItems = baseMapper.getValueReport();
        
        // 计算分页
        int total = allItems.size();
        int startIndex = (pageNo - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, total);
        
        List<Map<String, Object>> pageItems = allItems.subList(startIndex, endIndex);
        
        // 转换为InventoryItemVO
        List<InventoryReportVO.InventoryItemVO> items = new ArrayList<>();
        for (Map<String, Object> item : pageItems) {
            InventoryReportVO.InventoryItemVO itemVO = new InventoryReportVO.InventoryItemVO();
            itemVO.setProductId((String) item.get("product_id"));
            itemVO.setProductCode((String) item.get("product_code"));
            itemVO.setProductName((String) item.get("product_name"));
            itemVO.setQuantity((Integer) item.get("quantity"));
            itemVO.setUnitPrice((Double) item.get("unit_price"));
            itemVO.setTotalValue((Double) item.get("total_value"));
            itemVO.setMinStockThreshold((Integer) item.get("min_stock_threshold"));
            itemVO.setStatus((String) item.get("status"));
            items.add(itemVO);
        }
        
        report.setRecords(items);
        report.setTotal((long) total);
        report.setSize((long) pageSize);
        report.setCurrent((long) pageNo);
        report.setPages((long) ((total + pageSize - 1) / pageSize));
        
        // 计算汇总信息
        InventoryReportVO.InventorySummaryVO summary = new InventoryReportVO.InventorySummaryVO();
        summary.setTotalProducts(allItems.size());
        summary.setTotalQuantity(allItems.stream().mapToInt(item -> (Integer) item.get("quantity")).sum());
        summary.setTotalValue(allItems.stream().mapToDouble(item -> (Double) item.get("total_value")).sum());
        summary.setLowStockProducts((int) allItems.stream().filter(item -> "low_stock".equals(item.get("status"))).count());
        summary.setOutOfStockProducts((int) allItems.stream().filter(item -> "out_of_stock".equals(item.get("status"))).count());
        
        report.setSummary(summary);
        
        return report;
    }

    @Override
    public List<Map<String, Object>> getValueReport() {
        return baseMapper.getValueReport();
    }

    @Override
    public void exportInventoryReport(HttpServletRequest request, HttpServletResponse response, 
                                  String productId, String productName, String status) {
        // 构建查询条件
        QueryWrapper<Inventory> queryWrapper = new QueryWrapper<>();
        
        if (oConvertUtils.isNotEmpty(productId)) {
            queryWrapper.eq("product_id", productId);
        }
        
        if (oConvertUtils.isNotEmpty(status)) {
            if ("normal".equals(status)) {
                queryWrapper.gt("quantity", "min_stock_threshold");
            } else if ("low_stock".equals(status)) {
                queryWrapper.le("quantity", "min_stock_threshold");
                queryWrapper.gt("quantity", 0);
            } else if ("out_of_stock".equals(status)) {
                queryWrapper.eq("quantity", 0);
            }
        }
        
        // 查询数据
        List<Inventory> inventoryList = this.list(queryWrapper);
        
        // 导出Excel
        try {
            // 这里可以使用Apache POI或EasyExcel来导出Excel
            // 简化实现，实际项目中需要完善
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=inventory_report_" + DateUtils.getDate() + ".xlsx");
            
            // TODO: 实现Excel导出逻辑
            log.info("导出库存报告，数据量: {}", inventoryList.size());
            
        } catch (Exception e) {
            log.error("导出库存报告失败", e);
        }
    }

    @Override
    public InventoryStatisticsVO getStatistics() {
        InventoryStatisticsVO statistics = new InventoryStatisticsVO();
        
        // 获取基础统计信息
        Map<String, Object> baseStats = baseMapper.getStatistics();
        statistics.setTotalProducts((Integer) baseStats.get("totalProducts"));
        statistics.setTotalQuantity((Integer) baseStats.get("totalQuantity"));
        statistics.setTotalReserved((Integer) baseStats.get("totalReserved"));
        statistics.setTotalAvailable((Integer) baseStats.get("totalAvailable"));
        statistics.setTotalValue((Double) baseStats.get("totalValue"));
        statistics.setLowStockCount((Integer) baseStats.get("lowStockCount"));
        statistics.setOutOfStockCount((Integer) baseStats.get("outOfStockCount"));
        
        // 获取今日统计
        Map<String, Object> todayStats = baseMapper.getTodayCounts();
        statistics.setTodayInCount((Integer) todayStats.get("todayInCount"));
        statistics.setTodayOutCount((Integer) todayStats.get("todayOutCount"));
        
        // 获取本周统计
        Map<String, Object> weekStats = baseMapper.getWeekCounts();
        statistics.setWeekInCount((Integer) weekStats.get("weekInCount"));
        statistics.setWeekOutCount((Integer) weekStats.get("weekOutCount"));
        
        // 获取本月统计
        Map<String, Object> monthStats = baseMapper.getMonthCounts();
        statistics.setMonthInCount((Integer) monthStats.get("monthInCount"));
        statistics.setMonthOutCount((Integer) monthStats.get("monthOutCount"));
        
        return statistics;
    }

    @Override
    public List<Map<String, Object>> getInventoryTrends(String productId, Integer days) {
        return baseMapper.getInventoryTrends(productId, days);
    }
}