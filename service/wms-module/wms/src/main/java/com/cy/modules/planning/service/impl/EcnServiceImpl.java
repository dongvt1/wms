package com.cy.modules.planning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cy.modules.common.entity.Bom;
import com.cy.modules.common.entity.BomItem;
import com.cy.modules.common.entity.BomRevision;
import com.cy.modules.planning.entity.Ecn;
import com.cy.modules.planning.entity.EcnApproval;
import com.cy.modules.planning.entity.EcnItem;
import com.cy.modules.planning.mapper.*;
import com.cy.modules.planning.service.EcnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @Description: ECN Service Implementation
 * @Author: BMad
 * @Date: 2026-02-26
 */
@Service
public class EcnServiceImpl extends ServiceImpl<EcnMapper, Ecn> implements EcnService {

    @Autowired
    private EcnItemMapper ecnItemMapper;

    @Autowired
    private EcnApprovalMapper ecnApprovalMapper;

    @Autowired
    private BomItemMapper bomItemMapper;

    @Autowired
    private BomRevisionMapper bomRevisionMapper;

    @Autowired
    private BomMapper bomMapper;

    @Override
    public List<Ecn> getByBomId(String bomId) {
        return baseMapper.selectByBomId(bomId);
    }

    @Override
    public List<Ecn> getByStatus(String status) {
        return baseMapper.selectByStatus(status);
    }

    @Override
    public Map<String, Object> getEcnDetail(String ecnId) {
        Map<String, Object> result = new HashMap<>();
        Ecn ecn = this.getById(ecnId);
        result.put("ecn", ecn);
        List<EcnItem> items = ecnItemMapper.selectByEcnId(ecnId);
        result.put("items", items);
        List<EcnApproval> approvals = ecnApprovalMapper.selectByEcnId(ecnId);
        result.put("approvals", approvals);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createEcnWithItems(Ecn ecn, List<EcnItem> items, List<String> departments) {
        ecn.setStatus("draft");
        this.save(ecn);

        // Lưu các dòng thay đổi
        if (items != null) {
            for (EcnItem item : items) {
                item.setEcnId(ecn.getId());
                ecnItemMapper.insert(item);
            }
        }

        // Tạo bản ghi approval cho mỗi bộ phận
        if (departments != null) {
            for (String dept : departments) {
                EcnApproval approval = new EcnApproval();
                approval.setEcnId(ecn.getId());
                approval.setDepartment(dept);
                approval.setStatus("pending");
                ecnApprovalMapper.insert(approval);
            }
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitForApproval(String ecnId) {
        Ecn ecn = this.getById(ecnId);
        if (ecn == null || !"draft".equals(ecn.getStatus())) {
            return false;
        }
        ecn.setStatus("pending");
        return this.updateById(ecn);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approve(String ecnId, String department, String approverId, String approverName, String comments) {
        QueryWrapper<EcnApproval> qw = new QueryWrapper<>();
        qw.eq("ecn_id", ecnId).eq("department", department);
        EcnApproval approval = ecnApprovalMapper.selectOne(qw);
        if (approval == null)
            return false;

        approval.setStatus("approved");
        approval.setApproverId(approverId);
        approval.setApproverName(approverName);
        approval.setComments(comments);
        approval.setApprovedDate(new Date());
        ecnApprovalMapper.updateById(approval);

        // Kiểm tra nếu tất cả bộ phận đã phê duyệt → cập nhật ECN status = approved
        checkAllApproved(ecnId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reject(String ecnId, String department, String approverId, String approverName, String comments) {
        QueryWrapper<EcnApproval> qw = new QueryWrapper<>();
        qw.eq("ecn_id", ecnId).eq("department", department);
        EcnApproval approval = ecnApprovalMapper.selectOne(qw);
        if (approval == null)
            return false;

        approval.setStatus("rejected");
        approval.setApproverId(approverId);
        approval.setApproverName(approverName);
        approval.setComments(comments);
        approval.setApprovedDate(new Date());
        ecnApprovalMapper.updateById(approval);

        // Nếu bất kỳ bộ phận nào reject → ECN bị rejected
        Ecn ecn = this.getById(ecnId);
        ecn.setStatus("rejected");
        this.updateById(ecn);
        return true;
    }

    private void checkAllApproved(String ecnId) {
        List<EcnApproval> approvals = ecnApprovalMapper.selectByEcnId(ecnId);
        boolean allApproved = approvals.stream().allMatch(a -> "approved".equals(a.getStatus()));
        if (allApproved) {
            Ecn ecn = this.getById(ecnId);
            ecn.setStatus("approved");
            ecn.setApprovedDate(new Date());
            this.updateById(ecn);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean applyEcnToBom(String ecnId) {
        Ecn ecn = this.getById(ecnId);
        if (ecn == null || !"approved".equals(ecn.getStatus())) {
            return false;
        }

        String bomId = ecn.getBomId();
        List<EcnItem> changes = ecnItemMapper.selectByEcnId(ecnId);

        // Tạo revision snapshot trước khi thay đổi
        Bom bom = bomMapper.selectById(bomId);
        if (bom != null) {
            BomRevision revision = new BomRevision();
            revision.setBomId(bomId);
            revision.setRevisionCode(ecn.getFromRevision() != null ? ecn.getFromRevision() : bom.getVersion());
            revision.setReason("Before ECN: " + ecn.getEcnCode());
            revision.setStatus("superseded");
            revision.setCreatedByEcn(ecnId);

            // Snapshot dữ liệu BOM hiện tại
            List<BomItem> currentItems = bomItemMapper.selectByBomId(bomId);
            StringBuilder snapshot = new StringBuilder();
            snapshot.append("{\"bom\":").append(toJson(bom));
            snapshot.append(",\"items\":").append(toJsonArray(currentItems));
            snapshot.append("}");
            revision.setSnapshotData(snapshot.toString());
            bomRevisionMapper.insert(revision);
        }

        // Áp dụng các thay đổi
        for (EcnItem change : changes) {
            switch (change.getChangeType()) {
                case "add":
                    BomItem newItem = new BomItem();
                    newItem.setBomId(bomId);
                    newItem.setMaterialId(change.getNewMaterialId());
                    newItem.setQuantity(change.getNewQuantity());
                    newItem.setItemType("raw_material");
                    bomItemMapper.insert(newItem);
                    break;
                case "remove":
                    if (change.getBomItemId() != null) {
                        bomItemMapper.deleteById(change.getBomItemId());
                    }
                    break;
                case "modify":
                    if (change.getBomItemId() != null) {
                        BomItem existing = bomItemMapper.selectById(change.getBomItemId());
                        if (existing != null) {
                            if (change.getNewMaterialId() != null) {
                                existing.setMaterialId(change.getNewMaterialId());
                            }
                            if (change.getNewQuantity() != null) {
                                existing.setQuantity(change.getNewQuantity());
                            }
                            bomItemMapper.updateById(existing);
                        }
                    }
                    break;
            }
        }

        // Cập nhật version BOM
        if (bom != null && ecn.getToRevision() != null) {
            bom.setVersion(ecn.getToRevision());
            bomMapper.updateById(bom);
        }

        // Đánh dấu ECN đã áp dụng
        ecn.setStatus("applied");
        ecn.setAppliedDate(new Date());
        this.updateById(ecn);

        return true;
    }

    @Override
    public boolean isCodeUnique(String ecnCode, String excludeId) {
        QueryWrapper<Ecn> qw = new QueryWrapper<>();
        qw.eq("ecn_code", ecnCode);
        if (excludeId != null) {
            qw.ne("id", excludeId);
        }
        return count(qw) == 0;
    }

    /** Simple JSON helper – production code nên dùng Jackson/Gson */
    private String toJson(Object obj) {
        if (obj == null)
            return "null";
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }

    private String toJsonArray(List<?> list) {
        if (list == null)
            return "[]";
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writeValueAsString(list);
        } catch (Exception e) {
            return "[]";
        }
    }
}
