package com.vsc.service;

import com.vsc.exception.InsufficientStockException;
import com.vsc.model.SparePart;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The "stock can never go negative" rule, tested on the model and on the static
 * check used before a part is added to a job card.
 */
class StockRuleTest {

    private SparePart part(int stock) {
        return new SparePart(1, "BRK-01", "Brake pad", new BigDecimal("450.00"), stock, 5);
    }

    @Test
    void availableStockCanBeUsed() {
        assertTrue(part(10).canFulfil(10));
        assertTrue(part(10).canFulfil(1));
        assertDoesNotThrow(() -> InventoryService.ensureAvailable(part(3), 3));
    }

    @Test
    void moreThanAvailableStockIsRejected() {
        assertFalse(part(2).canFulfil(3));
        InsufficientStockException error = assertThrows(InsufficientStockException.class,
                () -> InventoryService.ensureAvailable(part(2), 3));

        assertEquals("Brake pad", error.getPartName());
        assertEquals(3, error.getRequested());
        assertEquals(2, error.getAvailable());
    }

    @Test
    void zeroOrNegativeQuantityIsNotAValidRequest() {
        assertFalse(part(10).canFulfil(0));
        assertFalse(part(10).canFulfil(-1));
        assertThrows(InsufficientStockException.class,
                () -> InventoryService.ensureAvailable(part(10), 0));
    }

    @Test
    void emptyStockReportsLowStock() {
        assertTrue(part(0).isLowStock());
        assertTrue(part(5).isLowStock());
        assertFalse(part(6).isLowStock());
    }
}
