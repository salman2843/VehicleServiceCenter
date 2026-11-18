package com.vsc.dao;

import com.vsc.model.Payment;
import com.vsc.model.PaymentMethod;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * All SQL for the {@code payments} table. Several rows per invoice are allowed,
 * which is what makes partial payments possible.
 */
public class PaymentDao {

    public Payment insert(Connection con, Payment payment) throws SQLException {
        String sql = "INSERT INTO payments (invoice_id, amount, method, reference) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, payment.getInvoiceId());
            ps.setBigDecimal(2, payment.getAmount());
            ps.setString(3, payment.getMethod().name());
            ps.setString(4, payment.getReference());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    payment.setId(keys.getInt(1));
                }
            }
        }
        return payment;
    }

    public List<Payment> findByInvoice(Connection con, int invoiceId) throws SQLException {
        String sql = "SELECT id, invoice_id, amount, method, reference, paid_on "
                + "FROM payments WHERE invoice_id = ? ORDER BY paid_on";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            List<Payment> payments = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    payments.add(new Payment(
                            rs.getInt("id"),
                            rs.getInt("invoice_id"),
                            rs.getBigDecimal("amount"),
                            PaymentMethod.valueOf(rs.getString("method")),
                            rs.getString("reference"),
                            rs.getObject("paid_on", LocalDateTime.class)));
                }
            }
            return payments;
        }
    }

    /** Sum of every payment recorded against one invoice. */
    public BigDecimal totalPaid(Connection con, int invoiceId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM payments WHERE invoice_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1);
            }
        }
    }
}
