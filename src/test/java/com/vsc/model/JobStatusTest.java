package com.vsc.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The job-card workflow rule from AGENTS.md: status moves forward one step at
 * a time only. This test needs no database because the rule lives on the enum.
 */
class JobStatusTest {

    @Test
    void everyStatusKnowsItsNextStep() {
        assertEquals(JobStatus.IN_PROGRESS, JobStatus.RECEIVED.next());
        assertEquals(JobStatus.COMPLETED, JobStatus.IN_PROGRESS.next());
        assertEquals(JobStatus.DELIVERED, JobStatus.COMPLETED.next());
        assertNull(JobStatus.DELIVERED.next());
    }

    @Test
    void oneStepForwardIsAllowed() {
        assertTrue(JobStatus.RECEIVED.canMoveTo(JobStatus.IN_PROGRESS));
        assertTrue(JobStatus.IN_PROGRESS.canMoveTo(JobStatus.COMPLETED));
        assertTrue(JobStatus.COMPLETED.canMoveTo(JobStatus.DELIVERED));
    }

    @Test
    void backwardsAndSkippedStepsAreRejected() {
        assertFalse(JobStatus.COMPLETED.canMoveTo(JobStatus.IN_PROGRESS));
        assertFalse(JobStatus.IN_PROGRESS.canMoveTo(JobStatus.IN_PROGRESS));
        assertFalse(JobStatus.RECEIVED.canMoveTo(JobStatus.COMPLETED));
        assertFalse(JobStatus.RECEIVED.canMoveTo(JobStatus.DELIVERED));
        assertFalse(JobStatus.DELIVERED.canMoveTo(null));
    }

    @Test
    void invoiceIsAllowedOnlyFromCompletedOrDelivered() {
        assertFalse(JobStatus.RECEIVED.isCompletedOrLater());
        assertFalse(JobStatus.IN_PROGRESS.isCompletedOrLater());
        assertTrue(JobStatus.COMPLETED.isCompletedOrLater());
        assertTrue(JobStatus.DELIVERED.isCompletedOrLater());
    }
}
