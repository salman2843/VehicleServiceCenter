package com.vsc.api;

import com.vsc.model.JobCard;
import com.vsc.model.JobStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Endpoints for job cards: open, assign, move status, add services and parts. */
final class JobApi {

    private JobApi() {
        // route registration only
    }

    static void register(Router router, ApiContext ctx) {
        router.get("/api/jobs", request -> {
            String status = request.query("status");
            List<JobCard> jobs = ctx.jobs.listAll();
            List<Object> rows = ApiMapping.jobSummaries(ctx, jobs);

            String keyword = request.query("q");
            if (status != null) {
                JobStatus wanted = parseStatus(status);
                rows.removeIf(row -> !wanted.name().equals(field(row, "status")));
            }
            if (keyword != null) {
                rows.removeIf(row -> !matches(row, keyword));
            }
            return rows;
        });

        router.post("/api/jobs", request -> {
            JobCard card = ctx.jobs.openJob(request.requiredInt("vehicleId"), request.text("complaint"));
            return ApiResponse.created(ApiMapping.jobDetail(ctx, card.getId()));
        });

        router.get("/api/jobs/{id}", request -> ApiMapping.jobDetail(ctx, request.pathInt("id")));

        router.post("/api/jobs/{id}/mechanic", request -> {
            int id = request.pathInt("id");
            ctx.jobs.assignMechanic(id, request.requiredInt("mechanicId"));
            return ApiMapping.jobDetail(ctx, id);
        });

        router.post("/api/jobs/{id}/status", request -> {
            int id = request.pathInt("id");
            String target = request.text("status");
            if (target == null || target.isBlank()) {
                ctx.jobs.advanceStatus(id);
            } else {
                ctx.jobs.updateStatus(id, parseStatus(target));
            }
            return ApiMapping.jobDetail(ctx, id);
        });

        router.post("/api/jobs/{id}/services", request -> {
            int id = request.pathInt("id");
            ctx.jobs.addService(id, request.requiredInt("serviceId"), request.decimal("agreedPrice"));
            return ApiMapping.jobDetail(ctx, id);
        });

        router.post("/api/jobs/{id}/parts", request -> {
            int id = request.pathInt("id");
            ctx.jobs.addPart(id, request.requiredInt("partId"), request.requiredInt("quantity"));
            return ApiMapping.jobDetail(ctx, id);
        });
    }

    private static JobStatus parseStatus(String raw) throws ApiException {
        try {
            return JobStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("status", "Unknown job status: " + raw + ".");
        }
    }

    private static boolean matches(Object row, String keyword) {
        String needle = keyword.trim().toLowerCase(Locale.ROOT);
        return contains(field(row, "registrationNo"), needle)
                || contains(field(row, "customerName"), needle)
                || contains(field(row, "complaint"), needle)
                || String.valueOf(field(row, "id")).equals(needle);
    }

    private static Object field(Object row, String key) {
        return row instanceof Map<?, ?> map ? map.get(key) : null;
    }

    private static boolean contains(Object value, String needle) {
        return value != null && String.valueOf(value).toLowerCase(Locale.ROOT).contains(needle);
    }
}
