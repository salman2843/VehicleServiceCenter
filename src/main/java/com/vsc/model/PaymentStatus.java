package com.vsc.model;

/**
 * Payment state of an invoice. It is never typed in by the user: the service
 * layer derives it from the recorded payments (see AGENTS.md, business rules).
 */
public enum PaymentStatus {
    UNPAID,
    PARTIALLY_PAID,
    PAID
}
