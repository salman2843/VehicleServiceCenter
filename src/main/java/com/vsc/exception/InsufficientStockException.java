package com.vsc.exception;

/**
 * Thrown when a service request tries to use more spare parts than are
 * currently in stock. Keeping this as a checked-style custom exception makes
 * the failure explicit and testable instead of hiding inside a RuntimeException.
 */
public class InsufficientStockException extends Exception {

    private final String partName;
    private final int requested;
    private final int available;

    public InsufficientStockException(String partName, int requested, int available) {
        super("Insufficient stock for " + partName
                + ": requested " + requested + ", available " + available);
        this.partName = partName;
        this.requested = requested;
        this.available = available;
    }

    public String getPartName() {
        return partName;
    }

    public int getRequested() {
        return requested;
    }

    public int getAvailable() {
        return available;
    }
}