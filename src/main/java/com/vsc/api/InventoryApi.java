package com.vsc.api;

import com.vsc.model.SparePart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Endpoints for spare parts and stock. */
final class InventoryApi {

    private InventoryApi() {
        // route registration only
    }

    static void register(Router router, ApiContext ctx) {
        router.get("/api/parts", request -> {
            List<SparePart> parts = ctx.inventory.listAll();
            List<Object> rows = new ArrayList<>(parts.size());
            for (SparePart part : parts) {
                rows.add(ApiMapping.part(part));
            }
            return rows;
        });

        router.post("/api/parts", request ->
                ApiResponse.created(ApiMapping.part(ctx.inventory.addPart(readPart(request, 0)))));

        router.put("/api/parts/{id}", request -> {
            int id = request.pathInt("id");
            ctx.inventory.updatePart(readPart(request, id));
            return ApiMapping.part(findPart(ctx, id));
        });

        router.post("/api/parts/{id}/receive", request -> {
            int id = request.pathInt("id");
            ctx.inventory.receiveStock(id, request.requiredInt("quantity"));
            return ApiMapping.part(findPart(ctx, id));
        });
    }

    private static SparePart findPart(ApiContext ctx, int id) throws Exception {
        for (SparePart part : ctx.inventory.listAll()) {
            if (part.getId() == id) {
                return part;
            }
        }
        throw ApiException.notFound("Spare part not found.");
    }

    private static SparePart readPart(ApiRequest request, int id) {
        BigDecimal price = request.decimal("unitPrice");
        int stock = request.integer("stockQty") == null ? 0 : request.integer("stockQty");
        int reorder = request.integer("reorderLevel") == null ? 0 : request.integer("reorderLevel");
        return new SparePart(id, request.text("partNumber"), request.text("name"),
                price == null ? BigDecimal.ZERO : price, stock, reorder);
    }
}
