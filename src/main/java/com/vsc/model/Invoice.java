package com.vsc.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * The bill for one job card: labour + parts - discount (+ tax) = total.
 * Amounts are plain BigDecimal; the arithmetic itself is done in the service
 * layer so it can be unit tested in one place.
 */
public class Invoice {

    private int id;
    private int jobCardId;
    private BigDecimal laborTotal;
    private BigDecimal partsTotal;
    private BigDecimal discount;
    private BigDecimal taxAmount;
    private BigDecimal total;
    private PaymentStatus paymentStatus;
    private LocalDateTime createdOn;

    public Invoice(int jobCardId, BigDecimal total) {
        this(0, jobCardId, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                total, PaymentStatus.UNPAID, LocalDateTime.now());
    }

    public Invoice(int id, int jobCardId, BigDecimal laborTotal, BigDecimal partsTotal,
                   BigDecimal discount, BigDecimal taxAmount, BigDecimal total,
                   PaymentStatus paymentStatus, LocalDateTime createdOn) {
        this.id = id;
        this.jobCardId = jobCardId;
        this.laborTotal = laborTotal;
        this.partsTotal = partsTotal;
        this.discount = discount;
        this.taxAmount = taxAmount;
        this.total = total;
        this.paymentStatus = paymentStatus;
        this.createdOn = createdOn;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getJobCardId() {
        return jobCardId;
    }

    public void setJobCardId(int jobCardId) {
        this.jobCardId = jobCardId;
    }

    public BigDecimal getLaborTotal() {
        return laborTotal;
    }

    public void setLaborTotal(BigDecimal laborTotal) {
        this.laborTotal = laborTotal;
    }

    public BigDecimal getPartsTotal() {
        return partsTotal;
    }

    public void setPartsTotal(BigDecimal partsTotal) {
        this.partsTotal = partsTotal;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    @Override
    public String toString() {
        return "Invoice #" + id + " - " + total + " [" + paymentStatus + "]";
    }
}
