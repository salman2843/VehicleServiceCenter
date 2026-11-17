package com.vsc.ui;

/**
 * Mechanic screen. Placeholder until the Mechanic DAO and service exist.
 */
public class MechanicPanel extends PlaceholderPanel {

    public MechanicPanel() {
        super("Mechanics",
                "The technicians who can be assigned to job cards.",
                "Will call MechanicService once com.vsc.dao.MechanicDao exists.",
                "List mechanics with specialization and active status",
                "Add or edit a mechanic",
                "Show only active mechanics in the job-card assignment dropdown",
                "See how many open jobs each mechanic currently has");
    }
}
