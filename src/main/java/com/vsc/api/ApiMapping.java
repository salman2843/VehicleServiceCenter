package com.vsc.api;

import com.vsc.model.Car;
import com.vsc.model.Customer;
import com.vsc.model.Invoice;
import com.vsc.model.JobCard;
import com.vsc.model.JobPartLine;
import com.vsc.model.JobServiceLine;
import com.vsc.model.Mechanic;
import com.vsc.model.Motorcycle;
import com.vsc.model.Payment;
import com.vsc.model.ServiceItem;
import com.vsc.model.SparePart;
import com.vsc.model.Vehicle;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Turns model objects into the JSON shapes the React frontend expects.
 * Kept in one place so every endpoint returns the same field names.
 */
final class ApiMapping {

    private ApiMapping() {
        // utility class, no instances
    }

    static Map<String, Object> customer(Customer customer, int vehicleCount) {
        return Json.obj(
                "id", customer.getId(),
                "name", customer.getName(),
                "phone", customer.getPhone(),
                "email", customer.getEmail(),
                "address", customer.getAddress(),
                "createdOn", customer.getCreatedOn(),
                "vehicleCount", vehicleCount);
    }

    static Map<String, Object> vehicle(Vehicle vehicle, String customerName) {
        Map<String, Object> json = Json.obj(
                "id", vehicle.getId(),
                "customerId", vehicle.getCustomerId(),
                "registrationNo", vehicle.getRegistrationNo(),
                "vehicleType", vehicle instanceof Car ? "CAR" : "MOTORCYCLE",
                "make", vehicle.getMake(),
                "modelName", vehicle.getModelName(),
                "year", vehicle.getYear(),
                "customerName", customerName);
        if (vehicle instanceof Car car) {
            json.put("numberOfDoors", car.getNumberOfDoors());
        } else if (vehicle instanceof Motorcycle bike) {
            json.put("engineCapacityCc", bike.getEngineCapacityCc());
        }
        return json;
    }

    static Map<String, Object> mechanic(Mechanic mechanic) {
        return Json.obj(
                "id", mechanic.getId(),
                "name", mechanic.getName(),
                "phone", mechanic.getPhone(),
                "specialization", mechanic.getSpecialization(),
                "active", mechanic.isActive());
    }

    static Map<String, Object> service(ServiceItem service) {
        return Json.obj(
                "id", service.getId(),
                "name", service.getName(),
                "details", service.getDetails(),
                "basePrice", service.getBasePrice());
    }

    static Map<String, Object> part(SparePart part) {
        return Json.obj(
                "id", part.getId(),
                "partNumber", part.getPartNumber(),
                "name", part.getName(),
                "unitPrice", part.getUnitPrice(),
                "stockQty", part.getStockQty(),
                "reorderLevel", part.getReorderLevel(),
                "lowStock", part.isLowStock());
    }

    static Map<String, Object> payment(Payment payment) {
        return Json.obj(
                "id", payment.getId(),
                "invoiceId", payment.getInvoiceId(),
                "amount", payment.getAmount(),
                "method", payment.getMethod(),
                "reference", payment.getReference(),
                "paidOn", payment.getPaidOn());
    }

    static Map<String, Object> job(JobCard job, Vehicle vehicle, Customer customer, Mechanic mechanic) {
        return Json.obj(
                "id", job.getId(),
                "vehicleId", job.getVehicleId(),
                "mechanicId", job.getMechanicId(),
                "complaint", job.getComplaint(),
                "status", job.getStatus(),
                "nextStatus", job.getStatus().next(),
                "receivedOn", job.getReceivedOn(),
                "completedOn", job.getCompletedOn(),
                "registrationNo", vehicle == null ? null : vehicle.getRegistrationNo(),
                "vehicleLabel", vehicle == null ? null : vehicle.getMake() + " " + vehicle.getModelName(),
                "vehicleType", vehicle == null ? null : (vehicle instanceof Car ? "CAR" : "MOTORCYCLE"),
                "customerId", customer == null ? null : customer.getId(),
                "customerName", customer == null ? null : customer.getName(),
                "customerPhone", customer == null ? null : customer.getPhone(),
                "mechanicName", mechanic == null ? null : mechanic.getName());
    }

    static Map<String, Object> invoice(Invoice invoice, BigDecimal paid, JobCard job,
                                       Vehicle vehicle, Customer customer) {
        BigDecimal total = invoice.getTotal() == null ? BigDecimal.ZERO : invoice.getTotal();
        BigDecimal settled = paid == null ? BigDecimal.ZERO : paid;
        return Json.obj(
                "id", invoice.getId(),
                "jobCardId", invoice.getJobCardId(),
                "laborTotal", invoice.getLaborTotal(),
                "partsTotal", invoice.getPartsTotal(),
                "discount", invoice.getDiscount(),
                "taxAmount", invoice.getTaxAmount(),
                "total", invoice.getTotal(),
                "paid", settled,
                "outstanding", total.subtract(settled),
                "paymentStatus", invoice.getPaymentStatus(),
                "createdOn", invoice.getCreatedOn(),
                "registrationNo", vehicle == null ? null : vehicle.getRegistrationNo(),
                "customerName", customer == null ? null : customer.getName(),
                "jobStatus", job == null ? null : job.getStatus());
    }

    /** Job list rows, with vehicle and customer names joined in memory. */
    static List<Object> jobSummaries(ApiContext ctx, List<JobCard> jobs) throws Exception {
        Map<Integer, Vehicle> vehicles = indexVehicles(ctx);
        Map<Integer, Customer> customers = indexCustomers(ctx);
        Map<Integer, Mechanic> mechanics = indexMechanics(ctx);

        List<Object> rows = new ArrayList<>(jobs.size());
        for (JobCard job : jobs) {
            Vehicle vehicle = vehicles.get(job.getVehicleId());
            Customer customer = vehicle == null ? null : customers.get(vehicle.getCustomerId());
            Mechanic mechanic = mechanics.get(job.getMechanicId());
            rows.add(job(job, vehicle, customer, mechanic));
        }
        return rows;
    }

    static List<Object> invoiceSummaries(ApiContext ctx, List<Invoice> invoices) throws Exception {
        Map<Integer, JobCard> jobs = indexJobs(ctx);
        Map<Integer, Vehicle> vehicles = indexVehicles(ctx);
        Map<Integer, Customer> customers = indexCustomers(ctx);
        Map<Integer, BigDecimal> paid = ctx.billing.paidTotals();

        List<Object> rows = new ArrayList<>(invoices.size());
        for (Invoice invoice : invoices) {
            JobCard job = jobs.get(invoice.getJobCardId());
            Vehicle vehicle = job == null ? null : vehicles.get(job.getVehicleId());
            Customer customer = vehicle == null ? null : customers.get(vehicle.getCustomerId());
            rows.add(invoice(invoice, paid.get(invoice.getId()), job, vehicle, customer));
        }
        return rows;
    }

    /** Everything the job-card screen needs in a single response. */
    static Map<String, Object> jobDetail(ApiContext ctx, int jobCardId) throws Exception {
        JobCard job = ctx.jobs.findById(jobCardId);
        if (job == null) {
            throw ApiException.notFound("Job card not found.");
        }
        Vehicle vehicle = ctx.vehicles.findById(job.getVehicleId());
        Customer customer = vehicle == null ? null : ctx.customers.findById(vehicle.getCustomerId());
        Mechanic mechanic = job.getMechanicId() > 0 ? ctx.mechanics.findById(job.getMechanicId()) : null;

        List<JobServiceLine> serviceLines = ctx.jobs.servicesOf(jobCardId);
        List<JobPartLine> partLines = ctx.jobs.partsOf(jobCardId);
        BigDecimal laborTotal = BigDecimal.ZERO;
        List<Object> services = new ArrayList<>(serviceLines.size());
        for (JobServiceLine line : serviceLines) {
            laborTotal = laborTotal.add(line.getLineTotal());
            services.add(Json.obj(
                    "id", line.getId(),
                    "serviceId", line.getServiceId(),
                    "serviceName", line.getServiceName(),
                    "agreedPrice", line.getAgreedPrice(),
                    "lineTotal", line.getLineTotal()));
        }
        BigDecimal partsTotal = BigDecimal.ZERO;
        List<Object> parts = new ArrayList<>(partLines.size());
        for (JobPartLine line : partLines) {
            partsTotal = partsTotal.add(line.getLineTotal());
            parts.add(Json.obj(
                    "id", line.getId(),
                    "partId", line.getPartId(),
                    "partNumber", line.getPartNumber(),
                    "partName", line.getPartName(),
                    "quantity", line.getQuantity(),
                    "unitPrice", line.getUnitPrice(),
                    "lineTotal", line.getLineTotal()));
        }

        Map<String, Object> json = job(job, vehicle, customer, mechanic);
        json.put("services", services);
        json.put("parts", parts);
        json.put("laborTotal", laborTotal);
        json.put("partsTotal", partsTotal);
        json.put("jobTotal", laborTotal.add(partsTotal));

        Invoice invoice = ctx.billing.findByJobCard(jobCardId);
        if (invoice != null) {
            BigDecimal paid = ctx.billing.paymentsOf(invoice.getId()).stream()
                    .map(Payment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            json.put("invoice", invoice(invoice, paid, job, vehicle, customer));
        } else {
            json.put("invoice", null);
        }
        return json;
    }

    static Map<Integer, Vehicle> indexVehicles(ApiContext ctx) throws Exception {
        Map<Integer, Vehicle> index = new HashMap<>();
        for (Vehicle vehicle : ctx.vehicles.listAll()) {
            index.put(vehicle.getId(), vehicle);
        }
        return index;
    }

    static Map<Integer, Customer> indexCustomers(ApiContext ctx) throws Exception {
        Map<Integer, Customer> index = new HashMap<>();
        for (Customer customer : ctx.customers.listAll()) {
            index.put(customer.getId(), customer);
        }
        return index;
    }

    static Map<Integer, Mechanic> indexMechanics(ApiContext ctx) throws Exception {
        Map<Integer, Mechanic> index = new HashMap<>();
        for (Mechanic mechanic : ctx.mechanics.listAll()) {
            index.put(mechanic.getId(), mechanic);
        }
        return index;
    }

    static Map<Integer, JobCard> indexJobs(ApiContext ctx) throws Exception {
        Map<Integer, JobCard> index = new HashMap<>();
        for (JobCard job : ctx.jobs.listAll()) {
            index.put(job.getId(), job);
        }
        return index;
    }
}
