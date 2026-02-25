package org.jeecg.modules.warehouse.controller;

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
import org.jeecg.modules.warehouse.entity.ProductionLine;
import org.jeecg.modules.warehouse.service.ProductionLineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.List;

/**
 * @Description: Production Line Controller
 * @Author: BMad
 * @Date: 2026-02-25
 * @Version: V1.0
 */
@Slf4j
@Tag(name = "Dây chuyền sản xuất")
@RestController
@RequestMapping("/warehouse/productionLine")
public class ProductionLineController extends JeecgController<ProductionLine, ProductionLineService> {

    @Autowired
    private ProductionLineService productionLineService;

    @Operation(summary = "Danh sách dây chuyền sản xuất")
    @GetMapping(value = "/list")
    public Result<?> list(ProductionLine productionLine,
                          @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                          @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                          HttpServletRequest req) {
        QueryWrapper<ProductionLine> queryWrapper = QueryGenerator.initQueryWrapper(productionLine, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<ProductionLine> page = new Page<>(pageNo, pageSize);
        IPage<ProductionLine> pageList = productionLineService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @PostMapping(value = "/add")
    @AutoLog(value = "Thêm dây chuyền sản xuất")
    @Operation(summary = "Thêm dây chuyền sản xuất")
    public Result<?> add(@RequestBody ProductionLine productionLine) {
        if (!productionLineService.isCodeUnique(productionLine.getLineCode(), null)) {
            return Result.error("Mã dây chuyền đã tồn tại!");
        }
        productionLineService.save(productionLine);
        return Result.OK("Thêm thành công!");
    }

    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    @AutoLog(value = "Sửa dây chuyền sản xuất", operateType = 3)
    @Operation(summary = "Sửa dây chuyền sản xuất")
    public Result<?> edit(@RequestBody ProductionLine productionLine) {
        if (!productionLineService.isCodeUnique(productionLine.getLineCode(), productionLine.getId())) {
            return Result.error("Mã dây chuyền đã tồn tại!");
        }
        productionLineService.updateById(productionLine);
        return Result.OK("Cập nhật thành công!");
    }

    @AutoLog(value = "Xóa dây chuyền sản xuất")
    @DeleteMapping(value = "/delete")
    @Operation(summary = "Xóa dây chuyền sản xuất")
    public Result<?> delete(@RequestParam(name = "id") String id) {
        productionLineService.removeById(id);
        return Result.OK("Xóa thành công!");
    }

    @DeleteMapping(value = "/deleteBatch")
    @Operation(summary = "Xóa hàng loạt dây chuyền")
    public Result<?> deleteBatch(@RequestParam(name = "ids") String ids) {
        this.productionLineService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("Xóa hàng loạt thành công!");
    }

    @GetMapping(value = "/queryById")
    @Operation(summary = "Xem chi tiết dây chuyền")
    public Result<?> queryById(@RequestParam(name = "id") String id) {
        ProductionLine productionLine = productionLineService.getById(id);
        return Result.OK(productionLine);
    }

    @GetMapping(value = "/getByStatus")
    @Operation(summary = "Lấy dây chuyền theo trạng thái")
    public Result<?> getByStatus(@RequestParam(name = "status") String status) {
        List<ProductionLine> list = productionLineService.getByStatus(status);
        return Result.OK(list);
    }

    @GetMapping(value = "/listAll")
    @Operation(summary = "Lấy tất cả dây chuyền đang hoạt động")
    public Result<?> listAll() {
        List<ProductionLine> list = productionLineService.getByStatus("active");
        return Result.OK(list);
    }

    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, ProductionLine productionLine) {
        return super.exportXls(request, productionLine, ProductionLine.class, "Dây chuyền sản xuất");
    }

    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, ProductionLine.class);
    }
}
