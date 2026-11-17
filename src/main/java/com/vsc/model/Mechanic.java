package com.vsc.model;

/** A technician who can be assigned to job cards. */
public class Mechanic {

    private int id;
    private String name;
    private String phone;
    private String specialization;
    private boolean active;

    public Mechanic(String name, String phone, String specialization) {
        this(0, name, phone, specialization, true);
    }

    public Mechanic(int id, String name, String phone, String specialization, boolean active) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.specialization = specialization;
        this.active = active;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return name + (specialization == null ? "" : " - " + specialization);
    }
}
