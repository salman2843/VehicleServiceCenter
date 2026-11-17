package com.vsc.ui;

import org.junit.jupiter.api.Test;

import javax.swing.JPanel;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Builds every screen without a display, so a broken panel is caught by
 * {@code mvn test} instead of by whoever opens that tab. The frame itself is not
 * created here because a window needs a real screen.
 */
class UiScreensTest {

    @Test
    void everyScreenCanBeCreated() {
        JPanel[] screens = {
                new DashboardPanel(),
                new CustomerPanel(),
                new VehiclePanel(),
                new JobCardPanel(),
                new InventoryPanel(),
                new InvoicePanel(),
                new MechanicPanel()
        };

        for (JPanel screen : screens) {
            assertTrue(screen.getComponentCount() > 0,
                    screen.getClass().getSimpleName() + " should contain at least one component");
        }
    }
}
