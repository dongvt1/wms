package org.jeecg.modules.warehouse.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.warehouse.entity.Inventory;
import org.jeecg.modules.warehouse.entity.InventoryTransaction;
import org.jeecg.modules.warehouse.entity.InventoryAdjustment;
import org.jeecg.modules.warehouse.entity.InventoryAlert;
import org.jeecg.modules.warehouse.service.IInventoryService;
import org.jeecg.modules.warehouse.service.IInventoryTransactionService;
import org.jeecg.modules.warehouse.service.IInventoryAdjustmentService;
import org.jeecg.modules.warehouse.service.IInventoryAlertService;
import org.jeecg.modules.warehouse.vo.InventoryReportVO;
import org.jeecg.modules.warehouse.vo.InventoryStatisticsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 库存管理控制器
 */
@Api(tags = "库存管理")
@RestController
@RequestMapping("/warehouse/inventory")
@Slf4j
public class InventoryController extends JeecgController<Inventory, IInventoryService> {

    @Autowired
    private IInventoryService inventoryService;

    @Autowired
    private IInventoryTransactionService inventoryTransactionService;

    @Autowired
    private IInventoryAdjustmentService inventoryAdjustmentService;

    @Autowired
    private IInventoryAlertService inventoryAlertService;

    /**
     * 分页列表查询
     */
    @ApiOperation(value = "库存管理-分页列表查询", notes = "库存管理-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<Inventory>> queryPageList(Inventory inventory,
                                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                   @RequestParam(name = "sort", required = false) String sort,
                                                   @RequestParam(name = "order", required = false) String order,
                                                   HttpServletRequest req) {
        QueryWrapper<Inventory> queryWrapper = QueryGenerator.initQueryWrapper(inventory, req.getParameterMap());
        Page<Inventory> page = new Page<Inventory>(pageNo, pageSize);
        IPage<Inventory> pageList = inventoryService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加库存
     */
    @AutoLog(value = "库存管理-添加库存")
    @ApiOperation(value = "库存管理-添加库存", notes = "库存管理-添加库存")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody Inventory inventory) {
        inventoryService.save(inventory);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑库存
     */
    @AutoLog(value = "库存管理-编辑库存")
    @ApiOperation(value = "库存管理-编辑库存", notes = "库存管理-编辑库存")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody Inventory inventory) {
        inventoryService.updateById(inventory);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除库存
     */
    @AutoLog(value = "库存管理-通过id删除库存")
    @ApiOperation(value = "库存管理-通过id删除库存", notes = "库存管理-通过id删除库存")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        inventoryService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除库存
     */
    @AutoLog(value = "库存管理-批量删除库存")
    @ApiOperation(value = "库存管理-批量删除库存", notes = "库存管理-批量删除库存")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.inventoryService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询库存
     */
    @ApiOperation(value = "库存管理-通过id查询库存", notes = "库存管理-通过id查询库存")
    @GetMapping(value = "/queryById")
    public Result<Inventory> queryById(@ApiParam(name = "id", value = "实例id", required = true) @RequestParam(name = "id", required = true) String id) {
        Inventory inventory = inventoryService.getById(id);
        if (inventory == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(inventory);
    }

    /**
     * 根据产品ID查询库存
     */
    @ApiOperation(value = "库存管理-根据产品ID查询库存", notes = "库存管理-根据产品ID查询库存")
    @GetMapping(value = "/product/{productId}")
    public Result<Inventory> getByProductId(@ApiParam(name = "productId", value = "产品ID", required = true) @PathVariable("productId") String productId) {
        QueryWrapper<Inventory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("product_id", productId);
        Inventory inventory = inventoryService.getOne(queryWrapper);
        return Result.OK(inventory);
    }

    /**
     * 更新库存信息
     */
    @AutoLog(value = "库存管理-更新库存信息")
    @ApiOperation(value = "库存管理-更新库存信息", notes = "库存管理-更新库存信息")
    @PutMapping(value = "/update")
    public Result<String> updateInventory(@RequestBody Map<String, Object> params) {
        String productId = (String) params.get("productId");
        Integer minStockThreshold = (Integer) params.get("minStockThreshold");
        
        QueryWrapper<Inventory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("product_id", productId);
        Inventory inventory = inventoryService.getOne(queryWrapper);
        
        if (inventory != null) {
            inventory.setMinStockThreshold(minStockThreshold);
            inventoryService.updateById(inventory);
            return Result.OK("更新成功!");
        } else {
            return Result.error("未找到对应库存记录");
        }
    }

    /**
     * 手动调整库存
     */
    @AutoLog(value = "库存管理-手动调整库存")
    @ApiOperation(value = "库存管理-手动调整库存", notes = "库存管理-手动调整库存")
    @PostMapping(value = "/adjust")
    public Result<String> adjustInventory(@RequestBody Map<String, Object> params) {
        String productId = (String) params.get("productId");
        Integer newQuantity = (Integer) params.get("newQuantity");
        String adjustmentReason = (String) params.get("adjustmentReason");
        
        return inventoryService.adjustInventory(productId, newQuantity, adjustmentReason);
    }

    /**
     * 获取库存交易历史
     */
    @ApiOperation(value = "库存管理-获取库存交易历史", notes = "库存管理-获取库存交易历史")
    @GetMapping(value = "/transactions")
    public Result<IPage<InventoryTransaction>> getTransactions(
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "productId", required = false) String productId,
            @RequestParam(name = "transactionType", required = false) String transactionType,
            HttpServletRequest req) {
        
        QueryWrapper<InventoryTransaction> queryWrapper = new QueryWrapper<>();
        
        if (oConvertUtils.isNotEmpty(productId)) {
            queryWrapper.eq("product_id", productId);
        }
        
        if (oConvertUtils.isNotEmpty(transactionType)) {
            queryWrapper.eq("transaction_type", transactionType);
        }
        
        queryWrapper.orderByDesc("created_at");
        
        Page<InventoryTransaction> page = new Page<InventoryTransaction>(pageNo, pageSize);
        IPage<InventoryTransaction> pageList = inventoryTransactionService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 获取库存调整历史
     */
    @ApiOperation(value = "库存管理-获取库存调整历史", notes = "库存管理-获取库存调整历史")
    @GetMapping(value = "/adjustments")
    public Result<IPage<InventoryAdjustment>> getAdjustments(
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "productId", required = false) String productId,
            HttpServletRequest req) {
        
        QueryWrapper<InventoryAdjustment> queryWrapper = new QueryWrapper<>();
        
        if (oConvertUtils.isNotEmpty(productId)) {
            queryWrapper.eq("product_id", productId);
        }
        
        queryWrapper.orderByDesc("created_at");
        
        Page<InventoryAdjustment> page = new Page<InventoryAdjustment>(pageNo, pageSize);
        IPage<InventoryAdjustment> pageList = inventoryAdjustmentService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 获取库存报告
     */
    @ApiOperation(value = "库存管理-获取库存报告", notes = "库存管理-获取库存报告")
    @GetMapping(value = "/report")
    public Result<InventoryReportVO> getReport(
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            HttpServletRequest req) {
        
        InventoryReportVO report = inventoryService.getInventoryReport(pageNo, pageSize);
        return Result.OK(report);
    }

    /**
     * 获取库存价值报告
     */
    @ApiOperation(value = "库存管理-获取库存价值报告", notes = "库存管理-获取库存价值报告")
    @GetMapping(value = "/value-report")
    public Result<List<Map<String, Object>>> getValueReport() {
        List<Map<String, Object>> valueReport = inventoryService.getValueReport();
        return Result.OK(valueReport);
    }

    /**
     * 导出库存报告
     */
    @ApiOperation(value = "库存管理-导出库存报告", notes = "库存管理-导出库存报告")
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void exportXls(HttpServletRequest request, HttpServletResponse response) {
        // 参数验证
        String productId = request.getParameter("productId");
        String productName = request.getParameter("productName");
        String status = request.getParameter("status");
        
        // 导出Excel
        inventoryService.exportInventoryReport(request, response, productId, productName, status);
    }

    /**
     * 获取低库存产品列表
     */
    @ApiOperation(value = "库存管理-获取低库存产品列表", notes = "库存管理-获取低库存产品列表")
    @GetMapping(value = "/low-stock")
    public Result<IPage<Inventory>> getLowStock(
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            HttpServletRequest req) {
        
        QueryWrapper<Inventory> queryWrapper = new QueryWrapper<>();
        queryWrapper.le("quantity", "min_stock_threshold");
        queryWrapper.orderByAsc("quantity");
        
        Page<Inventory> page = new Page<Inventory>(pageNo, pageSize);
        IPage<Inventory> pageList = inventoryService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 获取库存预警列表
     */
    @ApiOperation(value = "库存管理-获取库存预警列表", notes = "库存管理-获取库存预警列表")
    @GetMapping(value = "/alerts")
    public Result<IPage<InventoryAlert>> getAlerts(
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "alertType", required = false) String alertType,
            @RequestParam(name = "alertStatus", required = false) String alertStatus,
            HttpServletRequest req) {
        
        QueryWrapper<InventoryAlert> queryWrapper = new QueryWrapper<>();
        
        if (oConvertUtils.isNotEmpty(alertType)) {
            queryWrapper.eq("alert_type", alertType);
        }
        
        if (oConvertUtils.isNotEmpty(alertStatus)) {
            queryWrapper.eq("alert_status", alertStatus);
        }
        
        queryWrapper.orderByDesc("created_at");
        
        Page<InventoryAlert> page = new Page<InventoryAlert>(pageNo, pageSize);
        IPage<InventoryAlert> pageList = inventoryAlertService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 解决库存预警
     */
    @AutoLog(value = "库存管理-解决库存预警")
    @ApiOperation(value = "库存管理-解决库存预警", notes = "库存管理-解决库存预警")
    @PutMapping(value = "/alerts/resolve")
    public Result<String> resolveAlert(@RequestBody Map<String, String> params) {
        String alertId = params.get("alertId");
        return inventoryAlertService.resolveAlert(alertId);
    }

    /**
     * 忽略库存预警
     */
    @AutoLog(value = "库存管理-忽略库存预警")
    @ApiOperation(value = "库存管理-忽略库存预警", notes = "库存管理-忽略库存预警")
    @PutMapping(value = "/alerts/dismiss")
    public Result<String> dismissAlert(@RequestBody Map<String, String> params) {
        String alertId = params.get("alertId");
        return inventoryAlertService.dismissAlert(alertId);
    }

    /**
     * 批量解决库存预警
     */
    @AutoLog(value = "库存管理-批量解决库存预警")
    @ApiOperation(value = "库存管理-批量解决库存预警", notes = "库存管理-批量解决库存预警")
    @PutMapping(value = "/alerts/resolve-batch")
    public Result<String> resolveAlertsBatch(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<String> alertIds = (List<String>) params.get("alertIds");
        return inventoryAlertService.resolveAlertsBatch(alertIds);
    }

    /**
     * 批量忽略库存预警
     */
    @AutoLog(value = "库存管理-批量忽略库存预警")
    @ApiOperation(value = "库存管理-批量忽略库存预警", notes = "库存管理-批量忽略库存预警")
    @PutMapping(value = "/alerts/dismiss-batch")
    public Result<String> dismissAlertsBatch(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<String> alertIds = (List<String>) params.get("alertIds");
        return inventoryAlertService.dismissAlertsBatch(alertIds);
    }

    /**
     * 获取库存统计信息
     */
    @ApiOperation(value = "库存管理-获取库存统计信息", notes = "库存管理-获取库存统计信息")
    @GetMapping(value = "/statistics")
    public Result<InventoryStatisticsVO> getStatistics() {
        InventoryStatisticsVO statistics = inventoryService.getStatistics();
        return Result.OK(statistics);
    }

    /**
     * 获取库存趋势数据
     */
    @ApiOperation(value = "库存管理-获取库存趋势数据", notes = "库存管理-获取库存趋势数据")
    @GetMapping(value = "/trends")
    public Result<List<Map<String, Object>>> getTrends(
            @RequestParam(name = "productId", required = true) String productId,
            @RequestParam(name = "days", defaultValue = "30") Integer days) {
        
        List<Map<String, Object>> trends = inventoryService.getInventoryTrends(productId, days);
        return Result.OK(trends);
    }

    /**
     * 搜索库存
     */
    @ApiOperation(value = "库存管理-搜索库存", notes = "库存管理-搜索库存")
    @GetMapping(value = "/search")
    public Result<IPage<Inventory>> searchInventory(
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "keyword", required = false) String keyword,
            HttpServletRequest req) {
        
        QueryWrapper<Inventory> queryWrapper = new QueryWrapper<>();
        
        if (oConvertUtils.isNotEmpty(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                .like("product_code", keyword)
                .or()
                .like("product_name", keyword));
        }
        
        Page<Inventory> page = new Page<Inventory>(pageNo, pageSize);
        IPage<Inventory> pageList = inventoryService.page(page, queryWrapper);
        return Result.OK(pageList);
    }
}