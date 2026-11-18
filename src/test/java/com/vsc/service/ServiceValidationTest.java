package com.vsc.service;

import com.vsc.exception.ValidationException;
import com.vsc.model.Car;
import com.vsc.model.Customer;
import com.vsc.model.Mechanic;
import com.vsc.model.SparePart;
import com.vsc.util.InputValidator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validation runs before the first database call, so these cases pass on a
 * machine without MySQL. They prove that bad input never reaches SQL.
 */
class ServiceValidationTest {

    @Test
    void customerNameAndPhoneAreChecked() {
        ValidationException error = assertThrows(ValidationException.class,
                () -> new CustomerService().register(new Customer("A", "12345")));
        assertEquals("name", error.getField());

        error = assertThrows(ValidationException.class,
                () -> new CustomerService().register(new Customer("Asha Rao", "12")));
        assertEquals("phone", error.getField());
    }

    @Test
    void customerEmailIsOptionalButMustLookLikeAnEmail() {
        Customer badEmail = new Customer("Asha Rao", "9876543211");
        badEmail.setEmail("asha(at)example.com");
        ValidationException error = assertThrows(ValidationException.class,
                () -> new CustomerService().register(badEmail));
        assertEquals("email", error.getField());
    }

    @Test
    void vehicleRegistrationAndYearAreChecked() {
        ValidationException error = assertThrows(ValidationException.class,
                () -> new VehicleService().add(new Car(1, "AB", "Maruti", "Swift", 2022)));
        assertEquals("registration", error.getField());

        error = assertThrows(ValidationException.class,
                () -> new VehicleService().add(new Car(1, "KA01AB1234", "Maruti", "Swift", 1900)));
        assertEquals("year", error.getField());
    }

    @Test
    void sparePartPriceCannotBeNegative() {
        SparePart part = new SparePart("BRK-01", "Brake pad", new BigDecimal("-1.00"), 5, 2);
        ValidationException error = assertThrows(ValidationException.class,
                () -> new InventoryService().addPart(part));
        assertEquals("price", error.getField());
    }

    @Test
    void mechanicNameIsRequired() {
        ValidationException error = assertThrows(ValidationException.class,
                () -> new MechanicService().add(new Mechanic(" ", null, "Engine")));
        assertEquals("name", error.getField());
    }

    @Test
    void emailHelperAcceptsNormalAddressesOnly() {
        assertTrue(InputValidator.isValidEmail("asha@example.com"));
        assertFalse(InputValidator.isValidEmail("asha(at)example.com"));
        assertFalse(InputValidator.isValidEmail("asha@example"));
        assertFalse(InputValidator.isValidEmail(""));
    }

    @Test
    void registrationHelperIgnoresSpacesAndCase() {
        assertTrue(InputValidator.isValidRegistrationNo(" ka 01 ab 1234 "));
        assertFalse(InputValidator.isValidRegistrationNo("AB"));
    }
}
