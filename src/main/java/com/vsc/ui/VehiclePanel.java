package com.vsc.ui;

/**
 * Vehicle screen. Placeholder until the Vehicle DAO and service exist.
 */
public class VehiclePanel extends PlaceholderPanel {

    public VehiclePanel() {
        super("Vehicles",
                "Every vehicle belongs to a customer and is either a car or a motorcycle.",
                "Will call VehicleService once com.vsc.dao.VehicleDao exists.",
                "Add a vehicle to a selected customer",
                "Choose Car or Motorcycle and show only the fields that apply",
                "Registration number must be unique across the workshop",
                "List all vehicles of one customer");
    }
}
