package com.vsc.dao;

import com.vsc.model.ServiceItem;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** All SQL for the {@code services} catalogue table. */
public class ServiceItemDao {

    private static final String SELECT_ALL = "SELECT id, name, details, base_price FROM services";

    public ServiceItem insert(Connection con, ServiceItem service) throws SQLException {
        String sql = "INSERT INTO services (name, details, base_price) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, service.getName());
            ps.setString(2, service.getDetails());
            ps.setBigDecimal(3, service.getBasePrice());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    service.setId(keys.getInt(1));
                }
            }
        }
        return service;
    }

    public ServiceItem findById(Connection con, int id) throws SQLException {
        String sql = SELECT_ALL + " WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<ServiceItem> findAll(Connection con) throws SQLException {
        String sql = SELECT_ALL + " ORDER BY name";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            List<ServiceItem> services = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    services.add(map(rs));
                }
            }
            return services;
        }
    }

    private ServiceItem map(ResultSet rs) throws SQLException {
        BigDecimal price = rs.getBigDecimal("base_price");
        return new ServiceItem(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("details"),
                price);
    }
}
