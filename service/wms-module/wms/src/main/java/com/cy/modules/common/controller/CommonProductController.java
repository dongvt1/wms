package com.cy.modules.common.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import com.cy.modules.common.entity.Product;
import com.cy.modules.common.service.CommonProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;

/**
 * @Description: Common Product Controller – endpoint /common/product
 *               Dùng chung cho warehouse, planning, qms và tất cả module khác
 * @Author: BMad
 * @Date: 2026-03-02
 * @Version: V1.0
 */
@Slf4j
@Tag(name = "[Common] Sản phẩm & Nguyên vật liệu")
@RestController
@RequestMapping("/common/product")
public class CommonProductController extends JeecgController<Product, CommonProductService> {

    @Autowired
    private CommonProductService commonProductService;

    // ===== CRUD cơ bản =====

    @Operation(summary = "Danh sách sản phẩm (có phân trang)")
    @GetMapping(value = "/list")
    public Result<?> list(Product product,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            HttpServletRequest req) {
        QueryWrapper<Product> queryWrapper = QueryGenerator.initQueryWrapper(product, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<Product> page = new Page<>(pageNo, pageSize);
        IPage<Product> pageList = commonProductService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @PostMapping(value = "/add")
    @AutoLog(value = "[Common] Thêm sản phẩm")
    @Operation(summary = "Thêm sản phẩm")
    public Result<?> add(@RequestBody Product product) {
        if (!commonProductService.isCodeUnique(product.getCode(), null)) {
            return Result.error("Mã sản phẩm đã tồn tại!");
        }
        commonProductService.save(product);
        return Result.OK("Thêm sản phẩm thành công!");
    }

    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    @AutoLog(value = "[Common] Sửa sản phẩm", operateType = 3)
    @Operation(summary = "Sửa sản phẩm")
    public Result<?> edit(@RequestBody Product product) {
        if (!commonProductService.isCodeUnique(product.getCode(), product.getId())) {
            return Result.error("Mã sản phẩm đã tồn tại!");
        }
        commonProductService.updateById(product);
        return Result.OK("Cập nhật sản phẩm thành công!");
    }

    @AutoLog(value = "[Common] Xóa sản phẩm")
    @DeleteMapping(value = "/delete")
    @Operation(summary = "Xóa sản phẩm")
    public Result<?> delete(@RequestParam(name = "id") String id) {
        commonProductService.removeById(id);
        return Result.OK("Xóa thành công!");
    }

    @DeleteMapping(value = "/deleteBatch")
    @Operation(summary = "Xóa hàng loạt sản phẩm")
    public Result<?> deleteBatch(@RequestParam(name = "ids") String ids) {
        commonProductService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("Xóa hàng loạt thành công!");
    }

    @GetMapping(value = "/queryById")
    @Operation(summary = "Xem chi tiết sản phẩm")
    public Result<?> queryById(@RequestParam(name = "id") String id) {
        return Result.OK(commonProductService.getById(id));
    }

    // ===== Tìm kiếm & lọc =====

    @GetMapping(value = "/search")
    @Operation(summary = "Tìm kiếm sản phẩm theo từ khóa")
    public Result<?> search(@RequestParam(name = "keyword") String keyword) {
        return Result.OK(commonProductService.searchProducts(keyword));
    }

    @GetMapping(value = "/listActive")
    @Operation(summary = "Lấy tất cả sản phẩm đang hoạt động")
    public Result<?> listActive() {
        return Result.OK(commonProductService.listActive());
    }

    @GetMapping(value = "/listByType")
    @Operation(summary = "Lấy sản phẩm theo loại: product | material | semi")
    public Result<?> listByType(@RequestParam(name = "type") String type) {
        return Result.OK(commonProductService.getByType(type));
    }

    @GetMapping(value = "/listByCategory")
    @Operation(summary = "Lấy sản phẩm theo danh mục")
    public Result<?> listByCategory(@RequestParam(name = "categoryId") String categoryId) {
        return Result.OK(commonProductService.getByCategoryId(categoryId));
    }

    @GetMapping(value = "/getLowStock")
    @Operation(summary = "Lấy sản phẩm sắp hết hàng")
    public Result<?> getLowStock() {
        return Result.OK(commonProductService.getLowStockProducts());
    }

    // ===== Upload ảnh =====

    @PostMapping(value = "/uploadImage")
    @Operation(summary = "Upload ảnh sản phẩm")
    public Result<?> uploadImage(@RequestParam("file") MultipartFile file) {
        String imageUrl = commonProductService.uploadImage(file);
        return imageUrl != null ? Result.OK(imageUrl) : Result.error("Upload ảnh thất bại!");
    }

    // ===== Excel =====

    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, Product product) {
        return super.exportXls(request, product, Product.class, "Sản phẩm");
    }

    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, Product.class);
    }
}
