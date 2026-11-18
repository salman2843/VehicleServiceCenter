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

    /** Used when the job is already at the final status, so there is no next step. */
    public InvalidStatusTransitionException(JobStatus current) {
        super("Job is already " + current + " and cannot move further");
        this.current = current;
        this.requested = null;
    }

    public JobStatus getCurrent() {
        return current;
    }

    public JobStatus getRequested() {
        return requested;
    }
}
