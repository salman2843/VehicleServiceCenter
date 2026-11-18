package com.vsc.dao;

import com.vsc.model.Invoice;
import com.vsc.model.PaymentStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * All SQL for the {@code invoices} table. The totals are calculated in the
 * service layer (see {@code BillingService}) and then stored here.
 */
public class InvoiceDao {

    private static final String SELECT_ALL = "SELECT id, job_card_id, labor_total, parts_total, discount, "
            + "tax_amount, total, payment_status, created_on FROM invoices";

    public Invoice insert(Connection con, Invoice invoice) throws SQLException {
        String sql = "INSERT INTO invoices (job_card_id, labor_total, parts_total, discount, "
                + "tax_amount, total, payment_status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, invoice.getJobCardId());
            ps.setBigDecimal(2, invoice.getLaborTotal());
            ps.setBigDecimal(3, invoice.getPartsTotal());
            ps.setBigDecimal(4, invoice.getDiscount());
            ps.setBigDecimal(5, invoice.getTaxAmount());
            ps.setBigDecimal(6, invoice.getTotal());
            ps.setString(7, invoice.getPaymentStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    invoice.setId(keys.getInt(1));
                }
            }
        }
        return invoice;
    }

    public Invoice findById(Connection con, int id) throws SQLException {
        String sql = SELECT_ALL + " WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /** One invoice per job card, enforced by a UNIQUE key. */
    public Invoice findByJobCard(Connection con, int jobCardId) throws SQLException {
        String sql = SELECT_ALL + " WHERE job_card_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, jobCardId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<Invoice> findAll(Connection con) throws SQLException {
        String sql = SELECT_ALL + " ORDER BY created_on DESC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            List<Invoice> invoices = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    invoices.add(map(rs));
                }
            }
            return invoices;
        }
    }

    /** Called only after the payments of the invoice were summed up again. */
    public void updatePaymentStatus(Connection con, int invoiceId, PaymentStatus status) throws SQLException {
        String sql = "UPDATE invoices SET payment_status = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, invoiceId);
            ps.executeUpdate();
        }
    }

    private Invoice map(ResultSet rs) throws SQLException {
        return new Invoice(
                rs.getInt("id"),
                rs.getInt("job_card_id"),
                rs.getBigDecimal("labor_total"),
                rs.getBigDecimal("parts_total"),
                rs.getBigDecimal("discount"),
                rs.getBigDecimal("tax_amount"),
                rs.getBigDecimal("total"),
                PaymentStatus.valueOf(rs.getString("payment_status")),
                rs.getObject("created_on", LocalDateTime.class));
    }
}
