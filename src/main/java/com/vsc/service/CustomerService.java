package com.vsc.service;

import com.vsc.dao.CustomerDao;
import com.vsc.exception.ValidationException;
import com.vsc.model.Customer;
import com.vsc.util.DBConnection;
import com.vsc.util.InputValidator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Business rules for customers: validation, the "one phone number per customer"
 * rule, and the small queries the customer screen needs.
 *
 * <p>The UI calls this class only; it never touches {@link CustomerDao} or SQL
 * directly (see the layer rule in AGENTS.md).</p>
 */
public class CustomerService {

    private final CustomerDao customerDao;

    public CustomerService() {
        this(new CustomerDao());
    }

    /** Lets a test or a future alternative storage plug in a different DAO. */
    public CustomerService(CustomerDao customerDao) {
        this.customerDao = customerDao;
    }

    /**
     * Validates and stores a new customer.
     *
     * @throws ValidationException when a field is invalid or the phone is taken
     */
    public Customer register(Customer customer) throws ValidationException, SQLException {
        validate(customer);
        try (Connection con = DBConnection.getConnection()) {
            Customer samePhone = customerDao.findByPhone(con, customer.getPhone());
            if (samePhone != null) {
                throw new ValidationException("phone",
                        "A customer with this phone number already exists: " + samePhone.getName());
            }
            return customerDao.insert(con, customer);
        }
    }

    public void update(Customer customer) throws ValidationException, SQLException {
        if (customer.getId() <= 0) {
            throw new ValidationException("id", "Update needs a customer that was already saved.");
        }
        validate(customer);
        try (Connection con = DBConnection.getConnection()) {
            if (customerDao.findById(con, customer.getId()) == null) {
                throw new ValidationException("id", "Customer no longer exists.");
            }
            Customer samePhone = customerDao.findByPhone(con, customer.getPhone());
            if (samePhone != null && samePhone.getId() != customer.getId()) {
                throw new ValidationException("phone",
                        "This phone number belongs to " + samePhone.getName() + ".");
            }
            customerDao.update(con, customer);
        }
    }

    /** Returns the customer or {@code null}. */
    public Customer findById(int id) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            return customerDao.findById(con, id);
        }
    }

    /** Blank keyword lists every customer; otherwise matches name or phone. */
    public List<Customer> search(String keyword) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            return customerDao.search(con, keyword);
        }
    }

    public List<Customer> listAll() throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            return customerDao.findAll(con);
        }
    }

    /** Trims the text fields and checks them, so the database never sees blanks. */
    private void validate(Customer customer) throws ValidationException {
        if (customer == null) {
            throw new ValidationException("customer", "No customer details were supplied.");
        }
        customer.setName(InputValidator.trim(customer.getName()));
        customer.setPhone(InputValidator.trim(customer.getPhone()));
        customer.setEmail(InputValidator.trim(customer.getEmail()));
        customer.setAddress(InputValidator.trim(customer.getAddress()));

        if (!InputValidator.isValidName(customer.getName())) {
            throw new ValidationException("name", "Name must be 2 to 80 characters.");
        }
        if (!InputValidator.isValidPhone(customer.getPhone())) {
            throw new ValidationException("phone", "Phone must contain 7 to 15 digits.");
        }
        if (customer.getEmail() != null && !customer.getEmail().isEmpty()
                && !InputValidator.isValidEmail(customer.getEmail())) {
            throw new ValidationException("email", "E-mail looks invalid, e.g. name@example.com.");
        }
        if (customer.getAddress() != null && customer.getAddress().length() > 255) {
            throw new ValidationException("address", "Address must be at most 255 characters.");
        }
    }
}
