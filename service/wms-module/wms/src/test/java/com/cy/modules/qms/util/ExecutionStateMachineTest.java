package com.cy.modules.qms.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests cho ExecutionStateMachine.
 * Validates: Requirements 8.6
 */
@DisplayName("ExecutionStateMachine")
class ExecutionStateMachineTest {

    @Nested
    @DisplayName("validateTransition - valid transitions")
    class ValidTransitions {

        @Test
        @DisplayName("draft → in_progress: bắt đầu nhập liệu")
        void draftToInProgress() {
            assertTrue(ExecutionStateMachine.validateTransition("draft", "in_progress"));
        }

        @Test
        @DisplayName("in_progress → in_progress: lưu nháp")
        void inProgressToInProgress() {
            assertTrue(ExecutionStateMachine.validateTransition("in_progress", "in_progress"));
        }

        @Test
        @DisplayName("in_progress → pending_approval: submit")
        void inProgressToPendingApproval() {
            assertTrue(ExecutionStateMachine.validateTransition("in_progress", "pending_approval"));
        }

        @Test
        @DisplayName("pending_approval → approved: phê duyệt")
        void pendingApprovalToApproved() {
            assertTrue(ExecutionStateMachine.validateTransition("pending_approval", "approved"));
        }

        @Test
        @DisplayName("pending_approval → rejected: từ chối")
        void pendingApprovalToRejected() {
            assertTrue(ExecutionStateMachine.validateTransition("pending_approval", "rejected"));
        }

        @Test
        @DisplayName("pending_approval → in_progress: yêu cầu kiểm tra lại")
        void pendingApprovalToInProgress() {
            assertTrue(ExecutionStateMachine.validateTransition("pending_approval", "in_progress"));
        }
    }

    @Nested
    @DisplayName("validateTransition - invalid transitions")
    class InvalidTransitions {

        @ParameterizedTest(name = "{0} → {1} should be rejected")
        @CsvSource({
                "draft, pending_approval",
                "draft, approved",
                "draft, rejected",
                "draft, draft",
                "in_progress, draft",
                "in_progress, approved",
                "in_progress, rejected",
                "pending_approval, draft",
                "pending_approval, pending_approval",
                "approved, draft",
                "approved, in_progress",
                "approved, pending_approval",
                "approved, rejected",
                "rejected, draft",
                "rejected, in_progress",
                "rejected, pending_approval",
                "rejected, approved"
        })
        @DisplayName("Invalid transition throws IllegalStateException")
        void invalidTransitionThrows(String from, String to) {
            assertThrows(IllegalStateException.class,
                    () -> ExecutionStateMachine.validateTransition(from, to));
        }
    }

    @Nested
    @DisplayName("validateTransition - invalid arguments")
    class InvalidArguments {

        @Test
        @DisplayName("null currentStatus throws IllegalArgumentException")
        void nullCurrentStatus() {
            assertThrows(IllegalArgumentException.class,
                    () -> ExecutionStateMachine.validateTransition(null, "in_progress"));
        }

        @Test
        @DisplayName("empty currentStatus throws IllegalArgumentException")
        void emptyCurrentStatus() {
            assertThrows(IllegalArgumentException.class,
                    () -> ExecutionStateMachine.validateTransition("", "in_progress"));
        }

        @Test
        @DisplayName("null targetStatus throws IllegalArgumentException")
        void nullTargetStatus() {
            assertThrows(IllegalArgumentException.class,
                    () -> ExecutionStateMachine.validateTransition("draft", null));
        }

        @Test
        @DisplayName("empty targetStatus throws IllegalArgumentException")
        void emptyTargetStatus() {
            assertThrows(IllegalArgumentException.class,
                    () -> ExecutionStateMachine.validateTransition("draft", ""));
        }

        @Test
        @DisplayName("unknown currentStatus throws IllegalArgumentException")
        void unknownCurrentStatus() {
            assertThrows(IllegalArgumentException.class,
                    () -> ExecutionStateMachine.validateTransition("unknown_status", "in_progress"));
        }

        @Test
        @DisplayName("unknown targetStatus throws IllegalArgumentException")
        void unknownTargetStatus() {
            assertThrows(IllegalArgumentException.class,
                    () -> ExecutionStateMachine.validateTransition("draft", "unknown_status"));
        }
    }

    @Nested
    @DisplayName("isValidTransition - non-throwing check")
    class IsValidTransition {

        @Test
        @DisplayName("returns true for valid transition")
        void validTransitionReturnsTrue() {
            assertTrue(ExecutionStateMachine.isValidTransition("draft", "in_progress"));
        }

        @Test
        @DisplayName("returns false for invalid transition")
        void invalidTransitionReturnsFalse() {
            assertFalse(ExecutionStateMachine.isValidTransition("draft", "approved"));
        }

        @Test
        @DisplayName("returns false for null currentStatus")
        void nullCurrentReturnsFalse() {
            assertFalse(ExecutionStateMachine.isValidTransition(null, "in_progress"));
        }

        @Test
        @DisplayName("returns false for null targetStatus")
        void nullTargetReturnsFalse() {
            assertFalse(ExecutionStateMachine.isValidTransition("draft", null));
        }
    }

    @Nested
    @DisplayName("getValidTargets")
    class GetValidTargets {

        @Test
        @DisplayName("draft has only in_progress as valid target")
        void draftTargets() {
            Set<String> targets = ExecutionStateMachine.getValidTargets("draft");
            assertEquals(1, targets.size());
            assertTrue(targets.contains("in_progress"));
        }

        @Test
        @DisplayName("in_progress has in_progress and pending_approval as valid targets")
        void inProgressTargets() {
            Set<String> targets = ExecutionStateMachine.getValidTargets("in_progress");
            assertEquals(2, targets.size());
            assertTrue(targets.contains("in_progress"));
            assertTrue(targets.contains("pending_approval"));
        }

        @Test
        @DisplayName("pending_approval has approved, rejected, in_progress as valid targets")
        void pendingApprovalTargets() {
            Set<String> targets = ExecutionStateMachine.getValidTargets("pending_approval");
            assertEquals(3, targets.size());
            assertTrue(targets.contains("approved"));
            assertTrue(targets.contains("rejected"));
            assertTrue(targets.contains("in_progress"));
        }

        @Test
        @DisplayName("approved has no valid targets (terminal state)")
        void approvedTargets() {
            Set<String> targets = ExecutionStateMachine.getValidTargets("approved");
            assertTrue(targets.isEmpty());
        }

        @Test
        @DisplayName("rejected has no valid targets (terminal state)")
        void rejectedTargets() {
            Set<String> targets = ExecutionStateMachine.getValidTargets("rejected");
            assertTrue(targets.isEmpty());
        }

        @Test
        @DisplayName("null status returns empty set")
        void nullStatusReturnsEmpty() {
            Set<String> targets = ExecutionStateMachine.getValidTargets(null);
            assertTrue(targets.isEmpty());
        }
    }

    @Nested
    @DisplayName("isTerminalStatus")
    class IsTerminalStatus {

        @Test
        @DisplayName("approved is terminal")
        void approvedIsTerminal() {
            assertTrue(ExecutionStateMachine.isTerminalStatus("approved"));
        }

        @Test
        @DisplayName("rejected is terminal")
        void rejectedIsTerminal() {
            assertTrue(ExecutionStateMachine.isTerminalStatus("rejected"));
        }

        @Test
        @DisplayName("draft is not terminal")
        void draftIsNotTerminal() {
            assertFalse(ExecutionStateMachine.isTerminalStatus("draft"));
        }

        @Test
        @DisplayName("in_progress is not terminal")
        void inProgressIsNotTerminal() {
            assertFalse(ExecutionStateMachine.isTerminalStatus("in_progress"));
        }

        @Test
        @DisplayName("pending_approval is not terminal")
        void pendingApprovalIsNotTerminal() {
            assertFalse(ExecutionStateMachine.isTerminalStatus("pending_approval"));
        }
    }

    @Nested
    @DisplayName("isValidStatus")
    class IsValidStatus {

        @ParameterizedTest(name = "{0} is a valid status")
        @CsvSource({"draft", "in_progress", "pending_approval", "approved", "rejected"})
        void validStatuses(String status) {
            assertTrue(ExecutionStateMachine.isValidStatus(status));
        }

        @Test
        @DisplayName("unknown string is not a valid status")
        void unknownIsInvalid() {
            assertFalse(ExecutionStateMachine.isValidStatus("unknown"));
        }

        @Test
        @DisplayName("null is not a valid status")
        void nullIsInvalid() {
            assertFalse(ExecutionStateMachine.isValidStatus(null));
        }
    }
}
