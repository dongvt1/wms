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
import org.jeecg.modules.warehouse.entity.WarehouseSlot;
import org.jeecg.modules.warehouse.service.WarehouseSlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.List;

/**
 * @Description: vị trí trong kho
 * @Author: BMad
 * @Date:   2025-11-17
 * @Version: V1.0
 */
@Slf4j
@Tag(name = "Quản lý vị trí kho")
@RestController
@RequestMapping("/warehouse/slot")
public class WarehouseSlotController extends JeecgController<WarehouseSlot, WarehouseSlotService> {

    @Autowired
    private WarehouseSlotService warehouseSlotService;

    /**
     * Paginated list query
     *
     * @param warehouseSlot
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @Operation(summary = "GetVị trí khoData list")
    @GetMapping(value = "/list")
    @PermissionData(pageComponent = "warehouse/slot/list")
    public Result<?> list(WarehouseSlot warehouseSlot, 
                      @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, 
                      @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                      HttpServletRequest req) {
        QueryWrapper<WarehouseSlot> queryWrapper = QueryGenerator.initQueryWrapper(warehouseSlot, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<WarehouseSlot> page = new Page<WarehouseSlot>(pageNo, pageSize);

        IPage<WarehouseSlot> pageList = warehouseSlotService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * Add to
     *
     * @param warehouseSlot
     * @return
     */
    @PostMapping(value = "/add")
    @AutoLog(value = "Add toVị trí kho")
    @Operation(summary = "Add toVị trí kho")
    public Result<?> add(@RequestBody WarehouseSlot warehouseSlot) {
        warehouseSlotService.save(warehouseSlot);
        return Result.OK("Add to成功！");
    }

    /**
     * edit
     *
     * @param warehouseSlot
     * @return
     */
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
    @AutoLog(value = "editVị trí kho", operateType = 3)
    @Operation(summary = "editVị trí kho")
    public Result<?> edit(@RequestBody WarehouseSlot warehouseSlot) {
        warehouseSlotService.updateById(warehouseSlot);
        return Result.OK("Update successful！");
    }

    /**
     * passiddelete
     *
     * @param id
     * @return
     */
    @AutoLog(value = "deleteVị trí kho")
    @DeleteMapping(value = "/delete")
    @Operation(summary = "passIDdeleteVị trí kho")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        warehouseSlotService.removeById(id);
        return Result.OK("delete成功!");
    }

    /**
     * 批量delete
     *
     * @param ids
     * @return
     */
    @DeleteMapping(value = "/deleteBatch")
    @Operation(summary = "批量deleteVị trí kho")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.warehouseSlotService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量delete成功！");
    }

    /**
     * passidQuery
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/queryById")
    @Operation(summary = "passIDQueryVị trí kho")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        WarehouseSlot warehouseSlot = warehouseSlotService.getById(id);
        return Result.OK(warehouseSlot);
    }

    /**
     * Exportexcel
     *
     * @param request
     */
    @RequestMapping(value = "/exportXls")
    @PermissionData(pageComponent = "warehouse/slot/list")
    public ModelAndView exportXls(HttpServletRequest request, WarehouseSlot warehouseSlot) {
        return super.exportXls(request, warehouseSlot, WarehouseSlot.class, "Vị trí kho");
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
        return super.importExcel(request, response, WarehouseSlot.class);
    }

    /**
     * Lấy danh sách vị trí theo kệ
     *
     * @param shelfId
     * @return
     */
    @GetMapping(value = "/getByShelfId")
    @Operation(summary = "Lấy danh sách vị trí theo kệ")
    public Result<?> getByShelfId(@RequestParam(name = "shelfId", required = true) String shelfId) {
        List<WarehouseSlot> list = warehouseSlotService.getByShelfId(shelfId);
        return Result.OK(list);
    }

    /**
     * Lấy danh sách vị trí trống
     *
     * @return
     */
    @GetMapping(value = "/getEmptySlots")
    @Operation(summary = "Lấy danh sách vị trí trống")
    public Result<?> getEmptySlots() {
        List<WarehouseSlot> list = warehouseSlotService.getEmptySlots();
        return Result.OK(list);
    }

    /**
     * Gán sản phẩm vào vị trí
     *
     * @param id
     * @param productCode
     * @return
     */
    @PostMapping(value = "/assignProduct")
    @AutoLog(value = "Gán sản phẩm vào vị trí")
    @Operation(summary = "Gán sản phẩm vào vị trí")
    public Result<?> assignProduct(@RequestParam(name = "id", required = true) String id, 
                              @RequestParam(name = "productCode", required = true) String productCode) {
        boolean result = warehouseSlotService.assignProduct(id, productCode);
        if (result) {
            return Result.OK("Gán sản phẩm thành công！");
        } else {
            return Result.error("Gán sản phẩm thất bại！");
        }
    }

    /**
     * Xóa sản phẩm khỏi vị trí
     *
     * @param id
     * @return
     */
    @PostMapping(value = "/removeProduct")
    @AutoLog(value = "Xóa sản phẩm khỏi vị trí")
    @Operation(summary = "Xóa sản phẩm khỏi vị trí")
    public Result<?> removeProduct(@RequestParam(name = "id", required = true) String id) {
        boolean result = warehouseSlotService.removeProduct(id);
        if (result) {
            return Result.OK("Xóa sản phẩm thành công！");
        } else {
            return Result.error("Xóa sản phẩm thất bại！");
        }
    }

    /**
     * Di chuyển sản phẩm giữa các vị trí
     *
     * @param fromId
     * @param toId
     * @return
     */
    @PostMapping(value = "/moveProduct")
    @AutoLog(value = "Di chuyển sản phẩm giữa các vị trí")
    @Operation(summary = "Di chuyển sản phẩm giữa các vị trí")
    public Result<?> moveProduct(@RequestParam(name = "fromId", required = true) String fromId,
                             @RequestParam(name = "toId", required = true) String toId) {
        boolean result = warehouseSlotService.moveProduct(fromId, toId);
        if (result) {
            return Result.OK("Di chuyển sản phẩm thành công！");
        } else {
            return Result.error("Di chuyển sản phẩm thất bại！");
        }
    }

    /**
     * Tìm kiếm vị trí theo mã sản phẩm
     *
     * @param productCode
     * @return
     */
    @GetMapping(value = "/findByProductCode")
    @Operation(summary = "Tìm kiếm vị trí theo mã sản phẩm")
    public Result<?> findByProductCode(@RequestParam(name = "productCode", required = true) String productCode) {
        List<WarehouseSlot> list = warehouseSlotService.findByProductCode(productCode);
        return Result.OK(list);
    }

    /**
     * Lấy danh sách vị trí với thông tin kệ và khu vực
     *
     * @return
     */
    @GetMapping(value = "/getWithShelfAndAreaInfo")
    @Operation(summary = "Lấy danh sách vị trí với thông tin kệ và khu vực")
    public Result<?> getWithShelfAndAreaInfo() {
        List<WarehouseSlot> list = warehouseSlotService.getWithShelfAndAreaInfo();
        return Result.OK(list);
    }
}