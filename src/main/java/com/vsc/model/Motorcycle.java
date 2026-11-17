package com.vsc.model;

/** A two-wheeler serviced by the workshop. */
public class Motorcycle extends Vehicle {

    private int engineCapacityCc;

    public Motorcycle(int customerId, String registrationNo, String make, String modelName, int year) {
        this(0, customerId, registrationNo, make, modelName, year, 150);
    }

    public Motorcycle(int id, int customerId, String registrationNo, String make,
                      String modelName, int year, int engineCapacityCc) {
        super(id, customerId, registrationNo, make, modelName, year);
        this.engineCapacityCc = engineCapacityCc;
    }

    @Override
    public String getVehicleType() {
        return "Motorcycle";
    }

    public int getEngineCapacityCc() {
        return engineCapacityCc;
    }

    public void setEngineCapacityCc(int engineCapacityCc) {
        this.engineCapacityCc = engineCapacityCc;
    }
}
