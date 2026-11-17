package com.vsc.model;

import java.time.LocalDateTime;

/**
 * The workshop job card: what the customer complained about, who is working on
 * it and where it is in the workflow. Services and parts added to the job are
 * stored in separate join tables, so they are not held here.
 */
public class JobCard {

    private int id;
    private int vehicleId;
    private int mechanicId;
    private String complaint;
    private JobStatus status;
    private LocalDateTime receivedOn;
    private LocalDateTime completedOn;

    /** A brand-new job always starts as RECEIVED. */
    public JobCard(int vehicleId, String complaint) {
        this(0, vehicleId, 0, complaint, JobStatus.RECEIVED, LocalDateTime.now(), null);
    }

    public JobCard(int id, int vehicleId, int mechanicId, String complaint,
                   JobStatus status, LocalDateTime receivedOn, LocalDateTime completedOn) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.mechanicId = mechanicId;
        this.complaint = complaint;
        this.status = status;
        this.receivedOn = receivedOn;
        this.completedOn = completedOn;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public int getMechanicId() {
        return mechanicId;
    }

    public void setMechanicId(int mechanicId) {
        this.mechanicId = mechanicId;
    }

    public String getComplaint() {
        return complaint;
    }

    public void setComplaint(String complaint) {
        this.complaint = complaint;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public LocalDateTime getReceivedOn() {
        return receivedOn;
    }

    public void setReceivedOn(LocalDateTime receivedOn) {
        this.receivedOn = receivedOn;
    }

    public LocalDateTime getCompletedOn() {
        return completedOn;
    }

    public void setCompletedOn(LocalDateTime completedOn) {
        this.completedOn = completedOn;
    }

    @Override
    public String toString() {
        return "Job #" + id + " [" + status + "]";
    }
}
