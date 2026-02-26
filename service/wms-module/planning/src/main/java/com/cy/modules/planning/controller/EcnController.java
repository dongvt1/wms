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
import com.cy.modules.planning.entity.Ecn;
import com.cy.modules.planning.entity.EcnItem;
import com.cy.modules.planning.service.BomService;
import com.cy.modules.planning.service.EcnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Description: ECN Controller (Engineering Change Notice)
 * @Author: BMad
 * @Date: 2026-02-26
 * @Version: V1.0
 */
@Slf4j
@Tag(name = "Engineering Change Notice (ECN)")
@RestController
@RequestMapping("/planning/ecn")
public class EcnController extends JeecgController<Ecn, EcnService> {

    @Autowired
    private EcnService ecnService;

    @Autowired
    private BomService bomService;

    @Operation(summary = "Danh sách ECN")
    @GetMapping(value = "/list")
    public Result<?> list(Ecn ecn,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            HttpServletRequest req) {
        QueryWrapper<Ecn> queryWrapper = QueryGenerator.initQueryWrapper(ecn, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<Ecn> page = new Page<>(pageNo, pageSize);
        IPage<Ecn> pageList = ecnService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @SuppressWarnings("unchecked")
    @PostMapping(value = "/add")
    @AutoLog(value = "Tạo ECN")
    @Operation(summary = "Tạo ECN mới")
    public Result<?> add(@RequestBody Map<String, Object> requestBody) {
        Ecn ecn = extractEcn(requestBody);
        List<EcnItem> items = extractEcnItems(requestBody);
        List<String> departments = (List<String>) requestBody.get("departments");

        if (!ecnService.isCodeUnique(ecn.getEcnCode(), null)) {
            return Result.error("Mã ECN đã tồn tại!");
        }

        if (departments == null || departments.isEmpty()) {
            departments = Arrays.asList("production", "procurement", "quality");
        }

        ecnService.createEcnWithItems(ecn, items, departments);
        return Result.OK("Tạo ECN thành công!");
    }

    @PostMapping(value = "/submitForApproval")
    @AutoLog(value = "Gửi ECN để phê duyệt")
    @Operation(summary = "Gửi ECN để phê duyệt (draft → pending)")
    public Result<?> submitForApproval(@RequestParam(name = "id") String ecnId) {
        boolean result = ecnService.submitForApproval(ecnId);
        if (!result) {
            return Result.error("Không thể gửi phê duyệt! ECN phải ở trạng thái draft.");
        }
        return Result.OK("Đã gửi ECN để phê duyệt!");
    }

    @PostMapping(value = "/approve")
    @AutoLog(value = "Phê duyệt ECN")
    @Operation(summary = "Phê duyệt ECN từ một bộ phận")
    public Result<?> approve(@RequestParam(name = "ecnId") String ecnId,
            @RequestParam(name = "department") String department,
            @RequestParam(name = "approverId") String approverId,
            @RequestParam(name = "approverName") String approverName,
            @RequestParam(name = "comments", required = false) String comments) {
        boolean result = ecnService.approve(ecnId, department, approverId, approverName, comments);
        if (!result) {
            return Result.error("Không tìm thấy bản ghi phê duyệt cho bộ phận này!");
        }
        return Result.OK("Đã phê duyệt thành công!");
    }

    @PostMapping(value = "/reject")
    @AutoLog(value = "Từ chối ECN")
    @Operation(summary = "Từ chối ECN từ một bộ phận")
    public Result<?> reject(@RequestParam(name = "ecnId") String ecnId,
            @RequestParam(name = "department") String department,
            @RequestParam(name = "approverId") String approverId,
            @RequestParam(name = "approverName") String approverName,
            @RequestParam(name = "comments", required = false) String comments) {
        boolean result = ecnService.reject(ecnId, department, approverId, approverName, comments);
        if (!result) {
            return Result.error("Không tìm thấy bản ghi phê duyệt cho bộ phận này!");
        }
        return Result.OK("Đã từ chối ECN!");
    }

    @PostMapping(value = "/applyToBom")
    @AutoLog(value = "Áp dụng ECN vào BOM")
    @Operation(summary = "Áp dụng ECN đã duyệt vào BOM chính thức")
    public Result<?> applyToBom(@RequestParam(name = "ecnId") String ecnId) {
        boolean result = ecnService.applyEcnToBom(ecnId);
        if (!result) {
            return Result.error("Không thể áp dụng! ECN phải ở trạng thái approved.");
        }
        return Result.OK("Đã áp dụng ECN vào BOM thành công!");
    }

    @GetMapping(value = "/getDetail")
    @Operation(summary = "Xem chi tiết ECN (gồm items + approvals)")
    public Result<?> getDetail(@RequestParam(name = "id") String ecnId) {
        Map<String, Object> detail = ecnService.getEcnDetail(ecnId);
        return Result.OK(detail);
    }

    @GetMapping(value = "/getByBom")
    @Operation(summary = "Lấy danh sách ECN theo BOM")
    public Result<?> getByBom(@RequestParam(name = "bomId") String bomId) {
        List<Ecn> list = ecnService.getByBomId(bomId);
        return Result.OK(list);
    }

    @GetMapping(value = "/listByStatus")
    @Operation(summary = "Lấy ECN theo trạng thái")
    public Result<?> listByStatus(@RequestParam(name = "status") String status) {
        List<Ecn> list = ecnService.getByStatus(status);
        return Result.OK(list);
    }

    @GetMapping(value = "/compareBom")
    @Operation(summary = "So sánh 2 phiên bản BOM")
    public Result<?> compareBom(@RequestParam(name = "revisionId1") String revisionId1,
            @RequestParam(name = "revisionId2") String revisionId2) {
        Map<String, Object> comparison = bomService.compareBomRevisions(revisionId1, revisionId2);
        return Result.OK(comparison);
    }

    @AutoLog(value = "Xóa ECN")
    @DeleteMapping(value = "/delete")
    @Operation(summary = "Xóa ECN")
    public Result<?> delete(@RequestParam(name = "id") String id) {
        ecnService.removeById(id);
        return Result.OK("Xóa thành công!");
    }

    @DeleteMapping(value = "/deleteBatch")
    @Operation(summary = "Xóa hàng loạt ECN")
    public Result<?> deleteBatch(@RequestParam(name = "ids") String ids) {
        ecnService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("Xóa hàng loạt thành công!");
    }

    // ==== Helper methods ====

    @SuppressWarnings("unchecked")
    private Ecn extractEcn(Map<String, Object> body) {
        Map<String, Object> ecnMap = (Map<String, Object>) body.get("ecn");
        Ecn ecn = new Ecn();
        if (ecnMap != null) {
            ecn.setId((String) ecnMap.get("id"));
            ecn.setEcnCode((String) ecnMap.get("ecnCode"));
            ecn.setTitle((String) ecnMap.get("title"));
            ecn.setDescription((String) ecnMap.get("description"));
            ecn.setBomId((String) ecnMap.get("bomId"));
            ecn.setFromRevision((String) ecnMap.get("fromRevision"));
            ecn.setToRevision((String) ecnMap.get("toRevision"));
            ecn.setRequestedBy((String) ecnMap.get("requestedBy"));
        }
        return ecn;
    }

    @SuppressWarnings("unchecked")
    private List<EcnItem> extractEcnItems(Map<String, Object> body) {
        List<Map<String, Object>> itemMaps = (List<Map<String, Object>>) body.get("items");
        List<EcnItem> items = new java.util.ArrayList<>();
        if (itemMaps != null) {
            for (Map<String, Object> m : itemMaps) {
                EcnItem item = new EcnItem();
                item.setChangeType((String) m.get("changeType"));
                item.setBomItemId((String) m.get("bomItemId"));
                item.setOldMaterialId((String) m.get("oldMaterialId"));
                item.setNewMaterialId((String) m.get("newMaterialId"));
                item.setReason((String) m.get("reason"));
                if (m.get("oldQuantity") != null) {
                    item.setOldQuantity(new java.math.BigDecimal(m.get("oldQuantity").toString()));
                }
                if (m.get("newQuantity") != null) {
                    item.setNewQuantity(new java.math.BigDecimal(m.get("newQuantity").toString()));
                }
                items.add(item);
            }
        }
        return items;
    }
}
