package com.vsc.api;

import com.vsc.model.Customer;
import com.vsc.model.Vehicle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Endpoints for customers and the vehicles that belong to them. */
final class CustomerApi {

    private CustomerApi() {
        // route registration only
    }

    static void register(Router router, ApiContext ctx) {
        router.get("/api/customers", request -> {
            String keyword = request.query("q");
            List<Customer> customers = ctx.customers.search(keyword == null ? "" : keyword);
            Map<Integer, Integer> counts = vehicleCounts(ctx);
            List<Object> rows = new ArrayList<>(customers.size());
            for (Customer customer : customers) {
                rows.add(ApiMapping.customer(customer, counts.getOrDefault(customer.getId(), 0)));
            }
            return rows;
        });

        router.post("/api/customers", request -> {
            Customer saved = ctx.customers.register(readCustomer(request, 0));
            return ApiResponse.created(ApiMapping.customer(saved, 0));
        });

        router.get("/api/customers/{id}", request -> {
            int id = request.pathInt("id");
            Customer customer = ctx.customers.findById(id);
            if (customer == null) {
                throw ApiException.notFound("Customer not found.");
            }
            Map<String, Object> json = ApiMapping.customer(customer, 0);
            List<Object> vehicles = new ArrayList<>();
            for (Vehicle vehicle : ctx.vehicles.listByCustomer(id)) {
                vehicles.add(ApiMapping.vehicle(vehicle, customer.getName()));
            }
            json.put("vehicles", vehicles);
            return json;
        });

        router.put("/api/customers/{id}", request -> {
            int id = request.pathInt("id");
            Customer existing = ctx.customers.findById(id);
            if (existing == null) {
                throw ApiException.notFound("Customer not found.");
            }
            ctx.customers.update(readCustomer(request, id));
            Customer saved = ctx.customers.findById(id);
            return ApiMapping.customer(saved, ctx.vehicles.listByCustomer(id).size());
        });
    }

    private static Customer readCustomer(ApiRequest request, int id) {
        Customer customer = new Customer(id, request.text("name"), request.text("phone"),
                request.text("email"), request.text("address"), null);
        return customer;
    }

    private static Map<Integer, Integer> vehicleCounts(ApiContext ctx) throws Exception {
        Map<Integer, Integer> counts = new HashMap<>();
        for (Vehicle vehicle : ctx.vehicles.listAll()) {
            counts.merge(vehicle.getCustomerId(), 1, Integer::sum);
        }
        return counts;
    }
}
