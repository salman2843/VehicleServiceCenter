package com.vsc.service;

import com.vsc.exception.ValidationException;
import com.vsc.model.Invoice;
import com.vsc.model.PaymentStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Invoice arithmetic and payment status, tested without a database because both
 * rules are pure static methods on {@link BillingService}.
 */
class BillingCalculationTest {

    @Test
    void addsLabourAndPartsThenAppliesDiscount() throws Exception {
        Invoice invoice = BillingService.calculate(7,
                new BigDecimal("500.00"), new BigDecimal("250.50"),
                new BigDecimal("50.50"), BigDecimal.ZERO);

        assertEquals(new BigDecimal("500.00"), invoice.getLaborTotal());
        assertEquals(new BigDecimal("250.50"), invoice.getPartsTotal());
        assertEquals(new BigDecimal("50.50"), invoice.getDiscount());
        assertEquals(new BigDecimal("0.00"), invoice.getTaxAmount());
        assertEquals(new BigDecimal("700.00"), invoice.getTotal());
        assertEquals(PaymentStatus.UNPAID, invoice.getPaymentStatus());
        assertEquals(7, invoice.getJobCardId());
    }

    @Test
    void taxIsChargedOnTheDiscountedSubtotal() throws Exception {
        // (1000 - 100) x 18% = 162, total 1062
        Invoice invoice = BillingService.calculate(1,
                new BigDecimal("600.00"), new BigDecimal("400.00"),
                new BigDecimal("100.00"), new BigDecimal("18"));

        assertEquals(new BigDecimal("162.00"), invoice.getTaxAmount());
        assertEquals(new BigDecimal("1062.00"), invoice.getTotal());
    }

    @Test
    void roundsHalfUpToTwoDecimals() throws Exception {
        // 99.99 x 18% = 17.9982 -> 18.00, total 117.99
        Invoice invoice = BillingService.calculate(1,
                new BigDecimal("99.99"), BigDecimal.ZERO,
                BigDecimal.ZERO, new BigDecimal("18"));

        assertEquals(new BigDecimal("18.00"), invoice.getTaxAmount());
        assertEquals(new BigDecimal("117.99"), invoice.getTotal());
    }

    @Test
    void nullAmountsCountAsZero() {
        Invoice invoice = assertDoesNotThrowCalculation();
        assertEquals(new BigDecimal("0.00"), invoice.getTotal());
    }

    @Test
    void discountCannotBeMoreThanTheSubtotal() {
        assertThrows(ValidationException.class, () -> BillingService.calculate(1,
                new BigDecimal("100.00"), new BigDecimal("50.00"),
                new BigDecimal("200.00"), BigDecimal.ZERO));
    }

    @Test
    void negativeAmountsAndTaxAreRejected() {
        assertThrows(ValidationException.class, () -> BillingService.calculate(1,
                new BigDecimal("-1.00"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
        assertThrows(ValidationException.class, () -> BillingService.calculate(1,
                BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("-5.00"), BigDecimal.ZERO));
        assertThrows(ValidationException.class, () -> BillingService.calculate(1,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("101")));
    }

    @Test
    void paymentStatusIsDerivedFromTheMoneyPaid() {
        BigDecimal total = new BigDecimal("1000.00");

        assertEquals(PaymentStatus.UNPAID, BillingService.deriveStatus(total, BigDecimal.ZERO));
        assertEquals(PaymentStatus.UNPAID, BillingService.deriveStatus(total, null));
        assertEquals(PaymentStatus.PARTIALLY_PAID, BillingService.deriveStatus(total, new BigDecimal("0.01")));
        assertEquals(PaymentStatus.PARTIALLY_PAID, BillingService.deriveStatus(total, new BigDecimal("999.99")));
        assertEquals(PaymentStatus.PAID, BillingService.deriveStatus(total, new BigDecimal("1000.00")));
        assertEquals(PaymentStatus.PAID, BillingService.deriveStatus(total, new BigDecimal("1200.00")));
    }

    /** Small helper so the "nulls count as zero" test stays readable. */
    private Invoice assertDoesNotThrowCalculation() {
        try {
            return BillingService.calculate(1, null, null, null, null);
        } catch (ValidationException e) {
            throw new AssertionError("Null amounts should be treated as zero", e);
        }
    }
}
