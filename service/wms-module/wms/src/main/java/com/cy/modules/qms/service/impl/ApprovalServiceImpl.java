package com.cy.modules.qms.service.impl;

import com.cy.modules.qms.entity.ApprovalRecord;
import com.cy.modules.qms.entity.InspectionExecution;
import com.cy.modules.qms.entity.StepResult;
import com.cy.modules.qms.event.InspectionApprovedEvent;
import com.cy.modules.qms.mapper.ApprovalRecordMapper;
import com.cy.modules.qms.mapper.InspectionExecutionMapper;
import com.cy.modules.qms.mapper.StepResultMapper;
import com.cy.modules.qms.service.ApprovalService;
import com.cy.modules.qms.util.ExecutionStateMachine;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.system.vo.LoginUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Implementation ApprovalService.
 * Quản lý quy trình phê duyệt kết quả kiểm tra: approve, reject, re-inspect.
 *
 * @Author: QMS
 * @Date: 2026-03-15
 */
@Service
public class ApprovalServiceImpl implements ApprovalService {

    private static final Logger log = LoggerFactory.getLogger(ApprovalServiceImpl.class);

    @Autowired
    private InspectionExecutionMapper inspectionExecutionMapper;

    @Autowired
    private StepResultMapper stepResultMapper;

    @Autowired
    private ApprovalRecordMapper approvalRecordMapper;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(String executionId, String comment) {
        // 1. Validate execution tồn tại
        InspectionExecution execution = getExecutionOrThrow(executionId);

        // 2. Validate state transition: pending_approval → approved
        ExecutionStateMachine.validateTransition(execution.getStatus(), ExecutionStateMachine.STATUS_APPROVED);

        // 3. Lấy thông tin người phê duyệt
        String approver = getCurrentUsername();

        // 4. Cập nhật execution: status → approved, approvedBy, approvedTime
        execution.setStatus(ExecutionStateMachine.STATUS_APPROVED);
        execution.setApprovedBy(approver);
        execution.setApprovedTime(new Date());
        inspectionExecutionMapper.updateById(execution);

        // 5. Tạo ApprovalRecord
        ApprovalRecord record = new ApprovalRecord();
        record.setExecutionId(executionId);
        record.setAction("approve");
        record.setApprover(approver);
        record.setReason(comment);
        record.setActionTime(new Date());
        approvalRecordMapper.insert(record);

        // 6. Publish InspectionApprovedEvent để trigger WMS integration
        //    (release QC block + update defect count nếu FAIL)
        eventPublisher.publishEvent(new InspectionApprovedEvent(
                this, executionId, execution.getOverallResult(), execution.getWorkOrderId()));

        log.info("Execution {} đã được phê duyệt bởi {}", executionId, approver);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(String executionId, String reason) {
        // 1. Validate reason bắt buộc
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Lý do từ chối không được để trống");
        }

        // 2. Validate execution tồn tại
        InspectionExecution execution = getExecutionOrThrow(executionId);

        // 3. Validate state transition: pending_approval → rejected
        ExecutionStateMachine.validateTransition(execution.getStatus(), ExecutionStateMachine.STATUS_REJECTED);

        // 4. Lấy thông tin người phê duyệt
        String approver = getCurrentUsername();

        // 5. Cập nhật execution: status → rejected
        execution.setStatus(ExecutionStateMachine.STATUS_REJECTED);
        inspectionExecutionMapper.updateById(execution);

        // 6. Tạo ApprovalRecord
        ApprovalRecord record = new ApprovalRecord();
        record.setExecutionId(executionId);
        record.setAction("reject");
        record.setApprover(approver);
        record.setReason(reason);
        record.setActionTime(new Date());
        approvalRecordMapper.insert(record);

        // 7. Gửi notification cho Nhân_viên_QC
        notifyQcInspector(execution, "reject", reason);

        log.info("Execution {} đã bị từ chối bởi {}. Lý do: {}", executionId, approver, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reInspect(String executionId, String stepResultId, String reason) {
        // 1. Validate reason bắt buộc
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Lý do yêu cầu kiểm tra lại không được để trống");
        }

        // 2. Validate execution tồn tại
        InspectionExecution execution = getExecutionOrThrow(executionId);

        // 3. Validate state transition: pending_approval → in_progress
        ExecutionStateMachine.validateTransition(execution.getStatus(), ExecutionStateMachine.STATUS_IN_PROGRESS);

        // 4. Validate stepResult tồn tại và thuộc execution
        StepResult stepResult = stepResultMapper.selectById(stepResultId);
        if (stepResult == null) {
            throw new IllegalArgumentException("Không tìm thấy kết quả bước kiểm tra với ID: " + stepResultId);
        }
        if (!executionId.equals(stepResult.getExecutionId())) {
            throw new IllegalArgumentException(
                    "Kết quả bước kiểm tra không thuộc phiên kiểm tra này");
        }

        // 5. Lấy thông tin người phê duyệt
        String approver = getCurrentUsername();

        // 6. Reset step cụ thể: status → "re_inspect", result → null
        //    Các steps khác giữ nguyên trạng thái
        stepResult.setStatus("re_inspect");
        stepResult.setResult(null);
        stepResult.setCompletedTime(null);
        stepResultMapper.updateById(stepResult);

        // 7. Chuyển execution status → in_progress
        execution.setStatus(ExecutionStateMachine.STATUS_IN_PROGRESS);
        execution.setOverallResult(null);
        inspectionExecutionMapper.updateById(execution);

        // 8. Tạo ApprovalRecord
        ApprovalRecord record = new ApprovalRecord();
        record.setExecutionId(executionId);
        record.setStepResultId(stepResultId);
        record.setAction("re_inspect");
        record.setApprover(approver);
        record.setReason(reason);
        record.setActionTime(new Date());
        approvalRecordMapper.insert(record);

        // 9. Gửi notification cho Nhân_viên_QC
        notifyQcInspector(execution, "re_inspect", reason);

        log.info("Execution {} - Step {} yêu cầu kiểm tra lại bởi {}. Lý do: {}",
                executionId, stepResultId, approver, reason);
    }

    // ==================== Private Helper Methods ====================

    /**
     * Lấy execution theo ID hoặc throw exception nếu không tồn tại.
     */
    private InspectionExecution getExecutionOrThrow(String executionId) {
        if (executionId == null || executionId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID phiên kiểm tra không được để trống");
        }
        InspectionExecution execution = inspectionExecutionMapper.selectById(executionId);
        if (execution == null) {
            throw new IllegalArgumentException("Không tìm thấy phiên kiểm tra với ID: " + executionId);
        }
        return execution;
    }

    /**
     * Lấy username của người dùng hiện tại từ Shiro SecurityContext.
     * Trả về "system" nếu không có user đăng nhập (ví dụ: trong unit test).
     */
    private String getCurrentUsername() {
        try {
            LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (loginUser != null) {
                return loginUser.getUsername();
            }
        } catch (Exception e) {
            log.debug("Không thể lấy thông tin user hiện tại: {}", e.getMessage());
        }
        return "system";
    }

    /**
     * Gửi notification cho Nhân_viên_QC khi reject hoặc re-inspect.
     * Hiện tại chỉ log message, tích hợp notification thực tế sẽ được thêm sau.
     */
    private void notifyQcInspector(InspectionExecution execution, String action, String reason) {
        String inspector = execution.getInspector();
        String executionCode = execution.getExecutionCode();

        if ("reject".equals(action)) {
            log.info("[NOTIFICATION] Gửi thông báo từ chối cho Nhân_viên_QC '{}': " +
                            "Phiên kiểm tra '{}' đã bị từ chối. Lý do: {}",
                    inspector, executionCode, reason);
        } else if ("re_inspect".equals(action)) {
            log.info("[NOTIFICATION] Gửi thông báo yêu cầu kiểm tra lại cho Nhân_viên_QC '{}': " +
                            "Phiên kiểm tra '{}' cần kiểm tra lại. Lý do: {}",
                    inspector, executionCode, reason);
        }
    }
}
