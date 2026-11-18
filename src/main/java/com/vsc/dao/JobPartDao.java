package com.vsc.dao;

import com.vsc.model.JobPartLine;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * All SQL for {@code job_parts}: the spare parts used on a job card.
 * {@code unit_price} is the snapshot price at the moment the part was used.
 */
public class JobPartDao {

    public void add(Connection con, int jobCardId, int partId, int quantity, BigDecimal unitPrice)
            throws SQLException {
        String sql = "INSERT INTO job_parts (job_card_id, part_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, jobCardId);
            ps.setInt(2, partId);
            ps.setInt(3, quantity);
            ps.setBigDecimal(4, unitPrice);
            ps.executeUpdate();
        }
    }

    /** Lines of one job card, joined with the part number and name for display. */
    public List<JobPartLine> findByJob(Connection con, int jobCardId) throws SQLException {
        String sql = "SELECT jp.id, jp.job_card_id, jp.part_id, p.part_number, p.name, "
                + "jp.quantity, jp.unit_price "
                + "FROM job_parts jp JOIN spare_parts p ON p.id = jp.part_id "
                + "WHERE jp.job_card_id = ? ORDER BY jp.id";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, jobCardId);
            List<JobPartLine> lines = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lines.add(new JobPartLine(
                            rs.getInt("id"),
                            rs.getInt("job_card_id"),
                            rs.getInt("part_id"),
                            rs.getString("part_number"),
                            rs.getString("name"),
                            rs.getInt("quantity"),
                            rs.getBigDecimal("unit_price")));
                }
            }
            return lines;
        }
    }

    /** Parts total for an invoice: sum of quantity x snapshot price. */
    public BigDecimal partsTotal(Connection con, int jobCardId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(quantity * unit_price), 0) FROM job_parts WHERE job_card_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, jobCardId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1);
            }
        }
    }
}
