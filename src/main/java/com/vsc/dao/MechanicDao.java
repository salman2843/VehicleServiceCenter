package com.vsc.dao;

import com.vsc.model.Mechanic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** All SQL for the {@code mechanics} table. */
public class MechanicDao {

    private static final String SELECT_ALL =
            "SELECT id, name, phone, specialization, active FROM mechanics";

    public Mechanic insert(Connection con, Mechanic mechanic) throws SQLException {
        String sql = "INSERT INTO mechanics (name, phone, specialization, active) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, mechanic.getName());
            ps.setString(2, mechanic.getPhone());
            ps.setString(3, mechanic.getSpecialization());
            ps.setBoolean(4, mechanic.isActive());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    mechanic.setId(keys.getInt(1));
                }
            }
        }
        return mechanic;
    }

    public void update(Connection con, Mechanic mechanic) throws SQLException {
        String sql = "UPDATE mechanics SET name = ?, phone = ?, specialization = ?, active = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, mechanic.getName());
            ps.setString(2, mechanic.getPhone());
            ps.setString(3, mechanic.getSpecialization());
            ps.setBoolean(4, mechanic.isActive());
            ps.setInt(5, mechanic.getId());
            ps.executeUpdate();
        }
    }

    public Mechanic findById(Connection con, int id) throws SQLException {
        String sql = SELECT_ALL + " WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /** Job-card assignment should only offer active mechanics. */
    public List<Mechanic> findAll(Connection con, boolean activeOnly) throws SQLException {
        String sql = SELECT_ALL + (activeOnly ? " WHERE active = 1" : "") + " ORDER BY name";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            List<Mechanic> mechanics = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    mechanics.add(map(rs));
                }
            }
            return mechanics;
        }
    }

    private Mechanic map(ResultSet rs) throws SQLException {
        return new Mechanic(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("phone"),
                rs.getString("specialization"),
                rs.getBoolean("active"));
    }
}
