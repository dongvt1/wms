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
import org.jeecg.modules.warehouse.entity.WarehouseShelf;
import org.jeecg.modules.warehouse.service.WarehouseShelfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.List;

/**
 * @Description: kệ kho
 * @Author: BMad
 * @Date:   2025-11-17
 * @Version: V1.0
 */
@Slf4j
@Tag(name = "Quản lý kệ kho")
@RestController
@RequestMapping("/warehouse/shelf")
public class WarehouseShelfController extends JeecgController<WarehouseShelf, WarehouseShelfService> {

    @Autowired
    private WarehouseShelfService warehouseShelfService;

    /**
     * Paginated list query
     *
     * @param warehouseShelf
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @Operation(summary = "GetKệ khoData list")
    @GetMapping(value = "/list")
    @PermissionData(pageComponent = "warehouse/shelf/list")
    public Result<?> list(WarehouseShelf warehouseShelf, 
                      @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, 
                      @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                      HttpServletRequest req) {
        QueryWrapper<WarehouseShelf> queryWrapper = QueryGenerator.initQueryWrapper(warehouseShelf, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<WarehouseShelf> page = new Page<WarehouseShelf>(pageNo, pageSize);

        IPage<WarehouseShelf> pageList = warehouseShelfService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * Add to
     *
     * @param warehouseShelf
     * @return
     */
    @PostMapping(value = "/add")
    @AutoLog(value = "Add toKệ kho")
    @Operation(summary = "Add toKệ kho")
    public Result<?> add(@RequestBody WarehouseShelf warehouseShelf) {
        warehouseShelfService.save(warehouseShelf);
        return Result.OK("Add to成功！");
    }

    /**
     * edit
     *
     * @param warehouseShelf
     * @return
     */
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
    @AutoLog(value = "editKệ kho", operateType = 3)
    @Operation(summary = "editKệ kho")
    public Result<?> edit(@RequestBody WarehouseShelf warehouseShelf) {
        warehouseShelfService.updateById(warehouseShelf);
        return Result.OK("Update successful！");
    }

    /**
     * passiddelete
     *
     * @param id
     * @return
     */
    @AutoLog(value = "deleteKệ kho")
    @DeleteMapping(value = "/delete")
    @Operation(summary = "passIDdeleteKệ kho")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        warehouseShelfService.removeById(id);
        return Result.OK("delete成功!");
    }

    /**
     * 批量delete
     *
     * @param ids
     * @return
     */
    @DeleteMapping(value = "/deleteBatch")
    @Operation(summary = "批量deleteKệ kho")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.warehouseShelfService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量delete成功！");
    }

    /**
     * passidQuery
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/queryById")
    @Operation(summary = "passIDQueryKệ kho")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        WarehouseShelf warehouseShelf = warehouseShelfService.getById(id);
        return Result.OK(warehouseShelf);
    }

    /**
     * Exportexcel
     *
     * @param request
     */
    @RequestMapping(value = "/exportXls")
    @PermissionData(pageComponent = "warehouse/shelf/list")
    public ModelAndView exportXls(HttpServletRequest request, WarehouseShelf warehouseShelf) {
        return super.exportXls(request, warehouseShelf, WarehouseShelf.class, "Kệ kho");
    }

    /**
     * passexcelImport data
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, WarehouseShelf.class);
    }

    /**
     * Lấy danh sách kệ theo khu vực
     *
     * @param areaId
     * @return
     */
    @GetMapping(value = "/getByAreaId")
    @Operation(summary = "Lấy danh sách kệ theo khu vực")
    public Result<?> getByAreaId(@RequestParam(name = "areaId", required = true) String areaId) {
        List<WarehouseShelf> list = warehouseShelfService.getByAreaId(areaId);
        return Result.OK(list);
    }

    /**
     * Lấy danh sách kệ với thông tin khu vực
     *
     * @return
     */
    @GetMapping(value = "/getWithAreaInfo")
    @Operation(summary = "Lấy danh sách kệ với thông tin khu vực")
    public Result<?> getWithAreaInfo() {
        List<WarehouseShelf> list = warehouseShelfService.getWithAreaInfo();
        return Result.OK(list);
    }
}