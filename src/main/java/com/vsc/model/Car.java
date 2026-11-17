package com.vsc.model;

/** A passenger car serviced by the workshop. */
public class Car extends Vehicle {

    private int numberOfDoors;

    public Car(int customerId, String registrationNo, String make, String modelName, int year) {
        this(0, customerId, registrationNo, make, modelName, year, 4);
    }

    public Car(int id, int customerId, String registrationNo, String make,
               String modelName, int year, int numberOfDoors) {
        super(id, customerId, registrationNo, make, modelName, year);
        this.numberOfDoors = numberOfDoors;
    }

    @Override
    public String getVehicleType() {
        return "Car";
    }

    public int getNumberOfDoors() {
        return numberOfDoors;
    }

    public void setNumberOfDoors(int numberOfDoors) {
        this.numberOfDoors = numberOfDoors;
    }
}
