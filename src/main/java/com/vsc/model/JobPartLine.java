package com.vsc.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * One spare-part line of a job card (a row of {@code job_parts} joined with the
 * part). {@code unitPrice} is the snapshot price taken when the part was used.
 */
public class JobPartLine {

    private final int id;
    private final int jobCardId;
    private final int partId;
    private final String partNumber;
    private final String partName;
    private final int quantity;
    private final BigDecimal unitPrice;

    public JobPartLine(int id, int jobCardId, int partId, String partNumber,
                       String partName, int quantity, BigDecimal unitPrice) {
        this.id = id;
        this.jobCardId = jobCardId;
        this.partId = partId;
        this.partNumber = partNumber;
        this.partName = partName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    /** Quantity multiplied by the snapshot unit price. */
    public BigDecimal getLineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
    }

    public int getId() {
        return id;
    }

    public int getJobCardId() {
        return jobCardId;
    }

    public int getPartId() {
        return partId;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public String getPartName() {
        return partName;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    @Override
    public String toString() {
        return partNumber + " " + partName + " x " + quantity + " = " + getLineTotal();
    }
}
