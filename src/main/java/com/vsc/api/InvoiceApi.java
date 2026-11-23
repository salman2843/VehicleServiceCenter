package com.vsc.api;

import com.vsc.model.Customer;
import com.vsc.model.Invoice;
import com.vsc.model.JobCard;
import com.vsc.model.Payment;
import com.vsc.model.PaymentMethod;
import com.vsc.model.Vehicle;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Endpoints for invoices and payments. */
final class InvoiceApi {

    private InvoiceApi() {
        // route registration only
    }

    static void register(Router router, ApiContext ctx) {
        router.get("/api/invoices", request -> {
            List<Object> rows = ApiMapping.invoiceSummaries(ctx, ctx.billing.listAll());
            String status = request.query("status");
            if (status != null) {
                rows.removeIf(row -> !status.equalsIgnoreCase(String.valueOf(field(row, "paymentStatus"))));
            }
            String keyword = request.query("q");
            if (keyword != null) {
                String needle = keyword.trim().toLowerCase(Locale.ROOT);
                rows.removeIf(row -> !contains(field(row, "registrationNo"), needle)
                        && !contains(field(row, "customerName"), needle)
                        && !String.valueOf(field(row, "id")).equals(needle));
            }
            return rows;
        });

        router.post("/api/invoices", request -> {
            BigDecimal discount = request.decimal("discount");
            BigDecimal tax = request.decimal("taxPercent");
            Invoice invoice = ctx.billing.generateInvoice(request.requiredInt("jobCardId"),
                    discount == null ? BigDecimal.ZERO : discount,
                    tax == null ? BigDecimal.ZERO : tax);
            return ApiResponse.created(detail(ctx, invoice.getId()));
        });

        router.get("/api/invoices/{id}", request -> detail(ctx, request.pathInt("id")));

        router.post("/api/invoices/{id}/payments", request -> {
            int id = request.pathInt("id");
            PaymentMethod method = parseMethod(request.text("method"));
            ctx.billing.recordPayment(id, request.requiredDecimal("amount"), method, request.text("reference"));
            return detail(ctx, id);
        });
    }

    private static Map<String, Object> detail(ApiContext ctx, int invoiceId) throws Exception {
        Invoice invoice = ctx.billing.findById(invoiceId);
        if (invoice == null) {
            throw ApiException.notFound("Invoice not found.");
        }
        List<Payment> payments = ctx.billing.paymentsOf(invoiceId);
        BigDecimal paid = payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        JobCard job = ctx.jobs.findById(invoice.getJobCardId());
        Vehicle vehicle = job == null ? null : ctx.vehicles.findById(job.getVehicleId());
        Customer customer = vehicle == null ? null : ctx.customers.findById(vehicle.getCustomerId());

        Map<String, Object> json = new LinkedHashMap<>(
                ApiMapping.invoice(invoice, paid, job, vehicle, customer));

        List<Object> paymentRows = new ArrayList<>(payments.size());
        for (Payment payment : payments) {
            paymentRows.add(ApiMapping.payment(payment));
        }
        json.put("payments", paymentRows);

        Map<String, Object> jobJson = ApiMapping.jobDetail(ctx, invoice.getJobCardId());
        jobJson.remove("invoice");
        json.put("job", jobJson);
        return json;
    }

    private static PaymentMethod parseMethod(String raw) throws ApiException {
        if (raw == null || raw.isBlank()) {
            throw ApiException.badRequest("method", "Choose CASH, UPI or CARD.");
        }
        try {
            return PaymentMethod.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("method", "Unknown payment method: " + raw + ".");
        }
    }

    private static boolean contains(Object value, String needle) {
        return value != null && String.valueOf(value).toLowerCase(Locale.ROOT).contains(needle);
    }

    private static Object field(Object row, String key) {
        return row instanceof Map<?, ?> map ? map.get(key) : null;
    }
}
