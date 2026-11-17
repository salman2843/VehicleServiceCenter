package com.vsc.exception;

import com.vsc.model.JobStatus;

/**
 * Thrown when a job card is asked to move to a status that is not the next one
 * in the workflow (for example COMPLETED back to RECEIVED).
 */
public class InvalidStatusTransitionException extends Exception {

    private final JobStatus current;
    private final JobStatus requested;

    public InvalidStatusTransitionException(JobStatus current, JobStatus requested) {
        super("Cannot move job status from " + current + " to " + requested);
        this.current = current;
        this.requested = requested;
    }

    public JobStatus getCurrent() {
        return current;
    }

    public JobStatus getRequested() {
        return requested;
    }
}
