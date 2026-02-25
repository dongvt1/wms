package org.jeecg.modules.planning.controller;

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
import org.jeecg.modules.planning.entity.Routing;
import org.jeecg.modules.planning.entity.RoutingStep;
import org.jeecg.modules.planning.service.RoutingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Description: Routing Controller – Quy trình công nghệ
 * @Author: BMad
 * @Date: 2026-02-25
 * @Version: V1.0
 */
@Slf4j
@Tag(name = "Quy trình công nghệ (Routing)")
@RestController
@RequestMapping("/planning/routing")
public class RoutingController extends JeecgController<Routing, RoutingService> {

    @Autowired
    private RoutingService routingService;

    @Operation(summary = "Danh sách quy trình công nghệ")
    @GetMapping(value = "/list")
    public Result<?> list(Routing routing,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            HttpServletRequest req) {
        QueryWrapper<Routing> queryWrapper = QueryGenerator.initQueryWrapper(routing, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<Routing> page = new Page<>(pageNo, pageSize);
        IPage<Routing> pageList = routingService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @PostMapping(value = "/add")
    @AutoLog(value = "Thêm quy trình công nghệ")
    @Operation(summary = "Thêm quy trình công nghệ kèm các bước")
    public Result<?> add(@RequestBody Map<String, Object> requestBody) {
        Routing routing = extractRouting(requestBody);
        List<RoutingStep> steps = extractRoutingSteps(requestBody);
        if (!routingService.isCodeUnique(routing.getRoutingCode(), null)) {
            return Result.error("Mã quy trình đã tồn tại!");
        }
        routingService.saveRoutingWithSteps(routing, steps);
        return Result.OK("Thêm quy trình thành công!");
    }

    @RequestMapping(value = "/edit", method = { RequestMethod.PUT, RequestMethod.POST })
    @AutoLog(value = "Sửa quy trình công nghệ", operateType = 3)
    @Operation(summary = "Sửa quy trình công nghệ kèm các bước")
    public Result<?> edit(@RequestBody Map<String, Object> requestBody) {
        Routing routing = extractRouting(requestBody);
        List<RoutingStep> steps = extractRoutingSteps(requestBody);
        if (!routingService.isCodeUnique(routing.getRoutingCode(), routing.getId())) {
            return Result.error("Mã quy trình đã tồn tại!");
        }
        routingService.updateRoutingWithSteps(routing, steps);
        return Result.OK("Cập nhật quy trình thành công!");
    }

    @AutoLog(value = "Xóa quy trình công nghệ")
    @DeleteMapping(value = "/delete")
    @Operation(summary = "Xóa quy trình công nghệ")
    public Result<?> delete(@RequestParam(name = "id") String id) {
        routingService.removeById(id);
        return Result.OK("Xóa thành công!");
    }

    @DeleteMapping(value = "/deleteBatch")
    @Operation(summary = "Xóa hàng loạt quy trình")
    public Result<?> deleteBatch(@RequestParam(name = "ids") String ids) {
        this.routingService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("Xóa hàng loạt thành công!");
    }

    @GetMapping(value = "/queryById")
    @Operation(summary = "Xem chi tiết quy trình")
    public Result<?> queryById(@RequestParam(name = "id") String id) {
        Map<String, Object> detail = routingService.getRoutingDetail(id);
        return Result.OK(detail);
    }

    @GetMapping(value = "/getSteps")
    @Operation(summary = "Lấy danh sách bước của quy trình")
    public Result<?> getSteps(@RequestParam(name = "routingId") String routingId) {
        List<RoutingStep> steps = routingService.getRoutingSteps(routingId);
        return Result.OK(steps);
    }

    @GetMapping(value = "/getByProductId")
    @Operation(summary = "Lấy quy trình theo sản phẩm")
    public Result<?> getByProductId(@RequestParam(name = "productId") String productId) {
        List<Routing> list = routingService.getByProductId(productId);
        return Result.OK(list);
    }

    @GetMapping(value = "/listActive")
    @Operation(summary = "Lấy tất cả quy trình đang hoạt động")
    public Result<?> listActive() {
        List<Routing> list = routingService.getByStatus("active");
        return Result.OK(list);
    }

    @GetMapping(value = "/calculateLeadTime")
    @Operation(summary = "Tính tổng lead time sản xuất cho số lượng cho trước")
    public Result<?> calculateLeadTime(@RequestParam(name = "routingId") String routingId,
            @RequestParam(name = "quantity", defaultValue = "1") BigDecimal quantity) {
        Routing routing = routingService.getById(routingId);
        if (routing == null) {
            return Result.error("Không tìm thấy quy trình!");
        }
        BigDecimal totalLeadTimeHours = routingService.calculateTotalLeadTime(routingId, quantity);
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("routingId", routingId);
        result.put("routingName", routing.getRoutingName());
        result.put("quantity", quantity);
        result.put("totalLeadTimeHours", totalLeadTimeHours);
        result.put("totalLeadTimeDays",
                totalLeadTimeHours.divide(new BigDecimal("8"), 2, java.math.RoundingMode.HALF_UP));
        return Result.OK(result);
    }

    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, Routing routing) {
        return super.exportXls(request, routing, Routing.class, "Quy trình công nghệ");
    }

    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, Routing.class);
    }

    @SuppressWarnings("unchecked")
    private Routing extractRouting(Map<String, Object> body) {
        Map<String, Object> rMap = body.containsKey("routing")
                ? (Map<String, Object>) body.get("routing")
                : body;
        Routing routing = new Routing();
        routing.setId((String) rMap.get("id"));
        routing.setRoutingCode((String) rMap.get("routingCode"));
        routing.setRoutingName((String) rMap.get("routingName"));
        routing.setProductId((String) rMap.get("productId"));
        routing.setBomId((String) rMap.get("bomId"));
        routing.setVersion((String) rMap.getOrDefault("version", "1.0"));
        routing.setStatus((String) rMap.getOrDefault("status", "active"));
        routing.setNotes((String) rMap.get("notes"));
        return routing;
    }

    @SuppressWarnings("unchecked")
    private List<RoutingStep> extractRoutingSteps(Map<String, Object> body) {
        List<Map<String, Object>> stepMaps = (List<Map<String, Object>>) body.get("steps");
        List<RoutingStep> steps = new java.util.ArrayList<>();
        if (stepMaps != null) {
            for (Map<String, Object> m : stepMaps) {
                RoutingStep step = new RoutingStep();
                step.setId((String) m.get("id"));
                step.setStepName((String) m.get("stepName"));
                step.setWorkCenterId((String) m.get("workCenterId"));
                step.setDescription((String) m.get("description"));
                if (m.get("stepOrder") != null)
                    step.setStepOrder(Integer.parseInt(m.get("stepOrder").toString()));
                if (m.get("setupTimeMinutes") != null)
                    step.setSetupTimeMinutes(Integer.parseInt(m.get("setupTimeMinutes").toString()));
                if (m.get("runTimeMinutes") != null)
                    step.setRunTimeMinutes(Integer.parseInt(m.get("runTimeMinutes").toString()));
                if (m.get("waitTimeMinutes") != null)
                    step.setWaitTimeMinutes(Integer.parseInt(m.get("waitTimeMinutes").toString()));
                if (m.get("moveTimeMinutes") != null)
                    step.setMoveTimeMinutes(Integer.parseInt(m.get("moveTimeMinutes").toString()));
                steps.add(step);
            }
        }
        return steps;
    }
}
