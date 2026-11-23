package com.vsc.util;

/**
 * Single source of truth for the application's name, version and build stage.
 * Used by the HTTP health endpoint, the startup banner and the UI footer so
 * they never disagree with each other.
 *
 * <p>Remember to bump {@link #VERSION} and {@link #BUILD_STAGE} when a major
 * part of the application is finished (see AGENTS.md).</p>
 */
public final class AppInfo {

    public static final String NAME = "Vehicle Service Center";
    public static final String VERSION = "1.0.0";
    public static final String BUILD_STAGE = "Complete - React frontend, Java HTTP API, MySQL storage";

    private AppInfo() {
        // utility class, no instances
    }
}
