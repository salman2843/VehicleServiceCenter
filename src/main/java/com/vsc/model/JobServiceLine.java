package com.vsc.model;

import java.math.BigDecimal;

/**
 * One service line of a job card (a row of {@code job_services} joined with the
 * service name). The price is the agreed price frozen when the line was added,
 * not today's catalogue price, so old invoices never change.
 */
public class JobServiceLine {

    private final int id;
    private final int jobCardId;
    private final int serviceId;
    private final String serviceName;
    private final BigDecimal agreedPrice;

    public JobServiceLine(int id, int jobCardId, int serviceId, String serviceName, BigDecimal agreedPrice) {
        this.id = id;
        this.jobCardId = jobCardId;
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.agreedPrice = agreedPrice;
    }

    /** A service is charged once per job card, so the line total is the price. */
    public BigDecimal getLineTotal() {
        return agreedPrice;
    }

    public int getId() {
        return id;
    }

    public int getJobCardId() {
        return jobCardId;
    }

    public int getServiceId() {
        return serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public BigDecimal getAgreedPrice() {
        return agreedPrice;
    }

    @Override
    public String toString() {
        return serviceName + " - " + agreedPrice;
    }
}
