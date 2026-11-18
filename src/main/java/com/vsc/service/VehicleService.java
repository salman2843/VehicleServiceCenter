package com.vsc.service;

import com.vsc.dao.CustomerDao;
import com.vsc.dao.VehicleDao;
import com.vsc.exception.ValidationException;
import com.vsc.model.Car;
import com.vsc.model.Motorcycle;
import com.vsc.model.Vehicle;
import com.vsc.util.DBConnection;
import com.vsc.util.InputValidator;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Year;
import java.util.List;
import java.util.Locale;

/**
 * Business rules for vehicles: the owner must exist, the registration number is
 * normalised and unique, and a car / motorcycle carries its own extra field.
 */
public class VehicleService {

    private static final int OLDEST_YEAR = 1980;

    private final VehicleDao vehicleDao;
    private final CustomerDao customerDao;

    public VehicleService() {
        this(new VehicleDao(), new CustomerDao());
    }

    public VehicleService(VehicleDao vehicleDao, CustomerDao customerDao) {
        this.vehicleDao = vehicleDao;
        this.customerDao = customerDao;
    }

    public Vehicle add(Vehicle vehicle) throws ValidationException, SQLException {
        validate(vehicle);
        try (Connection con = DBConnection.getConnection()) {
            if (customerDao.findById(con, vehicle.getCustomerId()) == null) {
                throw new ValidationException("customer",
                        "Save the customer before adding a vehicle to them.");
            }
            if (vehicleDao.findByRegistration(con, vehicle.getRegistrationNo()) != null) {
                throw new ValidationException("registration",
                        "Registration number " + vehicle.getRegistrationNo() + " is already registered.");
            }
            return vehicleDao.insert(con, vehicle);
        }
    }

    public Vehicle findById(int id) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            return vehicleDao.findById(con, id);
        }
    }

    public Vehicle findByRegistration(String registrationNo) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            return vehicleDao.findByRegistration(con, normalize(registrationNo));
        }
    }

    public List<Vehicle> listByCustomer(int customerId) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            return vehicleDao.findByCustomer(con, customerId);
        }
    }

    public List<Vehicle> listAll() throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            return vehicleDao.findAll(con);
        }
    }

    /** UK-style plates become "KA01AB1234"; the number is stored in one shape. */
    private static String normalize(String registrationNo) {
        return registrationNo == null
                ? ""
                : registrationNo.trim().toUpperCase(Locale.ROOT).replaceAll("\\s+", "");
    }

    private void validate(Vehicle vehicle) throws ValidationException {
        if (vehicle == null) {
            throw new ValidationException("vehicle", "No vehicle details were supplied.");
        }
        vehicle.setRegistrationNo(normalize(vehicle.getRegistrationNo()));
        vehicle.setMake(InputValidator.trim(vehicle.getMake()));
        vehicle.setModelName(InputValidator.trim(vehicle.getModelName()));

        if (vehicle.getCustomerId() <= 0) {
            throw new ValidationException("customer", "Choose the customer who owns this vehicle.");
        }
        if (!InputValidator.isValidRegistrationNo(vehicle.getRegistrationNo())) {
            throw new ValidationException("registration",
                    "Registration number must be 4 to 16 characters, e.g. KA01AB1234.");
        }
        if (InputValidator.isBlank(vehicle.getMake()) || vehicle.getMake().length() > 50) {
            throw new ValidationException("make", "Make is required (max 50 characters).");
        }
        if (InputValidator.isBlank(vehicle.getModelName()) || vehicle.getModelName().length() > 50) {
            throw new ValidationException("model", "Model is required (max 50 characters).");
        }

        int year = vehicle.getYear();
        int newest = Year.now().getValue() + 1;
        if (year < OLDEST_YEAR || year > newest) {
            throw new ValidationException("year", "Year must be between " + OLDEST_YEAR + " and " + newest + ".");
        }

        if (vehicle instanceof Car car && (car.getNumberOfDoors() < 2 || car.getNumberOfDoors() > 7)) {
            throw new ValidationException("doors", "A car has 2 to 7 doors.");
        }
        if (vehicle instanceof Motorcycle motorcycle && motorcycle.getEngineCapacityCc() <= 0) {
            throw new ValidationException("engine", "Engine capacity must be a positive number of cc.");
        }
    }
}
