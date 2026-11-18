package com.vsc.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Opens JDBC connections to MySQL and nothing else.
 *
 * <p>Configuration comes from {@code src/main/resources/application.properties}
 * (git-ignored, one copy per developer) or from the matching {@code -D} system
 * properties. Because every value is read lazily, starting the application
 * never touches the database - the GUI still opens on a machine without MySQL.</p>
 *
 * <p>Callers must close the connection with try-with-resources; no connection is
 * cached here, which keeps transactions straightforward for a project of this
 * size.</p>
 */
public final class DBConnection {

    /** Classpath location of the developer's local settings. */
    private static final String CONFIG_RESOURCE = "/application.properties";

    private static final String KEY_URL = "db.url";
    private static final String KEY_USER = "db.username";
    private static final String KEY_PASSWORD = "db.password";

    /** Loaded once and reused; null when the classpath has no settings file. */
    private static Properties settings;

    private DBConnection() {
        // utility class, no instances
    }

    /**
     * True when a settings file exists and defines a URL plus a username.
     * Tests use this to skip database checks on machines without MySQL.
     */
    public static boolean isConfigured() {
        Properties config = loadSettings();
        return config != null
                && !value(config, KEY_URL).isBlank()
                && !value(config, KEY_USER).isBlank();
    }

    /**
     * Opens a new connection, or fails with a message that tells the developer
     * what to fix instead of a bare driver stack trace.
     *
     * @throws SQLException when the settings are missing, the driver is absent
     *                      or the server refuses the connection
     */
    public static Connection getConnection() throws SQLException {
        Properties config = loadSettings();
        if (config == null) {
            throw new SQLException(
                    "No database settings found. Copy src/main/resources/application.properties.example "
                            + "to application.properties and fill in your MySQL values.");
        }

        String url = value(config, KEY_URL);
        String username = value(config, KEY_USER);
        String password = value(config, KEY_PASSWORD);
        if (url.isBlank() || username.isBlank()) {
            throw new SQLException("db.url and db.username must be set in application.properties.");
        }

        loadDriver();
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new SQLException(
                    "Could not connect to MySQL at " + target(url)
                            + ". Check that the MySQL service is running and that db.username / db.password are correct.",
                    e);
        }
    }

    /** Short label for the status bar, e.g. {@code localhost:3306/vehicle_service_center}. */
    public static String describeTarget() {
        Properties config = loadSettings();
        return config == null ? "not configured" : target(value(config, KEY_URL));
    }

    /** Closes a connection and ignores the failure, for use in finally-style cleanup. */
    public static void closeQuietly(Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException ignored) {
            // nothing useful can be done when closing fails
        }
    }

    private static void loadDriver() throws SQLException {
        try {
            // Explicit load gives a clear message if mysql-connector-j is missing
            // from the classpath, instead of "No suitable driver found".
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                    "MySQL JDBC driver not found. Add mysql-connector-j to pom.xml.", e);
        }
    }

    /** Reads the properties file once; system properties win over the file. */
    private static synchronized Properties loadSettings() {
        if (settings != null) {
            return settings;
        }
        Properties config = new Properties();
        try (InputStream in = DBConnection.class.getResourceAsStream(CONFIG_RESOURCE)) {
            if (in == null) {
                return null;
            }
            config.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + CONFIG_RESOURCE, e);
        }
        for (String key : new String[]{KEY_URL, KEY_USER, KEY_PASSWORD}) {
            String override = System.getProperty(key);
            if (override != null && !override.isBlank()) {
                config.setProperty(key, override);
            }
        }
        settings = config;
        return settings;
    }

    private static String value(Properties config, String key) {
        String raw = config.getProperty(key);
        return raw == null ? "" : raw.trim();
    }

    /** Hides the password and query parameters before a URL reaches a log or dialog. */
    private static String target(String url) {
        String withoutParams = url.contains("?") ? url.substring(0, url.indexOf('?')) : url;
        int jdbc = withoutParams.indexOf("jdbc:mysql://");
        return jdbc >= 0 ? withoutParams.substring(jdbc + "jdbc:mysql://".length()) : withoutParams;
    }
}
