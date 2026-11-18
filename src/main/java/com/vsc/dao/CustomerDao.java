package com.vsc.dao;

import com.vsc.model.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * All SQL for the {@code customers} table.
 *
 * <p>Every method receives the {@link Connection} from the service layer instead
 * of opening its own, so several DAO calls can share one transaction. DAOs
 * contain SQL and row mapping only - no validation and no business rules.</p>
 */
public class CustomerDao {

    private static final String SELECT_ALL =
            "SELECT id, name, phone, email, address, created_on FROM customers";

    /** Inserts the customer and copies the generated id back onto the object. */
    public Customer insert(Connection con, Customer customer) throws SQLException {
        String sql = "INSERT INTO customers (name, phone, email, address) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, customer.getName());
            ps.setString(2, customer.getPhone());
            ps.setString(3, customer.getEmail());
            ps.setString(4, customer.getAddress());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    customer.setId(keys.getInt(1));
                }
            }
        }
        return customer;
    }

    public void update(Connection con, Customer customer) throws SQLException {
        String sql = "UPDATE customers SET name = ?, phone = ?, email = ?, address = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, customer.getName());
            ps.setString(2, customer.getPhone());
            ps.setString(3, customer.getEmail());
            ps.setString(4, customer.getAddress());
            ps.setInt(5, customer.getId());
            ps.executeUpdate();
        }
    }

    /** Returns the customer, or {@code null} when the id does not exist. */
    public Customer findById(Connection con, int id) throws SQLException {
        String sql = SELECT_ALL + " WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return singleRow(ps);
        }
    }

    /** Returns the first customer with this phone number, or {@code null}. */
    public Customer findByPhone(Connection con, String phone) throws SQLException {
        String sql = SELECT_ALL + " WHERE phone = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, phone);
            return singleRow(ps);
        }
    }

    /** Case-insensitive search on name or phone; blank input lists everybody. */
    public List<Customer> search(Connection con, String keyword) throws SQLException {
        String sql = SELECT_ALL + " WHERE name LIKE ? OR phone LIKE ? ORDER BY name";
        String pattern = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            return manyRows(ps);
        }
    }

    public List<Customer> findAll(Connection con) throws SQLException {
        String sql = SELECT_ALL + " ORDER BY name";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            return manyRows(ps);
        }
    }

    private Customer singleRow(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            return rs.next() ? map(rs) : null;
        }
    }

    private List<Customer> manyRows(PreparedStatement ps) throws SQLException {
        List<Customer> customers = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                customers.add(map(rs));
            }
        }
        return customers;
    }

    /** Builds the Java object from one row. */
    private Customer map(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("address"),
                rs.getObject("created_on", LocalDateTime.class));
    }
}
