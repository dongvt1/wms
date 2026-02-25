package org.jeecg.modules.warehouse.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.aspect.annotation.PermissionData;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.warehouse.entity.WarehouseArea;
import org.jeecg.modules.warehouse.service.WarehouseAreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.List;

/**
 * @Description: Warehouse Area
 * @Author: BMad
 * @Date:   2025-11-17
 * @Version: V1.0
 */
@Slf4j
@Tag(name = "Warehouse Area Management")
@RestController
@RequestMapping("/warehouse/area")
public class WarehouseAreaController extends JeecgController<WarehouseArea, WarehouseAreaService> {

    @Autowired
    private WarehouseAreaService warehouseAreaService;

    /**
     * Paginated list query
     *
     * @param warehouseArea
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @Operation(summary = "Get Warehouse Area data list")
    @GetMapping(value = "/list")
    @PermissionData(pageComponent = "warehouse:area:list")
    public Result<?> list(WarehouseArea warehouseArea, 
                      @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, 
                      @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                      HttpServletRequest req) {
        QueryWrapper<WarehouseArea> queryWrapper = QueryGenerator.initQueryWrapper(warehouseArea, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<WarehouseArea> page = new Page<WarehouseArea>(pageNo, pageSize);

        IPage<WarehouseArea> pageList = warehouseAreaService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * Add
     *
     * @param warehouseArea
     * @return
     */
    @PostMapping(value = "/add")
    @AutoLog(value = "Add Warehouse Area")
    @Operation(summary = "Add Warehouse Area")
    @PermissionData(pageComponent = "warehouse:area:add")
    public Result<?> add(@RequestBody WarehouseArea warehouseArea) {
        warehouseAreaService.save(warehouseArea);
        return Result.OK("Add successful!");
    }

    /**
     * Edit
     *
     * @param warehouseArea
     * @return
     */
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
    @AutoLog(value = "Edit Warehouse Area", operateType = 3)
    @Operation(summary = "Edit Warehouse Area")
    @PermissionData(pageComponent = "warehouse:area:edit")
    public Result<?> edit(@RequestBody WarehouseArea warehouseArea) {
        warehouseAreaService.updateById(warehouseArea);
        return Result.OK("Update successful!");
    }

    /**
     * Delete by id
     *
     * @param id
     * @return
     */
    @AutoLog(value = "deleteKhu vực kho")
    @DeleteMapping(value = "/delete")
    @Operation(summary = "Delete Warehouse Area by ID")
    @PermissionData(pageComponent = "warehouse:area:delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        warehouseAreaService.removeById(id);
        return Result.OK("Delete successful!");
    }

    /**
     * Batch delete
     *
     * @param ids
     * @return
     */
    @DeleteMapping(value = "/deleteBatch")
    @Operation(summary = "Batch delete Warehouse Area")
    @PermissionData(pageComponent = "warehouse:area:deletebath")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.warehouseAreaService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("Batch delete successful!");
    }

    /**
     * Query by id
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/queryById")
    @Operation(summary = "Query Warehouse Area by ID")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        WarehouseArea warehouseArea = warehouseAreaService.getById(id);
        return Result.OK(warehouseArea);
    }

    /**
     * Export excel
     *
     * @param request
     */
    @RequestMapping(value = "/exportXls")
    @PermissionData(pageComponent = "warehouse/area/list")
    public ModelAndView exportXls(HttpServletRequest request, WarehouseArea warehouseArea) {
        return super.exportXls(request, warehouseArea, WarehouseArea.class, "Warehouse Area");
    }

    /**
     * Import data through excel
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, WarehouseArea.class);
    }

    /**
     * Get list of areas by status
     *
     * @param status
     * @return
     */
    @GetMapping(value = "/getByStatus")
    @Operation(summary = "Get list of areas by status")
    public Result<?> getByStatus(@RequestParam(name = "status", required = true) Integer status) {
        List<WarehouseArea> list = warehouseAreaService.getByStatus(status);
        return Result.OK(list);
    }
}