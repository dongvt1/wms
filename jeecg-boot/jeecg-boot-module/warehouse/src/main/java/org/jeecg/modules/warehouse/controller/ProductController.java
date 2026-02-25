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
import org.jeecg.modules.warehouse.entity.Product;
import org.jeecg.modules.warehouse.service.ProductService;
import org.jeecg.modules.warehouse.service.ProductHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.List;

/**
 * @Description: Product
 * @Author: BMad
 * @Date:   2025-11-20
 * @Version: V1.0
 */
@Slf4j
@Tag(name = "Product Management")
@RestController
@RequestMapping("/warehouse/product")
public class ProductController extends JeecgController<Product, ProductService> {

    @Autowired
    private ProductService productService;
    
    @Autowired
    private ProductHistoryService productHistoryService;

    /**
     * Paginated list query
     *
     * @param product
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @Operation(summary = "Get Product data list")
    @GetMapping(value = "/list")
    @PermissionData(pageComponent = "warehouse/product/list")
    public Result<?> list(Product product, 
                      @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, 
                      @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                      HttpServletRequest req) {
        QueryWrapper<Product> queryWrapper = QueryGenerator.initQueryWrapper(product, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<Product> page = new Page<Product>(pageNo, pageSize);

        IPage<Product> pageList = productService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * Add
     *
     * @param product
     * @return
     */
    @PostMapping(value = "/add")
    @AutoLog(value = "Add Product")
    @Operation(summary = "Add Product")
    public Result<?> add(@RequestBody Product product) {
        // Check if product code is unique
        if (!productService.isCodeUnique(product.getCode(), null)) {
            return Result.error("Product code already exists!");
        }
        
        productService.save(product);
        
        // Record history
        productHistoryService.recordHistory(
            product.getId(), 
            "CREATE", 
            null, 
            "{\"code\":\"" + product.getCode() + "\",\"name\":\"" + product.getName() + "\"}", 
            "admin"
        );
        
        return Result.OK("Add successful!");
    }

    /**
     * Edit
     *
     * @param product
     * @return
     */
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
    @AutoLog(value = "Edit Product", operateType = 3)
    @Operation(summary = "Edit Product")
    public Result<?> edit(@RequestBody Product product) {
        // Get old product for history
        Product oldProduct = productService.getById(product.getId());
        
        // Check if product code is unique (excluding current product)
        if (!productService.isCodeUnique(product.getCode(), product.getId())) {
            return Result.error("Product code already exists!");
        }
        
        productService.updateById(product);
        
        // Record history
        productHistoryService.recordHistory(
            product.getId(), 
            "UPDATE", 
            "{\"code\":\"" + oldProduct.getCode() + "\",\"name\":\"" + oldProduct.getName() + "\"}", 
            "{\"code\":\"" + product.getCode() + "\",\"name\":\"" + product.getName() + "\"}", 
            "admin"
        );
        
        return Result.OK("Update successful!");
    }

    /**
     * Delete by id
     *
     * @param id
     * @return
     */
    @AutoLog(value = "Delete Product")
    @DeleteMapping(value = "/delete")
    @Operation(summary = "Delete Product by ID")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        // Get product for history
        Product product = productService.getById(id);
        
        productService.removeById(id);
        
        // Record history
        productHistoryService.recordHistory(
            id, 
            "DELETE", 
            "{\"code\":\"" + product.getCode() + "\",\"name\":\"" + product.getName() + "\"}", 
            null, 
            "admin"
        );
        
        return Result.OK("Delete successful!");
    }

    /**
     * Batch delete
     *
     * @param ids
     * @return
     */
    @DeleteMapping(value = "/deleteBatch")
    @Operation(summary = "Batch delete Product")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        List<String> idList = Arrays.asList(ids.split(","));
        
        // Record history for each product
        for (String id : idList) {
            Product product = productService.getById(id);
            if (product != null) {
                productHistoryService.recordHistory(
                    id, 
                    "DELETE", 
                    "{\"code\":\"" + product.getCode() + "\",\"name\":\"" + product.getName() + "\"}", 
                    null, 
                    "admin"
                );
            }
        }
        
        this.productService.removeByIds(idList);
        return Result.OK("Batch delete successful!");
    }

    /**
     * Query by id
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/queryById")
    @Operation(summary = "Query Product by ID")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        Product product = productService.getById(id);
        return Result.OK(product);
    }

    /**
     * Export excel
     *
     * @param request
     */
    @RequestMapping(value = "/exportXls")
    @PermissionData(pageComponent = "warehouse/product/list")
    public ModelAndView exportXls(HttpServletRequest request, Product product) {
        return super.exportXls(request, product, Product.class, "Product");
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
        return super.importExcel(request, response, Product.class);
    }

    /**
     * Search products
     *
     * @param keyword
     * @return
     */
    @GetMapping(value = "/search")
    @Operation(summary = "Search Products")
    public Result<?> search(@RequestParam(name = "keyword", required = true) String keyword) {
        List<Product> products = productService.searchProducts(keyword);
        return Result.OK(products);
    }

    /**
     * Upload product image
     *
     * @param file
     * @return
     */
    @PostMapping(value = "/uploadImage")
    @Operation(summary = "Upload Product Image")
    public Result<?> uploadImage(@RequestParam("file") MultipartFile file) {
        String imageUrl = productService.uploadImage(file);
        if (imageUrl != null) {
            return Result.OK(imageUrl);
        } else {
            return Result.error("Failed to upload image!");
        }
    }

    /**
     * Get product history
     *
     * @param productId
     * @return
     */
    @GetMapping(value = "/history")
    @Operation(summary = "Get Product History")
    public Result<?> history(@RequestParam(name = "productId", required = true) String productId) {
        List<?> history = productHistoryService.getByProductId(productId);
        return Result.OK(history);
    }

    /**
     * Get list of products by status
     *
     * @param status
     * @return
     */
    @GetMapping(value = "/getByStatus")
    @Operation(summary = "Get list of products by status")
    public Result<?> getByStatus(@RequestParam(name = "status", required = true) Integer status) {
        List<Product> list = productService.getByStatus(status);
        return Result.OK(list);
    }

    /**
     * Get list of products by category
     *
     * @param categoryId
     * @return
     */
    @GetMapping(value = "/getByCategory")
    @Operation(summary = "Get list of products by category")
    public Result<?> getByCategory(@RequestParam(name = "categoryId", required = true) String categoryId) {
        List<Product> list = productService.getByCategoryId(categoryId);
        return Result.OK(list);
    }

    /**
     * Get products with low stock
     *
     * @return
     */
    @GetMapping(value = "/getLowStock")
    @Operation(summary = "Get Products with Low Stock")
    public Result<?> getLowStock() {
        List<Product> list = productService.getLowStockProducts();
        return Result.OK(list);
    }
}