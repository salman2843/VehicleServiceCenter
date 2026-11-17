package com.vsc.model;

import java.time.LocalDateTime;

/**
 * A workshop customer - the person who owns one or more vehicles.
 * Pure data holder: no SQL and no Swing code belongs here.
 */
public class Customer {

    private int id;
    private String name;
    private String phone;
    private String email;
    private String address;
    private LocalDateTime createdOn;

    /** Used when registering a new customer, before the database assigns an id. */
    public Customer(String name, String phone) {
        this(0, name, phone, null, null, LocalDateTime.now());
    }

    public Customer(int id, String name, String phone, String email, String address, LocalDateTime createdOn) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.createdOn = createdOn;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    @Override
    public String toString() {
        return name + " (" + phone + ")";
    }
}
