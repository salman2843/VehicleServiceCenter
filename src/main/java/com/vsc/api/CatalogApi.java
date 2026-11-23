package com.vsc.api;

import com.vsc.model.ServiceItem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Endpoints for the service catalogue (labour charges). */
final class CatalogApi {

    private CatalogApi() {
        // route registration only
    }

    static void register(Router router, ApiContext ctx) {
        router.get("/api/services", request -> {
            List<ServiceItem> services = ctx.catalogue.listAll();
            List<Object> rows = new ArrayList<>(services.size());
            for (ServiceItem service : services) {
                rows.add(ApiMapping.service(service));
            }
            return rows;
        });

        router.post("/api/services", request -> {
            BigDecimal price = request.decimal("basePrice");
            ServiceItem service = new ServiceItem(0, request.text("name"), request.text("details"),
                    price == null ? BigDecimal.ZERO : price);
            return ApiResponse.created(ApiMapping.service(ctx.catalogue.add(service)));
        });
    }
}
