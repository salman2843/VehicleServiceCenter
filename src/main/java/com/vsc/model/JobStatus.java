package com.vsc.model;

/**
 * Lifecycle of a job card. The order of the constants is the only allowed
 * direction of movement: RECEIVED -> IN_PROGRESS -> COMPLETED -> DELIVERED.
 *
 * <p>The rules live on the enum itself so they can be unit tested without a
 * database; the service layer still decides when to apply them.</p>
 */
public enum JobStatus {
    RECEIVED,
    IN_PROGRESS,
    COMPLETED,
    DELIVERED;

    /**
     * The only status this one may move to, or {@code null} for the final one.
     */
    public JobStatus next() {
        return switch (this) {
            case RECEIVED -> IN_PROGRESS;
            case IN_PROGRESS -> COMPLETED;
            case COMPLETED -> DELIVERED;
            case DELIVERED -> null;
        };
    }

    /** True only for a one-step move forward; backwards and skipped steps fail. */
    public boolean canMoveTo(JobStatus target) {
        return target != null && target == next();
    }

    /** True once the workshop has finished the work (invoice may be generated). */
    public boolean isCompletedOrLater() {
        return this == COMPLETED || this == DELIVERED;
    }
}
