package com.vsc;

import com.vsc.api.ApiServer;
import com.vsc.util.AppInfo;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.List;

/**
 * Entry point of the Vehicle Service Center application.
 *
 * <p>Starts the HTTP API that the React frontend talks to and, when the frontend
 * has been built, serves it from the same port. Pass {@code --no-open} to stop
 * the browser from opening automatically. The port can be changed with
 * {@code -Dvsc.port=9090}.</p>
 */
public final class Main {

    private Main() {
        // entry-point class, no instances
    }

    public static void main(String[] args) {
        int port = Integer.getInteger("vsc.port", ApiServer.DEFAULT_PORT);

        ApiServer server;
        try {
            server = new ApiServer(port);
        } catch (IOException e) {
            System.err.println("Could not start the server on port " + port + ": " + e.getMessage());
            System.err.println("Another process may already use that port. Try -Dvsc.port=9090.");
            return;
        }

        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(server::close, "vsc-shutdown"));

        System.out.println(AppInfo.NAME + " " + AppInfo.VERSION);
        System.out.println("API and UI: " + server.url());
        System.out.println("Frontend dev server: cd frontend && npm run dev");

        if (!List.of(args).contains("--no-open")) {
            openBrowser(server.url());
        }
    }

    /** Best effort: a headless or locked-down machine simply does not open a tab. */
    private static void openBrowser(String url) {
        if (!Desktop.isDesktopSupported()) {
            return;
        }
        try {
            Desktop.getDesktop().browse(URI.create(url));
        } catch (IOException | RuntimeException ignored) {
            // the URL is printed above, so the user can still open it manually
        }
    }
}
