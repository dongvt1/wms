package com.cy.modules.qms.service;

import com.cy.modules.qms.util.ExecutionStateMachine;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Property-based test for State machine transition validity.
 *
 * **Validates: Requirements 8.6**
 *
 * Property 15: State machine transition validity.
 * For any Inspection Execution, the only valid state transitions SHALL be:
 * draft→in_progress, in_progress→in_progress (save draft),
 * in_progress→pending_approval, pending_approval→approved,
 * pending_approval→rejected, pending_approval→in_progress (re-inspect).
 * Any other transition SHALL be rejected.
 */
class StateMachineTransitionValidityPropertyTest {

    /** All 5 valid statuses in the state machine */
    private static final List<String> ALL_STATUSES = List.of(
            ExecutionStateMachine.STATUS_DRAFT,
            ExecutionStateMachine.STATUS_IN_PROGRESS,
            ExecutionStateMachine.STATUS_PENDING_APPROVAL,
            ExecutionStateMachine.STATUS_APPROVED,
            ExecutionStateMachine.STATUS_REJECTED
    );

    /** The complete set of valid transitions as defined in the design */
    private static final Set<String> VALID_TRANSITIONS = Set.of(
            "draft->in_progress",
            "in_progress->in_progress",
            "in_progress->pending_approval",
            "pending_approval->approved",
            "pending_approval->rejected",
            "pending_approval->in_progress"
    );

    // ==================== Providers ====================

    @Provide
    Arbitrary<String> statuses() {
        return Arbitraries.of(ALL_STATUSES);
    }

    @Provide
    Arbitrary<String[]> statusPairs() {
        return Arbitraries.of(ALL_STATUSES)
                .tuple2()
                .map(t -> new String[]{t.get1(), t.get2()});
    }

    @Provide
    Arbitrary<String[]> validTransitionPairs() {
        List<String[]> validPairs = List.of(
                new String[]{"draft", "in_progress"},
                new String[]{"in_progress", "in_progress"},
                new String[]{"in_progress", "pending_approval"},
                new String[]{"pending_approval", "approved"},
                new String[]{"pending_approval", "rejected"},
                new String[]{"pending_approval", "in_progress"}
        );
        return Arbitraries.of(validPairs);
    }

    @Provide
    Arbitrary<String[]> invalidTransitionPairs() {
        // Generate all 25 possible pairs and filter out the valid ones
        List<String[]> invalidPairs = new ArrayList<>();
        for (String from : ALL_STATUSES) {
            for (String to : ALL_STATUSES) {
                String key = from + "->" + to;
                if (!VALID_TRANSITIONS.contains(key)) {
                    invalidPairs.add(new String[]{from, to});
                }
            }
        }
        return Arbitraries.of(invalidPairs);
    }

    // ==================== Property tests ====================

    /**
     * Property 15a: Valid transitions are accepted by validateTransition.
     *
     * For any valid transition pair (currentStatus, targetStatus) from the defined
     * set of valid transitions, validateTransition SHALL return true.
     *
     * **Validates: Requirements 8.6**
     */
    @Property(tries = 200)
    void validTransitionsAreAccepted(@ForAll("validTransitionPairs") String[] pair) {
        String currentStatus = pair[0];
        String targetStatus = pair[1];

        boolean result = ExecutionStateMachine.validateTransition(currentStatus, targetStatus);

        assertThat(result)
                .as("Transition from '%s' to '%s' should be valid", currentStatus, targetStatus)
                .isTrue();
    }

    /**
     * Property 15b: Invalid transitions are rejected with IllegalStateException.
     *
     * For any invalid transition pair (currentStatus, targetStatus) that is NOT in
     * the defined set of valid transitions, validateTransition SHALL throw
     * IllegalStateException.
     *
     * **Validates: Requirements 8.6**
     */
    @Property(tries = 200)
    void invalidTransitionsAreRejected(@ForAll("invalidTransitionPairs") String[] pair) {
        String currentStatus = pair[0];
        String targetStatus = pair[1];

        assertThatThrownBy(() -> ExecutionStateMachine.validateTransition(currentStatus, targetStatus))
                .as("Transition from '%s' to '%s' should be rejected", currentStatus, targetStatus)
                .isInstanceOf(IllegalStateException.class);
    }

    /**
     * Property 15c: Terminal states (approved, rejected) have no valid outgoing transitions.
     *
     * For any terminal status (approved or rejected), attempting to transition to
     * ANY other status SHALL be rejected. Terminal states have empty valid target sets.
     *
     * **Validates: Requirements 8.6**
     */
    @Property(tries = 200)
    void terminalStatesHaveNoOutgoingTransitions(
            @ForAll("statuses") String targetStatus) {

        // Test 'approved' as terminal state
        Set<String> approvedTargets = ExecutionStateMachine.getValidTargets(ExecutionStateMachine.STATUS_APPROVED);
        assertThat(approvedTargets)
                .as("Terminal state 'approved' should have no valid outgoing transitions")
                .isEmpty();

        assertThatThrownBy(() ->
                ExecutionStateMachine.validateTransition(ExecutionStateMachine.STATUS_APPROVED, targetStatus))
                .as("Transition from 'approved' to '%s' should be rejected", targetStatus)
                .isInstanceOf(IllegalStateException.class);

        // Test 'rejected' as terminal state
        Set<String> rejectedTargets = ExecutionStateMachine.getValidTargets(ExecutionStateMachine.STATUS_REJECTED);
        assertThat(rejectedTargets)
                .as("Terminal state 'rejected' should have no valid outgoing transitions")
                .isEmpty();

        assertThatThrownBy(() ->
                ExecutionStateMachine.validateTransition(ExecutionStateMachine.STATUS_REJECTED, targetStatus))
                .as("Transition from 'rejected' to '%s' should be rejected", targetStatus)
                .isInstanceOf(IllegalStateException.class);
    }

    /**
     * Property 15d: isValidTransition is consistent with validateTransition.
     *
     * For any pair of statuses, isValidTransition returns true if and only if
     * validateTransition does not throw an exception.
     *
     * **Validates: Requirements 8.6**
     */
    @Property(tries = 500)
    void isValidTransitionConsistentWithValidateTransition(
            @ForAll("statuses") String currentStatus,
            @ForAll("statuses") String targetStatus) {

        boolean isValid = ExecutionStateMachine.isValidTransition(currentStatus, targetStatus);

        if (isValid) {
            // If isValidTransition says true, validateTransition should not throw
            boolean result = ExecutionStateMachine.validateTransition(currentStatus, targetStatus);
            assertThat(result)
                    .as("validateTransition should return true for valid transition '%s' -> '%s'",
                            currentStatus, targetStatus)
                    .isTrue();
        } else {
            // If isValidTransition says false, validateTransition should throw
            assertThatThrownBy(() ->
                    ExecutionStateMachine.validateTransition(currentStatus, targetStatus))
                    .as("validateTransition should throw for invalid transition '%s' -> '%s'",
                            currentStatus, targetStatus)
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    /**
     * Property 15e: Random state transition sequences - only valid sequences complete.
     *
     * Generate random sequences of state transitions starting from 'draft'.
     * Verify that only sequences following valid transitions can complete without exception.
     *
     * **Validates: Requirements 8.6**
     */
    @Property(tries = 300)
    void randomTransitionSequencesFollowStateMachine(
            @ForAll @IntRange(min = 1, max = 8) int sequenceLength,
            @ForAll("statuses") String s1,
            @ForAll("statuses") String s2,
            @ForAll("statuses") String s3,
            @ForAll("statuses") String s4) {

        // Build a random transition sequence starting from draft
        List<String> sequence = new ArrayList<>();
        sequence.add(ExecutionStateMachine.STATUS_DRAFT);
        List<String> candidates = List.of(s1, s2, s3, s4);
        for (int i = 0; i < Math.min(sequenceLength, candidates.size()); i++) {
            sequence.add(candidates.get(i));
        }

        // Walk through the sequence and verify each transition
        String currentState = sequence.get(0);
        for (int i = 1; i < sequence.size(); i++) {
            String nextState = sequence.get(i);
            String transitionKey = currentState + "->" + nextState;

            if (VALID_TRANSITIONS.contains(transitionKey)) {
                // Valid transition - should succeed
                boolean result = ExecutionStateMachine.validateTransition(currentState, nextState);
                assertThat(result).isTrue();
                currentState = nextState;
            } else {
                // Invalid transition - should throw and sequence stops
                final String fromState = currentState;
                assertThatThrownBy(() ->
                        ExecutionStateMachine.validateTransition(fromState, nextState))
                        .isInstanceOf(IllegalStateException.class);
                break; // Sequence cannot continue after invalid transition
            }
        }
    }
}
