package com.vsc.ui;

/**
 * Invoice and payment screen. Placeholder until the Invoice DAO and service exist.
 */
public class InvoicePanel extends PlaceholderPanel {

    public InvoicePanel() {
        super("Invoices and payments",
                "Generate the bill from a completed job and record full or partial payments.",
                "Will call BillingService once com.vsc.dao.InvoiceDao exists.",
                "Generate an invoice for a COMPLETED job card",
                "Labor total + parts total - discount (+ tax) = total",
                "Record payments by CASH, UPI or CARD, including partial payments",
                "Derive status: UNPAID, PARTIALLY_PAID or PAID",
                "Print or preview the invoice");
    }
}
