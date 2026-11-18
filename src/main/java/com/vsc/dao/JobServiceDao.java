package com.vsc.dao;

import com.vsc.model.JobServiceLine;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * All SQL for {@code job_services}: the services added to a job card.
 * {@code agreed_price} is a snapshot, so later catalogue price changes never
 * rewrite an old bill.
 */
public class JobServiceDao {

    public void add(Connection con, int jobCardId, int serviceId, BigDecimal agreedPrice)
            throws SQLException {
        String sql = "INSERT INTO job_services (job_card_id, service_id, agreed_price) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, jobCardId);
            ps.setInt(2, serviceId);
            ps.setBigDecimal(3, agreedPrice);
            ps.executeUpdate();
        }
    }

    /** Lines of one job card, joined with the catalogue name for display. */
    public List<JobServiceLine> findByJob(Connection con, int jobCardId) throws SQLException {
        String sql = "SELECT js.id, js.job_card_id, js.service_id, s.name, js.agreed_price "
                + "FROM job_services js JOIN services s ON s.id = js.service_id "
                + "WHERE js.job_card_id = ? ORDER BY js.id";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, jobCardId);
            List<JobServiceLine> lines = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lines.add(new JobServiceLine(
                            rs.getInt("id"),
                            rs.getInt("job_card_id"),
                            rs.getInt("service_id"),
                            rs.getString("name"),
                            rs.getBigDecimal("agreed_price")));
                }
            }
            return lines;
        }
    }

    /** Labour total for an invoice: the sum of the agreed prices. */
    public BigDecimal laborTotal(Connection con, int jobCardId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(agreed_price), 0) FROM job_services WHERE job_card_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, jobCardId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1);
            }
        }
    }
}
