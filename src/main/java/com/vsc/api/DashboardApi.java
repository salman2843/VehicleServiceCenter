package com.vsc.api;

import com.vsc.model.Invoice;
import com.vsc.model.JobCard;
import com.vsc.model.JobStatus;
import com.vsc.model.Mechanic;
import com.vsc.model.SparePart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** One call that fills the dashboard, so the screen needs a single request. */
final class DashboardApi {

    private DashboardApi() {
        // route registration only
    }

    static void register(Router router, ApiContext ctx) {
        router.get("/api/dashboard", request -> {
            List<JobCard> jobs = ctx.jobs.listAll();
            List<Invoice> invoices = ctx.billing.listAll();
            List<SparePart> lowStock = ctx.inventory.listLowStock();
            List<Mechanic> activeMechanics = ctx.mechanics.listActive();
            Map<Integer, BigDecimal> paid = ctx.billing.paidTotals();

            int openJobs = 0;
            Map<JobStatus, Integer> byStatus = new java.util.EnumMap<>(JobStatus.class);
            for (JobStatus status : JobStatus.values()) {
                byStatus.put(status, 0);
            }
            for (JobCard job : jobs) {
                byStatus.merge(job.getStatus(), 1, Integer::sum);
                if (job.getStatus() != JobStatus.DELIVERED) {
                    openJobs++;
                }
            }

            BigDecimal invoiced = BigDecimal.ZERO;
            BigDecimal collected = BigDecimal.ZERO;
            int unpaidInvoices = 0;
            for (Invoice invoice : invoices) {
                BigDecimal total = invoice.getTotal() == null ? BigDecimal.ZERO : invoice.getTotal();
                BigDecimal settled = paid.getOrDefault(invoice.getId(), BigDecimal.ZERO);
                invoiced = invoiced.add(total);
                collected = collected.add(settled);
                if (total.compareTo(settled) > 0) {
                    unpaidInvoices++;
                }
            }

            List<Object> statusRows = new ArrayList<>();
            byStatus.forEach((status, count) -> statusRows.add(Json.obj("status", status, "count", count)));

            List<Object> recentJobs = ApiMapping.jobSummaries(ctx, jobs.subList(0, Math.min(6, jobs.size())));

            List<Object> invoiceRows = ApiMapping.invoiceSummaries(ctx, invoices);
            List<Object> unpaidRows = new ArrayList<>();
            for (Object row : invoiceRows) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) row;
                BigDecimal outstanding = (BigDecimal) map.get("outstanding");
                if (outstanding != null && outstanding.compareTo(BigDecimal.ZERO) > 0) {
                    unpaidRows.add(row);
                }
            }

            List<Object> lowStockRows = new ArrayList<>(lowStock.size());
            for (SparePart part : lowStock) {
                lowStockRows.add(ApiMapping.part(part));
            }

            return Json.obj(
                    "counts", Json.obj(
                            "customers", ctx.customers.listAll().size(),
                            "vehicles", ctx.vehicles.listAll().size(),
                            "jobs", jobs.size(),
                            "openJobs", openJobs,
                            "invoices", invoices.size(),
                            "unpaidInvoices", unpaidInvoices,
                            "lowStockParts", lowStock.size(),
                            "mechanics", activeMechanics.size()),
                    "money", Json.obj(
                            "invoiced", invoiced,
                            "collected", collected,
                            "outstanding", invoiced.subtract(collected)),
                    "jobsByStatus", statusRows,
                    "recentJobs", recentJobs,
                    "outstandingInvoices", unpaidRows.subList(0, Math.min(5, unpaidRows.size())),
                    "lowStock", lowStockRows);
        });
    }
}
