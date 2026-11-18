package com.vsc.model;

import java.math.BigDecimal;

/**
 * A spare part kept in the workshop store.
 * Implements {@link Billable} so a part can be added to an invoice line.
 */
public class SparePart implements Billable {

    private int id;
    private String partNumber;
    private String name;
    private BigDecimal unitPrice;
    private int stockQty;
    private int reorderLevel;

    public SparePart(String partNumber, String name, BigDecimal unitPrice, int stockQty, int reorderLevel) {
        this(0, partNumber, name, unitPrice, stockQty, reorderLevel);
    }

    public SparePart(int id, String partNumber, String name, BigDecimal unitPrice,
                     int stockQty, int reorderLevel) {
        this.id = id;
        this.partNumber = partNumber;
        this.name = name;
        this.unitPrice = unitPrice;
        this.stockQty = stockQty;
        this.reorderLevel = reorderLevel;
    }

    /** True when the stock has fallen to (or below) the reorder level. */
    public boolean isLowStock() {
        return stockQty <= reorderLevel;
    }

    /** True when this many units can be taken from stock without going negative. */
    public boolean canFulfil(int quantity) {
        return quantity > 0 && quantity <= stockQty;
    }

    @Override
    public String getDescription() {
        return name;
    }

    @Override
    public BigDecimal getAmount() {
        return unitPrice;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getStockQty() {
        return stockQty;
    }

    public void setStockQty(int stockQty) {
        this.stockQty = stockQty;
    }

    public int getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(int reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    @Override
    public String toString() {
        return partNumber + " - " + name + " (stock: " + stockQty + ")";
    }
}
