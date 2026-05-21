package com.cy.modules.qms.util;

import java.util.*;

/**
 * State machine cho Inspection Execution status.
 * <p>
 * Định nghĩa các trạng thái hợp lệ và transitions giữa chúng:
 * <ul>
 *   <li>draft → in_progress (bắt đầu nhập liệu)</li>
 *   <li>in_progress → in_progress (lưu nháp)</li>
 *   <li>in_progress → pending_approval (submit khi tất cả bước bắt buộc hoàn thành)</li>
 *   <li>pending_approval → approved (Quản lý QC phê duyệt)</li>
 *   <li>pending_approval → rejected (Quản lý QC từ chối)</li>
 *   <li>pending_approval → in_progress (yêu cầu kiểm tra lại)</li>
 * </ul>
 *
 * @Author: QMS
 * @Date: 2026-03-15
 */
public class ExecutionStateMachine {

    /** Trạng thái hợp lệ */
    public static final String STATUS_DRAFT = "draft";
    public static final String STATUS_IN_PROGRESS = "in_progress";
    public static final String STATUS_PENDING_APPROVAL = "pending_approval";
    public static final String STATUS_APPROVED = "approved";
    public static final String STATUS_REJECTED = "rejected";

    /** Map chứa tất cả transitions hợp lệ: key = currentStatus, value = set of valid target statuses */
    private static final Map<String, Set<String>> VALID_TRANSITIONS;

    static {
        Map<String, Set<String>> transitions = new HashMap<>();
        transitions.put(STATUS_DRAFT, new HashSet<>(Collections.singletonList(STATUS_IN_PROGRESS)));
        transitions.put(STATUS_IN_PROGRESS, new HashSet<>(Arrays.asList(STATUS_IN_PROGRESS, STATUS_PENDING_APPROVAL)));
        transitions.put(STATUS_PENDING_APPROVAL, new HashSet<>(Arrays.asList(STATUS_APPROVED, STATUS_REJECTED, STATUS_IN_PROGRESS)));
        transitions.put(STATUS_APPROVED, Collections.emptySet());
        transitions.put(STATUS_REJECTED, Collections.emptySet());
        VALID_TRANSITIONS = Collections.unmodifiableMap(transitions);
    }

    private ExecutionStateMachine() {
        // utility class — no instantiation
    }

    /**
     * Validate xem transition từ currentStatus sang targetStatus có hợp lệ không.
     *
     * @param currentStatus trạng thái hiện tại của execution
     * @param targetStatus  trạng thái muốn chuyển sang
     * @return true nếu transition hợp lệ
     * @throws IllegalStateException nếu transition không hợp lệ, kèm thông báo mô tả
     * @throws IllegalArgumentException nếu currentStatus hoặc targetStatus là null/không hợp lệ
     */
    public static boolean validateTransition(String currentStatus, String targetStatus) {
        if (currentStatus == null || currentStatus.isEmpty()) {
            throw new IllegalArgumentException("Trạng thái hiện tại không được để trống");
        }
        if (targetStatus == null || targetStatus.isEmpty()) {
            throw new IllegalArgumentException("Trạng thái đích không được để trống");
        }

        Set<String> allowedTargets = VALID_TRANSITIONS.get(currentStatus);
        if (allowedTargets == null) {
            throw new IllegalArgumentException(
                    String.format("Trạng thái '%s' không phải là trạng thái hợp lệ của Inspection Execution", currentStatus));
        }

        if (!VALID_TRANSITIONS.containsKey(targetStatus) && !isTerminalStatus(targetStatus)) {
            throw new IllegalArgumentException(
                    String.format("Trạng thái đích '%s' không phải là trạng thái hợp lệ của Inspection Execution", targetStatus));
        }

        if (!allowedTargets.contains(targetStatus)) {
            throw new IllegalStateException(
                    String.format("Không thể chuyển trạng thái từ '%s' sang '%s'. Các trạng thái hợp lệ từ '%s': %s",
                            currentStatus, targetStatus, currentStatus,
                            allowedTargets.isEmpty() ? "(không có - trạng thái kết thúc)" : allowedTargets));
        }

        return true;
    }

    /**
     * Kiểm tra xem transition có hợp lệ không (không throw exception).
     *
     * @param currentStatus trạng thái hiện tại
     * @param targetStatus  trạng thái đích
     * @return true nếu transition hợp lệ, false nếu không
     */
    public static boolean isValidTransition(String currentStatus, String targetStatus) {
        if (currentStatus == null || targetStatus == null) {
            return false;
        }
        Set<String> allowedTargets = VALID_TRANSITIONS.get(currentStatus);
        return allowedTargets != null && allowedTargets.contains(targetStatus);
    }

    /**
     * Lấy danh sách trạng thái đích hợp lệ từ trạng thái hiện tại.
     *
     * @param currentStatus trạng thái hiện tại
     * @return set các trạng thái có thể chuyển sang, hoặc empty set nếu là trạng thái kết thúc
     */
    public static Set<String> getValidTargets(String currentStatus) {
        if (currentStatus == null) {
            return Collections.emptySet();
        }
        Set<String> targets = VALID_TRANSITIONS.get(currentStatus);
        return targets != null ? Collections.unmodifiableSet(targets) : Collections.emptySet();
    }

    /**
     * Kiểm tra xem trạng thái có phải là trạng thái kết thúc (terminal) không.
     * Trạng thái kết thúc không thể chuyển sang trạng thái khác.
     *
     * @param status trạng thái cần kiểm tra
     * @return true nếu là trạng thái kết thúc (approved hoặc rejected)
     */
    public static boolean isTerminalStatus(String status) {
        return STATUS_APPROVED.equals(status) || STATUS_REJECTED.equals(status);
    }

    /**
     * Kiểm tra xem một chuỗi có phải là trạng thái hợp lệ không.
     *
     * @param status chuỗi cần kiểm tra
     * @return true nếu là trạng thái hợp lệ
     */
    public static boolean isValidStatus(String status) {
        return status != null && VALID_TRANSITIONS.containsKey(status);
    }
}
