package com.vsc.model;

/**
 * Abstract base class for every vehicle handled by the workshop.
 *
 * <p>Modelling the vehicle as an abstract class is what lets the system treat
 * cars and motorcycles the same way (they share the job-card workflow) while
 * still reporting their own type through {@link #getVehicleType()}.</p>
 */
public abstract class Vehicle {

    private int id;
    private int customerId;
    private String registrationNo;
    private String make;
    private String modelName;
    private int year;

    protected Vehicle(int id, int customerId, String registrationNo,
                      String make, String modelName, int year) {
        this.id = id;
        this.customerId = customerId;
        this.registrationNo = registrationNo;
        this.make = make;
        this.modelName = modelName;
        this.year = year;
    }

    /** Each subclass reports its own kind, e.g. "Car" or "Motorcycle". */
    public abstract String getVehicleType();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getRegistrationNo() {
        return registrationNo;
    }

    public void setRegistrationNo(String registrationNo) {
        this.registrationNo = registrationNo;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    @Override
    public String toString() {
        return registrationNo + " - " + make + " " + modelName + " (" + getVehicleType() + ")";
    }
}
