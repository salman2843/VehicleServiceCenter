package com.vsc.ui;

/**
 * Inventory screen. Placeholder until the SparePart DAO and service exist.
 */
public class InventoryPanel extends PlaceholderPanel {

    public InventoryPanel() {
        super("Inventory",
                "Spare parts in the workshop store, with stock levels and reorder warnings.",
                "Will call InventoryService once com.vsc.dao.SparePartDao exists.",
                "Table of parts with number, name, price, stock and reorder level",
                "Low-stock warning when stock quantity is at or below the reorder level",
                "Add a new part, or receive stock for an existing part",
                "Block any operation that would take stock below zero");
    }
}
