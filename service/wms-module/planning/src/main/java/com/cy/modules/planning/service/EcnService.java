package com.cy.modules.planning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cy.modules.planning.entity.Ecn;
import com.cy.modules.planning.entity.EcnItem;

import java.util.List;
import java.util.Map;

/**
 * @Description: ECN Service
 * @Author: BMad
 * @Date: 2026-02-26
 */
public interface EcnService extends IService<Ecn> {

    /**
     * Lấy danh sách ECN theo BOM
     */
    List<Ecn> getByBomId(String bomId);

    /**
     * Lấy ECN theo trạng thái
     */
    List<Ecn> getByStatus(String status);

    /**
     * Lấy chi tiết ECN (bao gồm items và approvals)
     */
    Map<String, Object> getEcnDetail(String ecnId);

    /**
     * Tạo ECN mới với danh sách thay đổi
     */
    boolean createEcnWithItems(Ecn ecn, List<EcnItem> items, List<String> departments);

    /**
     * Gửi ECN để phê duyệt (draft → pending)
     */
    boolean submitForApproval(String ecnId);

    /**
     * Phê duyệt ECN từ một bộ phận
     */
    boolean approve(String ecnId, String department, String approverId, String approverName, String comments);

    /**
     * Từ chối ECN từ một bộ phận
     */
    boolean reject(String ecnId, String department, String approverId, String approverName, String comments);

    /**
     * Áp dụng ECN đã được duyệt vào BOM chính thức
     */
    boolean applyEcnToBom(String ecnId);

    /**
     * Kiểm tra mã ECN unique
     */
    boolean isCodeUnique(String ecnCode, String excludeId);
}
