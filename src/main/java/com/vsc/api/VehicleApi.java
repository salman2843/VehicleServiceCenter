package com.vsc.api;

import com.vsc.model.Car;
import com.vsc.model.Customer;
import com.vsc.model.JobCard;
import com.vsc.model.Motorcycle;
import com.vsc.model.Vehicle;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Endpoints for vehicles and a vehicle's service history. */
final class VehicleApi {

    private VehicleApi() {
        // route registration only
    }

    static void register(Router router, ApiContext ctx) {
        router.get("/api/vehicles", request -> {
            String keyword = request.query("q");
            String customerId = request.query("customerId");
            List<Vehicle> vehicles = customerId == null
                    ? ctx.vehicles.listAll()
                    : ctx.vehicles.listByCustomer(Integer.parseInt(customerId));

            Map<Integer, Customer> customers = ApiMapping.indexCustomers(ctx);
            List<Object> rows = new ArrayList<>();
            for (Vehicle vehicle : vehicles) {
                Customer owner = customers.get(vehicle.getCustomerId());
                String ownerName = owner == null ? null : owner.getName();
                if (keyword == null || matches(vehicle, ownerName, keyword)) {
                    rows.add(ApiMapping.vehicle(vehicle, ownerName));
                }
            }
            return rows;
        });

        router.post("/api/vehicles", request -> {
            Vehicle vehicle = readVehicle(request);
            Vehicle saved = ctx.vehicles.add(vehicle);
            Customer owner = ctx.customers.findById(saved.getCustomerId());
            return ApiResponse.created(ApiMapping.vehicle(saved, owner == null ? null : owner.getName()));
        });

        router.get("/api/vehicles/{id}", request -> {
            int id = request.pathInt("id");
            Vehicle vehicle = ctx.vehicles.findById(id);
            if (vehicle == null) {
                throw ApiException.notFound("Vehicle not found.");
            }
            Customer owner = ctx.customers.findById(vehicle.getCustomerId());
            Map<String, Object> json = ApiMapping.vehicle(vehicle, owner == null ? null : owner.getName());
            List<JobCard> history = ctx.jobs.history(id);
            json.put("jobs", ApiMapping.jobSummaries(ctx, history));
            return json;
        });

        router.get("/api/vehicles/{id}/jobs", request ->
                ApiMapping.jobSummaries(ctx, ctx.jobs.history(request.pathInt("id"))));
    }

    private static Vehicle readVehicle(ApiRequest request) {
        String type = request.text("vehicleType");
        String kind = type == null ? "CAR" : type.trim().toUpperCase(Locale.ROOT);
        int customerId = request.integer("customerId") == null ? 0 : request.integer("customerId");
        String registrationNo = request.text("registrationNo");
        String make = request.text("make");
        String model = request.text("modelName");
        int year = request.integer("year") == null ? 0 : request.integer("year");

        if ("MOTORCYCLE".equals(kind)) {
            int cc = request.integer("engineCapacityCc") == null ? 0 : request.integer("engineCapacityCc");
            return new Motorcycle(0, customerId, registrationNo, make, model, year, cc);
        }
        int doors = request.integer("numberOfDoors") == null ? 4 : request.integer("numberOfDoors");
        return new Car(0, customerId, registrationNo, make, model, year, doors);
    }

    private static boolean matches(Vehicle vehicle, String ownerName, String keyword) {
        String needle = keyword.trim().toLowerCase(Locale.ROOT);
        return contains(vehicle.getRegistrationNo(), needle)
                || contains(vehicle.getMake(), needle)
                || contains(vehicle.getModelName(), needle)
                || contains(ownerName, needle);
    }

    private static boolean contains(String value, String needle) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(needle);
    }
}
