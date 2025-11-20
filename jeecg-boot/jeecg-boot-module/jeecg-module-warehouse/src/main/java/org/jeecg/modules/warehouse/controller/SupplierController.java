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
import org.jeecg.modules.warehouse.entity.Supplier;
import org.jeecg.modules.warehouse.service.ISupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: Supplier Controller
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
@Slf4j
@Tag(name = "Supplier Management")
@RestController
@RequestMapping("/warehouse/supplier")
public class SupplierController extends JeecgController<Supplier, ISupplierService> {

    @Autowired
    private ISupplierService supplierService;

    /**
     * Paginated list query
     *
     * @param supplier
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @Operation(summary = "Get Supplier data list")
    @GetMapping(value = "/list")
    @PermissionData(pageComponent = "warehouse/supplier/list")
    public Result<?> list(Supplier supplier, 
                       @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, 
                       @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                       HttpServletRequest req) {
        QueryWrapper<Supplier> queryWrapper = QueryGenerator.initQueryWrapper(supplier, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<Supplier> page = new Page<Supplier>(pageNo, pageSize);

        IPage<Supplier> pageList = supplierService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * Add
     *
     * @param supplier
     * @return
     */
    @PostMapping(value = "/add")
    @AutoLog(value = "Add Supplier")
    @Operation(summary = "Add Supplier")
    public Result<?> add(@RequestBody Supplier supplier) {
        // Check if supplier code is unique
        if (!supplierService.isCodeUnique(supplier.getSupplierCode(), null)) {
            return Result.error("Supplier code already exists!");
        }
        
        supplierService.save(supplier);
        return Result.OK("Add successful!");
    }

    /**
     * Edit
     *
     * @param supplier
     * @return
     */
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
    @AutoLog(value = "Edit Supplier", operateType = 3)
    @Operation(summary = "Edit Supplier")
    public Result<?> edit(@RequestBody Supplier supplier) {
        // Check if supplier code is unique (excluding current supplier)
        if (!supplierService.isCodeUnique(supplier.getSupplierCode(), supplier.getId())) {
            return Result.error("Supplier code already exists!");
        }
        
        supplierService.updateById(supplier);
        return Result.OK("Update successful!");
    }

    /**
     * Delete by id
     *
     * @param id
     * @return
     */
    @AutoLog(value = "Delete Supplier")
    @DeleteMapping(value = "/delete")
    @Operation(summary = "Delete Supplier by ID")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        supplierService.removeById(id);
        return Result.OK("Delete successful!");
    }

    /**
     * Batch delete
     *
     * @param ids
     * @return
     */
    @DeleteMapping(value = "/deleteBatch")
    @Operation(summary = "Batch delete Supplier")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        List<String> idList = Arrays.asList(ids.split(","));
        this.supplierService.removeByIds(idList);
        return Result.OK("Batch delete successful!");
    }

    /**
     * Query by id
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/queryById")
    @Operation(summary = "Query Supplier by ID")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        Supplier supplier = supplierService.getById(id);
        return Result.OK(supplier);
    }

    /**
     * Get supplier by code
     *
     * @param supplierCode
     * @return
     */
    @GetMapping(value = "/getByCode")
    @Operation(summary = "Get Supplier by Code")
    public Result<?> getByCode(@RequestParam(name = "supplierCode", required = true) String supplierCode) {
        Supplier supplier = supplierService.getByCode(supplierCode);
        return Result.OK(supplier);
    }

    /**
     * Get active suppliers
     *
     * @return
     */
    @GetMapping(value = "/getActive")
    @Operation(summary = "Get Active Suppliers")
    public Result<?> getActiveSuppliers() {
        List<Supplier> list = supplierService.getActiveSuppliers();
        return Result.OK(list);
    }

    /**
     * Export excel
     *
     * @param request
     */
    @RequestMapping(value = "/exportXls")
    @PermissionData(pageComponent = "warehouse/supplier/list")
    public ModelAndView exportXls(HttpServletRequest request, Supplier supplier) {
        return super.exportXls(request, supplier, Supplier.class, "Supplier");
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
        return super.importExcel(request, response, Supplier.class);
    }
}