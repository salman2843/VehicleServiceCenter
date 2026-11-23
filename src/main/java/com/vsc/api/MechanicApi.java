package com.vsc.api;

import com.vsc.model.Mechanic;

import java.util.ArrayList;
import java.util.List;

/** Endpoints for mechanics. */
final class MechanicApi {

    private MechanicApi() {
        // route registration only
    }

    static void register(Router router, ApiContext ctx) {
        router.get("/api/mechanics", request -> {
            boolean activeOnly = "true".equalsIgnoreCase(request.query("active"));
            List<Mechanic> mechanics = activeOnly ? ctx.mechanics.listActive() : ctx.mechanics.listAll();
            List<Object> rows = new ArrayList<>(mechanics.size());
            for (Mechanic mechanic : mechanics) {
                rows.add(ApiMapping.mechanic(mechanic));
            }
            return rows;
        });

        router.post("/api/mechanics", request -> {
            Mechanic saved = ctx.mechanics.add(readMechanic(request, 0));
            return ApiResponse.created(ApiMapping.mechanic(saved));
        });

        router.put("/api/mechanics/{id}", request -> {
            int id = request.pathInt("id");
            if (ctx.mechanics.findById(id) == null) {
                throw ApiException.notFound("Mechanic not found.");
            }
            ctx.mechanics.update(readMechanic(request, id));
            return ApiMapping.mechanic(ctx.mechanics.findById(id));
        });
    }

    private static Mechanic readMechanic(ApiRequest request, int id) {
        boolean active = !"false".equalsIgnoreCase(String.valueOf(request.body().get("active")));
        return new Mechanic(id, request.text("name"), request.text("phone"),
                request.text("specialization"), active);
    }
}
