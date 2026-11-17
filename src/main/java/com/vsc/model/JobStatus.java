package com.vsc.model;

/**
 * Lifecycle of a job card. The order of the constants is the only allowed
 * direction of movement: RECEIVED -> IN_PROGRESS -> COMPLETED -> DELIVERED.
 * The service layer is responsible for rejecting backward jumps.
 */
public enum JobStatus {
    RECEIVED,
    IN_PROGRESS,
    COMPLETED,
    DELIVERED
}
