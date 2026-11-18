package com.vsc.dao;

import com.vsc.model.Car;
import com.vsc.model.Motorcycle;
import com.vsc.model.Vehicle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * All SQL for the {@code vehicles} table.
 *
 * <p>{@link Car} and {@link Motorcycle} share one table (single-table
 * inheritance): {@code vehicle_type} says which Java class to build and the two
 * nullable columns hold the subclass-only fields.</p>
 */
public class VehicleDao {

    private static final String SELECT_ALL =
            "SELECT id, customer_id, registration_no, vehicle_type, make, model_name, "
                    + "model_year, number_of_doors, engine_capacity_cc FROM vehicles";

    /** Inserts a car or a motorcycle and copies the generated id onto the object. */
    public Vehicle insert(Connection con, Vehicle vehicle) throws SQLException {
        String sql = "INSERT INTO vehicles (customer_id, registration_no, vehicle_type, make, "
                + "model_name, model_year, number_of_doors, engine_capacity_cc) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, vehicle.getCustomerId());
            ps.setString(2, vehicle.getRegistrationNo());
            ps.setString(3, databaseType(vehicle));
            ps.setString(4, vehicle.getMake());
            ps.setString(5, vehicle.getModelName());
            setNullableInt(ps, 6, vehicle.getYear());

            if (vehicle instanceof Car car) {
                setNullableInt(ps, 7, car.getNumberOfDoors());
                ps.setNull(8, Types.INTEGER);
            } else if (vehicle instanceof Motorcycle motorcycle) {
                ps.setNull(7, Types.INTEGER);
                setNullableInt(ps, 8, motorcycle.getEngineCapacityCc());
            } else {
                ps.setNull(7, Types.INTEGER);
                ps.setNull(8, Types.INTEGER);
            }

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    vehicle.setId(keys.getInt(1));
                }
            }
        }
        return vehicle;
    }

    public Vehicle findById(Connection con, int id) throws SQLException {
        String sql = SELECT_ALL + " WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /** Used to enforce the "one registration number per vehicle" rule. */
    public Vehicle findByRegistration(Connection con, String registrationNo) throws SQLException {
        String sql = SELECT_ALL + " WHERE registration_no = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, registrationNo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<Vehicle> findByCustomer(Connection con, int customerId) throws SQLException {
        String sql = SELECT_ALL + " WHERE customer_id = ? ORDER BY registration_no";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            return manyRows(ps);
        }
    }

    public List<Vehicle> findAll(Connection con) throws SQLException {
        String sql = SELECT_ALL + " ORDER BY registration_no";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            return manyRows(ps);
        }
    }

    private List<Vehicle> manyRows(PreparedStatement ps) throws SQLException {
        List<Vehicle> vehicles = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                vehicles.add(map(rs));
            }
        }
        return vehicles;
    }

    /** Chooses the subclass from {@code vehicle_type}, then reads its own columns. */
    private Vehicle map(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int customerId = rs.getInt("customer_id");
        String registrationNo = rs.getString("registration_no");
        String make = rs.getString("make");
        String modelName = rs.getString("model_name");
        int year = rs.getInt("model_year");   // NULL becomes 0

        if ("MOTORCYCLE".equals(rs.getString("vehicle_type"))) {
            return new Motorcycle(id, customerId, registrationNo, make, modelName, year,
                    rs.getInt("engine_capacity_cc"));
        }
        return new Car(id, customerId, registrationNo, make, modelName, year,
                rs.getInt("number_of_doors"));
    }

    private String databaseType(Vehicle vehicle) {
        return vehicle instanceof Motorcycle ? "MOTORCYCLE" : "CAR";
    }

    /** Year 0 (unknown) is stored as NULL instead of 1970. */
    private void setNullableInt(PreparedStatement ps, int index, int value) throws SQLException {
        if (value > 0) {
            ps.setInt(index, value);
        } else {
            ps.setNull(index, Types.INTEGER);
        }
    }
}
