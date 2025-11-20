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
import org.jeecg.modules.warehouse.entity.ProductCategory;
import org.jeecg.modules.warehouse.service.IProductCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: Product Category
 * @Author: BMad
 * @Date:   2025-11-20
 * @Version: V1.0
 */
@Slf4j
@Tag(name = "Product Category Management")
@RestController
@RequestMapping("/warehouse/category")
public class ProductCategoryController extends JeecgController<ProductCategory, IProductCategoryService> {

    @Autowired
    private IProductCategoryService productCategoryService;

    /**
     * Paginated list query
     *
     * @param productCategory
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @Operation(summary = "Get Product Category data list")
    @GetMapping(value = "/list")
    @PermissionData(pageComponent = "warehouse/category/list")
    public Result<?> list(ProductCategory productCategory, 
                      @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, 
                      @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                      HttpServletRequest req) {
        QueryWrapper<ProductCategory> queryWrapper = QueryGenerator.initQueryWrapper(productCategory, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<ProductCategory> page = new Page<ProductCategory>(pageNo, pageSize);

        IPage<ProductCategory> pageList = productCategoryService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * Add
     *
     * @param productCategory
     * @return
     */
    @PostMapping(value = "/add")
    @AutoLog(value = "Add Product Category")
    @Operation(summary = "Add Product Category")
    public Result<?> add(@RequestBody ProductCategory productCategory) {
        productCategoryService.save(productCategory);
        return Result.OK("Add successful!");
    }

    /**
     * Edit
     *
     * @param productCategory
     * @return
     */
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
    @AutoLog(value = "Edit Product Category", operateType = 3)
    @Operation(summary = "Edit Product Category")
    public Result<?> edit(@RequestBody ProductCategory productCategory) {
        productCategoryService.updateById(productCategory);
        return Result.OK("Update successful!");
    }

    /**
     * Delete by id
     *
     * @param id
     * @return
     */
    @AutoLog(value = "Delete Product Category")
    @DeleteMapping(value = "/delete")
    @Operation(summary = "Delete Product Category by ID")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        productCategoryService.removeById(id);
        return Result.OK("Delete successful!");
    }

    /**
     * Batch delete
     *
     * @param ids
     * @return
     */
    @DeleteMapping(value = "/deleteBatch")
    @Operation(summary = "Batch delete Product Category")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.productCategoryService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("Batch delete successful!");
    }

    /**
     * Query by id
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/queryById")
    @Operation(summary = "Query Product Category by ID")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        ProductCategory productCategory = productCategoryService.getById(id);
        return Result.OK(productCategory);
    }

    /**
     * Export excel
     *
     * @param request
     */
    @RequestMapping(value = "/exportXls")
    @PermissionData(pageComponent = "warehouse/category/list")
    public ModelAndView exportXls(HttpServletRequest request, ProductCategory productCategory) {
        return super.exportXls(request, productCategory, ProductCategory.class, "Product Category");
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
        return super.importExcel(request, response, ProductCategory.class);
    }

    /**
     * Get category tree structure
     *
     * @return
     */
    @GetMapping(value = "/tree")
    @Operation(summary = "Get Product Category Tree")
    public Result<?> tree() {
        List<ProductCategory> tree = productCategoryService.getTree();
        return Result.OK(tree);
    }

    /**
     * Get list of categories by status
     *
     * @param status
     * @return
     */
    @GetMapping(value = "/getByStatus")
    @Operation(summary = "Get list of categories by status")
    public Result<?> getByStatus(@RequestParam(name = "status", required = true) Integer status) {
        List<ProductCategory> list = productCategoryService.getByStatus(status);
        return Result.OK(list);
    }
}