package com.vsc;

import com.vsc.model.Car;
import com.vsc.model.Invoice;
import com.vsc.model.JobCard;
import com.vsc.model.JobStatus;
import com.vsc.model.Motorcycle;
import com.vsc.model.PaymentStatus;
import com.vsc.model.ServiceItem;
import com.vsc.model.SparePart;
import com.vsc.model.Vehicle;
import com.vsc.util.InputValidator;
import com.vsc.util.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Smoke test for the starter build: proves the project compiles, the model
 * classes behave as intended, and the shared helpers work. It deliberately
 * needs no database, so it runs on any machine.
 */
class AppSmokeTest {

    @Test
    void carAndMotorcycleReportTheirOwnVehicleType() {
        Vehicle car = new Car(1, "KA01AB1234", "Maruti", "Swift", 2020);
        Vehicle bike = new Motorcycle(1, "KA02CD5678", "Honda", "Activa", 2021);

        assertEquals("Car", car.getVehicleType());
        assertEquals("Motorcycle", bike.getVehicleType());
    }

    @Test
    void newJobCardStartsAsReceived() {
        JobCard job = new JobCard(7, "Brake noise");

        assertEquals(JobStatus.RECEIVED, job.getStatus());
    }

    @Test
    void sparePartFlagsLowStockAtOrBelowReorderLevel() {
        SparePart part = new SparePart("BRK-01", "Brake pad", Money.of(450.00), 3, 5);

        assertTrue(part.isLowStock());

        part.setStockQty(10);
        assertFalse(part.isLowStock());
    }

    @Test
    void billableItemsExposeTheirCharge() {
        ServiceItem service = new ServiceItem("Oil change", Money.of(800.00));
        SparePart part = new SparePart("OIL-05", "Engine oil", Money.of(1200.00), 8, 2);

        assertEquals("Oil change", service.getDescription());
        assertEquals(0, service.getAmount().compareTo(new BigDecimal("800.00")));
        assertEquals(0, part.getAmount().compareTo(new BigDecimal("1200.00")));
    }

    @Test
    void newInvoiceIsUnpaidUntilPaymentsAreRecorded() {
        Invoice invoice = new Invoice(7, Money.of(2000.00));

        assertEquals(PaymentStatus.UNPAID, invoice.getPaymentStatus());
    }

    @Test
    void validatorRejectsBadCustomerInput() {
        assertFalse(InputValidator.isValidName(" "));
        assertTrue(InputValidator.isValidName("Asha Rao"));
        assertFalse(InputValidator.isValidPhone("12"));
        assertTrue(InputValidator.isValidPhone("+91 98765 43210"));
        assertFalse(InputValidator.isValidRegistrationNo("AB"));
    }

    @Test
    void moneyKeepsTwoDecimalPlaces() {
        assertEquals(new BigDecimal("1050.00"), Money.add(Money.of(1000.00), Money.of(50.00)));
        assertTrue(Money.isPositive(Money.of(0.01)));
        assertFalse(Money.isPositive(Money.of(0.00)));
    }
}
