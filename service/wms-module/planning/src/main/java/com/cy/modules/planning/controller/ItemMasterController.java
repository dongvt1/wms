package com.cy.modules.planning.controller;

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
import com.cy.modules.planning.entity.ApprovedManufacturer;
import com.cy.modules.planning.entity.ApprovedVendor;
import com.cy.modules.planning.entity.ItemMaster;
import com.cy.modules.planning.mapper.ApprovedManufacturerMapper;
import com.cy.modules.planning.mapper.ApprovedVendorMapper;
import com.cy.modules.planning.service.ItemMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Description: Item Master Controller (Danh mục linh kiện điện tử)
 * @Author: BMad
 * @Date: 2026-02-26
 * @Version: V1.0
 */
@Slf4j
@Tag(name = "Danh mục linh kiện điện tử (Item Master)")
@RestController
@RequestMapping("/planning/itemMaster")
public class ItemMasterController extends JeecgController<ItemMaster, ItemMasterService> {

    @Autowired
    private ItemMasterService itemMasterService;

    @Autowired
    private ApprovedManufacturerMapper approvedManufacturerMapper;

    @Autowired
    private ApprovedVendorMapper approvedVendorMapper;

    @Operation(summary = "Danh sách linh kiện")
    @GetMapping(value = "/list")
    public Result<?> list(ItemMaster itemMaster,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            HttpServletRequest req) {
        QueryWrapper<ItemMaster> queryWrapper = QueryGenerator.initQueryWrapper(itemMaster, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<ItemMaster> page = new Page<>(pageNo, pageSize);
        IPage<ItemMaster> pageList = itemMasterService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @PostMapping(value = "/add")
    @AutoLog(value = "Thêm linh kiện")
    @Operation(summary = "Thêm linh kiện")
    public Result<?> add(@RequestBody ItemMaster itemMaster) {
        if (!itemMasterService.isIpnUnique(itemMaster.getIpn(), null)) {
            return Result.error("Mã IPN đã tồn tại!");
        }
        itemMasterService.save(itemMaster);
        return Result.OK("Thêm linh kiện thành công!");
    }

    @RequestMapping(value = "/edit", method = { RequestMethod.PUT, RequestMethod.POST })
    @AutoLog(value = "Sửa linh kiện", operateType = 3)
    @Operation(summary = "Sửa linh kiện")
    public Result<?> edit(@RequestBody ItemMaster itemMaster) {
        if (!itemMasterService.isIpnUnique(itemMaster.getIpn(), itemMaster.getId())) {
            return Result.error("Mã IPN đã tồn tại!");
        }
        itemMasterService.updateById(itemMaster);
        return Result.OK("Cập nhật linh kiện thành công!");
    }

    @AutoLog(value = "Xóa linh kiện")
    @DeleteMapping(value = "/delete")
    @Operation(summary = "Xóa linh kiện")
    public Result<?> delete(@RequestParam(name = "id") String id) {
        itemMasterService.removeById(id);
        return Result.OK("Xóa thành công!");
    }

    @DeleteMapping(value = "/deleteBatch")
    @Operation(summary = "Xóa hàng loạt linh kiện")
    public Result<?> deleteBatch(@RequestParam(name = "ids") String ids) {
        itemMasterService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("Xóa hàng loạt thành công!");
    }

    @GetMapping(value = "/queryById")
    @Operation(summary = "Xem chi tiết linh kiện")
    public Result<?> queryById(@RequestParam(name = "id") String id) {
        ItemMaster item = itemMasterService.getById(id);
        return Result.OK(item);
    }

    @GetMapping(value = "/searchByMpn")
    @Operation(summary = "Tìm kiếm theo MPN")
    public Result<?> searchByMpn(@RequestParam(name = "mpn") String mpn) {
        List<ItemMaster> list = itemMasterService.searchByMpn(mpn);
        return Result.OK(list);
    }

    @GetMapping(value = "/searchByIpn")
    @Operation(summary = "Tìm kiếm theo IPN")
    public Result<?> searchByIpn(@RequestParam(name = "ipn") String ipn) {
        List<ItemMaster> list = itemMasterService.searchByIpn(ipn);
        return Result.OK(list);
    }

    @GetMapping(value = "/listByLifecycle")
    @Operation(summary = "Lọc theo vòng đời: active, obsolete, nrnd")
    public Result<?> listByLifecycle(@RequestParam(name = "status") String status) {
        List<ItemMaster> list = itemMasterService.getByLifecycle(status);
        return Result.OK(list);
    }

    @GetMapping(value = "/listByCategory")
    @Operation(summary = "Lọc theo danh mục linh kiện")
    public Result<?> listByCategory(@RequestParam(name = "category") String category) {
        List<ItemMaster> list = itemMasterService.getByCategory(category);
        return Result.OK(list);
    }

    @GetMapping(value = "/getAlternatives")
    @Operation(summary = "Lấy danh sách nhà sản xuất/nhà cung cấp thay thế (AML + AVL)")
    public Result<?> getAlternatives(@RequestParam(name = "id") String itemMasterId) {
        Map<String, Object> alternatives = itemMasterService.getAlternatives(itemMasterId);
        return Result.OK(alternatives);
    }

    // ---- AML endpoints ----

    @PostMapping(value = "/aml/add")
    @AutoLog(value = "Thêm nhà sản xuất được phê duyệt")
    @Operation(summary = "Thêm nhà sản xuất vào AML")
    public Result<?> addManufacturer(@RequestBody ApprovedManufacturer manufacturer) {
        approvedManufacturerMapper.insert(manufacturer);
        return Result.OK("Thêm nhà sản xuất thành công!");
    }

    @RequestMapping(value = "/aml/edit", method = { RequestMethod.PUT, RequestMethod.POST })
    @AutoLog(value = "Sửa nhà sản xuất AML", operateType = 3)
    @Operation(summary = "Sửa nhà sản xuất trong AML")
    public Result<?> editManufacturer(@RequestBody ApprovedManufacturer manufacturer) {
        approvedManufacturerMapper.updateById(manufacturer);
        return Result.OK("Cập nhật thành công!");
    }

    @DeleteMapping(value = "/aml/delete")
    @Operation(summary = "Xóa nhà sản xuất khỏi AML")
    public Result<?> deleteManufacturer(@RequestParam(name = "id") String id) {
        approvedManufacturerMapper.deleteById(id);
        return Result.OK("Xóa thành công!");
    }

    @GetMapping(value = "/aml/list")
    @Operation(summary = "Danh sách AML theo linh kiện")
    public Result<?> listManufacturers(@RequestParam(name = "itemMasterId") String itemMasterId) {
        List<ApprovedManufacturer> list = approvedManufacturerMapper.selectByItemMasterId(itemMasterId);
        return Result.OK(list);
    }

    // ---- AVL endpoints ----

    @PostMapping(value = "/avl/add")
    @AutoLog(value = "Thêm nhà cung cấp được phê duyệt")
    @Operation(summary = "Thêm nhà cung cấp vào AVL")
    public Result<?> addVendor(@RequestBody ApprovedVendor vendor) {
        approvedVendorMapper.insert(vendor);
        return Result.OK("Thêm nhà cung cấp thành công!");
    }

    @RequestMapping(value = "/avl/edit", method = { RequestMethod.PUT, RequestMethod.POST })
    @AutoLog(value = "Sửa nhà cung cấp AVL", operateType = 3)
    @Operation(summary = "Sửa nhà cung cấp trong AVL")
    public Result<?> editVendor(@RequestBody ApprovedVendor vendor) {
        approvedVendorMapper.updateById(vendor);
        return Result.OK("Cập nhật thành công!");
    }

    @DeleteMapping(value = "/avl/delete")
    @Operation(summary = "Xóa nhà cung cấp khỏi AVL")
    public Result<?> deleteVendor(@RequestParam(name = "id") String id) {
        approvedVendorMapper.deleteById(id);
        return Result.OK("Xóa thành công!");
    }

    @GetMapping(value = "/avl/list")
    @Operation(summary = "Danh sách AVL theo linh kiện")
    public Result<?> listVendors(@RequestParam(name = "itemMasterId") String itemMasterId) {
        List<ApprovedVendor> list = approvedVendorMapper.selectByItemMasterId(itemMasterId);
        return Result.OK(list);
    }

    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, ItemMaster itemMaster) {
        return super.exportXls(request, itemMaster, ItemMaster.class, "ItemMaster");
    }

    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, ItemMaster.class);
    }
}
