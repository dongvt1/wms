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
import com.cy.modules.planning.entity.WorkCenter;
import com.cy.modules.planning.service.WorkCenterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.List;

/**
 * @Description: Work Center Controller – Trung tâm sản xuất
 * @Author: BMad
 * @Date: 2026-02-25
 * @Version: V1.0
 */
@Slf4j
@Tag(name = "Trung tâm sản xuất (Work Center)")
@RestController
@RequestMapping("/planning/workCenter")
public class WorkCenterController extends JeecgController<WorkCenter, WorkCenterService> {

    @Autowired
    private WorkCenterService workCenterService;

    @Operation(summary = "Danh sách trung tâm sản xuất")
    @GetMapping(value = "/list")
    public Result<?> list(WorkCenter workCenter,
                          @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                          @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                          HttpServletRequest req) {
        QueryWrapper<WorkCenter> queryWrapper = QueryGenerator.initQueryWrapper(workCenter, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<WorkCenter> page = new Page<>(pageNo, pageSize);
        IPage<WorkCenter> pageList = workCenterService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @PostMapping(value = "/add")
    @AutoLog(value = "Thêm trung tâm sản xuất")
    @Operation(summary = "Thêm trung tâm sản xuất")
    public Result<?> add(@RequestBody WorkCenter workCenter) {
        if (!workCenterService.isCodeUnique(workCenter.getCenterCode(), null)) {
            return Result.error("Mã trung tâm sản xuất đã tồn tại!");
        }
        workCenterService.save(workCenter);
        return Result.OK("Thêm thành công!");
    }

    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    @AutoLog(value = "Sửa trung tâm sản xuất", operateType = 3)
    @Operation(summary = "Sửa trung tâm sản xuất")
    public Result<?> edit(@RequestBody WorkCenter workCenter) {
        if (!workCenterService.isCodeUnique(workCenter.getCenterCode(), workCenter.getId())) {
            return Result.error("Mã trung tâm sản xuất đã tồn tại!");
        }
        workCenterService.updateById(workCenter);
        return Result.OK("Cập nhật thành công!");
    }

    @AutoLog(value = "Xóa trung tâm sản xuất")
    @DeleteMapping(value = "/delete")
    @Operation(summary = "Xóa trung tâm sản xuất")
    public Result<?> delete(@RequestParam(name = "id") String id) {
        workCenterService.removeById(id);
        return Result.OK("Xóa thành công!");
    }

    @DeleteMapping(value = "/deleteBatch")
    @Operation(summary = "Xóa hàng loạt trung tâm sản xuất")
    public Result<?> deleteBatch(@RequestParam(name = "ids") String ids) {
        this.workCenterService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("Xóa hàng loạt thành công!");
    }

    @GetMapping(value = "/queryById")
    @Operation(summary = "Xem chi tiết trung tâm sản xuất")
    public Result<?> queryById(@RequestParam(name = "id") String id) {
        WorkCenter workCenter = workCenterService.getById(id);
        return Result.OK(workCenter);
    }

    @GetMapping(value = "/getByStatus")
    @Operation(summary = "Lấy trung tâm theo trạng thái")
    public Result<?> getByStatus(@RequestParam(name = "status") String status) {
        List<WorkCenter> list = workCenterService.getByStatus(status);
        return Result.OK(list);
    }

    @GetMapping(value = "/listByLine")
    @Operation(summary = "Lấy trung tâm theo dây chuyền")
    public Result<?> listByLine(@RequestParam(name = "lineId") String lineId) {
        List<WorkCenter> list = workCenterService.getByProductionLineId(lineId);
        return Result.OK(list);
    }

    @GetMapping(value = "/listActive")
    @Operation(summary = "Lấy tất cả trung tâm đang hoạt động")
    public Result<?> listActive() {
        List<WorkCenter> list = workCenterService.getByStatus("active");
        return Result.OK(list);
    }

    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, WorkCenter workCenter) {
        return super.exportXls(request, workCenter, WorkCenter.class, "Trung tâm sản xuất");
    }

    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, WorkCenter.class);
    }
}
