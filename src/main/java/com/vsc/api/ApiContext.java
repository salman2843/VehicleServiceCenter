package com.vsc.api;

import com.vsc.service.BillingService;
import com.vsc.service.CustomerService;
import com.vsc.service.InventoryService;
import com.vsc.service.JobCardService;
import com.vsc.service.MechanicService;
import com.vsc.service.ServiceCatalogService;
import com.vsc.service.VehicleService;

/**
 * The services the HTTP handlers share. Every service is stateless and opens its
 * own database connection per call, so one instance each is safe for concurrent
 * requests - and the UI layer still talks to services only (AGENTS.md rule 4).
 */
final class ApiContext {

    final CustomerService customers = new CustomerService();
    final VehicleService vehicles = new VehicleService();
    final MechanicService mechanics = new MechanicService();
    final ServiceCatalogService catalogue = new ServiceCatalogService();
    final InventoryService inventory = new InventoryService();
    final JobCardService jobs = new JobCardService();
    final BillingService billing = new BillingService();
}
