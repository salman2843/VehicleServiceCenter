package com.vsc.service;

import com.vsc.dao.InvoiceDao;
import com.vsc.dao.JobCardDao;
import com.vsc.dao.JobPartDao;
import com.vsc.dao.JobServiceDao;
import com.vsc.dao.PaymentDao;
import com.vsc.exception.ValidationException;
import com.vsc.model.Invoice;
import com.vsc.model.JobCard;
import com.vsc.model.Payment;
import com.vsc.model.PaymentMethod;
import com.vsc.model.PaymentStatus;
import com.vsc.util.DBConnection;
import com.vsc.util.InputValidator;
import com.vsc.util.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Billing rules: build an invoice from the saved job lines and record payments.
 *
 * <p>Formula (AGENTS.md section 6):</p>
 * <pre>
 * subtotal = labour total + parts total
 * taxable  = subtotal - discount
 * total    = taxable + (taxable x tax% / 100)
 * </pre>
 *
 * <p>{@link #calculate} and {@link #deriveStatus} are pure static methods, so
 * the arithmetic and the payment-status rule can be unit tested without a
 * database.</p>
 */
public class BillingService {

    private final InvoiceDao invoiceDao;
    private final PaymentDao paymentDao;
    private final JobCardDao jobCardDao;
    private final JobServiceDao jobServiceDao;
    private final JobPartDao jobPartDao;

    public BillingService() {
        this(new InvoiceDao(), new PaymentDao(), new JobCardDao(), new JobServiceDao(), new JobPartDao());
    }

    public BillingService(InvoiceDao invoiceDao, PaymentDao paymentDao, JobCardDao jobCardDao,
                          JobServiceDao jobServiceDao, JobPartDao jobPartDao) {
        this.invoiceDao = invoiceDao;
        this.paymentDao = paymentDao;
        this.jobCardDao = jobCardDao;
        this.jobServiceDao = jobServiceDao;
        this.jobPartDao = jobPartDao;
    }

    /** Generates the invoice with no tax (the workshop does not charge it yet). */
    public Invoice generateInvoice(int jobCardId, BigDecimal discount)
            throws ValidationException, SQLException {
        return generateInvoice(jobCardId, discount, BigDecimal.ZERO);
    }

    /**
     * Generates the invoice for a completed job, using the prices saved on the
     * job lines - not today's catalogue prices.
     */
    public Invoice generateInvoice(int jobCardId, BigDecimal discount, BigDecimal taxPercent)
            throws ValidationException, SQLException {
        try (Connection con = DBConnection.getConnection()) {
            JobCard card = jobCardDao.findById(con, jobCardId);
            if (card == null) {
                throw new ValidationException("job", "Job card not found.");
            }
            if (!card.getStatus().isCompletedOrLater()) {
                throw new ValidationException("job",
                        "The job must be COMPLETED before an invoice can be generated.");
            }
            if (invoiceDao.findByJobCard(con, jobCardId) != null) {
                throw new ValidationException("invoice", "This job already has an invoice.");
            }
            BigDecimal laborTotal = jobServiceDao.laborTotal(con, jobCardId);
            BigDecimal partsTotal = jobPartDao.partsTotal(con, jobCardId);
            Invoice invoice = calculate(jobCardId, laborTotal, partsTotal, discount, taxPercent);
            return invoiceDao.insert(con, invoice);
        }
    }

    /**
     * The whole invoice arithmetic in one testable place.
     *
     * @param taxPercent 0 for no tax, e.g. 18 for 18%
     * @throws ValidationException for negative amounts, a discount larger than
     *                             the subtotal, or a tax percentage outside 0-100
     */
    public static Invoice calculate(int jobCardId, BigDecimal laborTotal, BigDecimal partsTotal,
                                    BigDecimal discount, BigDecimal taxPercent) throws ValidationException {
        BigDecimal labor = scaled(laborTotal);
        BigDecimal parts = scaled(partsTotal);
        BigDecimal off = scaled(discount);
        BigDecimal percent = scaled(taxPercent);

        if (Money.isNegative(labor) || Money.isNegative(parts)) {
            throw new ValidationException("amount", "Job totals cannot be negative.");
        }
        if (Money.isNegative(off)) {
            throw new ValidationException("discount", "Discount cannot be negative.");
        }
        BigDecimal subtotal = Money.add(labor, parts);
        if (off.compareTo(subtotal) > 0) {
            throw new ValidationException("discount", "Discount cannot be more than the subtotal " + subtotal + ".");
        }
        if (Money.isNegative(percent) || percent.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new ValidationException("tax", "Tax percentage must be between 0 and 100.");
        }

        BigDecimal taxable = Money.subtract(subtotal, off);
        BigDecimal taxAmount = Money.multiply(taxable, percent)
                .divide(BigDecimal.valueOf(100), Money.SCALE, RoundingMode.HALF_UP);
        BigDecimal total = Money.add(taxable, taxAmount);

        return new Invoice(0, jobCardId, labor, parts, off, taxAmount, total,
                PaymentStatus.UNPAID, LocalDateTime.now());
    }

    /**
     * Status is never typed in by hand; it is always derived from the money:
     * nothing paid = UNPAID, part paid = PARTIALLY_PAID, total reached = PAID.
     */
    public static PaymentStatus deriveStatus(BigDecimal total, BigDecimal paid) {
        BigDecimal invoiceTotal = scaled(total);
        BigDecimal paidSoFar = scaled(paid);
        if (paidSoFar.compareTo(BigDecimal.ZERO) <= 0) {
            return PaymentStatus.UNPAID;
        }
        return paidSoFar.compareTo(invoiceTotal) >= 0 ? PaymentStatus.PAID : PaymentStatus.PARTIALLY_PAID;
    }

    /**
     * Records a full or partial payment. The payment row and the invoice status
     * are written in one transaction.
     *
     * @throws ValidationException when the amount is not positive or is more
     *                             than what is still outstanding
     */
    public Payment recordPayment(int invoiceId, BigDecimal amount, PaymentMethod method, String reference)
            throws ValidationException, SQLException {
        if (amount == null || !Money.isPositive(amount)) {
            throw new ValidationException("amount", "Payment amount must be more than zero.");
        }
        if (method == null) {
            throw new ValidationException("method", "Choose a payment method: CASH, UPI or CARD.");
        }
        BigDecimal paidNow = amount.setScale(Money.SCALE, RoundingMode.HALF_UP);

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                Invoice invoice = invoiceDao.findById(con, invoiceId);
                if (invoice == null) {
                    throw new ValidationException("invoice", "Invoice not found.");
                }
                BigDecimal alreadyPaid = scaled(paymentDao.totalPaid(con, invoiceId));
                BigDecimal remaining = Money.subtract(invoice.getTotal(), alreadyPaid);
                if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new ValidationException("amount", "This invoice is already fully paid.");
                }
                if (paidNow.compareTo(remaining) > 0) {
                    throw new ValidationException("amount",
                            "Payment is more than the outstanding amount " + remaining + ".");
                }

                Payment payment = new Payment(invoiceId, paidNow, method,
                        InputValidator.trim(reference));
                paymentDao.insert(con, payment);
                invoiceDao.updatePaymentStatus(con, invoiceId,
                        deriveStatus(invoice.getTotal(), Money.add(alreadyPaid, paidNow)));
                con.commit();
                return payment;
            } catch (SQLException | ValidationException e) {
                con.rollback();
                throw e;
            } finally {
                restoreAutoCommit(con);
            }
        }
    }

    /** How much the customer still owes on an invoice. */
    public BigDecimal outstanding(int invoiceId) throws ValidationException, SQLException {
        try (Connection con = DBConnection.getConnection()) {
            Invoice invoice = invoiceDao.findById(con, invoiceId);
            if (invoice == null) {
                throw new ValidationException("invoice", "Invoice not found.");
            }
            return Money.subtract(invoice.getTotal(), scaled(paymentDao.totalPaid(con, invoiceId)));
        }
    }

    public List<Payment> paymentsOf(int invoiceId) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            return paymentDao.findByInvoice(con, invoiceId);
        }
    }

    public Invoice findById(int id) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            return invoiceDao.findById(con, id);
        }
    }

    public Invoice findByJobCard(int jobCardId) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            return invoiceDao.findByJobCard(con, jobCardId);
        }
    }

    public List<Invoice> listAll() throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            return invoiceDao.findAll(con);
        }
    }

    /** Null amount counts as zero, and everything is normalised to 2 decimals. */
    private static BigDecimal scaled(BigDecimal value) {
        return value == null
                ? BigDecimal.ZERO.setScale(Money.SCALE, RoundingMode.HALF_UP)
                : value.setScale(Money.SCALE, RoundingMode.HALF_UP);
    }

    private void restoreAutoCommit(Connection con) {
        try {
            con.setAutoCommit(true);
        } catch (SQLException ignored) {
            // closed right after this method anyway
        }
    }
}
