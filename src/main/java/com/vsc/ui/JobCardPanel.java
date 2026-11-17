package com.vsc.ui;

/**
 * Job card screen. Placeholder until the JobCard DAO and service exist.
 */
public class JobCardPanel extends PlaceholderPanel {

    public JobCardPanel() {
        super("Job cards",
                "One job card per service visit: complaint, services, parts and assigned mechanic.",
                "Will call JobCardService once com.vsc.dao.JobCardDao exists.",
                "Create a job card for a vehicle with the reported complaint",
                "Add services with the agreed price and spare parts with quantity",
                "Assign a mechanic and deduct part stock in one transaction",
                "Move status: RECEIVED -> IN_PROGRESS -> COMPLETED -> DELIVERED",
                "Show the full history of a vehicle's job cards");
    }
}
