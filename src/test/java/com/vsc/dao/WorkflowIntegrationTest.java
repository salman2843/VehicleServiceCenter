package com.vsc.dao;

import com.vsc.exception.InsufficientStockException;
import com.vsc.exception.ValidationException;
import com.vsc.model.Car;
import com.vsc.model.Customer;
import com.vsc.model.Invoice;
import com.vsc.model.JobCard;
import com.vsc.model.JobStatus;
import com.vsc.model.Mechanic;
import com.vsc.model.Payment;
import com.vsc.model.PaymentMethod;
import com.vsc.model.PaymentStatus;
import com.vsc.model.ServiceItem;
import com.vsc.model.SparePart;
import com.vsc.model.Vehicle;
import com.vsc.service.BillingService;
import com.vsc.service.CustomerService;
import com.vsc.service.InventoryService;
import com.vsc.service.JobCardService;
import com.vsc.service.MechanicService;
import com.vsc.service.ServiceCatalogService;
import com.vsc.service.VehicleService;
import com.vsc.util.DBConnection;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end check of the whole backend against a <em>real</em> MySQL server:
 * customer -> vehicle -> job card -> services and parts -> status changes ->
 * invoice -> partial and full payment.
 *
 * <p>The test skips itself (instead of failing) when MySQL is not reachable, so
 * a teammate can still run {@code mvn test} without a database. Every row it
 * creates is deleted again in {@code finally}.</p>
 */
class WorkflowIntegrationTest {

    private final CustomerService customerService = new CustomerService();
    private final VehicleService vehicleService = new VehicleService();
    private final MechanicService mechanicService = new MechanicService();
    private final ServiceCatalogService catalogService = new ServiceCatalogService();
    private final InventoryService inventoryService = new InventoryService();
    private final JobCardService jobCardService = new JobCardService();
    private final BillingService billingService = new BillingService();

    @Test
    void receptionistCanCompleteAWholeServiceVisit() throws Exception {
        requireDatabase();

        String run = String.valueOf(System.nanoTime());
        String suffix = run.substring(run.length() - 6);
        String phone = "9" + run.substring(run.length() - 9);

        // Collected for the cleanup in finally; the final copies below are what
        // the lambdas of assertThrows may capture.
        int customerId = 0;
        int vehicleId = 0;
        int mechanicId = 0;
        int serviceId = 0;
        int partId = 0;
        int jobCardId = 0;
        int invoiceId = 0;

        try {
            // 1. register the customer and their car
            Customer customer = customerService.register(new Customer("Test Customer " + suffix, phone));
            customerId = customer.getId();
            assertTrue(customerId > 0, "the database should assign a customer id");
            assertNotNull(customerService.findById(customerId).getCreatedOn());

            Vehicle vehicle = vehicleService.add(
                    new Car(customerId, "ka" + suffix + "ab1234", "Maruti", "Swift", 2022));
            vehicleId = vehicle.getId();
            assertEquals("KA" + suffix + "AB1234", vehicle.getRegistrationNo());
            assertEquals(1, vehicleService.listByCustomer(customerId).size());

            // 2. workshop master data used by the job
            Mechanic mechanic = mechanicService.add(new Mechanic("Ravi " + suffix, "98765" + suffix, "Engine"));
            mechanicId = mechanic.getId();
            assertTrue(mechanic.isActive());

            ServiceItem service = catalogService.add(
                    new ServiceItem("General service " + suffix, new BigDecimal("500.00")));
            serviceId = service.getId();

            SparePart part = inventoryService.addPart(
                    new SparePart("FLT-" + suffix, "Oil filter " + suffix, new BigDecimal("250.00"), 10, 3));
            partId = part.getId();
            final int partKey = partId;

            // 3. open the job card
            JobCard job = jobCardService.openJob(vehicleId, "Brake noise and oil change");
            jobCardId = job.getId();
            final int jobKey = jobCardId;
            assertEquals(JobStatus.RECEIVED, job.getStatus());
            assertNotNull(job.getReceivedOn());

            jobCardService.assignMechanic(jobCardId, mechanicId);
            assertEquals(mechanicId, jobCardService.findById(jobCardId).getMechanicId());

            // 4. services and parts (the agreed price defaults to the catalogue)
            jobCardService.addService(jobCardId, serviceId, null);
            assertEquals(0, jobCardService.servicesOf(jobCardId).get(0).getAgreedPrice()
                    .compareTo(new BigDecimal("500.00")));

            jobCardService.addPart(jobCardId, partId, 2);
            assertEquals(8, stockOf(partKey));

            // not enough stock: nothing is written and the stock stays at 8
            assertThrows(InsufficientStockException.class,
                    () -> jobCardService.addPart(jobKey, partKey, 500));
            assertEquals(8, stockOf(partKey));
            assertEquals(1, jobCardService.partsOf(jobCardId).size());

            // 5. status moves forward one step at a time
            assertEquals(JobStatus.IN_PROGRESS, jobCardService.advanceStatus(jobCardId).getStatus());
            JobCard completed = jobCardService.advanceStatus(jobCardId);
            assertEquals(JobStatus.COMPLETED, completed.getStatus());
            assertNotNull(completed.getCompletedOn(), "COMPLETED should stamp the completion time");

            // 6. invoice from the saved lines: (500 + 500 - 100) + 18% = 1062
            Invoice invoice = billingService.generateInvoice(
                    jobCardId, new BigDecimal("100.00"), new BigDecimal("18"));
            invoiceId = invoice.getId();
            final int invoiceKey = invoiceId;
            assertEquals(0, invoice.getTotal().compareTo(new BigDecimal("1062.00")));
            assertEquals(PaymentStatus.UNPAID, invoice.getPaymentStatus());
            assertThrows(ValidationException.class,
                    () -> billingService.generateInvoice(jobKey, BigDecimal.ZERO));

            // 7. partial payment, overpayment, then the rest
            Payment first = billingService.recordPayment(
                    invoiceId, new BigDecimal("500.00"), PaymentMethod.UPI, "UPI-" + suffix);
            assertTrue(first.getId() > 0);
            assertEquals(PaymentStatus.PARTIALLY_PAID,
                    billingService.findById(invoiceId).getPaymentStatus());
            assertEquals(0, billingService.outstanding(invoiceId).compareTo(new BigDecimal("562.00")));

            assertThrows(ValidationException.class, () -> billingService.recordPayment(
                    invoiceKey, new BigDecimal("600.00"), PaymentMethod.CASH, null));

            billingService.recordPayment(invoiceId, new BigDecimal("562.00"), PaymentMethod.CASH, null);
            assertEquals(PaymentStatus.PAID, billingService.findById(invoiceId).getPaymentStatus());
            assertEquals(0, billingService.outstanding(invoiceId).compareTo(BigDecimal.ZERO));
            assertEquals(2, billingService.paymentsOf(invoiceId).size());

            // 8. hand the vehicle over and check the history
            assertEquals(JobStatus.DELIVERED, jobCardService.advanceStatus(jobCardId).getStatus());
            List<JobCard> history = jobCardService.history(vehicleId);
            assertEquals(1, history.size());
            assertEquals(jobCardId, history.get(0).getId());

            // 9. a delivered job is closed for money changes
            assertThrows(ValidationException.class,
                    () -> jobCardService.addPart(jobKey, partKey, 1));
        } finally {
            cleanUp(customerId, vehicleId, mechanicId, serviceId, partId, jobCardId, invoiceId);
        }
    }

    private int stockOf(int partId) throws SQLException {
        return inventoryService.listAll().stream()
                .filter(p -> p.getId() == partId)
                .findFirst()
                .orElseThrow()
                .getStockQty();
    }

    /** Skips the test when no MySQL settings / server are available. */
    private void requireDatabase() {
        try (Connection ignored = DBConnection.getConnection()) {
            // connected: run the test
        } catch (SQLException e) {
            Assumptions.abort("MySQL not available, skipping the integration test: " + e.getMessage());
        }
    }

    /** Deletes the rows of this run in foreign-key order. */
    private void cleanUp(int customerId, int vehicleId, int mechanicId, int serviceId,
                         int partId, int jobCardId, int invoiceId) {
        try (Connection con = DBConnection.getConnection()) {
            delete(con, "DELETE FROM payments WHERE invoice_id = ?", invoiceId);
            delete(con, "DELETE FROM invoices WHERE id = ?", invoiceId);
            delete(con, "DELETE FROM job_parts WHERE job_card_id = ?", jobCardId);
            delete(con, "DELETE FROM job_services WHERE job_card_id = ?", jobCardId);
            delete(con, "DELETE FROM job_cards WHERE id = ?", jobCardId);
            delete(con, "DELETE FROM vehicles WHERE id = ?", vehicleId);
            delete(con, "DELETE FROM customers WHERE id = ?", customerId);
            delete(con, "DELETE FROM mechanics WHERE id = ?", mechanicId);
            delete(con, "DELETE FROM services WHERE id = ?", serviceId);
            delete(con, "DELETE FROM spare_parts WHERE id = ?", partId);
        } catch (SQLException e) {
            System.err.println("Cleanup after the integration test failed: " + e.getMessage());
        }
    }

    private void delete(Connection con, String sql, int id) throws SQLException {
        if (id <= 0) {
            return;
        }
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
