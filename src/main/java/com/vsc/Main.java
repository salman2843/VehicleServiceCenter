package com.vsc;

import com.vsc.ui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Entry point of the Vehicle Service Center application.
 *
 * <p>All Swing work happens on the Event Dispatch Thread (EDT), which is why the
 * window is created inside {@link SwingUtilities#invokeLater(Runnable)}. This is
 * the class to run from the IDE to check that the application starts.</p>
 */
public final class Main {

    private Main() {
        // entry-point class, no instances
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            applySystemLookAndFeel();
            new MainFrame().setVisible(true);
        });
    }

    /** Uses the operating system's native look so the app does not look like a 1998 demo. */
    private static void applySystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Not fatal: the app simply keeps the default Swing look and feel.
            System.err.println("Could not apply system look and feel, using default.");
        }
    }
}
