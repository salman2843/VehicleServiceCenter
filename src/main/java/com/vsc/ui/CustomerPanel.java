package com.vsc.ui;

/**
 * Customer screen. Placeholder until the Customer DAO and service exist.
 */
public class CustomerPanel extends PlaceholderPanel {

    public CustomerPanel() {
        super("Customers",
                "Register a customer, find them by phone, and open their vehicles.",
                "Will call CustomerService once com.vsc.dao.CustomerDao exists.",
                "Search customers by name, phone or vehicle number",
                "Register a new customer: name, phone, email, address",
                "Open a customer to see their vehicles and service history",
                "Edit customer details with validation feedback");
    }
}
