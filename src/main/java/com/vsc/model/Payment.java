package com.vsc.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** One payment against an invoice. Several payments are allowed (part payment). */
public class Payment {

    private int id;
    private int invoiceId;
    private BigDecimal amount;
    private PaymentMethod method;
    private String reference;
    private LocalDateTime paidOn;

    public Payment(int invoiceId, BigDecimal amount, PaymentMethod method) {
        this(0, invoiceId, amount, method, null, LocalDateTime.now());
    }

    public Payment(int id, int invoiceId, BigDecimal amount, PaymentMethod method,
                   String reference, LocalDateTime paidOn) {
        this.id = id;
        this.invoiceId = invoiceId;
        this.amount = amount;
        this.method = method;
        this.reference = reference;
        this.paidOn = paidOn;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public LocalDateTime getPaidOn() {
        return paidOn;
    }

    public void setPaidOn(LocalDateTime paidOn) {
        this.paidOn = paidOn;
    }

    @Override
    public String toString() {
        return amount + " via " + method;
    }
}
