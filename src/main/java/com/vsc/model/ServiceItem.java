package com.vsc.model;

import java.math.BigDecimal;

/**
 * A service offered by the workshop, e.g. "Wheel alignment".
 * The price here is the catalogue price; the price actually agreed with the
 * customer is stored per job so old invoices never change.
 */
public class ServiceItem implements Billable {

    private int id;
    private String name;
    private String details;
    private BigDecimal basePrice;

    public ServiceItem(String name, BigDecimal basePrice) {
        this(0, name, null, basePrice);
    }

    public ServiceItem(int id, String name, String details, BigDecimal basePrice) {
        this.id = id;
        this.name = name;
        this.details = details;
        this.basePrice = basePrice;
    }

    @Override
    public String getDescription() {
        return name;
    }

    @Override
    public BigDecimal getAmount() {
        return basePrice;
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

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    @Override
    public String toString() {
        return name;
    }
}
