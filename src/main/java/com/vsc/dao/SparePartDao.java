package com.vsc.dao;

import com.vsc.model.SparePart;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * All SQL for the {@code spare_parts} inventory table.
 *
 * <p>{@link #deductStock} uses a conditional UPDATE
 * ({@code WHERE stock_qty >= ?}) so two simultaneous jobs can never push the
 * stock below zero, even if both read the same starting quantity.</p>
 */
public class SparePartDao {

    private static final String SELECT_ALL =
            "SELECT id, part_number, name, unit_price, stock_qty, reorder_level FROM spare_parts";

    public SparePart insert(Connection con, SparePart part) throws SQLException {
        String sql = "INSERT INTO spare_parts (part_number, name, unit_price, stock_qty, reorder_level) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, part.getPartNumber());
            ps.setString(2, part.getName());
            ps.setBigDecimal(3, part.getUnitPrice());
            ps.setInt(4, part.getStockQty());
            ps.setInt(5, part.getReorderLevel());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    part.setId(keys.getInt(1));
                }
            }
        }
        return part;
    }

    public void update(Connection con, SparePart part) throws SQLException {
        String sql = "UPDATE spare_parts SET part_number = ?, name = ?, unit_price = ?, "
                + "stock_qty = ?, reorder_level = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, part.getPartNumber());
            ps.setString(2, part.getName());
            ps.setBigDecimal(3, part.getUnitPrice());
            ps.setInt(4, part.getStockQty());
            ps.setInt(5, part.getReorderLevel());
            ps.setInt(6, part.getId());
            ps.executeUpdate();
        }
    }

    public SparePart findById(Connection con, int id) throws SQLException {
        String sql = SELECT_ALL + " WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public SparePart findByPartNumber(Connection con, String partNumber) throws SQLException {
        String sql = SELECT_ALL + " WHERE part_number = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, partNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<SparePart> findAll(Connection con) throws SQLException {
        String sql = SELECT_ALL + " ORDER BY name";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            return manyRows(ps);
        }
    }

    /** Parts at or below their reorder level, for the low-stock warning. */
    public List<SparePart> findLowStock(Connection con) throws SQLException {
        String sql = SELECT_ALL + " WHERE stock_qty <= reorder_level ORDER BY name";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            return manyRows(ps);
        }
    }

    /**
     * Takes {@code quantity} units out of stock.
     *
     * @return true when the row was updated, false when there was not enough
     *         stock (the caller decides which exception to throw)
     */
    public boolean deductStock(Connection con, int partId, int quantity) throws SQLException {
        String sql = "UPDATE spare_parts SET stock_qty = stock_qty - ? WHERE id = ? AND stock_qty >= ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, partId);
            ps.setInt(3, quantity);
            return ps.executeUpdate() == 1;
        }
    }

    /** Receives new stock for an existing part. */
    public void addStock(Connection con, int partId, int quantity) throws SQLException {
        String sql = "UPDATE spare_parts SET stock_qty = stock_qty + ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, partId);
            ps.executeUpdate();
        }
    }

    private List<SparePart> manyRows(PreparedStatement ps) throws SQLException {
        List<SparePart> parts = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                parts.add(map(rs));
            }
        }
        return parts;
    }

    private SparePart map(ResultSet rs) throws SQLException {
        return new SparePart(
                rs.getInt("id"),
                rs.getString("part_number"),
                rs.getString("name"),
                rs.getBigDecimal("unit_price"),
                rs.getInt("stock_qty"),
                rs.getInt("reorder_level"));
    }
}
