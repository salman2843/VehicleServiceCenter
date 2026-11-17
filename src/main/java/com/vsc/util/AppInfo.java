package com.vsc.util;

/**
 * Single source of truth for the application's name, version and build stage.
 * Used by the window title, the status bar and the About dialog so they never
 * disagree with each other.
 *
 * <p>Remember to bump {@link #VERSION} and {@link #BUILD_STAGE} when a major
 * part of the application is finished (see AGENTS.md).</p>
 */
public final class AppInfo {

    public static final String NAME = "Vehicle Service Center";
    public static final String VERSION = "0.2.0";
    public static final String BUILD_STAGE = "Starter UI - database layer not connected yet";

    private AppInfo() {
        // utility class, no instances
    }

    public static String javaVersion() {
        return System.getProperty("java.version");
    }

    public static String operatingSystem() {
        return System.getProperty("os.name");
    }

    /** Text shown in Help -> About. */
    public static String aboutText() {
        return NAME + " " + VERSION
                + "\n" + BUILD_STAGE
                + "\n\nJava " + javaVersion() + " on " + operatingSystem()
                + "\n\nMCA Java / OOP project - swing, JDBC and MySQL";
    }
}
